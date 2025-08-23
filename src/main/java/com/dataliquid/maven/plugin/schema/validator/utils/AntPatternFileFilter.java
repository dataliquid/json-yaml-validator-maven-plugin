package com.dataliquid.maven.plugin.schema.validator.utils;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Custom file filter that handles Ant-style patterns for includes/excludes.
 * <p>
 * Supports:
 * <ul>
 * <li>? matches one character</li>
 * <li>* matches zero or more characters (but not directory separators)</li>
 * <li>** matches zero or more directories</li>
 * </ul>
 */
public class AntPatternFileFilter implements IOFileFilter {

    private static final int MIN_DOUBLE_STAR_LENGTH = 1;
    private static final int DOUBLE_STAR_SLASH_LENGTH = 3;
    private static final int DOUBLE_STAR_LENGTH = 2;
    private static final int ZERO_INDEX = 0;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final File sourceDirectory;
    private final String[] includes;
    private final String[] excludes;

    public AntPatternFileFilter(File sourceDirectory, String[] includes, String... excludes) {
        this.sourceDirectory = sourceDirectory;
        this.includes = includes != null ? includes.clone() : EMPTY_STRING_ARRAY;
        this.excludes = excludes != null ? excludes.clone() : EMPTY_STRING_ARRAY;
    }

    @Override
    public boolean accept(File file) {
        String relativePath = sourceDirectory.toURI().relativize(file.toURI()).getPath();

        // Check if file matches any include pattern
        boolean included = false;
        for (String include : includes) {
            if (matchesAntPattern(relativePath, include)) {
                included = true;
                break;
            }
        }

        if (!included) {
            return false;
        }

        // Check if file matches any exclude pattern
        if (excludes != null) {
            for (String exclude : excludes) {
                if (matchesAntPattern(relativePath, exclude)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean accept(File dir, String name) {
        return accept(new File(dir, name));
    }

    /**
     * Matches a path against an Ant-style pattern.
     */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private boolean matchesAntPattern(String path, String pattern) {
        // Normalize paths to use forward slashes
        String normalizedPath = FilenameUtils.separatorsToUnix(path);
        String normalizedPattern = FilenameUtils.separatorsToUnix(pattern);

        // Convert Ant pattern to regex
        StringBuilder regex = new StringBuilder();
        int i = 0;

        while (i < normalizedPattern.length()) {
            char c = normalizedPattern.charAt(i);

            if (c == '*') {
                // Check for **
                if (i + MIN_DOUBLE_STAR_LENGTH < normalizedPattern.length()
                        && normalizedPattern.charAt(i + MIN_DOUBLE_STAR_LENGTH) == '*') {
                    // Handle ** pattern
                    if (i + DOUBLE_STAR_LENGTH < normalizedPattern.length()
                            && normalizedPattern.charAt(i + DOUBLE_STAR_LENGTH) == '/') {
                        // **/ means any number of directories (including none)
                        regex.append("(?:(?:.*/)?)?");
                        i += DOUBLE_STAR_SLASH_LENGTH; // Skip **/
                    } else if (i == ZERO_INDEX || normalizedPattern.charAt(i - MIN_DOUBLE_STAR_LENGTH) == '/') {
                        // /** at the end or in the middle
                        regex.append(".*");
                        i += DOUBLE_STAR_LENGTH; // Skip **
                    } else {
                        // Just two * in a row, treat as two wildcards
                        regex.append("[^/]*[^/]*");
                        i += DOUBLE_STAR_LENGTH;
                    }
                } else {
                    // Single * matches any characters except /
                    regex.append("[^/]*");
                    i++;
                }
            } else if (c == '?') {
                // ? matches exactly one character except /
                regex.append("[^/]");
                i++;
            } else if (isRegexSpecialChar(c)) {
                // Escape special regex characters
                regex.append('\\').append(c);
                i++;
            } else {
                // Regular character
                regex.append(c);
                i++;
            }
        }

        return normalizedPath.matches(regex.toString());
    }

    private boolean isRegexSpecialChar(char c) {
        return c == '.' || c == '[' || c == ']' || c == '{' || c == '}' || c == '(' || c == ')' || c == '+' || c == '^'
                || c == '$' || c == '|' || c == '\\';
    }
}