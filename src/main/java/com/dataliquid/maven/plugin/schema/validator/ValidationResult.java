package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;
import java.util.List;

import com.networknt.schema.Error;

/**
 * Result of validating a single file against a JSON schema.
 */
record ValidationResult(File file, List<Error> errors, Exception exception) {

    public ValidationResult(File file, List<Error> errors) {
        this(file, errors, null);
    }

    public ValidationResult(File file, Exception exception) {
        this(file, null, exception);
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
