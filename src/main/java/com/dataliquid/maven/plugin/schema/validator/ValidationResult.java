package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;
import java.util.List;

import com.networknt.schema.Error;

/**
 * Result of validating a single file against a JSON schema.
 */
class ValidationResult {
    private final File file;
    private final List<Error> errors;
    private final Exception exception;

    public ValidationResult(File file, List<Error> errors) {
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

    public List<Error> getErrors() {
        return errors;
    }

    public Exception getException() {
        return exception;
    }
}
