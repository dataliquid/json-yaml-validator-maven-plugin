package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import com.dataliquid.maven.plugin.schema.validator.JsonYamlValidatorMojo;

public class JsonYamlValidatorMojoV7Test extends AbstractMojoTestCase {

    public void testV7ValidOrderJson() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v7-valid-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception
        mojo.execute();
    }

    public void testV7ValidOrderVipYaml() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v7-valid-vip-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception
        mojo.execute();
    }

    public void testV7InvalidOrderPattern() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v7-invalid-pattern-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid patterns");
        } catch (MojoFailureException e) {
            // Expected exception - multiple violations
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV7InvalidVipDiscount() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v7-invalid-vip-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to wrong VIP discount");
        } catch (MojoFailureException e) {
            // Expected exception - if-then-else violation
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV7IfThenElseValidation() throws Exception {
        // Test that if-then-else works properly
        File pom = getTestFile("target/test-classes/test-poms/v7-valid-vip-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // VIP customer with correct 0.2 discount
        mojo.execute();
    }

    public void testV7ValidOrderWithImports() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v7-imports-valid-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should execute without exception - tests schema importing
        mojo.execute();
    }

    public void testV7InvalidOrderWithImportsAddress() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v7-imports-invalid-address-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid address in imported schema");
        } catch (MojoFailureException e) {
            // Expected exception - address validation from imported schema should fail
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV7InvalidOrderWithImportsCustomer() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/v7-imports-invalid-customer-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        try {
            mojo.execute();
            fail("Expected MojoFailureException due to invalid customer in imported schema");
        } catch (MojoFailureException e) {
            // Expected exception - customer validation from imported schema should fail
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }

    public void testV7SchemaImportWithMapping() throws Exception {
        // Test schema imports with custom mappings
        File pom = getTestFile("target/test-classes/test-poms/v7-imports-with-mapping-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);
        
        // Should successfully validate data that uses imported schemas
        mojo.execute();
    }
}