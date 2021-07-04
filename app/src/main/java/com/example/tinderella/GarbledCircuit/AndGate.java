package com.example.tinderella.GarbledCircuit;

public class AndGate extends Gate {
	public AndGate() {
		super(new int[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 1, 0, 0 }, { 1, 1, 1 } });
	}
}
