package com.sm9.batch;

import com.sm9.KGC;
import com.sm9.MasterKeyPair;
import com.sm9.MasterPublicKey;
import com.sm9.PrivateKey;
import com.sm9.PrivateKeyType;
import com.sm9.ResultSignature;
import com.sm9.SM9;
import com.sm9.SM9Curve;

public class Sm9Benchmark {

    public static void main(String[] args) throws Exception {
        System.out.println("[1/5] Initializing SM9 curve (JPBC init can take 1-3 min)...");
        long t0 = System.currentTimeMillis();
        SM9Curve curve = new SM9Curve();
        KGC kgc = new KGC(curve);
        MasterKeyPair mkp = kgc.genSignMasterKeyPair();
        SM9 sm9 = new SM9(curve);
        System.out.println("      curve ready in " + (System.currentTimeMillis() - t0) / 1000 + " s");

        String id = "bench@user.com";
        byte[] msg = "benchmark message".getBytes();
        System.out.println("[2/5] Generating key and signature...");
        PrivateKey sk = kgc.genPrivateKey(mkp.getPrivateKey(), id, PrivateKeyType.KEY_SIGN);
        MasterPublicKey mpk = mkp.getPublicKey();
        ResultSignature sig = sm9.sign(mpk, sk, msg);
        if (!sm9.verify(mpk, id, msg, sig)) {
            System.out.println("ERROR: verify failed, abort.");
            return;
        }
        System.out.println("      sanity check passed.");

        int warmup = 30;
        int measure = 100;

        System.out.println("[3/5] Measuring SIGN (" + (warmup + measure) + " ops)...");
        for (int i = 0; i < warmup; i++) sm9.sign(mpk, sk, msg);
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) sm9.sign(mpk, sk, msg);
        double signMs = (System.nanoTime() - start) / 1e6 / measure;
        System.out.printf("      sign   : %.2f ms/op  (%.1f ops/sec)%n", signMs, 1000.0 / signMs);

        System.out.println("[4/5] Measuring VERIFY (" + (warmup + measure) + " ops)...");
        for (int i = 0; i < warmup; i++) sm9.verify(mpk, id, msg, sig);
        start = System.nanoTime();
        for (int i = 0; i < measure; i++) sm9.verify(mpk, id, msg, sig);
        double verifyMs = (System.nanoTime() - start) / 1e6 / measure;
        System.out.printf("      verify : %.2f ms/op  (%.1f ops/sec)%n", verifyMs, 1000.0 / verifyMs);

        System.out.println("[5/5] Done.");
        System.exit(0);
    }
}
