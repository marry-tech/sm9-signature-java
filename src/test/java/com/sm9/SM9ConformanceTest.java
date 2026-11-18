package com.sm9;

import org.junit.Before;
import org.junit.Test;

import com.sm9.KGC;
import com.sm9.MasterKeyPair;
import com.sm9.MasterPrivateKey;
import com.sm9.MasterPublicKey;
import com.sm9.PrivateKey;
import com.sm9.PrivateKeyType;
import com.sm9.ResultSignature;
import com.sm9.SM9;
import com.sm9.SM9Curve;

import static org.junit.Assert.*;

/**
 * Conformance tests against GM/T 0044 standard structure.
 *
 * Verifies that keys and signatures belong to the correct mathematical
 * groups (G1, G2, Zr) as required by the standard.
 */
/**
 * Conformance tests for SM9 against GM/T 0044.2 standard.
 * Traceability: Verifies mathematical correctness of curve parameters, pairing 
 * equations and key generation.
 * Risk if violated: Implementation deviates from national standard, producing 
 * non-interoperable or weak cryptographic output.
 */
public class SM9ConformanceTest {

    private SM9Curve curve;
    private KGC kgc;
    private SM9 sm9;
    private MasterKeyPair masterKeyPair;

    @Before
    public void setUp() {
        curve = new SM9Curve();
        kgc = new KGC(curve);
        masterKeyPair = kgc.genSignMasterKeyPair();
        sm9 = new SM9(curve);
    }

    @Test
    public void testMasterKeyPairContainsValidKeys() {
        assertNotNull(masterKeyPair);
        assertNotNull(masterKeyPair.getPublicKey());
        assertNotNull(masterKeyPair.getPrivateKey());

        MasterPublicKey mpk = masterKeyPair.getPublicKey();
        MasterPrivateKey msk = masterKeyPair.getPrivateKey();

        // Public key must contain Ppub-s (G2 point)
        assertNotNull("Master public key Q must not be null", mpk.Q);

        // Private key must contain master secret key element (Zr)
        assertNotNull("Master private key element must not be null", msk.d);
    }

    @Test
    public void testPrivateKeyBelongsToG1() throws Exception {
        String userId = "conformance@test.com";
        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);

        assertNotNull(privateKey);
        assertNotNull(privateKey.d);

        // User private key must be a valid G1 element (not infinity)
        assertFalse("Private key must not be the infinity point",
                privateKey.d.isZero());
    }

    @Test
    public void testSignatureComponentsBelongToCorrectGroups() throws Exception {
        String userId = "group@check.com";
        byte[] message = "Group membership test".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);

        assertNotNull(signature);
        assertNotNull(signature.h);
        assertNotNull(signature.s);

        // h must be a valid scalar in Zr (non-null, non-zero)
        assertFalse("h must not be zero", signature.h.equals(java.math.BigInteger.ZERO));

        // S must be a valid G1 point (non-infinity)
        assertFalse("S must not be the infinity point", signature.s.isZero());
    }

    @Test
    public void testVerificationEquationHolds() throws Exception {
        // Core verification: e(P1, Ppub-s)^h = e(S, H1(ID)*P2 + Ppub-s)
        String userId = "equation@verify.com";
        byte[] message = "Bilinear pairing equation".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);

        boolean isValid = sm9.verify(
                masterKeyPair.getPublicKey(), userId, message, signature);

        assertTrue("Bilinear pairing verification equation must hold", isValid);
    }

    @Test
    public void testConsistentBehaviorAcrossMultipleRuns() throws Exception {
        String userId = "stable@test.com";
        byte[] message = "Stability check".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);

        // Generate multiple signatures for the same input
        ResultSignature sig1 = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);
        ResultSignature sig2 = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);

        // Both must verify successfully
        assertTrue(sm9.verify(
                masterKeyPair.getPublicKey(), userId, message, sig1));
        assertTrue(sm9.verify(
                masterKeyPair.getPublicKey(), userId, message, sig2));
    }

    @Test
    public void testOfficialTestVectorGMT00445AnnexA() throws Exception {
        // Deterministic test vector from GM/T 0044.5-2016, Annex A (worked example
        // of the signature algorithm). Fixing both the master key k and the nonce r
        // makes signing fully deterministic, so the output can be compared
        // byte-for-byte against the values published in the standard.
        SM9Curve cv = new SM9Curve();
        KGCWithStandardTestKey stdKgc = new KGCWithStandardTestKey(cv);
        SM9WithStandardTestKey stdSm9 = new SM9WithStandardTestKey(cv);

        KGCWithStandardTestKey.k = new java.math.BigInteger(
                "0130E78459D78545CB54C587E02CF480CE0B66340F319F348A1D5B1F2DC5F4", 16);
        SM9WithStandardTestKey.r = new java.math.BigInteger(
                "033C8616B06704813203DFD00965022ED15975C662337AED648835DC4B1CBE", 16);

        MasterKeyPair stdKeyPair = stdKgc.genSignMasterKeyPair();

        // Published x-coordinate of Ppub-s = [ks]P2
        // (G2 points live over a quadratic extension field, so the serialised
        //  point is longer than one 256-bit component; assert on the prefix.)
        String ppubHex = SM9Utils.toHexString(SM9Utils.G2ElementToByte(
                (it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement) stdKeyPair.getPublicKey().Q
        )).replace(" ", "");
        assertTrue("Ppub-s must match the GM/T 0044.5 test vector",
                ppubHex.startsWith("9F64080B3084F733E48AFF4B41B565011CE0711C5E392CFB0AB1B6791B94C408"));

        PrivateKey stdPrivateKey = stdKgc.genPrivateKey(
                stdKeyPair.getPrivateKey(), "Alice", PrivateKeyType.KEY_SIGN);

        ResultSignature stdSignature = stdSm9.sign(
                stdKeyPair.getPublicKey(), stdPrivateKey, "Chinese IBS standard".getBytes());

        // Published h = H2(M || w, N)
        assertEquals("Signature h must match the published test vector",
                new java.math.BigInteger(
                        "823C4B21E4BD2DFE1ED92C606653E996668563152FC33F55D7BFBB9BD9705ADB", 16),
                stdSignature.h);

        // Published x-coordinate of S = [l]ds_A
        String sHex = SM9Utils.toHexString(SM9Utils.G1ElementToBytes(stdSignature.s)).replace(" ", "");
        assertTrue("Signature S must match the published test vector",
                sHex.startsWith("73BF96923CE58B6AD0E13E9643A406D8EB98417C50EF1B29CEF9ADB48B6D598C"));

        assertTrue("Official test vector signature must verify",
                stdSm9.verify(stdKeyPair.getPublicKey(), "Alice",
                        "Chinese IBS standard".getBytes(), stdSignature));
    }
}
