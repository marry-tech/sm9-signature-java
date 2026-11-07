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
 * Boundary condition tests for SM9 implementation.
 *
 * Tests edge cases: null inputs, empty strings, oversized messages,
 * and unicode identifiers.
 */
public class SM9BoundaryTest {

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

    // ==================== Null Input Tests ====================

    @Test(expected = IllegalArgumentException.class)
    public void testSignWithNullPublicKey() throws Exception {
        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), "test@user.com", PrivateKeyType.KEY_SIGN);
        sm9.sign(null, privateKey, "message".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignWithNullPrivateKey() throws Exception {
        sm9.sign(masterKeyPair.getPublicKey(), null, "message".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignWithNullMessage() throws Exception {
        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), "test@user.com", PrivateKeyType.KEY_SIGN);
        sm9.sign(masterKeyPair.getPublicKey(), privateKey, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignWithEmptyMessage() throws Exception {
        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), "test@user.com", PrivateKeyType.KEY_SIGN);
        sm9.sign(masterKeyPair.getPublicKey(), privateKey, new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyWithNullPublicKey() throws Exception {
        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), "test@user.com", PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, "msg".getBytes());
        sm9.verify(null, "test@user.com", "msg".getBytes(), signature);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyWithNullId() throws Exception {
        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), "test@user.com", PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, "msg".getBytes());
        sm9.verify(masterKeyPair.getPublicKey(), null, "msg".getBytes(), signature);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyWithEmptyId() throws Exception {
        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), "test@user.com", PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, "msg".getBytes());
        sm9.verify(masterKeyPair.getPublicKey(), "", "msg".getBytes(), signature);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyWithNullSignature() {
        sm9.verify(masterKeyPair.getPublicKey(), "user@test.com", "msg".getBytes(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractPrivateKeyWithNullMasterKey() throws Exception {
        kgc.genPrivateKey(null, "test@user.com", PrivateKeyType.KEY_SIGN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractPrivateKeyWithNullId() throws Exception {
        kgc.genPrivateKey(masterKeyPair.getPrivateKey(), null, PrivateKeyType.KEY_SIGN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractPrivateKeyWithEmptyId() throws Exception {
        kgc.genPrivateKey(masterKeyPair.getPrivateKey(), "", PrivateKeyType.KEY_SIGN);
    }

    // ==================== Oversized Message Tests ====================

    @Test
    public void testSignWithLargeMessage() throws Exception {
        String userId = "large@msg.com";
        byte[] largeMessage = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeMessage.length; i++) {
            largeMessage[i] = (byte) (i % 256);
        }

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, largeMessage);

        assertNotNull("Signature of large message should not be null", signature);

        boolean isValid = sm9.verify(
                masterKeyPair.getPublicKey(), userId, largeMessage, signature);
        assertTrue("Large message signature must verify correctly", isValid);
    }

    // ==================== Unicode Identifier Tests ====================

    @Test
    public void testSignWithUnicodeIdentifier() throws Exception {
        String userId = "user@\u6D4B\u8BD5.com"; // Chinese characters

        PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, "msg".getBytes());

        boolean isValid = sm9.verify(
                masterKeyPair.getPublicKey(), userId, "msg".getBytes(), signature);
        assertTrue("Unicode identifier signature must verify", isValid);
    }
}
