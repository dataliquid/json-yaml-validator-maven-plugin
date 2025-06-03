package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Custom file filter that handles Ant-style patterns for includes/excludes.
 * Supports:
 * - ? matches one character
 * - * matches zero or more characters (but not directory separators)
 * - ** matches zero or more directories
 */
public class AntPatternFileFilter implements IOFileFilter {
    
    private final File sourceDirectory;
    private final String[] includes;
    private final String[] excludes;
    
    public AntPatternFileFilter(File sourceDirectory, String[] includes, String[] excludes) {
        this.sourceDirectory = sourceDirectory;
        this.includes = includes;
        this.excludes = excludes;
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
    private boolean matchesAntPattern(String path, String pattern) {
        // Normalize paths to use forward slashes
        path = FilenameUtils.separatorsToUnix(path);
        pattern = FilenameUtils.separatorsToUnix(pattern);
        
        // Convert Ant pattern to regex
        StringBuilder regex = new StringBuilder();
        int i = 0;
        
        while (i < pattern.length()) {
            char c = pattern.charAt(i);
            
            if (c == '*') {
                // Check for **
                if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                    // Handle ** pattern
                    if (i + 2 < pattern.length() && pattern.charAt(i + 2) == '/') {
                        // **/ means any number of directories (including none)
                        regex.append("(?:(?:.*/)?)?");
                        i += 3; // Skip **/
                    } else if (i == 0 || pattern.charAt(i - 1) == '/') {
                        // /** at the end or in the middle
                        regex.append(".*");
                        i += 2; // Skip **
                    } else {
                        // Just two * in a row, treat as two wildcards
                        regex.append("[^/]*[^/]*");
                        i += 2;
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
            } else if (c == '.' || c == '[' || c == ']' || c == '{' || c == '}' || 
                      c == '(' || c == ')' || c == '+' || c == '^' || c == '$' || 
                      c == '|' || c == '\\') {
                // Escape special regex characters
                regex.append('\\').append(c);
                i++;
            } else {
                // Regular character
                regex.append(c);
                i++;
            }
        }
        
        return path.matches(regex.toString());
    }
}