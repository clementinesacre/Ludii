package app.move;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.List;

import app.PlayerApp;
import app.utils.GameUtil;
import app.utils.MVCSetup;
import game.Game;
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

		GameUtil.resetGameWithoutResetContext(app);

		app.manager().settingsManager().setAgentsPaused(app.manager(), true);

		final int moveToJumpToWithSetup = 0;
		final List<Move> newDoneMoves = allMoves.subList(0, moveToJumpToWithSetup);
		final List<Move> newUndoneMoves = allMoves.subList(moveToJumpToWithSetup, allMoves.size());
		
		app.manager().ref().makeSavedMoves(app.manager(), newDoneMoves);
		app.manager().setUndoneMoves(newUndoneMoves);

		// this is just a tiny bit hacky, but makes sure MCTS won't reuse incorrect tree after going back in Trial
		context.game().incrementGameStartCount();

		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
		GameUtil.resetUIVariables(app);

		// Reset state but without reseting movers - as moves are not re-applied, state of movers is still good
		int mover = context.state().mover();
		int prev = context.state().prev();
		int next = context.state().next();
		resetState(context);
		context.state().setMover(mover);
		context.state().setNext(next);
		context.state().setPrev(prev);
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
		if (replayMoves) // TODO does not change if we call it or not - test that
			resetMoves(app);
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
	
	public static void displayInfo(Context context)
	{
		System.out.println("\n\n");
		System.out.println("GrowingBoardVisual.java displayInfo() containerStates 0 : "+(ContainerFlatState) context.state().containerStates()[0]);
		System.out.println("GrowingBoardVisual.java displayInfo() offset : "+Arrays.toString(context.game().equipment().offset()));
		System.out.println("GrowingBoardVisual.java displayInfo() containerId : "+Arrays.toString(context.game().equipment().containerId()));
		System.out.println("GrowingBoardVisual.java displayInfo() sitesFrom : "+Arrays.toString(context.game().equipment().sitesFrom()));
		System.out.println("GrowingBoardVisual.java displayInfo() mover : "+context.state().mover());
		System.out.println("GrowingBoardVisual.java displayInfo() containerId : "+Arrays.toString(context.containerId()));
		for (int i=0; i<((FlatCellOnlyOwned) context.state().owned()).locations().length; i++)
			System.out.println("GrowingBoard.java updateOwnedPrevToNew() locations["+i+"] 2: "+Arrays.toString(((FlatCellOnlyOwned) context.state().owned()).locations()[i]));
		System.out.println("\n\n");
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
			List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());
			System.out.println("\nGrowingBoardVisual.java checkMoveImpactOnBoard() isTouchingEdge : "+isTouchingEdge(perimeter, move.to())+" - move : "+move);
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() game.equipment.containers : "+game.equipment().containers().length);
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() game.equipment.sitesFrom : "+Arrays.toString(game.equipment().sitesFrom()));
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() context.containerId : "+Arrays.toString(context.containerId()));
			if (isTouchingEdge(perimeter, move.to())) 
			{
				updateBoard(app, context, fromSize, toSize, replayMoves);
				
				//displayInfo(context); //TODO : to remove once code is ready
				return true;
			}
		}
		return false;
	}
}
