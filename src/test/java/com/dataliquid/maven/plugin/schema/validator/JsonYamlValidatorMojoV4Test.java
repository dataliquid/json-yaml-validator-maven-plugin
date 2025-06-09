package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import com.dataliquid.maven.plugin.schema.validator.JsonYamlValidatorMojo;

public class JsonYamlValidatorMojoV4Test extends AbstractMojoTestCase {

    public void testV4ValidProductJson() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v4-valid-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception
        mojo.execute();
    }

    public void testV4ValidProductMinimalYaml() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v4-valid-minimal-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception
        mojo.execute();
    }

    public void testV4InvalidProductPrice() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v4-invalid-price-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid price");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV4InvalidProductMissingRequired() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v4-invalid-missing-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to missing required fields");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV4WithFailOnErrorFalse() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v4-invalid-no-fail-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception even with invalid data
        mojo.execute();
    }

    public void testV4ValidProductWithImports() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v4-imports-valid-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception - tests schema importing
        mojo.execute();
    }

    public void testV4InvalidProductWithImportsPrice() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v4-imports-invalid-price-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid price and currency");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV4InvalidProductWithImportsSupplier() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v4-imports-invalid-supplier-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid supplier data");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }
}