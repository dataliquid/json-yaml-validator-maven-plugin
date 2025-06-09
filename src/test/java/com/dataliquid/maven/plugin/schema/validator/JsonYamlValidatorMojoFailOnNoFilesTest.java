package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import com.dataliquid.maven.plugin.schema.validator.JsonYamlValidatorMojo;

public class JsonYamlValidatorMojoFailOnNoFilesTest extends AbstractMojoTestCase {

    public void testFailOnNoFilesFoundDefault() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/no-files-found-default-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException when no files found with default failOnNoFilesFound=true");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("No files found matching the include patterns"));
        }
    }

    public void testFailOnNoFilesFoundExplicitTrue() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/no-files-found-true-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException when no files found with failOnNoFilesFound=true");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("No files found matching the include patterns"));
            assertTrue(e.getMessage().contains("failOnNoFilesFound=false"));
        }
    }

    public void testNoFailOnNoFilesFoundFalse() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/no-files-found-false-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception when failOnNoFilesFound=false
        mojo.execute();
    }

    public void testNonExistentSourceDirectory() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/non-existent-source-dir-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException when source directory doesn't exist with default failOnNoFilesFound=true");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("No files found matching the include patterns"));
        }
    }
}