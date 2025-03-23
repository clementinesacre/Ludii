package app.move;

import java.awt.EventQueue;
import java.util.List;

import app.PlayerApp;
import app.utils.GameUtil;
import app.utils.MVCSetup;
import game.Game;
import game.equipment.container.board.Boardless;
import game.rules.play.moves.Moves;
import main.Constants;
import other.context.Context;
import other.location.FullLocation;
import other.move.Move;
import other.topology.TopologyElement;
import other.trial.Trial;

public class GrowingBoardVisual extends GrowingBoard
{
	
	/** 
	 * Updates the board dimensions and update the visual to reflect the new size.
	 * 
	 * @param app
	 * @param board
	 * @param boardSizeChange Determines how the board size should be adjusted based on the last move.
	 * -2: Reset the board to its initial size ; -1: Reduce the board size based on last move ;
	 *  0: Keep the board at its current size ; 1: Expand the board size based on the last move.
	 */
	private static void updateBoardDimensions(final PlayerApp app, Boardless board, int boardSizeChange) 
	{
		updateBoardDimensions(app.manager().ref().context(), board, boardSizeChange);

		// Update the visual 
		// TODO Check if all the code inside setMVC is useful (inspired from GameUtil.resetUIVariables())
		MVCSetup.setMVC(app);

		// TODO Check this line is useful (inspired from GameUtil.resetUIVariables())
		EventQueue.invokeLater(() -> 
		{
			app.repaint();
		});
	}
	
	/** 
	 * Cancel all the moves from the beginning, to have a fresh base with an empty board.
	 * Is equivalent to restore to initial state.
	 * Code taken from ToolView.jumpToMove().
	 * 
	 * @param app
	 */
	private static void resetMoves(final PlayerApp app)
	{
		Context context = app.manager().ref().context();
		
		app.manager().settingsManager().setAgentsPaused(app.manager(), true);
		app.settingsPlayer().setWebGameResultValid(false);
		
		// Store the previous saved trial, and reload it after resetting the game.
		final List<Move> allMoves = context.trial().generateCompleteMovesList();
		allMoves.addAll(app.manager().undoneMoves());
		
		GameUtil.resetGame(app, true, true);
		
		app.manager().settingsManager().setAgentsPaused(app.manager(), true);
		
		// this is just a tiny bit hacky, but makes sure MCTS won't reuse incorrect tree after going back in Trial
		context.game().incrementGameStartCount();

		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
		GameUtil.resetUIVariables(app);
		
		// Reset 
		resetState(context);
	}
	
	/** 
	 * Start over the game on the new board and apply the historic of move mapped to the new board.
	 * 
	 * @param app
	 */
	public static void remakeTrial(final PlayerApp app, final boolean replayMoves) 
	{
		Context context = app.manager().ref().context();
		Trial trial = context.trial();
		List<Move> movesDone = trial.generateCompleteMovesList();
		Moves legalMoves = trial.cachedLegalMoves();
		if (replayMoves) // TODO does not change if we call it or not - test that
			resetMoves(app);
		remakeTrial(context, movesDone, legalMoves, replayMoves);
	}
	
	/**
	 * Updates board by making it grow logically and visually.
	 * TODO
	 * 
	 * @param app
	 * @param context
	 * @param move
	 * @param boardSizeChange Determines how the board size should be adjusted based on the last move.
	 * -2: Reset the board to its initial size ; -1: Reduce the board size based on last move ;
	 *  0: Keep the board at its current size ; 1: Expand the board size based on the last move.
	 */
	public static void updateBoardWithoutRemakeTrial(final PlayerApp app, Context context, Move move, int boardSizeChange)
	{
		Game game = context.game();
		Boardless board = (Boardless) game.board();
		initMainConstants(context, move, boardSizeChange);
		
		// TODO check that the move is applied on a board type container
		System.out.println("GrowingBoardVisual.java updateBoard() : touching an edge in a boardless game --> need to increase board size");
		// update dimensions only if board change size
		if (boardSizeChange != 0)
			updateBoardDimensions(app, board, boardSizeChange);
	}
	
	/**
	 * Updates board by making it grow logically and visually.
	 * 
	 * @param app
	 * @param context
	 * @param move
	 * @param boardSizeChange Determines how the board size should be adjusted based on the last move.
	 * -2: Reset the board to its initial size ; -1: Reduce the board size based on last move ;
	 *  0: Keep the board at its current size ; 1: Expand the board size based on the last move.
	 * @param replayMoves TODO
	 */
	public static void updateBoard(final PlayerApp app, Context context, Move move, int boardSizeChange, final boolean replayMoves)
	{
		Game game = context.game();
		Boardless board = (Boardless) game.board();
		initMainConstants(context, move, boardSizeChange);
		
		// TODO check that the move is applied on a board type container
		System.out.println("GrowingBoardVisual.java updateBoard() : touching an edge in a boardless game --> need to increase board size");
		// update dimensions only if board change size
		if (boardSizeChange != 0)
			updateBoardDimensions(app, board, boardSizeChange);
		remakeTrial(app, replayMoves);
	}
	
	/** 
	 * Check if the move was made on a boardless board and on one edge of the board. 
	 * If so, update the size of the  board and update the visual.
	 * 
	 * @param app
	 * @param move
	 * @param boardSizeChange Determines how the board size should be adjusted based on the last move.
	 * -2: Reset the board to its initial size ; -1: Reduce the board size based on last move ;
	 *  0: Keep the board at its current size ; 1: Expand the board size based on the last move.
	 * @param replayMoves if wee need to re-apply the moves 
	 */
	public static void checkMoveImpactOnBoard(final PlayerApp app, final Move move, int boardSizeChange, final boolean replayMoves) 
	{
		final Context context = app.manager().ref().context();

		if (context.game().isBoardless()) 
		{
			List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());
			System.out.println("\nGrowingBoardVisual.java checkMoveImpactOnBoard() isTouchingEdge : "+isTouchingEdge(perimeter, move.to())+" - move : "+move);
			if (isTouchingEdge(perimeter, move.to())) 
			{
				updateBoard(app, context, move, boardSizeChange, replayMoves);				
			}
		}
	}
}
