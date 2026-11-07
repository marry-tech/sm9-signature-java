package com.sm9;

import org.junit.Before;
import org.junit.Test;

import com.sm9.KGC;
import com.sm9.MasterKeyPair;
import com.sm9.PrivateKey;
import com.sm9.PrivateKeyType;
import com.sm9.ResultSignature;
import com.sm9.SM9;
import com.sm9.SM9Curve;

import static org.junit.Assert.*;

/**
 * Functional tests for SM9 signature scheme under normal operating conditions.
 *
 * Verifies basic correctness of key generation, signing, and verification.
 */
public class SM9FunctionalTest {

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
    public void testMasterKeyPairGeneration() {
        assertNotNull("Master public key should not be null",
                masterKeyPair.getPublicKey());
        assertNotNull("Master private key should not be null",
                masterKeyPair.getPrivateKey());
    }

    @Test
    public void testPrivateKeyExtraction() throws Exception {
        String userId = "user@example.com";
        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);

        assertNotNull("Extracted private key should not be null", privateKey);
        assertNotNull("Private key element should not be null", privateKey.d);
    }

    @Test
    public void testSignAndVerifyRoundTrip() throws Exception {
        String userId = "alice@test.com";
        byte[] message = "Hello, SM9!".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);

        assertNotNull("Signature should not be null", signature);
        assertNotNull("Signature element h should not be null", signature.h);
        assertNotNull("Signature element S should not be null", signature.s);

        boolean isValid = sm9.verify(
                masterKeyPair.getPublicKey(), userId, message, signature);

        assertTrue("Signature should be valid for original message and key", isValid);
    }

    @Test
    public void testVerifyWithCorrectPublicKey() throws Exception {
        String userId = "bob@verify.com";
        byte[] message = "Verification test data".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);

        boolean result = sm9.verify(
                masterKeyPair.getPublicKey(), userId, message, signature);

        assertTrue("Verification with correct public key must succeed", result);
    }

    @Test
    public void testSignWithDifferentIdentifiers() throws Exception {
        String id1 = "user1@domain.com";
        String id2 = "user2@domain.com";
        byte[] message = "Same message".getBytes();

        PrivateKey sk1 = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), id1, PrivateKeyType.KEY_SIGN);
        PrivateKey sk2 = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), id2, PrivateKeyType.KEY_SIGN);

        ResultSignature sig1 = sm9.sign(masterKeyPair.getPublicKey(), sk1, message);
        ResultSignature sig2 = sm9.sign(masterKeyPair.getPublicKey(), sk2, message);

        assertNotEquals("Signatures from different users should differ",
                sig1.h, sig2.h);
    }

    @Test
    public void testSignSameMessageTwice() throws Exception {
        String userId = "repeat@user.com";
        byte[] message = "Deterministic input".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature sig1 = sm9.sign(masterKeyPair.getPublicKey(), privateKey, message);
        ResultSignature sig2 = sm9.sign(masterKeyPair.getPublicKey(), privateKey, message);

        boolean valid1 = sm9.verify(
                masterKeyPair.getPublicKey(), userId, message, sig1);
        boolean valid2 = sm9.verify(
                masterKeyPair.getPublicKey(), userId, message, sig2);

        assertTrue("First signature must be valid", valid1);
        assertTrue("Second signature must be valid", valid2);
    }
}
