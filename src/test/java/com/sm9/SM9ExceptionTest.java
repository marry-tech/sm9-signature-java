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
import com.sm9.SM9Utils;

import java.math.BigInteger;

import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;

import static org.junit.Assert.*;

/**
 * Exception and security tests for SM9 implementation.
 *
 * Verifies that the implementation correctly rejects invalid cryptographic
 * inputs: tampered signatures, wrong keys, and other attack scenarios.
 */
public class SM9ExceptionTest {

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

    // ==================== Tampered Signature Tests ====================

    @Test
    public void testVerifyWithTamperedH() throws Exception {
        String userId = "alice@secure.com";
        byte[] message = "Important contract".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature validSig = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);

        // Tamper with h: create new signature with modified h
        BigInteger tamperedH = validSig.h.add(BigInteger.ONE).mod(curve.N);
        ResultSignature tamperedSig = new ResultSignature(tamperedH, validSig.s);

        boolean result = sm9.verify(
                masterKeyPair.getPublicKey(), userId, message, tamperedSig);
        assertFalse("Verification must fail when h is tampered", result);
    }

    @Test
    public void testVerifyWithTamperedS() throws Exception {
        String userId = "bob@secure.com";
        byte[] message = "Payment authorization".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature validSig = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);

        // Tamper with S: multiply by a random scalar
        CurveElement tamperedS = validSig.s.duplicate().mul(SM9Utils.genRandom(curve.random, curve.N));
        ResultSignature tamperedSig = new ResultSignature(validSig.h, tamperedS);

        boolean result = sm9.verify(
                masterKeyPair.getPublicKey(), userId, message, tamperedSig);
        assertFalse("Verification must fail when S is tampered", result);
    }

    // ==================== Wrong Key Tests ====================

    @Test
    public void testVerifyWithWrongPublicKey() throws Exception {
        // Generate a different KGC's key pair
        KGC otherKgc = new KGC(new SM9Curve());
        MasterKeyPair otherKeyPair = otherKgc.genSignMasterKeyPair();

        String userId = "eve@attack.com";
        byte[] message = "Forged message".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);

        // Verify with attacker's public key
        boolean result = sm9.verify(
                otherKeyPair.getPublicKey(), userId, message, signature);
        assertFalse("Verification must fail with wrong master public key", result);
    }

    @Test
    public void testVerifyWithWrongUserId() throws Exception {
        String aliceId = "alice@domain.com";
        String bobId = "bob@domain.com";
        byte[] message = "Shared message".getBytes();

        PrivateKey aliceKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), aliceId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), aliceKey, message);

        boolean result = sm9.verify(
                masterKeyPair.getPublicKey(), bobId, message, signature);
        assertFalse("Verification must fail with mismatched user ID", result);
    }

    @Test
    public void testVerifyWithWrongMessage() throws Exception {
        String userId = "charlie@honest.com";
        byte[] originalMessage = "Original terms".getBytes();
        byte[] alteredMessage = "Modified terms!!".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, originalMessage);

        boolean result = sm9.verify(
                masterKeyPair.getPublicKey(), userId, alteredMessage, signature);
        assertFalse("Verification must fail with altered message", result);
    }

    // ==================== Replay Attack Test ====================

    @Test
    public void testVerifyWithReusedSignature() throws Exception {
        String userId = "replay@victim.com";
        byte[] message1 = "Transfer $100".getBytes();
        byte[] message2 = "Transfer $10000".getBytes();

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message1);

        boolean result = sm9.verify(
                masterKeyPair.getPublicKey(), userId, message2, signature);
        assertFalse("Reused signature for different message must fail", result);
    }

    // ==================== Edge Case ====================

    @Test
    public void testSignatureWithSingleByteMessage() throws Exception {
        String userId = "zero@edge.com";
        byte[] singleByteMessage = new byte[]{0x00};

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, singleByteMessage);

        assertNotNull("Signature for single-byte message should not be null", signature);

        boolean isValid = sm9.verify(
                masterKeyPair.getPublicKey(), userId, singleByteMessage, signature);
        assertTrue("Single-byte message signature must verify", isValid);
    }
}
