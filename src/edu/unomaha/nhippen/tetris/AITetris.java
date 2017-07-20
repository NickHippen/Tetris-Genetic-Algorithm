package edu.unomaha.nhippen.tetris;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.unomaha.nhippen.tetris.ai.TetrisGenes;
import edu.unomaha.nhippen.tetris.ai.TetrisReaderUtil;

public class AITetris extends AbstractTetris {

	private TetrisGenes genes;
	private boolean complete;
	private int id;
	private boolean limitPieces;
	public boolean prevEquals = false;
	private long pieceCount = 0;
	
	public AITetris(int id, TetrisGenes genes, int tickDelay, boolean limitPieces) {
		this.id = id;
		this.genes = genes;
		this.complete = false;
		INITIAL_DELAY = tickDelay;
		this.limitPieces = limitPieces;
	}
	
	@Override
	public void start() {
		final AbstractTetris self = this;
		this.complete = false;
		setTimer(new Timer(INITIAL_DELAY, new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				synchronized(getTimer()) {
					if (limitPieces && pieceCount >= 1000) {
						gameOver();
						return;
					}
					pieceCount++;
//					System.out.println("SYNC: " + id);
//					List<TetrisPiece> expansion = TetrisBoardReader.expandPossiblePiecePositions(self, cur_piece);
					List<TetrisPiece> firstPieces = new ArrayList<>();
					List<TetrisPiece> secondPieces = new ArrayList<>();
					TetrisBoardReader.expandPossiblePiecePositions2(self, cur_piece, firstPieces, secondPieces);
					List<int[][]> grids = new ArrayList<>(firstPieces.size());
					List<Double> heuristics = new ArrayList<>(firstPieces.size());
					for (int i = 0; i < firstPieces.size(); i++) {
						TetrisPiece piece = firstPieces.get(i);
						TetrisPiece piece2 = secondPieces.get(i);
						int[][] copiedGrid = TetrisReaderUtil.copyGrid(getGrid());
						piece.paste(copiedGrid);
						piece2.paste(copiedGrid);
						TetrisBoardReader reader = new TetrisBoardReader(copiedGrid);
						double heuristic = reader.calculateHeuristicValue(genes);
						piece2.unpaste(copiedGrid);
						grids.add(copiedGrid);
						heuristics.add(heuristic);
					}
					double largest = Double.NEGATIVE_INFINITY;
					int[][] bestGrid = null;
					TetrisPiece piece = null;
					for (int i = 0; i < heuristics.size(); i++) {
						double heuristic = heuristics.get(i);
						if (heuristic > largest) {
							largest = heuristic;
							bestGrid = grids.get(i);
							piece = firstPieces.get(i);
						}
					}
					if (bestGrid == null) {
						gameOver();
						return;
					}
					System.arraycopy(bestGrid, 0, grid, 0, grid.length);
					self.cur_piece = piece;
					if(cur_piece.canStepDown()) {
						cur_piece.cut();
						cur_piece.stepDown();
						cur_piece.paste();
						if(getTimer().isFast())
							score_label.addValue(1); // a small reward for using fast mode
					}
					else { // it hit something
						getTimer().setFast(false);
						if( ! cur_piece.isTotallyOnGrid()) {
							gameOver();
							return;
						} else {
							removeFullRows();
							installNewPiece();
						}
					}
				}
				game_grid.repaint();
			}
		}));
		getTimer().start(); // pauses immediately
	}

	@Override
	public void init() {
		sounds = new TetrisSound(); // NOTE: Must be initialized after Applet fully constructed!
		installNewPiece();

		pause_resume_butt.setEnabled(false);
		start_newgame_butt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if(start_newgame_butt.getLabel().equals("Start"))
					startGame();
				else
					newGame();
			}
		});		
		
		Panel right_panel = new Panel(new GridLayout(3, 1));	
		right_panel.setBackground(BACKGROUND_COLOR);
		
		Panel control_panel = new Panel();
		control_panel.add(start_newgame_butt);
		control_panel.add(pause_resume_butt);
		control_panel.setBackground(BACKGROUND_COLOR);
		right_panel.add(control_panel);
		
		Panel tmp = new Panel(new BorderLayout());
		tmp.add("North", new TetrisLabel("    Next Piece:"));
		tmp.add("Center", next_piece_canvas);
		tmp.setBackground(BACKGROUND_COLOR);
		right_panel.add(tmp);
		
		Panel stats_panel = new Panel(new GridLayout(4, 2));
		stats_panel.add(new TetrisLabel("    Rows Deleted: "));
		stats_panel.add(rows_deleted_label);
		stats_panel.add(new TetrisLabel("    Level: "));
		stats_panel.add(level_label);
		stats_panel.add(new TetrisLabel("    Score: "));
		stats_panel.add(score_label);
		stats_panel.add(new TetrisLabel("    High Score: "));
		stats_panel.add(high_score_label);
		tmp = new Panel(new BorderLayout());
		tmp.setBackground(BACKGROUND_COLOR);
		tmp.add("Center", stats_panel);
		right_panel.add(tmp);
		
		// finaly, add all the main panels to the applet panel
		this.setLayout(new GridLayout(1, 2));
		this.add(game_grid);
		this.add(right_panel);
		this.setBackground(BACKGROUND_COLOR);
		this.validate();
	}
	
	@Override
	public void gameOver() {
		super.gameOver();
		System.out.println("NEW FINISH: " + getId());
		setComplete(true);
	}
	
	
	@Override
	public void newGame() {
		System.out.println("RESET RANDOM!!!!");
		this.random = new Random(45612L);
		this.complete = false;
		this.cur_piece = randomPiece();
		this.next_piece = randomPiece();
		pieceCount = 0;
		super.newGame();
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public void setComplete(boolean complete) {
		this.complete = complete;
		TetrisRunner.processCompletion();
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public TetrisGenes getGenes() {
		return genes;
	}
	
	public void setGenes(TetrisGenes genes) {
		this.genes = genes;
	}
	
}
