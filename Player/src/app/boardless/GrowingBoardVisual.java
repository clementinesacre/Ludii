package app.boardless;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.PlayerApp;
import app.utils.GameUtil;
import app.utils.MVCSetup;
import game.Game;
import game.boardless.GrowingBoard;
import game.equipment.container.board.Boardless;
import main.Constants;
import other.context.Context;
import other.location.FullLocation;
import other.move.Move;
import other.topology.TopologyElement;
import other.trial.Trial;
import other.state.container.ContainerFlatState;
import other.state.owned.FlatCellOnlyOwned;

public class GrowingBoardVisual extends GrowingBoard
{
	
	/** 
	 * Updates the board dimensions and update the visual to reflect the new size.
	 * 
	 * @param app
	 * @param board
	 * @param newSize new size of the board.
	 */
	public static void updateBoardDimensions(final PlayerApp app, Boardless board, int newSize) 
	{
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
	public static void updateBoardWithoutRemakeTrial(final PlayerApp app, Context context, int fromSize, int toSize)
	{
		Game game = context.game();
		Boardless board = (Boardless) game.board();
		initMainConstants(context, fromSize, toSize);

		// TODO check that the move is applied on a board type container
		System.out.println("GrowingBoardVisual.java updateBoard() : touching an edge in a boardless game --> need to increase board size (new size : "+toSize+")");
		updateBoardDimensions(app, board, toSize);
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
	public static void updateBoard(final PlayerApp app, Context context, int fromSize, int toSize, final boolean replayMoves)
	{
		Game game = context.game();
		Boardless board = (Boardless) game.board();
		initMainConstants(context, fromSize, toSize);

		// TODO check that the move is applied on a board type container
		System.out.println("GrowingBoardVisual.java updateBoard() : touching an edge in a boardless game --> need to increase board size (new size : "+toSize+")");
		updateBoardDimensions(app, board, toSize);
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
	public static boolean checkMoveImpactOnBoard(final PlayerApp app, final Move move, int fromSize, final int toSize, final boolean replayMoves) 
	{
		final Context context = app.manager().ref().context();

		if (context.game().isBoardless()) 
		{
			perimeter = new ArrayList<>(context.topology().perimeter(context.board().defaultSite()));
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() game.equipment.containers : "+game.equipment().containers().length);
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() game.equipment.sitesFrom : "+Arrays.toString(game.equipment().sitesFrom()));
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() context.containerId : "+Arrays.toString(context.containerId()));
			if (isTouchingEdge(move.to())) 
			{
				updateBoard(app, context, fromSize, toSize, replayMoves);
				
				//displayInfo(context); //TODO : to remove once code is ready
				return true;
			}
		}
		return false;
	}
	
	public static void checkMoveImpactOnBoard2(final PlayerApp app, final Move move, int fromSize, final int toSize, final boolean replayMoves) 
	{
		final Context context = app.manager().ref().context();
		if (!isVisual)
			isVisual = true;
		
		if (context.game().isBoardless()) 
		{
			perimeter = new ArrayList<>(context.topology().perimeter(context.board().defaultSite()));
			System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard2() isTouchingEdge : "+isTouchingEdge(move.to())+" - move : "+move);
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() game.equipment.containers : "+game.equipment().containers().length);
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() game.equipment.sitesFrom : "+Arrays.toString(game.equipment().sitesFrom()));
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() context.containerId : "+Arrays.toString(context.containerId()));
			if (isTouchingEdge(move.to())) 
			{
				Game game = context.game();
				Boardless board = (Boardless) game.board();
				
				initMainConstants(context, fromSize, toSize);
				
				// TODO check that the move is applied on a board type container
				updateBoardDimensions(app, board, toSize);

				Trial trial = context.trial();
				movesDone = trial.generateCompleteMovesList();
				System.out.println("GrwingBoardVisual.java checkMoveImpactOnBoard2() movesDone1 : "+movesDone);
				//if (replayMoves) // TODO does not change if we call it or not - test that
				//	resetMoves(app);
				System.out.println("GrwingBoardVisual.java checkMoveImpactOnBoard2() movesDone2 : "+movesDone);
				
				//remakeTrial(context, movesDone, legalMoves, replayMoves);
			}
		}
	}
}
