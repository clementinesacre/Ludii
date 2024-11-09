package app.move;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.List;

import app.PlayerApp;
import app.utils.GameUtil;
import app.utils.MVCSetup;
import game.Game;
import game.equipment.container.board.Boardless;
import game.rules.play.moves.Moves;
import game.types.play.ModeType;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import main.Constants;
import other.context.Context;
import other.location.FullLocation;
import other.move.Move;
import other.move.MoveSequence;
import other.topology.TopologyElement;
import other.trial.Trial;
import other.state.container.ContainerFlatState;
import other.state.container.ContainerState;
import other.state.zhash.ZobristHashUtilities;

public class GrowingBoardVisual extends GrowingBoard
{
	
	/** 
	 * Updates the board dimensions and update the visual to reflect the new size.
	 * 
	 * @param app
	 * @param board
	 */
	private static void updateBoardDimensions(final PlayerApp app, Boardless board) 
	{
		updateBoardDimensions(app.manager().ref().context(), board);

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
		
		//----------
		GameUtil.resetGameWithoutResetContext(app);
		//----------
		
		// also reset initial placement moves
		context.trial().setMoves(new MoveSequence(null), context.trial().numInitialPlacementMoves());
		app.manager().settingsManager().setAgentsPaused(app.manager(), true);
		
		//final int moveToJumpToWithSetup = context.currentInstanceContext().trial().numInitialPlacementMoves();
		final int moveToJumpToWithSetup = 0;
		final List<Move> newDoneMoves = allMoves.subList(0, moveToJumpToWithSetup);
		final List<Move> newUndoneMoves = allMoves.subList(moveToJumpToWithSetup, allMoves.size());
		
		app.manager().ref().makeSavedMoves(app.manager(), newDoneMoves);
		app.manager().setUndoneMoves(newUndoneMoves);
		
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
	private static void remakeTrial(final PlayerApp app) 
	{
		Context context = app.manager().ref().context();
		Trial trial = context.trial();
		List<Move> movesDone = trial.generateCompleteMovesList();
		Moves legalMoves = trial.cachedLegalMoves();
		//int mover = context.state().mover();
		resetMoves(app);
		remakeTrial(context, movesDone, legalMoves);
		//context.state().setMover(mover);
	}
	
	/**
	 * Updates board by making it grow logically and visually.
	 * 
	 * @param app
	 * @param context
	 */
	public static void updateBoard(final PlayerApp app, Context context)
	{
		Game game = context.game();
		Boardless board = (Boardless) game.board();
		initMainConstants(context, board.dimension());
		
		// TODO check that the move is applied on a board type container
		System.out.println("GrowingBoardVisual.java impactBoard() : touching an edge in a boardless game --> need to increase board size");
		updateBoardDimensions(app, board);
		remakeTrial(app);
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
		System.out.println("\n\n");
	}
	
	/** 
	 * Check if the move was made on a boardless board and on one edge of the board. 
	 * If so, update the size of the  board and update the visual.
	 * 
	 * @param app
	 * @param move
	 */
	public static void checkMoveImpactOnBoard(final PlayerApp app, final Move move) 
	{
		final Context context = app.manager().ref().context();
		
		if (context.game().isBoardless()) 
		{
			List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());
			System.out.println("\nGrowingBoardVisual.java checkMoveImpactOnBoard() isTouchingEdge : "+isTouchingEdge(perimeter, move.to()));
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() game.equipment.containers : "+game.equipment().containers().length);
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() game.equipment.sitesFrom : "+Arrays.toString(game.equipment().sitesFrom()));
			//System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() context.containerId : "+Arrays.toString(context.containerId()));
			
			if (isTouchingEdge(perimeter, move.to())) 
			{
				updateBoard(app, context);
				
				//displayInfo(context); //TODO : to remove once code is ready
			}
		}
		System.out.println("\nGrowingBoardVisual.java checkMoveImpactOnBoard() mover after : "+context.state().mover());
		
	}
}