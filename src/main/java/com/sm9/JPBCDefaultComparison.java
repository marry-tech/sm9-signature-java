package cmo.test;

import com.sm9.SM9CurveParameters;

import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

/**
 * Demonstrates the difference between JPBC default Type-A curve parameters
 * and SM9 standard parameters (GM/T 0044).
 *
 * JPBC's built-in Type-A curve uses a pre-configured parameter set optimized
 * for general pairing-based cryptography, but it does NOT conform to the
 * SM9 national standard.
 *
 * This class is used for testing and educational purposes to show why
 * explicit parameter mapping is required for SM9 conformance.
 */
public class JPBCDefaultComparison {

    /**
     * Returns JPBC's default Type-A curve parameters.
     * These are NOT compatible with SM9.
     */
    public static PropertiesParameters getJPBCDefaultTypeAParameters() {
        PropertiesParameters params = new PropertiesParameters();
        params.put("type", "a");
        params.put("q", "87807107996633125224377819847540498158068831994142082"
                + "110020023644710430959422974611749300242106652374131707");
        params.put("h", "12016012264891146079388821366740534204802954401251311"
                + "8229196531971443062");
        params.put("r", "730750818665451621361119245571504901405976559617");
        params.put("exp2", "159");
        params.put("exp1", "107");
        params.put("sign1", "1");
        params.put("sign0", "1");
        return params;
    }

    /**
     * Returns the SM9 standard parameters as configured in SM9CurveParameters.
     */
    public static PropertiesParameters getSM9StandardParameters() {
        return SM9CurveParameters.createSM9PropertiesParameters();
    }

    /**
     * Prints a side-by-side comparison of key parameter values.
     */
    public static void printComparison() {
        PropertiesParameters jpbc = getJPBCDefaultTypeAParameters();
        PropertiesParameters sm9 = getSM9StandardParameters();

        System.out.println("=== JPBC Default Type-A vs SM9 Standard Parameters ===");
        System.out.println();
        System.out.println("Field order (q):");
        System.out.println("  JPBC:  " + jpbc.getString("q"));
        System.out.println("  SM9:   " + sm9.getString("q"));
        System.out.println();
        System.out.println("Subgroup order (r/N):");
        System.out.println("  JPBC:  " + jpbc.getString("r"));
        System.out.println("  SM9:   " + sm9.getString("r"));
        System.out.println();
        System.out.println("Curve parameter (b):");
        System.out.println("  JPBC:  (derived from type)");
        System.out.println("  SM9:   " + sm9.getString("b"));
        System.out.println();
        System.out.println("Parameter type:");
        System.out.println("  JPBC:  a (Type-A supersingular curve)");
        System.out.println("  SM9:   f (finite field with custom parameters)");
        System.out.println();
        System.out.println("Conclusion: Direct use of JPBC defaults would yield");
        System.out.println("an incompatible curve. SM9 parameters must be explicitly");
        System.out.println("configured via PropertiesParameters.");
    }

    public static void main(String[] args) {
        printComparison();
    }
}
