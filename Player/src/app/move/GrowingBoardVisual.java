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
import other.move.MoveSequence;
import other.topology.TopologyElement;
import other.trial.Trial;

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
		
		GameUtil.resetGame(app, true);
		// also reset initial placement moves
		context.trial().setMoves(new MoveSequence(null), 0);
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
		resetMoves(app);
		remakeTrial(context, movesDone, legalMoves);
	}
	
	public static void impactBoard(final PlayerApp app, Context context)
	{
		Game game = context.game();
		Boardless board = (Boardless) game.board();
		initMainConstants(context, board.dimension());
		
		// TODO check that the move is applied on a board type container
		System.out.println("GrowingBoardVisual.java checkMoveImpactOnBoard() : touching an edge in a boardless game --> need to increase board size");
		updateBoardDimensions(app, board);
		remakeTrial(app);
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
				impactBoard(app, context);
			}
		}
	}
}
