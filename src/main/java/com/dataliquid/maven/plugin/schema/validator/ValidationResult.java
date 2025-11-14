package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;
import java.util.Set;

import com.networknt.schema.ValidationMessage;

/**
 * Result of validating a single file against a JSON schema.
 */
class ValidationResult {
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
