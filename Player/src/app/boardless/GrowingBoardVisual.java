package app.boardless;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import app.PlayerApp;
import app.utils.MVCSetup;
import game.Game;
import game.boardless.GrowingBoard;
import game.equipment.container.board.Boardless;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

public class GrowingBoardVisual extends GrowingBoard
{
	/** 
	 * Updates the board dimensions and update the visual to reflect the new size.
	 * 
	 * @param app
	 * @param newSize new size of the board.
	 */
	public static void updateBoardDimensions(final PlayerApp app, final int newSize) 
	{
		Game game = app.manager().ref().context().game();
		Boardless board = (Boardless) game.board();
		updateBoardDimensions(app.manager().ref().context(), board, newSize);

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
	 * Start over the game on the new board and apply the historic of move mapped to the new board.
	 * 
	 * @param app
	 */
	public static void remakeTrial(final PlayerApp app, final boolean replayMoves) 
	{
		Context context = app.manager().ref().context();
		Trial trial = context.trial();
		List<Move> movesDone = trial.generateCompleteMovesList();
		remakeTrial(context, movesDone, replayMoves);
	}
	
	/**
	 * Updates board by making it grow logically and visually.
	 * TODO
	 * 
	 * @param app
	 * @param context
	 * @param fromSize TODO
	 * @param toSize TODO
	 * @param replayMoves TODO
	 */
	public static void updateBoardWithoutRemakeTrial(final PlayerApp app, final Context context, final int fromSize, final int toSize)
	{
		initMainConstants(context, fromSize, toSize);

		// TODO check that the move is applied on a board type container
		updateBoardDimensions(app, toSize);
		perimeter = new ArrayList<>(context.topology().perimeter(context.board().defaultSite()));
	}
	
	/**
	 * Updates board by making it grow logically and visually.
	 * 
	 * @param app
	 * @param context
	 * @param fromSize TODO
	 * @param toSize TODO
	 * @param replayMoves TODO
	 */
	private static void updateBoard(final PlayerApp app, final Context context, final int fromSize, final int toSize, final boolean replayMoves)
	{
		initMainConstants(context, fromSize, toSize);

		// TODO check that the move is applied on a board type container
		updateBoardDimensions(app, toSize);
		remakeTrial(app, replayMoves);
	}
	
	/** 
	 * Check if the move was made on a boardless board and on one edge of the board. 
	 * If so, update the size of the  board and update the visual.
	 * 
	 * @param app
	 * @param move
	 * @param fromSize
	 * @param toSize
	 * @param replayMoves if wee need to re-apply the moves 
	 * @return true if move was applied on edge
	 */
	public static boolean checkMoveImpactOnBoard(final PlayerApp app, final Move move, final int fromSize, final int toSize, final boolean replayMoves) 
	{
		final Context context = app.manager().ref().context();

		if (context.game().isBoardless()) 
		{
			perimeter = new ArrayList<>(context.topology().perimeter(context.board().defaultSite()));
			if (isTouchingEdge(move.to())) 
			{
				updateBoard(app, context, fromSize, toSize, replayMoves);
				return true;
			}
		}
		return false;
	}
}
