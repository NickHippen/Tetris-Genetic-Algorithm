package edu.unomaha.nhippen.tetris;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PlayerTetris extends AbstractTetris {

	@Override
	public void start() {
		setTimer(new Timer(INITIAL_DELAY, new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				synchronized(getTimer()) {
					if(cur_piece.canStepDown()) {
						cur_piece.cut();
						cur_piece.stepDown();
						cur_piece.paste();
						if(getTimer().isFast())
							score_label.addValue(1); // a small reward for using fast mode
					}
					else { // it hit something
						getTimer().setFast(false);
						if( ! cur_piece.isTotallyOnGrid())
							gameOver();
						else {
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
		pause_resume_butt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if(pause_resume_butt.getLabel().equals("Pause"))
					pauseGame();
				else
					resumeGame();
			}
		});
		
		//create key listener for rotating, moving left, moving right
		KeyListener key_listener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(getTimer().isPaused()) //don't do anything if game is paused
					return;
				if (e.getKeyCode() == 37 || e.getKeyCode() == 39) { //left or right arrow pressed
					int dir = e.getKeyCode() == 37 ? -1 : 1;
					synchronized(getTimer()) {
						cur_piece.cut();
						cur_piece.setX(cur_piece.getX() + dir); // try to move
						if( ! cur_piece.canPaste())
							cur_piece.setX(cur_piece.getX() - dir); // undo move
						cur_piece.paste();
					}
					game_grid.repaint();
				}
				else if (e.getKeyCode() == 38) { //rotate
					synchronized(getTimer()) {
						cur_piece.cut();
						cur_piece.rotate();
						if( ! cur_piece.canPaste())
							cur_piece.rotateBack();
						cur_piece.paste();
					}
					game_grid.repaint();
				}
				if (e.getKeyCode() == 40) { //down arrow pressed; drop piece
					getTimer().setFast(true);
				}
			}
		};
		
		// add the key listener to all components that might get focus
		// so that it'll work regardless of which has focus
		start_newgame_butt.addKeyListener(key_listener);
		pause_resume_butt.addKeyListener(key_listener);
		
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
	
}
