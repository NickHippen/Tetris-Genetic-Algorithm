package edu.unomaha.nhippen.tetris.ai;

import java.util.Random;

public class TetrisGeneWeights {
	
	private static final Random RANDOM = new Random();

	private float holeCount;
	private float jaggedness;
	private float maxHeight;
	private float heightDiff;
	private float linesCleared;

	public TetrisGeneWeights() {
		holeCount = generateRandomGeneWeight();
		jaggedness = generateRandomGeneWeight();
		maxHeight = generateRandomGeneWeight();
		heightDiff = generateRandomGeneWeight();
		linesCleared = generateRandomGeneWeight();
	}

	private static float generateRandomGeneWeight() {
		return RANDOM.nextFloat() * 2 - 1;
	}

}
