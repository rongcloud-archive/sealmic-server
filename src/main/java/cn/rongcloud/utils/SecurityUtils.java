package cn.rongcloud.utils;

import org.apache.commons.lang.RandomStringUtils;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.*;

public class SecurityUtils {
	public static enum HmacAlgorithm {
		HMAC_MD5("HmacMD5"), HMAC_SHA1("HmacSHA1"), HMAC_SHA256("HmacSHA256"), HMAC_SHA384("HmacSHA384"), HMAC_SHA512(
				"HmacSHA512");

		private String algorithm;

		private HmacAlgorithm(String algorithm) {
			this.algorithm = algorithm;
		}

		public String getAlgorithm() {
			return algorithm;
		}
	}
	//AES加密方法
	public static final String AES_ECB_PKCS7 = "AES/ECB/PKCS7Padding";
    public static final String AES_ECB_PKCS5 = "AES/ECB/PKCS5Padding";
    public static final String AES_CBC_PKCS7 = "AES/CBC/PKCS7Padding";
    public static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";

	public static byte[] encryptHMAC(String secret, String data, HmacAlgorithm algorithm) throws IOException {
		return encryptHMAC(secret.getBytes("utf-8"), data.getBytes("utf-8"), algorithm);
	}

	public static byte[] encryptHMAC(byte[] secret, byte[] data, HmacAlgorithm algorithm) throws IOException {
		byte[] bytes = null;
		try {
			SecretKey secretKey = new SecretKeySpec(secret, algorithm.getAlgorithm());
			Mac mac = Mac.getInstance(secretKey.getAlgorithm());
			mac.init(secretKey);
			bytes = mac.doFinal(data);
		} catch (GeneralSecurityException gse) {
			throw new IOException(gse.toString());
		}
		return bytes;
	}

	public static String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (byte aByte : bytes) {
			String hex = Integer.toHexString(aByte & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex);
		}
		return sign.toString();
	}

	public static String generateSalt(int length) {
		return RandomStringUtils.random(length);
	}

	public static byte[] encryptWithAES128ECB(byte[] in, String key) throws UnsupportedEncodingException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		if (key == null || key.length() != 16) {
			throw new InvalidParameterException("key is invalid");
		}

		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("utf-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		return cipher.doFinal(in);
	}
}
