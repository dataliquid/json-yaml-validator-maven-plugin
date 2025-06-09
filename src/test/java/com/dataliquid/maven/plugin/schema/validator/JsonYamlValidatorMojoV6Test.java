package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import com.dataliquid.maven.plugin.schema.validator.JsonYamlValidatorMojo;

public class JsonYamlValidatorMojoV6Test extends AbstractMojoTestCase {

    public void testV6ValidUserJson() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v6-valid-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception
        mojo.execute();
    }

    public void testV6ValidUserMinimalYaml() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v6-valid-minimal-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception
        mojo.execute();
    }

    public void testV6InvalidUserPattern() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v6-invalid-pattern-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid patterns");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV6InvalidUserProperties() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v6-invalid-properties-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid properties");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV6ContainsValidation() throws Exception {
        // Test that the 'contains' constraint works properly in V6
        File pom = getTestFile("target/test-classes/test-poms/v6-valid-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // The valid user has 'user' role which satisfies the contains constraint
        mojo.execute();
    }

    public void testV6ValidEmployeeWithImports() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v6-imports-valid-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception - tests schema importing
        mojo.execute();
    }

    public void testV6InvalidEmployeeWithImportsContact() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v6-imports-invalid-contact-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid contact information");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV6InvalidEmployeeWithImportsRole() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v6-imports-invalid-role-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid role and other errors");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }
}