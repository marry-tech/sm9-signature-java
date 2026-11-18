package com.sm9.batch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.sm9.KGC;
import com.sm9.MasterKeyPair;
import com.sm9.MasterPublicKey;
import com.sm9.PrivateKey;
import com.sm9.PrivateKeyType;
import com.sm9.ResultSignature;
import com.sm9.SM9;
import com.sm9.SM9Curve;

public class Sm9ParallelBenchmark {

    public static void main(String[] args) throws Exception {
        int[] threadCounts = {1, 2, 4, 8};
        int secondsPerConfig = 15;

        // Main thread: generate one key pair and one signature, then serialise
        SM9Curve setupCurve = new SM9Curve();
        KGC setupKgc = new KGC(setupCurve);
        MasterKeyPair mkp = setupKgc.genSignMasterKeyPair();
        SM9 setupSm9 = new SM9(setupCurve);

        final String id = "bench@user.com";
        final byte[] msg = "benchmark message for SM9 throughput test".getBytes();
        PrivateKey sk = setupKgc.genPrivateKey(mkp.getPrivateKey(), id, PrivateKeyType.KEY_SIGN);
        ResultSignature sig = setupSm9.sign(mkp.getPublicKey(), sk, msg);

        final byte[] mpkBytes = mkp.getPublicKey().toByteArray();
        final byte[] sigBytes = sig.toByteArray();

        System.out.println("threads,throughput(ops/sec)");
        for (int n : threadCounts) {
            ExecutorService pool = Executors.newFixedThreadPool(n);
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicLong completed = new AtomicLong(0);
            CountDownLatch latch = new CountDownLatch(n);

            long start = System.currentTimeMillis();
            for (int t = 0; t < n; t++) {
                pool.submit(() -> {
                    try {
                        // Critical: every thread builds its own SM9Curve/SM9 instance (JPBC is not thread-safe)
                        SM9Curve curve = new SM9Curve();
                        SM9 sm9 = new SM9(curve);
                        MasterPublicKey mpk = MasterPublicKey.fromByteArray(curve, mpkBytes);
                        ResultSignature s = ResultSignature.fromByteArray(curve, sigBytes);
                        while (running.get()) {
                            if (sm9.verify(mpk, id, msg, s)) {
                                completed.incrementAndGet();
                            }
                        }
                    } catch (Throwable e) {
                        System.err.println("thread error: " + e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            Thread.sleep(secondsPerConfig * 1000L);
            running.set(false);
            latch.await();
            long elapsed = System.currentTimeMillis() - start;
            System.out.println(n + "," + (completed.get() * 1000 / elapsed));
            pool.shutdownNow();
        }
        System.exit(0);
    }
}
