package com.example.tinderella.GarbledCircuit;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

	public static byte[] generateKey(int size, String type) throws Exception {
		if (type.equals("AES")) {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(size);
			SecretKey secretKey = keyGenerator.generateKey();

			return secretKey.getEncoded();
		} else {
			throw new Exception("Wrong type");
		}
	}

	public static byte[] encrypt(byte[] plain, byte[] key, String type) throws Exception {
		if (type.equals("AES")) {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

			return cipher.doFinal(plain);
		} else {
			throw new Exception("Wrong type");
		}
	}

	public static byte[] decrypt(byte[] encrypted, byte[] key, String type) throws Exception {
		if (type.equals("AES")) {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

			return cipher.doFinal(encrypted);
		} else {
			throw new Exception("Wrong type");
		}
	}

	public static KeyPair generateKeyRSA(int size) throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(size);
		KeyPair key = keyPairGenerator.generateKeyPair();

		return key;
	}

	public static PublicKey stringToPublicKey(String publicKeyString) throws Exception {
		byte[] publicBytes = Base64.getDecoder().decode(publicKeyString);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);

		return publicKey;
	}
}
