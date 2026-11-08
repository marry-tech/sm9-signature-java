package cmo.test;
import java.math.BigInteger;

import com.aa.Main;
import com.sm9.KGC;
import com.sm9.MasterKeyPair;
import com.sm9.PrivateKey;
import com.sm9.PrivateKeyType;
import com.sm9.ResultSignature;
import com.sm9.SM9;
import com.sm9.KGCWithStandardTestKey;
import com.sm9.MasterPublicKey;
import com.sm9.SM9Utils;
import com.sm9.SM9WithStandardTestKey;
import com.sm9.SM9Curve;

/**
 * SM9 算法测试.
 *
 * @param testType 1-SM9国密标准数据测试，随机数已指定; 其他值-普通测试，随机生成密钥;
 * @param allowReconstructData 在 testType 为1时，顺便进行SM9算法中的密钥和结果值的重构测试
 */


public class SM9Test {
	  public static void test_sm9_sign(KGC kgc, SM9 sm9) throws Exception {
	        Main.showMsg("\n");
	        Main.showMsg("SM9签名测试\n");

	        String id_A = "Alice";

	        //生成签名主密钥对
	        MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
	        Main.showMsg("签名主私钥 ks:");
	        Main.showMsg(signMasterKeyPair.getPrivateKey().toString());
	        Main.showMsg("签名主公钥 Ppub-s:");
	        Main.showMsg(signMasterKeyPair.getPublicKey().toString());

	        //显示ID信息
	        Main.showMsg("实体A的标识IDA:");
	        Main.showMsg(id_A);

	        //生成签名私钥
	        PrivateKey signPrivateKey = kgc.genPrivateKey(signMasterKeyPair.getPrivateKey(), id_A, PrivateKeyType.KEY_SIGN);
	        Main.showMsg("签名私钥 ds_A:");
	        Main.showMsg(signPrivateKey.toString());


	        //签名
	        Main.showMsg("签名步骤中的相关值:");
	        String msg = "Chinese IBS standard";
	        Main.showMsg("待签名消息 M:");
	        Main.showMsg(msg);

	        ResultSignature signature = sm9.sign(signMasterKeyPair.getPublicKey(), signPrivateKey, msg.getBytes());
	        Main.showMsg("消息M的签名为(h,s):");
	        Main.showMsg(signature.toString());

	        //验签
	        if(sm9.verify(signMasterKeyPair.getPublicKey(), id_A, msg.getBytes(), signature))
	            Main.showMsg("verify OK");
	        else
	            Main.showMsg("verify failed");
	    }
	  public static void test_sm9_sign_standard(KGC kgc, SM9 sm9) throws Exception {
	        Main.showMsg("\n");
	        Main.showMsg("SM9签名测试\n");

	        String id_A = "Alice";

	        Main.showMsg("签名主密钥和用户签名私钥产生过程中的相关值:");

	        //生成签名主密钥对
	        KGCWithStandardTestKey.k= new BigInteger("0130E78459D78545CB54C587E02CF480CE0B66340F319F348A1D5B1F2DC5F4", 16);
	        MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
	        Main.showMsg("签名主私钥 ks:");
	        Main.showMsg(signMasterKeyPair.getPrivateKey().toString());
	        Main.showMsg("签名主公钥 Ppub-s:");
	        Main.showMsg(signMasterKeyPair.getPublicKey().toString());

	        //显示ID信息
	        Main.showMsg("实体A的标识IDA:");
	        Main.showMsg(id_A);
	        Main.showMsg("IDA的16进制表示");
	        Main.showMsg(SM9Utils.toHexString(id_A.getBytes()));

	        //生成签名私钥
	        PrivateKey signPrivateKey = kgc.genPrivateKey(signMasterKeyPair.getPrivateKey(), id_A, PrivateKeyType.KEY_SIGN);
	        Main.showMsg("签名私钥 ds_A:");
	        Main.showMsg(signPrivateKey.toString());


	        //签名
	        Main.showMsg("签名步骤中的相关值:");
	        String msg = "Chinese IBS standard";
	        Main.showMsg("待签名消息 M:");
	        Main.showMsg(msg);
	        Main.showMsg("M的16进制表示");
	        Main.showMsg(SM9Utils.toHexString(msg.getBytes()));
	        SM9WithStandardTestKey.r = new BigInteger("033C8616B06704813203DFD00965022ED15975C662337AED648835DC4B1CBE", 16);
	        ResultSignature signature = sm9.sign(signMasterKeyPair.getPublicKey(), signPrivateKey, msg.getBytes());
	        Main.showMsg("消息M的签名为(h,s):");
	        Main.showMsg(signature.toString());


	        //验签
	        Main.showMsg("验证步骤中的相关值:");
	        if(sm9.verify(signMasterKeyPair.getPublicKey(), id_A, msg.getBytes(), signature))
	            Main.showMsg("verify OK");
	        else
	            Main.showMsg("verify failed");
	    }

