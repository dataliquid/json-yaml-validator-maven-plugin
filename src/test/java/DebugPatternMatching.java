import org.apache.commons.io.FilenameUtils;

public class DebugPatternMatching {
    public static void main(String[] args) {
        // Test paths
        String[] testPaths = {
            "config.json",
            "data.yaml",
            "level1/config.json",
            "level1/normal/normal.yaml",
            "level1/level2/data.json",
            "level1/level2/level3/deep.json",
            "level1/level2/level3/config.yml"
        };
        
        // Include patterns
        String[] includes = {"**/*.json", "**/*.yaml", "**/*.yml"};
        
        // Test pattern matching
        System.out.println("Testing pattern matching:");
        System.out.println("========================");
        
        for (String path : testPaths) {
            System.out.println("\nPath: " + path);
            for (String pattern : includes) {
                boolean matches = matchesAntPattern(path, pattern);
                System.out.println("  Pattern '" + pattern + "': " + matches);
                
                // Show regex conversion
                String regex = convertToRegex(pattern);
                System.out.println("    Regex: " + regex);
                System.out.println("    Direct match: " + path.matches(regex));
            }
        }
    }
    
    private static boolean matchesAntPattern(String path, String pattern) {
        // Normalize paths to use forward slashes
        path = FilenameUtils.separatorsToUnix(path);
        pattern = FilenameUtils.separatorsToUnix(pattern);
        
        // Convert Ant pattern to regex
        String regex = pattern
            .replace(".", "\\.")
            .replace("?", "[^/]")
            .replace("**/", "(?:.*/)?")  // ** followed by / matches any number of directories
            .replace("/**", "/.*")       // /** at the end matches any subdirectory
            .replace("*", "[^/]*");      // * matches any characters except /
        
        return path.matches(regex);
    }
    
    private static String convertToRegex(String pattern) {
        pattern = FilenameUtils.separatorsToUnix(pattern);
        return pattern
            .replace(".", "\\.")
            .replace("?", "[^/]")
            .replace("**/", "(?:.*/)?")
            .replace("/**", "/.*")
            .replace("*", "[^/]*");
    }
}