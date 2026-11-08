package com.aa;

import com.sm9.KGC;
import com.sm9.SM9;
import com.sm9.SM9Curve;
import com.sm9.MasterKeyPair;
import com.sm9.PrivateKey;
import com.sm9.PrivateKeyType;
import com.sm9.ResultSignature;
import com.sm9.SM9Utils;

/**
 * Console demo for the SM9 signature module.
 *
 * Normal flow: generate a signing master key pair, extract a user private key
 * from the identity, sign a message, and verify the signature.
 * Validation: correct signature must pass; empty message, null identity,
 * tampered message, wrong identity and wrong master public key must all be
 * rejected.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        SM9Curve curve = new SM9Curve();
        KGC kgc = new KGC(curve);
        SM9 sm9 = new SM9(curve);

        System.out.println("=== Normal Signature Flow ===");

        MasterKeyPair masterKeyPair = kgc.genSignMasterKeyPair();
        System.out.println("[1] Signing master key pair generated.");

        String userId = "Alice";
        PrivateKey userKey = kgc.genPrivateKey(
                masterKeyPair.getPrivateKey(), userId, PrivateKeyType.KEY_SIGN);
        System.out.println("[2] Private key extracted for user: " + userId);

        byte[] message = "Test message for SM9 signature verification".getBytes("UTF-8");
        ResultSignature signature = sm9.sign(masterKeyPair.getPublicKey(), userKey, message);
        System.out.println("[3] Message signed.");
        System.out.println("    Signature h (hex preview): "
                + SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(signature.h)).substring(0, 32) + "...");

        boolean ok = sm9.verify(masterKeyPair.getPublicKey(), userId, message, signature);
        System.out.println("[4] Verify with correct identity and message: " + (ok ? "PASS" : "FAIL"));

        System.out.println("\n=== Boundary and Exception Validation ===");

        try {
            sm9.sign(masterKeyPair.getPublicKey(), userKey, new byte[0]);
            System.out.println("[1] Empty message: UNEXPECTED SUCCESS");
        } catch (IllegalArgumentException e) {
            System.out.println("[1] Empty message: REJECTED by input validation (" + e.getMessage() + ")");
        }

        try {
            kgc.genPrivateKey(masterKeyPair.getPrivateKey(), null, PrivateKeyType.KEY_SIGN);
            System.out.println("[2] Null identity: UNEXPECTED SUCCESS");
        } catch (IllegalArgumentException e) {
            System.out.println("[2] Null identity: REJECTED by input validation (" + e.getMessage() + ")");
        }

        byte[] tampered = "Tampered content".getBytes("UTF-8");
        boolean tamperedRejected = !sm9.verify(masterKeyPair.getPublicKey(), userId, tampered, signature);
        System.out.println("[3] Tampered message: " + (tamperedRejected ? "CORRECTLY REJECTED" : "UNEXPECTED PASS"));

        boolean wrongIdRejected = !sm9.verify(masterKeyPair.getPublicKey(), "Bob", message, signature);
        System.out.println("[4] Wrong user identity: " + (wrongIdRejected ? "CORRECTLY REJECTED" : "UNEXPECTED PASS"));

        MasterKeyPair otherKeyPair = kgc.genSignMasterKeyPair();
        boolean wrongKeyRejected = !sm9.verify(otherKeyPair.getPublicKey(), userId, message, signature);
        System.out.println("[5] Wrong master public key: " + (wrongKeyRejected ? "CORRECTLY REJECTED" : "UNEXPECTED PASS"));

        System.out.println("\nDemo finished.");
    }

    /** Console output helper used by the standard-test-vector demo classes. */
    public static void showMsg(String msg) {
        System.out.println(msg);
    }
}
