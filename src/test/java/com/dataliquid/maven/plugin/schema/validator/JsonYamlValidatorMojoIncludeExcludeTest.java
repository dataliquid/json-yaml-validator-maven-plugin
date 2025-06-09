package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * Test case for validating include/exclude functionality with recursive directory traversal.
 * 
 * IMPORTANT: This test demonstrates that the exclude functionality is NOT working correctly!
 * Files in 'draft' and 'temp' directories are NOT being excluded as expected.
 */
public class JsonYamlValidatorMojoIncludeExcludeTest extends AbstractMojoTestCase {

    private static final String TEST_DATA_DIR = "src/test/resources/test-data/include-exclude-test";
    
    /**
     * Test that validates the exact count of processed files.
     * This test ensures that the include/exclude patterns work correctly by
     * verifying that exactly 7 files are processed out of the 13 total files.
     * 
     * Expected behavior:
     * - Include: All .json, .yaml, and .yml files
     * - Exclude: All files in 'draft' and 'temp' directories, and all .backup.json files
     * 
     * Should validate these 7 files:
     * 1. config.json (root level)
     * 2. data.yaml (root level)
     * 3. level1/config.json
     * 4. level1/normal/normal.yaml
     * 5. level1/level2/data.json
     * 6. level1/level2/level3/deep.json
     * 7. level1/level2/level3/config.yml
     * 
     * Should exclude these 6 files:
     * 1. data.backup.json (root level - matches *.backup.json)
     * 2. draft/draft-config.json (in draft directory)
     * 3. temp/temp-data.yml (in temp directory)
     * 4. level1/level2/config.backup.json (matches *.backup.json)
     * 5. level1/level2/draft/draft.json (in draft directory)
     * 6. level1/level2/temp/temp.yaml (in temp directory)
     */
    public void testIncludeExcludePatterns() throws Exception {
        File pom = getTestFile("src/test/resources/test-poms/include-exclude-pom.xml");
        assertNotNull("POM file should exist", pom);
        assertTrue("POM file should exist", pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull("Mojo should not be null", mojo);
        
        // Debug: Print the source directory
        Field sourceDirectoryField = JsonYamlValidatorMojo.class.getDeclaredField("sourceDirectory");
        sourceDirectoryField.setAccessible(true);
        File sourceDir = (File) sourceDirectoryField.get(mojo);
        System.out.println("Test source directory: " + sourceDir.getAbsolutePath());
        
        // Count files before execution
        int fileCount = countValidatedFiles(mojo);
        System.out.println("\nTotal files found by mojo: " + fileCount);
        
        // Expected counts
        final int EXPECTED_INCLUDED_FILES = 7;
        final int EXPECTED_EXCLUDED_FILES = 6;
        final int TOTAL_FILES = 13;
        
        // Assert the expected number of files
        assertEquals("Mojo should find exactly " + EXPECTED_INCLUDED_FILES + " files after applying include/exclude patterns", 
                     EXPECTED_INCLUDED_FILES, fileCount);
        
        // Execute the mojo - it should succeed as all included files are valid
        try {
            mojo.execute();
            // If execution succeeds, validation passed for all included files
        } catch (MojoExecutionException e) {
            fail("Mojo execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to count the files that would be validated by the mojo.
     * Uses reflection to access private methods and fields.
     */
    private int countValidatedFiles(JsonYamlValidatorMojo mojo) throws Exception {
        // Use reflection to call findFilesToValidate
        java.lang.reflect.Method findFilesMethod = JsonYamlValidatorMojo.class.getDeclaredMethod("findFilesToValidate");
        findFilesMethod.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        List<File> files = (List<File>) findFilesMethod.invoke(mojo);
        
        // Debug output
        System.out.println("\nFiles found by mojo:");
        Field sourceDirectoryField = JsonYamlValidatorMojo.class.getDeclaredField("sourceDirectory");
        sourceDirectoryField.setAccessible(true);
        File sourceDir = (File) sourceDirectoryField.get(mojo);
        
        for (File file : files) {
            String relativePath = sourceDir.toURI().relativize(file.toURI()).getPath();
            
            // Check if file should be excluded
            boolean shouldBeExcluded = false;
            String reason = "";
            
            if (relativePath.contains("/draft/") || relativePath.startsWith("draft/")) {
                shouldBeExcluded = true;
                reason = "in draft directory";
            } else if (relativePath.contains("/temp/") || relativePath.startsWith("temp/")) {
                shouldBeExcluded = true;
                reason = "in temp directory";
            } else if (relativePath.endsWith(".backup.json")) {
                shouldBeExcluded = true;
                reason = "backup file";
            }
            
            System.out.println("  - " + relativePath + (shouldBeExcluded ? " [SHOULD BE EXCLUDED: " + reason + "]" : " [OK]"));
        }
        
        return files.size();
    }
    
    /**
     * Test to verify all files in the test directory structure.
     * This helps understand what files exist and what the mojo is finding.
     */
    public void testVerifyTestDirectoryStructure() throws Exception {
        File testDir = getTestFile(TEST_DATA_DIR);
        assertTrue("Test directory should exist", testDir.exists());
        
        System.out.println("\nAll files in test directory:");
        try (Stream<Path> paths = Files.walk(testDir.toPath())) {
            List<Path> files = paths
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.getFileName().toString();
                    return name.endsWith(".json") || name.endsWith(".yaml") || name.endsWith(".yml");
                })
                .sorted()
                .collect(Collectors.toList());
                
            assertEquals("Should have exactly 13 test files", 13, files.size());
            
            for (Path file : files) {
                String relativePath = testDir.toPath().relativize(file).toString();
                System.out.println("  - " + relativePath);
            }
        }
    }
}