package com.dataliquid.maven.plugin.schema.validator;

import java.util.Set;

/**
 * Result of matching expected errors against actual errors.
 */
record ErrorMatchingResult(Set<String> matchedPatterns, Set<String> unmatchedPatterns, Set<String> unexpectedErrors) {

    public boolean hasUnmatchedPatterns() {
        return !unmatchedPatterns.isEmpty();
    }

    public boolean hasUnexpectedErrors() {
        return !unexpectedErrors.isEmpty();
    }

    public boolean isFullMatch() {
        return !hasUnmatchedPatterns() && !hasUnexpectedErrors();
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
}
