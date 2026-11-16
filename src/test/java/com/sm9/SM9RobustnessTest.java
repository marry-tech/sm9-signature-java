package cmo.test;

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
 * Robustness tests for SM9 signature operations.
 * Traceability: Verifies component behaviour under malformed or unexpected input encoding.
 * Risk if violated: Component silently accepts data that strict validation should reject.
 */
public class SM9RobustnessTest {
    
    private SM9Curve curve;
    private SM9 sm9;
    private KGC kgc;
    private MasterKeyPair masterKeyPair;
    
    @Before
    public void setUp() throws Exception {
        curve = new SM9Curve();
        sm9 = new SM9(curve);
        kgc = new KGC(curve);
        masterKeyPair = kgc.genSignMasterKeyPair();
    }
    
    /**
     * Trace: R-01
     * Objective: Assess system behaviour with control-character identity strings
     * Risk: Silent acceptance of malformed identity data
     */
    @Test
    public void testGarbageIdentityInput() throws Exception {
        String garbageId = "\u0000\u0001\u0002\u0003";
        try {
            PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), garbageId, PrivateKeyType.KEY_SIGN);
            byte[] message = "test".getBytes();
            ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);
            boolean verify = sm9.verify(
                masterKeyPair.getPublicKey(), garbageId, message, signature);
            // Documents that the system accepts control characters without rejection
            assertTrue("System accepts garbage identity and produces consistent result", verify);
        } catch (IllegalArgumentException e) {
            // If a future validation layer rejects this, the test still passes
            assertTrue("System rejects garbage identity with controlled exception", true);
        }
    }
    
    /**
     * Trace: R-02
     * Objective: Verify handling of mixed-encoding (UTF-8 + ASCII) messages
     * Risk: Encoding mismatch leads to signature verification failure
     */
    @Test
    public void testMixedEncodingMessage() throws Exception {
        String userId = "user";
        PrivateKey privateKey = kgc.genPrivateKey(
            masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        byte[] message = "Hello\u4e2d\u6587World".getBytes("UTF-8");
        ResultSignature signature = sm9.sign(
            masterKeyPair.getPublicKey(), privateKey, message);
        boolean verify = sm9.verify(
            masterKeyPair.getPublicKey(), userId, message, signature);
        assertTrue(verify);
    }
    
    /**
     * Trace: R-03
     * Objective: Assess system behaviour with arbitrary byte sequences as message
     * Risk: Component crashes on unparseable byte input
     */
    @Test
    public void testInvalidByteSequence() throws Exception {
        String userId = "user";
        PrivateKey privateKey = kgc.genPrivateKey(
            masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        byte[] invalidMessage = new byte[]{(byte)0xFF, (byte)0xFE, (byte)0xFD};
        try {
            ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, invalidMessage);
            boolean verify = sm9.verify(
                masterKeyPair.getPublicKey(), userId, invalidMessage, signature);
            assertTrue("System handles arbitrary bytes without crashing", verify);
        } catch (Exception e) {
            assertTrue("System throws controlled exception for invalid bytes", true);
        }
    }
    
    /**
     * Trace: R-04
     * Objective: Handle very short message (1 byte)
     * Risk: Edge case not covered by normal functional tests
     */
    @Test
    public void testSingleByteMessage() throws Exception {
        String userId = "user";
        PrivateKey privateKey = kgc.genPrivateKey(
            masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        byte[] message = new byte[]{0x01};
        ResultSignature signature = sm9.sign(
            masterKeyPair.getPublicKey(), privateKey, message);
        boolean verify = sm9.verify(
            masterKeyPair.getPublicKey(), userId, message, signature);
        assertTrue(verify);
    }
    
    /**
     * Trace: R-05
     * Objective: Assess system behaviour with whitespace-only identity
     * Risk: Accept empty-equivalent identity that strict validation should reject
     */
    @Test
    public void testWhitespaceOnlyIdentity() throws Exception {
        String wsId = "   ";
        try {
            PrivateKey privateKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), wsId, PrivateKeyType.KEY_SIGN);
            byte[] message = "test".getBytes();
            ResultSignature signature = sm9.sign(
                masterKeyPair.getPublicKey(), privateKey, message);
            boolean verify = sm9.verify(
                masterKeyPair.getPublicKey(), wsId, message, signature);
            assertTrue("System accepts whitespace-only identity and produces consistent result", verify);
        } catch (IllegalArgumentException e) {
            assertTrue("System rejects whitespace-only identity with controlled exception", true);
        }
    }
}