	    public static void test_sm9_sign_re(KGC kgc, SM9 sm9) throws Exception {
	        Main.showMsg("\n");
	        Main.showMsg("SM9签名测试\n");

	        String id_A = "Alice";

	        Main.showMsg("签名主密钥和用户签名私钥产生过程中的相关值:");

	        //生成签名主密钥对
	        KGCWithStandardTestKey.k= new BigInteger("0130E78459D78545CB54C587E02CF480CE0B66340F319F348A1D5B1F2DC5F4", 16);
	        MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
	        Main.showMsg("签名主私钥 ks:");
	        Main.showMsg(signMasterKeyPair.getPrivateKey().toString());
	        Main.showMsg("签名主公钥 Ppub-s:");
	        Main.showMsg(signMasterKeyPair.getPublicKey().toString());
	        Main.showMsg("实体A的标识IDA:");
	        Main.showMsg(id_A);
	        Main.showMsg("IDA的16进制表示");
	        Main.showMsg(SM9Utils.toHexString(id_A.getBytes()));


	        //重构签名主密钥对
	        Main.showMsg("主密钥对重构测试:");
	        MasterKeyPair signMasterKeyPair0 = MasterKeyPair.fromByteArray(kgc.getCurve(), signMasterKeyPair.toByteArray());
	        Main.showMsg("重构后的主密钥:");
	        Main.showMsg(signMasterKeyPair0.toString());


	        //生成签名私钥
	        Main.showMsg("使用重构后的签名主密钥对的私钥生成签名私钥:");
	        PrivateKey signPrivateKey = kgc.genPrivateKey(signMasterKeyPair0.getPrivateKey(), id_A, PrivateKeyType.KEY_SIGN);
	        Main.showMsg("签名私钥 ds_A:");
	        Main.showMsg(signPrivateKey.toString());


	        //重构签名主公钥
	        Main.showMsg("签名主公钥重构测试:");
	        MasterPublicKey masterPublicKey = MasterPublicKey.fromByteArray(sm9.getCurve(), signMasterKeyPair.getPublicKey().toByteArray());
	        Main.showMsg("重构后的签名主公钥:");
	        Main.showMsg(masterPublicKey.toString());


	        //重构签名私钥
	        Main.showMsg("签名私钥重构测试:");
	        PrivateKey signPrivateKey0 = PrivateKey.fromByteArray(kgc.getCurve(), signPrivateKey.toByteArray());
	        Main.showMsg("重构后的签名私钥:");
	        Main.showMsg(signPrivateKey0.toString());


	        //签名
	        Main.showMsg("使用重构后的签名主公钥和重构后的签名私钥进行签名:");
	        Main.showMsg("签名步骤中的相关值:");
	        String msg = "Chinese IBS standard";
	        Main.showMsg("待签名消息 M:");
	        Main.showMsg(msg);
	        Main.showMsg("M的16进制表示");
	        Main.showMsg(SM9Utils.toHexString(msg.getBytes()));
	        SM9WithStandardTestKey.r = new BigInteger("033C8616B06704813203DFD00965022ED15975C662337AED648835DC4B1CBE", 16);
	        ResultSignature signature = sm9.sign(masterPublicKey, signPrivateKey0, msg.getBytes());
	        Main.showMsg("消息M的签名为(h,s):");
	        Main.showMsg(signature.toString());


	        //重构签名结果值
	        Main.showMsg("签名结果值重构测试:");
	        ResultSignature signature1 = ResultSignature.fromByteArray(sm9.getCurve(), signature.toByteArray());
	        Main.showMsg("重构后的签名值:");
	        Main.showMsg(signature1.toString());


	        //验签
	        Main.showMsg("使用重构后的签名主公钥和重构后的签名值进行验签:");
	        Main.showMsg("验证步骤中的相关值:");
	        if(sm9.verify(masterPublicKey, id_A, msg.getBytes(), signature1))
	            Main.showMsg("verify OK");
	        else
	            Main.showMsg("verify failed");
	    }
		public static void test(int testType, boolean allowReconstructData) {
			// TODO Auto-generated method stub
			
		
		
		
		    SM9Curve sm9Curve = new SM9Curve();
		    Main.showMsg(sm9Curve.toString());

		    KGC kgc = new KGC(sm9Curve);
		    SM9 sm9 = new SM9(sm9Curve);

		    try {
		         if(testType==1) {
		            kgc = new KGCWithStandardTestKey(sm9Curve);
		            sm9 = new SM9WithStandardTestKey(sm9Curve);

		            if(allowReconstructData) {
		                test_sm9_sign_re(kgc, sm9);
		               
		            } else {
		                test_sm9_sign_standard(kgc, sm9);
		                
		            }
		        } else {
		             test_sm9_sign(kgc, sm9);
		            
		         }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}

}


