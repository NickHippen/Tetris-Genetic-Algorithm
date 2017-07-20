package edu.unomaha.nhippen.tetris;

import java.util.ArrayList;
import java.util.List;

import edu.unomaha.nhippen.tetris.AbstractTetris.TetrisPiece;
import edu.unomaha.nhippen.tetris.ai.TetrisGenes;

/**
 * Contains methods that converts the tetris board from having (0, 0) at the top left to having (0, 0) at the bottom left of the board to make coordinates easier to understand.
 * It also flips the (y, x) style that the array has to (x, y)
 * Example:
 * (16, 0), that is y=16, x=0, will translate to the reader-friendly (0, 1)
 */
public class TetrisBoardReader {

	private int[][] board;
	
	public TetrisBoardReader(int[][] board) {
		this.board = board;
	}
	
	public Tile getTile(int x, int y) {
		return new Tile(board[17 - y][x]);
	}
	
	public int getXLength() {
		return board[0].length;
	}
	
	public int getYLength() {
		return board.length;
	}
	
	public int countHoles() {
		int count = 0;
		for (int x = 0; x < getXLength(); x++) {
			for (int y = 0; y < getYLength(); y++) {
				if (isHole(x, y)) {
					count++;
				}
			}
		}
		return count;
	}
	
	public boolean isHole(int x, int y) {
		if (getTile(x, y).isX()) {
			// Already filled in
			return false;
		}
		for (int h = y + 1; h < getYLength(); h++) {
			if (getTile(x, h).isX()) {
				return true;
			}
		}
		return false;
	}

	public int getTallestYAtX(int x) {
		for (int y = getYLength() - 1; y >= 0; y--) {
			if (getTile(x, y).isX()) {
				return y;
			}
		}
		return -1;
	}

	public int getJaggedness() {
		int jaggednessVal = 0;
		int prevHeight = getTallestYAtX(0);
		for (int x = 1; x < getXLength(); x++) {
			int height = getTallestYAtX(x);
			int diff = Math.abs(height - prevHeight);
			jaggednessVal += diff;
			prevHeight = height;
		}
		return jaggednessVal;
	}

	public int getTallestHeight() {
		int highestY = -1;
		for (int x = 0; x < getXLength(); x++) {
			int height = getTallestYAtX(x);
			if (height > highestY) {
				highestY = height;
			}
		}
		return highestY + 1;
	}

	public int getAbsoluteHeightDifference() {
		int lowest = Integer.MAX_VALUE;
		int highest = -1;
		for (int x = 0; x < getXLength(); x++) {
			int height = getTallestYAtX(x);
			if (height > highest) {
				highest = height;
			}
			if (height < lowest) {
				lowest = height;
			}
		}
		return highest - lowest;
	}
	
	private boolean rowIsFull(int row) {
		for(int i=0; i<AbstractTetris.COLUMNS; i++)
			if(board[row][i] == AbstractTetris.EMPTY)
				return false;
		return true;
	}
	
	public int countFullRows() {
		int n_full_rows = 0;
		for(int i=0; i<AbstractTetris.ROWS; i++)
			if(rowIsFull(i))
				n_full_rows++;
		return n_full_rows;
	}
	
	public int countBlockades() {
		int count = 0;
		for (int x = 0; x < getXLength(); x++) {
			boolean markBlockade = false;
			for (int y = 0; y < getYLength(); y++) {
				if (isHole(x, y)) {
					markBlockade = true;
				}
				if (markBlockade && getTile(x, y).isX()) {
					count++;
				}
			}
		}
		return count;
	}
	
	public double calculateHeuristicValue(TetrisGenes genes) {
		double value = 0;
		value += getAbsoluteHeightDifference() * genes.getHeightDiff();
		value += getJaggedness() * genes.getJaggedness();
		value += getTallestHeight() * genes.getMaxHeight();
		value += countHoles() * genes.getHoleCount();
		value += countFullRows() * genes.getLinesCleared();
		value += countBlockades() * genes.getBlockades();
		return value;
	}
	
	public static synchronized List<TetrisPiece> expandPossiblePiecePositions(AbstractTetris game, TetrisPiece piece) {
//		AITetris gameCopy = new AITetris(-1, null);
//		System.arraycopy(game.grid, 0, gameCopy.grid, 0, game.grid.length);
		piece = game.new TetrisPiece(piece);
		TetrisPiece nextPiece = game.new TetrisPiece(game.next_piece);
		List<TetrisPiece> expansion = new ArrayList<>();
		for (int r = 0; r < 4; r++) {
			// Check each rotation
			piece.rotate();
			for (int x = -1; x <= 8; x++) {
				// Check each x coord
				int y = -4;
				piece.setPosition(x, y);
				while (piece.canStepDown()) {
					// Check each y coord
					piece.cut();
					piece.stepDown();
					piece.paste();
				}
				if (!piece.isTotallyOnGrid()) {
					continue;
				}
//				game.game_grid.repaint();
//				quickSleep(200);
				TetrisPiece clone = game.new TetrisPiece(piece);
				expansion.add(clone);
				piece.cut();
			}
		}
		return expansion;
	}
	
	public static synchronized void expandPossiblePiecePositions2(AbstractTetris game, TetrisPiece piece, 
			List<TetrisPiece> firstPiece, List<TetrisPiece> secondPiece) {
//		AITetris gameCopy = new AITetris(-1, null);
//		System.arraycopy(game.grid, 0, gameCopy.grid, 0, game.grid.length);
		piece = game.new TetrisPiece(piece);
		TetrisPiece nextPiece = game.new TetrisPiece(game.next_piece);
		for (int r = 0; r < 4; r++) {
			// Check each rotation
			piece.rotate();
			for (int x = -1; x <= 8; x++) {
				// Check each x coord
				int y = -4;
				piece.setPosition(x, y);
				while (piece.canStepDown()) {
					// Check each y coord
					piece.cut();
					piece.stepDown();
					piece.paste();
				}
//				game.game_grid.repaint();
//				quickSleep(200);
				if (!piece.isOnGrid()) {
					piece.cut();
					continue;
				}
				TetrisPiece clone = game.new TetrisPiece(piece);
				for (int r2 = 0; r2 < 4; r2++) {
					// Check each rotation
					nextPiece.rotate();
					for (int x2 = -1; x2 <= 8; x2++) {
						// Check each x coord
						int y2 = -4;
						nextPiece.setPosition(x2, y2);
						while (nextPiece.canStepDown()) {
							// Check each y coord
							nextPiece.cut();
							nextPiece.stepDown();
							nextPiece.paste();
						}
//						game.game_grid.repaint();
//						quickSleep(50);
						if (!nextPiece.isOnGrid()) {
							nextPiece.cut();
							continue;
						}
						TetrisPiece clone2 = game.new TetrisPiece(nextPiece);
						firstPiece.add(clone);
						secondPiece.add(clone2);
						nextPiece.cut();
					}
				}
				piece.cut();
			}
		}
	}
	
	private static void quickSleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public class Tile {
		
		private int value;
		
		public Tile(int value) {
			this.value = value;
		}
		
		public boolean isX() {
			return value != -1;
		}
		
		public boolean isO() {
			return !isX();
		}
		
		public int getValue() {
			return value;
		}
		
	}
	
}
