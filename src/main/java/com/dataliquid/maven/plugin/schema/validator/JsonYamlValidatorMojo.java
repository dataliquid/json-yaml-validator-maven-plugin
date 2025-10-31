package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.dataliquid.maven.plugin.schema.validator.utils.AntPatternFileFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.VALIDATE)
public class JsonYamlValidatorMojo extends AbstractMojo {

    @Parameter(property = "schema.validator.schemaFile", required = true)
    private File schemaFile;

    @Parameter(property = "schema.validator.sourceDirectory", defaultValue = "${project.basedir}/src/main/resources")
    private File sourceDirectory;

    @Parameter(property = "schema.validator.includes", defaultValue = "**/*.json,**/*.yaml,**/*.yml")
    private String[] includes;

    @Parameter(property = "schema.validator.excludes")
    private String[] excludes;

    @Parameter(property = "schema.validator.failOnError", defaultValue = "true")
    private boolean failOnError;

    @Parameter(property = "schema.validator.schemaVersion", defaultValue = "V202012")
    private String schemaVersion;

    @Parameter(property = "schema.validator.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(property = "schema.validator.schemaMappings")
    private String[] schemaMappings;

    @Parameter(property = "schema.validator.failOnNoFilesFound", defaultValue = "true")
    private boolean failOnNoFilesFound;

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping JSON/YAML validation (skip=true)");
            return;
        }

        getLog().info("Starting JSON/YAML validation...");

        if (!schemaFile.exists()) {
            throw new MojoExecutionException("Schema file not found: " + schemaFile.getAbsolutePath());
        }

        try {
            JsonSchema schema = loadSchema();
            List<ValidationResult> results = validateFiles(schema);

            reportResults(results);

            if (failOnError && hasErrors(results)) {
                throw new MojoFailureException("Validation failed. See errors above.");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error during validation", e);
        }
    }

    private JsonSchema loadSchema() throws IOException, MojoExecutionException {
        if (getLog().isInfoEnabled()) {
            getLog().info("Loading schema from: " + schemaFile.getAbsolutePath());
        }

        JsonNode schemaNode = readFile(schemaFile);
        SpecVersion.VersionFlag version = getSchemaVersion();

        JsonSchemaFactory factory;

        // If schema mappings are configured, create factory with custom schema mappers
        if (schemaMappings != null && schemaMappings.length > 0) {
            // Collect prefix mappings and direct mappings separately
            Map<String, String> directMappings = new ConcurrentHashMap<>();

            factory = JsonSchemaFactory.getInstance(version, builder -> builder.schemaMappers(schemaMappers -> {
                // Add mappings from configuration
                final int MAPPING_PARTS_EXPECTED = 2;
                for (String mapping : schemaMappings) {
                    String[] parts = mapping.split("=", MAPPING_PARTS_EXPECTED);
                    if (parts.length == MAPPING_PARTS_EXPECTED) {
                        String schemaId = parts[0].trim();
                        String localPath = parts[1].trim();

                        // Convert local file path to proper URI
                        String mappedUri;
                        if (localPath.startsWith("classpath:") || localPath.startsWith("http://")
                                || localPath.startsWith("https://") || localPath.startsWith("file:")) {
                            // Already a proper URI scheme
                            mappedUri = localPath;
                        } else {
                            // Convert file path to proper file URI
                            File localFile = resolveFile(localPath, schemaFile.getParentFile());
                            mappedUri = localFile.toURI().toString();
                        }

                        // Check if this is a prefix mapping (ends with /) or a direct mapping
                        if (schemaId.endsWith("/") && localPath.endsWith("/")) {
                            // This is a prefix mapping for a directory
                            if (getLog().isInfoEnabled()) {
                                getLog().info("Adding schema prefix mapping: " + schemaId + " -> " + mappedUri);
                            }
                            schemaMappers.mapPrefix(schemaId, mappedUri);
                        } else if (schemaId.endsWith("/") && !localPath.endsWith("/")) {
                            // Schema ID is a prefix but local path is a directory - ensure it ends with /
                            File dir = resolveFile(localPath, schemaFile.getParentFile());
                            if (dir.isDirectory()) {
                                mappedUri = dir.toURI().toString();
                                if (getLog().isInfoEnabled()) {
                                    getLog().info("Adding schema prefix mapping: " + schemaId + " -> " + mappedUri);
                                }
                                schemaMappers.mapPrefix(schemaId, mappedUri);
                            } else {
                                if (getLog().isWarnEnabled()) {
                                    getLog().warn("Schema prefix mapping points to non-directory: " + localPath);
                                }
                            }
                        } else {
                            // This is a direct schema mapping - add to map
                            if (getLog().isInfoEnabled()) {
                                getLog().info("Adding direct schema mapping: " + schemaId + " -> " + mappedUri);
                            }
                            directMappings.put(schemaId, mappedUri);
                        }
                    } else {
                        if (getLog().isWarnEnabled()) {
                            getLog().warn("Invalid schema mapping format: " + mapping + " (expected: schemaId=path)");
                        }
                    }
                }

                // Apply all direct mappings at once
                if (!directMappings.isEmpty()) {
                    schemaMappers.mappings(directMappings);
                }
            }));
        } else {
            factory = JsonSchemaFactory.getInstance(version);
        }

        // Set the schema URI to enable proper resolution of relative $ref imports
        return factory.getSchema(schemaFile.toURI(), schemaNode);
    }

