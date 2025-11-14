package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    /**
     * Expected result of the validation. Use SUCCESS for positive tests (expect
     * valid files) and ERROR for negative tests (expect validation errors).
     */
    public enum ExpectedResult {
        SUCCESS, ERROR
    }

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

    @Parameter(property = "schema.validator.expectedResult", defaultValue = "SUCCESS")
    private String expectedResult;

    @Parameter(property = "schema.validator.expectedErrors")
    private String[] expectedErrors;

    @Parameter(property = "schema.validator.strictErrorMatching", defaultValue = "false")
    private boolean strictErrorMatching;

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

        ExpectedResult expected = ExpectedResult.valueOf(expectedResult.toUpperCase(Locale.ROOT));

        try {
            JsonSchema schema = loadSchema();
            List<ValidationResult> results = validateFiles(schema);

            reportResults(results);

            boolean hasErrors = hasErrors(results);

            handleExpectedResult(expected, hasErrors, results);
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

        if (schemaMappings != null && schemaMappings.length > 0) {
            Map<String, String> directMappings = new ConcurrentHashMap<>();

            factory = JsonSchemaFactory.getInstance(version, builder -> builder.schemaMappers(schemaMappers -> {
                final int MAPPING_PARTS_EXPECTED = 2;
                for (String mapping : schemaMappings) {
                    String[] parts = mapping.split("=", MAPPING_PARTS_EXPECTED);
                    if (parts.length == MAPPING_PARTS_EXPECTED) {
                        String schemaId = parts[0].trim();
                        String localPath = parts[1].trim();

                        String mappedUri;
                        if (localPath.startsWith("classpath:") || localPath.startsWith("http://")
                                || localPath.startsWith("https://") || localPath.startsWith("file:")) {
                            mappedUri = localPath;
                        } else {
                            File localFile = resolveFile(localPath, schemaFile.getParentFile());
                            mappedUri = localFile.toURI().toString();
                        }

                        if (schemaId.endsWith("/") && localPath.endsWith("/")) {
                            if (getLog().isInfoEnabled()) {
                                getLog().info("Adding schema prefix mapping: " + schemaId + " -> " + mappedUri);
                            }
                            schemaMappers.mapPrefix(schemaId, mappedUri);
                        } else if (schemaId.endsWith("/") && !localPath.endsWith("/")) {
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

                if (!directMappings.isEmpty()) {
                    schemaMappers.mappings(directMappings);
                }
            }));
        } else {
            factory = JsonSchemaFactory.getInstance(version);
        }

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

        IOFileFilter fileFilter = new AntPatternFileFilter(sourceDirectory, includes, excludes);
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
            try (InputStream in = Files.newInputStream(file.toPath())) {
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

    private void handleExpectedResult(ExpectedResult expected, boolean hasErrors, List<ValidationResult> results)
            throws MojoFailureException {
        if (expected == ExpectedResult.SUCCESS) {
            handleExpectedSuccess(hasErrors);
        } else {
            handleExpectedError(hasErrors, results);
        }
    }

    private void handleExpectedSuccess(boolean hasErrors) throws MojoFailureException {
        if (hasErrors && failOnError) {
            throw new MojoFailureException("Validation failed. See errors above.");
        }
    }

    private void handleExpectedError(boolean hasErrors, List<ValidationResult> results) throws MojoFailureException {
        if (!hasErrors) {
            handleNoErrorsFound();
        } else if (expectedErrors != null && expectedErrors.length > 0) {
            handleExpectedErrorPatterns(results);
        } else {
            getLog().info("Validation errors found as expected - negative test passed.");
        }
    }

    private void handleNoErrorsFound() throws MojoFailureException {
        if (failOnError) {
            throw new MojoFailureException("Expected validation errors but all files were valid. "
                    + "This indicates the negative test failed - " + "invalid input was not detected as invalid.");
        } else {
            getLog().warn("Expected validation errors but all files were valid.");
        }
    }

    private void handleExpectedErrorPatterns(List<ValidationResult> results) throws MojoFailureException {
        ErrorMatchingResult matchResult = matchExpectedErrors(results);
        reportMatchingResults(matchResult);
        validateMatchingResults(matchResult);
    }

    private void reportMatchingResults(ErrorMatchingResult matchResult) {
        if (getLog().isInfoEnabled()) {
            for (String pattern : matchResult.getMatchedPatterns()) {
                getLog().info("  \u2713 Expected error matched: " + pattern);
            }
        }

        if (matchResult.hasUnmatchedPatterns()) {
            if (getLog().isErrorEnabled()) {
                for (String pattern : matchResult.getUnmatchedPatterns()) {
                    getLog().error("  \u2717 Expected error not found: " + pattern);
                }
            }
        }

        if (matchResult.hasUnexpectedErrors()) {
            for (String error : matchResult.getUnexpectedErrors()) {
                if (strictErrorMatching) {
                    if (getLog().isErrorEnabled()) {
                        getLog().error("  ! Unexpected error: " + error);
                    }
                } else {
                    if (getLog().isWarnEnabled()) {
                        getLog().warn("  ! Unexpected error: " + error);
                    }
                }
            }
        }
    }

    private void validateMatchingResults(ErrorMatchingResult matchResult) throws MojoFailureException {
        if (matchResult.hasUnmatchedPatterns()) {
            throw new MojoFailureException("Expected error patterns were not found. See unmatched patterns above.");
        }

        if (strictErrorMatching && matchResult.hasUnexpectedErrors()) {
            throw new MojoFailureException("Unexpected validation errors found with strictErrorMatching=true. "
                    + "See unexpected errors above.");
        }

        if (matchResult.isFullMatch()) {
            getLog().info("All expected errors matched - negative test passed.");
        } else if (!strictErrorMatching) {
            getLog().info("Expected errors matched - negative test passed (with additional errors).");
        }
    }

    /**
     * Matches actual validation errors against expected error patterns.
     *
     * @param  results The validation results containing actual errors
     *
     * @return         The matching result
     */
    private ErrorMatchingResult matchExpectedErrors(List<ValidationResult> results) {
        Set<String> matchedPatterns = new HashSet<>();
        Set<String> unmatchedPatterns = new HashSet<>();
        Set<String> unexpectedErrors = new HashSet<>();

        Set<String> actualErrors = new HashSet<>();
        for (ValidationResult result : results) {
            if (result.getErrors() != null) {
                for (ValidationMessage error : result.getErrors()) {
                    actualErrors.add(error.getMessage());
                }
            }
        }

        for (String errorMsg : actualErrors) {
            boolean matched = false;
            for (String pattern : expectedErrors) {
                if (errorMsg.matches(pattern)) {
                    matchedPatterns.add(pattern);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                unexpectedErrors.add(errorMsg);
            }
        }

        for (String pattern : expectedErrors) {
            if (!matchedPatterns.contains(pattern)) {
                unmatchedPatterns.add(pattern);
            }
        }

        return new ErrorMatchingResult(matchedPatterns, unmatchedPatterns, unexpectedErrors);
    }

    private File resolveFile(String path, File parentDir) {
        File file = new File(path);
        if (file.isAbsolute()) {
            return file;
        } else {
            return new File(parentDir, path);
        }
    }
}
