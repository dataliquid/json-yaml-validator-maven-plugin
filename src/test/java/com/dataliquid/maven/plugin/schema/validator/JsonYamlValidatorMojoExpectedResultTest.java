package com.dataliquid.maven.plugin.schema.validator;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class JsonYamlValidatorMojoExpectedResultTest extends AbstractMojoTestCase {

    public void testExpectedResultSuccess() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/expected-result-success-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);

        // Should execute without exception when valid files are provided and
        // expectedResult=SUCCESS
        mojo.execute();
    }

    public void testExpectedResultError() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/expected-result-error-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);

        // Should execute without exception when invalid files are provided and
        // expectedResult=ERROR
        mojo.execute();
    }

    public void testExpectedResultErrorWithExpectedErrors() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/expected-errors-matching-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);

        // Should execute without exception when expected errors match
        mojo.execute();
    }

    public void testExpectedResultErrorWithStrictMatching() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/expected-errors-strict-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);

        // Should execute without exception when expected errors match exactly in strict
        // mode
        mojo.execute();
    }

    public void testExpectedResultErrorButFilesValid() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/expected-result-success-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);

        // Manually set expectedResult to ERROR to test mismatch
        setVariableValueToObject(mojo, "expectedResult", "ERROR");

        try {
            mojo.execute();
            fail("Expected MojoFailureException when expectedResult=ERROR but files are valid");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Expected validation errors but all files were valid"));
        }
    }

    public void testExpectedResultSuccessButFilesInvalid() throws Exception {
        File pom = getTestFile("target/test-classes/test-poms/expected-result-error-test-pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JsonYamlValidatorMojo mojo = (JsonYamlValidatorMojo) lookupMojo("validate", pom);
        assertNotNull(mojo);

        // Manually set expectedResult to SUCCESS to test mismatch
        setVariableValueToObject(mojo, "expectedResult", "SUCCESS");

        try {
            mojo.execute();
            fail("Expected MojoFailureException when expectedResult=SUCCESS but files are invalid");
        } catch (MojoFailureException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Validation failed"));
        }
    }
}
