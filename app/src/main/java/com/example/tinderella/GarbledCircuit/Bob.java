package com.example.tinderella.GarbledCircuit;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

public class Bob {
	private boolean choice;

	private Gate circle;
	private String a;
	private String x0, x1;
	private String out0, out1;
	private PublicKey publicKey;

	private String r;
	private String v;

	private String b;

	public Bob(boolean choice) {
		this.choice = choice;
	}

	private void generateR() {
		try {
			r = new BigInteger(Utils.generateKey(Gate.AES_KEYLENGTH, "AES")).abs().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setInfoFromAlice(String[] garbledCircuit, String a, String out0, String out1, String x0, String x1, String publicKey) {
		try {
			this.circle = new Gate(garbledCircuit);
			this.a = a;
			this.out0 = out0;
			this.out1 = out1;
			this.x0 = x0;
			this.x1 = x1;

			this.publicKey = Utils.stringToPublicKey(publicKey);

			generateR();
			computeV();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void computeV() {
		try {
			String xb = (choice) ? x1 : x0;

			RSAPublicKey alicePublicKey = (RSAPublicKey) publicKey;

			BigInteger N = alicePublicKey.getModulus();
			BigInteger e = alicePublicKey.getPublicExponent();

			BigInteger _xb = new BigInteger(xb);
			BigInteger _r = new BigInteger(r);

			v = _xb.add(_r.modPow(e, N)).mod(N).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getV() {
		return v;
	}

	public void receiveEncryptedLabels(String encB0, String encB1) {
		BigInteger _r = new BigInteger(r);
		BigInteger _encB0 = new BigInteger(encB0);
		BigInteger _encB1 = new BigInteger(encB1);

		if (choice) {
			b = _encB1.subtract(_r).toString();
		} else {
			b = _encB0.subtract(_r).toString();
		}

	}
	
	public String getResult() {
		return circle.evaluate(a, b, out0, out1);
	}
	
	public boolean getMatchResult() {
		return circle.evaluate(a, b, out0, out1).equals(out1) ? true : false ;
	}
}
