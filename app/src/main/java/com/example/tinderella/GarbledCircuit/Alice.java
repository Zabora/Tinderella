package com.example.tinderella.GarbledCircuit;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;

public class Alice {
	private boolean choice;

	private Gate circle;

	private HashMap<String, String> labels;
	private String[] garbledCircuit;
	private String out0, out1;

	private KeyPair keyPair;
	private String x0, x1;

	private String v;
	private BigInteger r0, r1;
	private String encB0, encB1;

	public Alice(boolean choice) {
		try {
			this.choice = choice;
			circle = new AndGate();

			circle.letLabel();
			circle.letEncrypt();

			labels = circle.getLabels();
			garbledCircuit = circle.getGarbledCircuit();
			
			out0 = labels.get("out0");
			out1 = labels.get("out1");

			generateRSA();
			generateRandomMessages();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void generateRSA() {
		try {
			keyPair = Utils.generateKeyRSA(1024);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void generateRandomMessages() {
		try {
			x0 = new BigInteger(Utils.generateKey(Gate.AES_KEYLENGTH, "AES")).abs().toString();
			x1 = new BigInteger(Utils.generateKey(Gate.AES_KEYLENGTH, "AES")).abs().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getLabelByChoice() {
		return choice ? labels.get("a1") : labels.get("a0");
	}

	public String keyToString(String type) {
		if (type.equalsIgnoreCase("private")) {
			return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
		} else {
			return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
		}
	}

	public String[] getGarbledCircuit() {
		String[] arrayToShuffle = Arrays.copyOf(garbledCircuit, garbledCircuit.length);

		Random random = new Random();
		for (int n = 0; n < arrayToShuffle.length; n++) {
			for (int i = 1; i < arrayToShuffle.length; i++) {
				if (random.nextBoolean()) {
					String tmp = arrayToShuffle[i - 1];
					arrayToShuffle[i - 1] = arrayToShuffle[i];
					arrayToShuffle[i] = tmp;
				}
			}
		}

		return arrayToShuffle;
	}

	public String getX0() {
		return x0;
	}

	public String getX1() {
		return x1;
	}

	public void setV(String v) {
		this.v = v;

		computeR();
	}

	private void computeR() {
		RSAPrivateKey alicePrivateKey = (RSAPrivateKey) keyPair.getPrivate();

		BigInteger N = alicePrivateKey.getModulus();
		BigInteger d = alicePrivateKey.getPrivateExponent();

		BigInteger _x0 = new BigInteger(x0);
		BigInteger _x1 = new BigInteger(x1);
		BigInteger _v = new BigInteger(v);

		r0 = _v.subtract(_x0).modPow(d, N).mod(N);
		r1 = _v.subtract(_x1).modPow(d, N).mod(N);

		computeBobLabels();
	}

	private void computeBobLabels() {
		BigInteger b0 = new BigInteger(labels.get("b0"));
		BigInteger b1 = new BigInteger(labels.get("b1"));

		encB0 = b0.add(r0).toString();
		encB1 = b1.add(r1).toString();
	}

	public String getEncB0() {
		return encB0;
	}

	public String getEncB1() {
		return encB1;
	}

	public String getOut0() {
		return out0;
	}
	
	public String getOut1() {
		return out1;
	}
	
	public void printLabelTruthTable() {
		circle.printLabelTruthTable();
	}

	public boolean getMatchResult(String result) {
		return (out1.equals(result)) ? true : false;
	}
}
