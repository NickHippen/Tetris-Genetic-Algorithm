package edu.unomaha.nhippen.tetris;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import edu.unomaha.nhippen.tetris.ai.TetrisGenes;

public class TetrisRunner {
	
	private static final Random RANDOM = new Random();
	
	private static int currentIndex = 0;
	private static int generation = 1;
	
	/**
	 * The size of the population for each generation.
	 */
//	private static final int POPULATION_SIZE = 60;
	private static final int POPULATION_SIZE = 25;
	/**
	 * The size of the elite population that will be straight copied over to the next generation. The most fit elite will be duplicated three times.
	 */
//	private static final int ELITE_SIZE = 6;
	private static final int ELITE_SIZE = 2;
	/**
	 * The chance that a gene will mutate on breeding.
	 */
	private static final float MUTATION_RATE = 0.03F;
	private static final int SIZE_X = 600;
	private static final int SIZE_Y = 441;
	
	public static final List<AITetris> TETRIS_GAMES = new ArrayList<>();
	public static final List<TetrisGenes> TETRIS_GENES = new ArrayList<>();
	public static final int[] SCORES = new int[POPULATION_SIZE];
	
	private static TetrisGenes overallBestGenes;

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		System.out.print("How often would you like a piece placed (in ms)?: ");
		int tickDelay = input.nextInt();
		input.nextLine();
		String answer;
		do {
			System.out.println("Would you like to limit the number of pieces to 1000 for each game run? (yes or no)");
			answer = input.nextLine();
		} while (!answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no"));
		boolean limitPieces = answer.equalsIgnoreCase("yes");
		input.close();
		for (int i = 0; i < POPULATION_SIZE; i++) {
			TetrisGenes genes = new TetrisGenes();
			TETRIS_GENES.add(genes);
		}
		Frame frame = new Frame("Tetris");
		AITetris tetris = new AITetris(0, TETRIS_GENES.get(0), tickDelay, limitPieces);
		TETRIS_GAMES.add(tetris);
		frame.add(tetris);
		tetris.init();
		tetris.start();

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		frame.setSize(SIZE_X, SIZE_Y);
		frame.setResizable(false);
		frame.setVisible(true);
		restartAll();
	}
	
	public static String stackTraceToString(StackTraceElement[] elements) {
	    StringBuilder sb = new StringBuilder();
	    for (StackTraceElement element : elements) {
	        sb.append(element.toString());
	        sb.append("\n");
	    }
	    return sb.toString();
	}
	
	public static synchronized void processCompletion() {
		if (generation == 1 || currentIndex >= ELITE_SIZE) {
			SCORES[currentIndex] = TETRIS_GAMES.get(0).getScore();
			currentIndex++;
			if (currentIndex >= POPULATION_SIZE) {
				applyGenetics();
				generation++;
				System.out.println("STARTING GENERATION " + generation);
				currentIndex = 0;
				return;
			}
			startAgent();
		} else {
			System.out.println("Skipping " + currentIndex + " due to elitism (already calcualted score)");
			currentIndex++;
			processCompletion();
		}
	}
	
	private static void startAgent() {
		TETRIS_GAMES.get(0).setGenes(TETRIS_GENES.get(currentIndex));
		TETRIS_GAMES.get(0).setId(currentIndex);
		System.out.println("STARTING NEW AGENT: ");
		System.out.println("GENERATION:  " + generation);
		System.out.println("ID:          " + currentIndex);
		TETRIS_GENES.get(currentIndex).printValues();
		restartAll();
	}
	
	public static synchronized void applyGenetics() {
		System.out.println("ALL COMPLETE!");
		int sumScore = 0;
		writeScores();
		int[] eliteIndexes = new int[ELITE_SIZE];
		int[] eliteScores = new int[ELITE_SIZE];
		for (int i = 0; i < ELITE_SIZE - 1; i++) {
			eliteIndexes[i] = -1;
			eliteScores[i] = -1;
		}
		for (int i = 0; i < SCORES.length; i++) {
			int score = SCORES[i];
			int replace = -1;
			for (int j = 0; j < ELITE_SIZE - 1; j++) {
				if (score > eliteScores[j]) {
					if (replace != -1 && eliteScores[j] > eliteScores[replace]) {
						continue;
					}
					replace = j;
					continue;
				}
			}
			if (replace != -1) {
				eliteIndexes[replace] = i;
				eliteScores[replace] = score;
			}
			sumScore += score;
		}
		int max = max(eliteScores);
		System.out.print("Elites are: ");
		int maxIndex = -1;
		for (int i = 0; i < ELITE_SIZE - 1; i++) {
			System.out.print(eliteIndexes[i] + ": " + eliteScores[i] + ", ");
			TETRIS_GENES.set(i, TETRIS_GENES.get(eliteIndexes[i]));
			SCORES[i] = eliteScores[i];
			if (eliteScores[i] == max) {
				maxIndex = i;
				// Double the best gene for next generation
				overallBestGenes = TETRIS_GENES.get(eliteIndexes[i]);
				TETRIS_GENES.set(ELITE_SIZE - 1, TETRIS_GENES.get(eliteIndexes[i]));
				SCORES[ELITE_SIZE - 1] = eliteScores[i];
			}
		}
		writeBestGenes();
		System.out.println();
		System.out.println("Best elite was: " + eliteIndexes[maxIndex] + ", Score: " + eliteScores[maxIndex]);
		for (int i = ELITE_SIZE; i < POPULATION_SIZE; i++) {
			TetrisGenes parent1 = getRandomWeightedParent(sumScore);
			TetrisGenes parent2;
			do {
				parent2 = getRandomWeightedParent(sumScore);
			} while (parent1.equals(parent2));
			TetrisGenes offspringGenes = breed(parent1, parent2);
			if (offspringGenes.isNewMutation()) {
				System.out.println("MUTATED " + i);
			}
			TETRIS_GENES.set(i, offspringGenes);
		}
		System.out.println("Starting next generation in 5 seconds.");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (ELITE_SIZE > 0) {
			System.out.println("Skipping first " + ELITE_SIZE + " due to elitism (already calculated score)");
			currentIndex = ELITE_SIZE;
			startAgent();
		}
		restartAll();
	}
	
