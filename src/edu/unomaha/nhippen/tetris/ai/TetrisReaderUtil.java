package edu.unomaha.nhippen.tetris.ai;

import edu.unomaha.nhippen.tetris.Tetris;

public class TetrisReaderUtil {
	
	/*
	 * (0, 0) on grid = top left
	 * grid[y][x]
	 * 
	 * -1 --> EMPTY
	 */
	
	public static int countHoles(int[][] grid) {
		int count = 0;
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid[y].length; x++) {
				if (isHole(grid, x, y)) {
					count++;
				}
			}
		}
		return count;
	}
	
	public static boolean isHole(int[][] grid, int x, int y) {
		for (int h = y - 1; h >= 0; h--) {
			if (grid[h][x] != -1) {
				return true;
			}
		}
		return false;
	}

	public static int getHighestYAtX(int[][] grid, int x) {
		for (int y = 0; y < grid.length; y++) {
			if (grid[y][x] != -1) {
				return y;
			}
		}
		return 0;
	}

	public static int getJaggedness(int[][] grid) {
		int jaggednessVal = 0;
		int prevHeight = getHighestYAtX(grid, 0);
		for (int x = 1; x < Tetris.COLUMNS; x++) {
			int diff = Math.abs(getHighestYAtX(grid, x) - prevHeight);
			jaggednessVal += diff;
		}
		return jaggednessVal;
	}

	public static int getHighestHeight(int grid[][]) {
		int highest = 0;
		for (int x = 0; x < Tetris.COLUMNS; x++) {
			int height = getHighestYAtX(grid, x);
			if (height > highest) {
				highest = height;
			}
		}
		return highest;
	}

	public static int getAbsoluteHeightDifference(int grid[][]) {
		int lowest = Integer.MAX_VALUE;
		int highest = 0;
		for (int x = 0; x < Tetris.COLUMNS; x++) {
			int height = getHighestYAtX(grid, x);
			if (height > highest) {
				highest = height;
			}
			if (height < lowest) {
				lowest = height;
			}
		}
		return highest - lowest;
	}

	public static void cleanPrintGrid(int[][] grid) {
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid[y].length; x++) {
				String gridItem;
				if (grid[y][x] == -1) {
					gridItem = "-";
				} else {
					gridItem = "X";
				}
				System.out.print(gridItem + " ");
			}
			System.out.println();
		}
	}
	
}
