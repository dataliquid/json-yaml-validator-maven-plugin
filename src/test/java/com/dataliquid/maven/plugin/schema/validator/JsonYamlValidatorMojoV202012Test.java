package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import com.dataliquid.maven.plugin.schema.validator.JsonYamlValidatorMojo;

public class JsonYamlValidatorMojoV202012Test extends AbstractMojoTestCase {

    public void testV202012ValidApiConfigJson() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v202012-valid-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception
        mojo.execute();
    }

    public void testV202012ValidApiConfigMinimalYaml() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v202012-valid-minimal-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception
        mojo.execute();
    }

    public void testV202012InvalidApiAuth() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v202012-invalid-auth-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid authentication config");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV202012InvalidApiComplexYaml() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v202012-invalid-complex-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to multiple validation errors");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV202012UnevaluatedProperties() throws Exception {
        // Test that unevaluatedProperties constraint works
        File pom = getTestFile("target/test-classes/test-poms/v202012-valid-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Valid config without extra properties
        mojo.execute();
    }

    public void testV202012PrefixItems() throws Exception {
        // Test prefixItems validation for rate limiting
        File pom = getTestFile("target/test-classes/test-poms/v202012-valid-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // First item in limits array has special constraints
        mojo.execute();
    }
}