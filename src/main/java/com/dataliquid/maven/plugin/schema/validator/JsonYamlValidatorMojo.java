package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

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
        getLog().info("Loading schema from: " + schemaFile.getAbsolutePath());
        
        JsonNode schemaNode = readFile(schemaFile);
        SpecVersion.VersionFlag version = getSchemaVersion();
        
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(version);
        return factory.getSchema(schemaNode);
    }

    private SpecVersion.VersionFlag getSchemaVersion() throws MojoExecutionException {
        switch (schemaVersion.toUpperCase()) {
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

    private List<ValidationResult> validateFiles(JsonSchema schema) throws IOException {
        List<ValidationResult> results = new ArrayList<>();
        List<File> filesToValidate = findFilesToValidate();
        
        getLog().info("Found " + filesToValidate.size() + " files to validate");
        
        for (File file : filesToValidate) {
            results.add(validateFile(file, schema));
        }
        
        return results;
    }

    private List<File> findFilesToValidate() throws IOException {
        if (!sourceDirectory.exists()) {
            getLog().warn("Source directory does not exist: " + sourceDirectory.getAbsolutePath());
            return new ArrayList<>();
        }

        try (Stream<Path> paths = Files.walk(sourceDirectory.toPath())) {
            return paths
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(this::shouldIncludeFile)
                .collect(Collectors.toList());
        }
    }

    private boolean shouldIncludeFile(File file) {
        String relativePath = sourceDirectory.toURI().relativize(file.toURI()).getPath();
        
        boolean included = false;
        for (String include : includes) {
            if (matchesPattern(relativePath, include)) {
                included = true;
                break;
            }
        }
        
        if (!included) {
            return false;
        }
        
        if (excludes != null) {
            for (String exclude : excludes) {
                if (matchesPattern(relativePath, exclude)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private boolean matchesPattern(String path, String pattern) {
        String regex = pattern
            .replace(".", "\\.")
            .replace("**", ".*")
            .replace("*", "[^/]*")
            .replace("?", ".");
        return path.matches(regex);
    }

    private ValidationResult validateFile(File file, JsonSchema schema) {
        getLog().debug("Validating: " + file.getAbsolutePath());
        
        try {
            JsonNode content = readFile(file);
            Set<ValidationMessage> errors = schema.validate(content);
            
            return new ValidationResult(file, errors);
        } catch (Exception e) {
            return new ValidationResult(file, e);
        }
    }

    private JsonNode readFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            return yamlMapper.readTree(file);
        } else {
            return jsonMapper.readTree(file);
        }
    }

    private void reportResults(List<ValidationResult> results) {
        int totalFiles = results.size();
        int failedFiles = (int) results.stream().filter(r -> !r.isValid()).count();
        
        getLog().info(String.format("Validation complete: %d files processed, %d failed", 
            totalFiles, failedFiles));
        
        for (ValidationResult result : results) {
            if (!result.isValid()) {
                getLog().error("Validation failed for: " + result.getFile().getAbsolutePath());
                
                if (result.getException() != null) {
                    getLog().error("  Error: " + result.getException().getMessage());
                } else {
                    for (ValidationMessage error : result.getErrors()) {
                        getLog().error("  - " + error.getMessage());
                    }
                }
            } else {
                getLog().debug("Validation passed for: " + result.getFile().getAbsolutePath());
            }
        }
    }

    private boolean hasErrors(List<ValidationResult> results) {
        return results.stream().anyMatch(r -> !r.isValid());
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