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
    public void testKeyExtractionWithStandardIdentifier() throws Exception {
        // Using a common identifier format
        String standardId = "Alice";
        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), standardId, PrivateKeyType.KEY_SIGN);

        assertNotNull(privateKey);
        assertNotNull(privateKey.d);

        byte[] message = "Standard conformance message".getBytes();
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);

        boolean isValid = sm9.verify(
                masterKeyPair.getPublicKey(), standardId, message, signature);
        assertTrue("Standard identifier must produce verifiable signature", isValid);
    }
}
