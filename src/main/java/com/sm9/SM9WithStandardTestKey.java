package com.sm9;

import com.aa.Main;


import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;
import it.unisa.dia.gas.plaf.jpbc.util.Arrays;

/**
 * SM9 algorithm.
 *
 * Show SM9 standard test data in console.
 *验证数字签名算法
 *
 */
public class SM9WithStandardTestKey extends SM9 {
    public static BigInteger r = BigInteger.ONE;

    public SM9WithStandardTestKey(SM9Curve curve){
        super(curve);
    }

    @Override
    public boolean verify(MasterPublicKey masterPublicKey, String id, byte[] data, ResultSignature signature)
    {
        // Step1 : check if h in the range [1, N-1]
        if(!SM9Utils.isBetween(signature.h, mCurve.N))
            return false;

        // Step2 : check if S is on G1
        if(!signature.s.isValid())
            return false;

        // Step3 : g = e(P1, Ppub-s)
        Element g = mCurve.pairing(mCurve.P1, masterPublicKey.Q);
        Main.showMsg("群GT中的元素 g:");
        Main.showMsg(SM9Utils.toHexString(SM9Utils.GTFiniteElementToByte(g)));

        // Step4 : calculate t=g^h
        Element t = g.pow(signature.h);
        Main.showMsg("群GT中的元素 t:");
        Main.showMsg(SM9Utils.toHexString(SM9Utils.GTFiniteElementToByte(t)));

        // Step5 : calculate h1=H1(IDA||hid,N)
        BigInteger h1 = SM9Utils.H1(id, SM9Curve.HID_SIGN, mCurve.N);
        Main.showMsg("h1:");
        Main.showMsg(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(h1)));

        // Step6 : P=[h1]P2+Ppubs
        CurveElement p = mCurve.P2.duplicate().mul(h1).add(masterPublicKey.Q);
        Main.showMsg("群G2中的元素 p:");
        Main.showMsg(SM9Utils.toHexString(SM9Utils.G2ElementToByte(p)));

        // Step7 : u=e(S,P)
        Element u = mCurve.pairing(signature.s, p);
        Main.showMsg("群GT中的元素 u:");
        Main.showMsg(SM9Utils.toHexString(SM9Utils.GTFiniteElementToByte(u)));

        // Step8 : w=u*t
        Element w2 = u.mul(t);
        Main.showMsg("群GT中的元素 w':");
        Main.showMsg(SM9Utils.toHexString(SM9Utils.GTFiniteElementToByte(w2)));

        // Step9 : h2=H2(M||w,N)
        BigInteger h2 = SM9Utils.H2(data, w2, mCurve.N);
        Main.showMsg("h2:");
        Main.showMsg(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(h2)));
        if(h2.equals(signature.h))
            Main.showMsg("h2 = h, 验证通过! \n");

        return h2.equals(signature.h);
    }


}
