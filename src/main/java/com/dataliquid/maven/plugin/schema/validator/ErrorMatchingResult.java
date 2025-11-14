package com.dataliquid.maven.plugin.schema.validator;

import java.util.Set;

/**
 * Result of matching expected errors against actual errors.
 */
class ErrorMatchingResult {
    private final Set<String> matchedPatterns;
    private final Set<String> unmatchedPatterns;
    private final Set<String> unexpectedErrors;

    public ErrorMatchingResult(Set<String> matchedPatterns, Set<String> unmatchedPatterns,
            Set<String> unexpectedErrors) {
        this.matchedPatterns = matchedPatterns;
        this.unmatchedPatterns = unmatchedPatterns;
        this.unexpectedErrors = unexpectedErrors;
    }

    public Set<String> getMatchedPatterns() {
        return matchedPatterns;
    }

    public Set<String> getUnmatchedPatterns() {
        return unmatchedPatterns;
    }

    public Set<String> getUnexpectedErrors() {
        return unexpectedErrors;
    }

    public boolean hasUnmatchedPatterns() {
        return !unmatchedPatterns.isEmpty();
    }

    public boolean hasUnexpectedErrors() {
        return !unexpectedErrors.isEmpty();
    }

    public boolean isFullMatch() {
        return !hasUnmatchedPatterns() && !hasUnexpectedErrors();
    }
}
