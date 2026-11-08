package com.aa;

import com.sm9.KGC;
import com.sm9.SM9;
import com.sm9.SM9Curve;
import com.sm9.MasterKeyPair;
import com.sm9.PrivateKey;
import com.sm9.ResultSignature;

public class Main {
    public static void main(String[] args) {
        SM9Curve curve = new SM9Curve();
        KGC kgc = new KGC(curve);
        SM9 sm9 = new SM9(curve);

        // ========== Part 1: Normal Signature & Verification Flow ==========
        System.out.println("=== Normal Signature Flow ===");

        MasterKeyPair masterKeyPair = kgc.genSignMasterKeyPair();
        System.out.println("[1] Master key pair generated.");

        String userId = "Alice";
        PrivateKey userKey = kgc.genSignPrivateKey(masterKeyPair.getMasterPrivateKey(), userId);
        System.out.println("[2] Private key extracted for user: " + userId);

        String message = "Test message for SM9 signature verification";
        ResultSignature signature = sm9.sign(userKey, message);
        System.out.println("[3] Message signed successfully.");
        System.out.printf("    Signature h (hex preview): %s...%n",
                signature.getH().toString(16).toUpperCase().substring(0, 32));

        boolean normalPass = sm9.verify(masterKeyPair.getMasterPublicKey(), userId, message, signature);
        System.out.println("[4] Verify with correct user & message: " + (normalPass ? "PASS" : "FAIL"));

        // ========== Part 2: Boundary & Exception Validation ==========
        System.out.println("\n=== Boundary & Exception Validation ===");

        // 1. Sign and verify with empty message
        ResultSignature emptySig = sm9.sign(userKey, "");
        boolean emptyPass = sm9.verify(masterKeyPair.getMasterPublicKey(), userId, "", emptySig);
        System.out.println("[1] Empty message sign & verify: " + (emptyPass ? "PASS" : "FAIL"));

        // 2. Sign and verify with Unicode user identifier
        String unicodeId = "user_test_001_Unicode";
        PrivateKey unicodeKey = kgc.genSignPrivateKey(masterKeyPair.getMasterPrivateKey(), unicodeId);
        ResultSignature unicodeSig = sm9.sign(unicodeKey, message);
        boolean unicodePass = sm9.verify(masterKeyPair.getMasterPublicKey(), unicodeId, message, unicodeSig);
        System.out.println("[2] Unicode user ID verify: " + (unicodePass ? "PASS" : "FAIL"));

        // 3. Tampered message should be rejected
        boolean tamperedReject = !sm9.verify(masterKeyPair.getMasterPublicKey(), userId, "Tampered content", signature);
        System.out.println("[3] Tampered message: " + (tamperedReject ? "CORRECTLY REJECTED" : "UNEXPECTED PASS"));

        // 4. Wrong user identity should be rejected
        boolean wrongUserReject = !sm9.verify(masterKeyPair.getMasterPublicKey(), "Bob", message, signature);
        System.out.println("[4] Wrong user ID: " + (wrongUserReject ? "CORRECTLY REJECTED" : "UNEXPECTED PASS"));

        // 5. Wrong master public key should be rejected
        MasterKeyPair otherKeyPair = kgc.genSignMasterKeyPair();
        boolean wrongKeyReject = !sm9.verify(otherKeyPair.getMasterPublicKey(), userId, message, signature);
        System.out.println("[5] Wrong master public key: " + (wrongKeyReject ? "CORRECTLY REJECTED" : "UNEXPECTED PASS"));

        System.out.println("\nDemo finished.");
    }
}
