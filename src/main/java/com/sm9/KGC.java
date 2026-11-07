package com.sm9;

import java.math.BigInteger;

import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;

/**
 * SM9 Key Generator Center.
 */
public class KGC {
    protected SM9Curve mCurve;
    
    public KGC(SM9Curve curve) {
        if (curve == null) {
            throw new IllegalArgumentException("SM9Curve cannot be null");
        }
        mCurve = curve;
    }

    public SM9Curve getCurve() {
        return mCurve;
    }

    /**
     * Generate SM9 sign master key pair.
     *
     * @return A master key pair containing master public key and master private key.
     */
    public MasterKeyPair genSignMasterKeyPair()
    {
        BigInteger ks = SM9Utils.genRandom(mCurve.random, mCurve.N);
        CurveElement ppubs = mCurve.P2.duplicate().mul(ks);
        return new MasterKeyPair(new MasterPrivateKey(ks), new MasterPublicKey(ppubs, true));
    }

   
    protected BigInteger T2(MasterPrivateKey privateKey, String id, byte hid) throws Exception {
        BigInteger h1 = SM9Utils.H1(id, hid, mCurve.N);
        BigInteger t1 = h1.add(privateKey.d).mod(mCurve.N);
        if(t1.equals(BigInteger.ZERO))
            throw new Exception("Need to update the master private key");

        return privateKey.d.multiply(t1.modInverse(mCurve.N)).mod(mCurve.N);
    }

    /**
     * Generate SM9 private key.
     *
     * @param masterPrivateKey master private key
     * @param id user ID
     * @param privateKeyType master private key type
     * @return A master private key
     * @throws Exception If error occurs.
     */
    public PrivateKey genPrivateKey(MasterPrivateKey masterPrivateKey, String id, PrivateKeyType privateKeyType) throws Exception {
        if (masterPrivateKey == null) {
            throw new IllegalArgumentException("Master private key cannot be null");
        }
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (privateKeyType == null) {
            throw new IllegalArgumentException("Private key type cannot be null");
        }
        
        if(privateKeyType==PrivateKeyType.KEY_SIGN)
            return genSignPrivateKey(masterPrivateKey, id);
        else
            throw new Exception("Not support private key type");
    }

    PrivateKey genSignPrivateKey(MasterPrivateKey privateKey, String id) throws Exception {
        BigInteger t2 = T2(privateKey, id, SM9Curve.HID_SIGN);
        CurveElement ds = mCurve.P1.duplicate().mul(t2);
        return new PrivateKey(ds, SM9Curve.HID_SIGN);
    }

   
}
