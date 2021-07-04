package com.example.tinderella.GarbledCircuit;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;

public class Gate {
	public static final int AES_KEYLENGTH = 128;
	private String[][] labelTruthTable;
	private int[][] truthTable;
	private HashMap<String, String> labels;
	private String[] garbledCircuit;

	public Gate(int[][] truthTable) {
		super();
		this.truthTable = truthTable;

		this.labelTruthTable = null;
		this.labels = null;

		this.garbledCircuit = null;
	}

	public Gate(String[] garbledCircuit) {
		super();
		this.truthTable = null;

		this.labelTruthTable = null;
		this.labels = null;

		this.garbledCircuit = garbledCircuit;
	}

	public void letLabel() throws Exception {
		HashMap<String, String> labels = new HashMap<String, String>();

		byte[] a0 = Utils.generateKey(AES_KEYLENGTH, "AES");
		byte[] a1 = Utils.generateKey(AES_KEYLENGTH, "AES");
		labels.put("a0", new BigInteger(a0).abs().toString());
		labels.put("a1", new BigInteger(a1).abs().toString());

		byte[] b0 = Utils.generateKey(AES_KEYLENGTH, "AES");
		byte[] b1 = Utils.generateKey(AES_KEYLENGTH, "AES");
		labels.put("b0", new BigInteger(b0).abs().toString());
		labels.put("b1", new BigInteger(b1).abs().toString());

		byte[] out0 = Utils.generateKey(AES_KEYLENGTH, "AES");
		byte[] out1 = Utils.generateKey(AES_KEYLENGTH, "AES");
		labels.put("out0", new BigInteger(out0).abs().toString());
		labels.put("out1", new BigInteger(out1).abs().toString());

		labelTruthTable = new String[4][3];

		// Alice labels
		for (int i = 0; i < truthTable.length; i++) {
			if (truthTable[i][0] == 0) {
				labelTruthTable[i][0] = labels.get("a0");
			} else {
				labelTruthTable[i][0] = labels.get("a1");
			}
		}

		// Bob labels
		for (int i = 0; i < truthTable.length; i++) {
			if (truthTable[i][1] == 0) {
				labelTruthTable[i][1] = labels.get("b0");
			} else {
				labelTruthTable[i][1] = labels.get("b1");
			}
		}

		// output labels
		for (int i = 0; i < truthTable.length; i++) {
			if (truthTable[i][2] == 0) {
				labelTruthTable[i][2] = labels.get("out0");
			} else {
				labelTruthTable[i][2] = labels.get("out1");
			}
		}

		this.labels = labels;
	}

	public HashMap<String, String> getLabels() {
		return labels;
	}

	public void letEncrypt() throws Exception {
		String[] garbledCircuit = new String[4];
		for (int i = 0; i < labelTruthTable.length; i++) {
			byte[] a = new BigInteger(labelTruthTable[i][0]).toByteArray();
			byte[] b = new BigInteger(labelTruthTable[i][1]).toByteArray();
			byte[] out = new BigInteger(labelTruthTable[i][2]).toByteArray();

			byte[] x = Utils.encrypt(Utils.encrypt(out, b, "AES"), a, "AES");
			garbledCircuit[i] = new BigInteger(x).toString();
		}

		this.garbledCircuit = garbledCircuit;
	}

	public String[] getGarbledCircuit() {
		return Arrays.copyOf(garbledCircuit, garbledCircuit.length);
	}

	public String evaluate(String a, String b, String out1, String out2) {
		byte[] _a = new BigInteger(a).toByteArray();
		byte[] _b = new BigInteger(b).toByteArray();

		for (int i = 0; i < garbledCircuit.length; i++) {
			byte[] encryptedOut = new BigInteger(garbledCircuit[i]).toByteArray();
			try {
				byte[] out = Utils.decrypt(Utils.decrypt(encryptedOut, _a, "AES"), _b, "AES");
				String _out = new BigInteger(out).toString();
				
				if (_out.equals(out1) || _out.equals(out2)) {
					return _out;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	public void printLabelTruthTable() {
		for (int i = 0; i < labelTruthTable.length; i++) {
			for (int j = 0; j < labelTruthTable[i].length; j++) {
				System.out.print(labelTruthTable[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}
}
