package com.sm9;

import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;
import org.junit.Test;

import com.sm9.SM9CurveParameters;

import static org.junit.Assert.*;

/**
 * Verifies that SM9 curve parameters are correctly mapped from the
 * GM/T 0044 standard into JPBC's PropertiesParameters format.
 *
 * These tests document the technical reason for explicit parameter
 * configuration instead of using JPBC's built-in defaults.
 */
public class ParameterMappingTest {

    @Test
    public void testSM9ParametersAreNotEmpty() {
        PropertiesParameters params = SM9CurveParameters.createSM9PropertiesParameters();

        assertNotNull("q should be defined", params.getString("q"));
        assertNotNull("r should be defined", params.getString("r"));
        assertNotNull("b should be defined", params.getString("b"));
        assertNotNull("beta should be defined", params.getString("beta"));
    }

    @Test
    public void testSM9ParametersDifferFromJPBCDefaults() {
        PropertiesParameters sm9 = SM9CurveParameters.createSM9PropertiesParameters();
        PropertiesParameters jpbc = JPBCDefaultComparison.getJPBCDefaultTypeAParameters();

        // Field order must differ
        assertNotEquals("SM9 field order q must differ from JPBC default",
                sm9.getString("q"), jpbc.getString("q"));

        // Subgroup order must differ
        assertNotEquals("SM9 subgroup order r must differ from JPBC default",
                sm9.getString("r"), jpbc.getString("r"));
    }

    @Test
    public void testSM9ParameterFormatIsTypeF() {
        PropertiesParameters params = SM9CurveParameters.createSM9PropertiesParameters();

        // SM9 uses 'f' type (finite field with custom parameters)
        // JPBC default Type-A uses 'a' type
        assertEquals("SM9 parameter type should be 'f'", "f", params.getString("type"));
    }

    @Test
    public void testSM9FieldOrderIs256Bits() {
        PropertiesParameters params = SM9CurveParameters.createSM9PropertiesParameters();
        String q = params.getString("q");

        // SM9 field order q should be approximately 256 bits
        // Log2(10) ¡Ö 3.32, so 77 decimal digits ¡Ö 256 bits
        assertTrue("SM9 field order should be around 256 bits",
                q.length() >= 70 && q.length() <= 80);
    }

    @Test
    public void testSM9SubgroupOrderMatchesN() {
        PropertiesParameters params = SM9CurveParameters.createSM9PropertiesParameters();
        String r = params.getString("r");

        // Subgroup order r (named N in SM9 standard) should match
        assertEquals("Subgroup order r should match SM9 standard N",
                SM9CurveParameters.N.toString(), r);
    }
}