	private static int max(int... vals) {
		int max = vals[0];
		for (int i = 1; i < vals.length; i++) {
			max = Math.max(vals[i], max);
		}
		return max;
	}
	
	private static synchronized TetrisGenes getRandomWeightedParent(int sumScore) {
		if (sumScore == 0) { // No games went above 0
			return TETRIS_GENES.get(RANDOM.nextInt(TETRIS_GENES.size()));
		}
		int roll = RANDOM.nextInt(sumScore);
		int pos = 0;
		for (int i = 0; i < POPULATION_SIZE; i++) {
			TetrisGenes genes = TETRIS_GENES.get(i);
			pos += SCORES[i];
			if (roll < pos) {
				return genes;
			}
		}
		throw new IllegalStateException("Unable to find random parent.");
	}
	
	public static synchronized TetrisGenes breed(TetrisGenes parent1, TetrisGenes parent2) {
		TetrisGenes offspring = new TetrisGenes();
		float diff = Math.abs(parent1.getHeightDiff() - parent2.getHeightDiff());
		offspring.setHeightDiff(Math.min(parent1.getHeightDiff(), parent2.getHeightDiff()) + (diff * RANDOM.nextFloat()));
		diff = Math.abs(parent1.getHoleCount() - parent2.getHoleCount());
		offspring.setHoleCount(Math.min(parent1.getHoleCount(), parent2.getHoleCount()) + (diff * RANDOM.nextFloat()));
		diff = Math.abs(parent1.getJaggedness() - parent2.getJaggedness());
		offspring.setJaggedness(Math.min(parent1.getJaggedness(), parent2.getJaggedness()) + (diff * RANDOM.nextFloat()));
		diff = Math.abs(parent1.getLinesCleared() - parent2.getLinesCleared());
		offspring.setLinesCleared(Math.min(parent1.getLinesCleared(), parent2.getLinesCleared()) + (diff * RANDOM.nextFloat()));
		diff = Math.abs(parent1.getMaxHeight() - parent2.getMaxHeight());
		offspring.setMaxHeight(Math.min(parent1.getMaxHeight(), parent2.getMaxHeight()) + (diff * RANDOM.nextFloat()));
		diff = Math.abs(parent1.getBlockades() - parent2.getBlockades());
		offspring.setBlockades(Math.min(parent1.getBlockades(), parent2.getBlockades()) + (diff * RANDOM.nextFloat()));
		
		if (RANDOM.nextFloat() < MUTATION_RATE) {
			offspring.setHeightDiff(TetrisGenes.generateRandomGeneWeight());
			offspring.setNewMutation(true);
		}
		if (RANDOM.nextFloat() < MUTATION_RATE) {
			offspring.setHoleCount(TetrisGenes.generateRandomGeneWeight());
			offspring.setNewMutation(true);
		}
		if (RANDOM.nextFloat() < MUTATION_RATE) {
			offspring.setJaggedness(TetrisGenes.generateRandomGeneWeight());
			offspring.setNewMutation(true);
		}
		if (RANDOM.nextFloat() < MUTATION_RATE) {
			offspring.setLinesCleared(TetrisGenes.generateRandomGeneWeight());
			offspring.setNewMutation(true);
		}
		if (RANDOM.nextFloat() < MUTATION_RATE) {
			offspring.setMaxHeight(TetrisGenes.generateRandomGeneWeight());
			offspring.setNewMutation(true);
		}
		if (RANDOM.nextFloat() < MUTATION_RATE) {
			offspring.setBlockades(TetrisGenes.generateRandomGeneWeight());
			offspring.setNewMutation(true);
		}
		return offspring;
	}
	
	public static synchronized void writeScores() {
		try {
			PrintWriter scoreWriter = new PrintWriter("scores-generation-" + generation + ".txt", "UTF-8");
			for (int i = 0; i < SCORES.length; i++) {
				int score = SCORES[i];
				boolean mutated = TETRIS_GENES.get(i).isNewMutation();
				scoreWriter.println(score + (mutated ? "*" : "") + (i < ELITE_SIZE ? "^" : ""));
			}
			scoreWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void writeBestGenes() {
		try {
			PrintWriter geneWriter = new PrintWriter("best-gene.txt", "UTF-8");
			geneWriter.println(overallBestGenes.toString());
			geneWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void restartAll() {
		for (AITetris tetris : TETRIS_GAMES) {
			tetris.newGame();
		}
	}
	
}