    private SpecVersion.VersionFlag getSchemaVersion() throws MojoExecutionException {
        switch (schemaVersion.toUpperCase(Locale.ROOT)) {
        case "V4":
            return SpecVersion.VersionFlag.V4;
        case "V6":
            return SpecVersion.VersionFlag.V6;
        case "V7":
            return SpecVersion.VersionFlag.V7;
        case "V201909":
            return SpecVersion.VersionFlag.V201909;
        case "V202012":
            return SpecVersion.VersionFlag.V202012;
        default:
            throw new MojoExecutionException("Unsupported schema version: " + schemaVersion);
        }
    }

    private List<ValidationResult> validateFiles(JsonSchema schema) throws IOException, MojoFailureException {
        List<ValidationResult> results = new ArrayList<>();
        List<File> filesToValidate = findFilesToValidate();

        if (getLog().isInfoEnabled()) {
            getLog().info("Found " + filesToValidate.size() + " files to validate");
        }

        if (filesToValidate.isEmpty() && failOnNoFilesFound) {
            getLog().debug("failOnNoFilesFound is true and no files found - throwing exception");
            throw new MojoFailureException("No files found matching the include patterns. "
                    + "Check your configuration or set failOnNoFilesFound=false to ignore this error.");
        }

        for (File file : filesToValidate) {
            results.add(validateFile(file, schema));
        }

        return results;
    }

    private List<File> findFilesToValidate() throws IOException {
        if (!sourceDirectory.exists()) {
            if (getLog().isWarnEnabled()) {
                getLog().warn("Source directory does not exist: " + sourceDirectory.getAbsolutePath());
            }
            return new ArrayList<>();
        }

        // Create a custom file filter that handles Ant-style patterns
        IOFileFilter fileFilter = new AntPatternFileFilter(sourceDirectory, includes, excludes);

        // Use Apache Commons IO to find files
        Collection<File> files = FileUtils.listFiles(sourceDirectory, fileFilter, TrueFileFilter.INSTANCE);

        return new ArrayList<>(files);
    }

    private ValidationResult validateFile(File file, JsonSchema schema) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Validating: " + file.getAbsolutePath());
        }

        try {
            JsonNode content = readFile(file);
            Set<ValidationMessage> errors = schema.validate(content);

            return new ValidationResult(file, errors);
        } catch (Exception e) {
            return new ValidationResult(file, e);
        }
    }

    private JsonNode readFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            Yaml snakeYaml = new Yaml();
            Object loadedYaml;
            try (InputStream in = new FileInputStream(file)) {
                loadedYaml = snakeYaml.load(in);
            }
            return yamlMapper.valueToTree(loadedYaml);
        } else {
            return jsonMapper.readTree(file);
        }
    }

    private void reportResults(List<ValidationResult> results) {
        int totalFiles = results.size();
        int failedFiles = (int) results.stream().filter(r -> !r.isValid()).count();

        if (getLog().isInfoEnabled()) {
            getLog().info(String.format("Validation complete: %d files processed, %d failed", totalFiles, failedFiles));
        }

        for (ValidationResult result : results) {
            if (!result.isValid()) {
                if (getLog().isErrorEnabled()) {
                    getLog().error("Validation failed for: " + result.getFile().getAbsolutePath());
                }

                if (result.getException() != null) {
                    if (getLog().isErrorEnabled()) {
                        getLog().error("  Error: " + result.getException().getMessage());
                    }
                } else {
                    for (ValidationMessage error : result.getErrors()) {
                        if (getLog().isErrorEnabled()) {
                            getLog().error("  - " + error.getMessage());
                        }
                    }
                }
            } else {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Validation passed for: " + result.getFile().getAbsolutePath());
                }
            }
        }
    }

    private boolean hasErrors(List<ValidationResult> results) {
        return results.stream().anyMatch(r -> !r.isValid());
    }

    private File resolveFile(String path, File parentDir) {
        File file = new File(path);
        if (file.isAbsolute()) {
            return file;
        } else {
            return new File(parentDir, path);
        }
    }

    private static class ValidationResult {
        private final File file;
        private final Set<ValidationMessage> errors;
        private final Exception exception;

        public ValidationResult(File file, Set<ValidationMessage> errors) {
            this.file = file;
            this.errors = errors;
            this.exception = null;
        }

        public ValidationResult(File file, Exception exception) {
            this.file = file;
            this.errors = null;
            this.exception = exception;
        }

        public boolean isValid() {
            return (errors == null || errors.isEmpty()) && exception == null;
        }

        public File getFile() {
            return file;
        }

        public Set<ValidationMessage> getErrors() {
            return errors;
        }

        public Exception getException() {
            return exception;
        }
    }
}
