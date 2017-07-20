package edu.unomaha.nhippen.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.unomaha.nhippen.tetris.ai.TetrisReaderUtil;

public class TetrisReaderUtilTest {

	private static final int O = -1;
	private static final int X = 1;
	
	private int[][] grid = {
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, O},
			{O, O, O, O, O, O, O, O, O, X},
			{O, O, O, O, O, O, O, O, O, O},
	};
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testIsHole() {
		assertTrue(TetrisReaderUtil.isHole(grid, 9, 17));
	}
	
	@Test
	public void testCountHole() {
		TetrisReaderUtil.cleanPrintGrid(grid);
		assertEquals(1, TetrisReaderUtil.countHoles(grid));
	}

}
