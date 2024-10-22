package app.move;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import app.PlayerApp;
import app.utils.GameUtil;
import app.utils.MVCSetup;
import game.Game;
import game.equipment.container.Container;
import game.equipment.container.board.Boardless;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.ChunkSet;
import main.collections.FastArrayList;
import other.action.Action;
import other.context.Context;
import other.location.FullLocation;
import other.move.Move;
import other.move.MoveSequence;
import other.state.container.ContainerFlatState;
import other.state.container.ContainerState;
import other.state.zhash.HashedBitSet;
import other.state.zhash.HashedChunkSet;
import other.state.zhash.ZobristHashGenerator;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Functions for handling the board growing regarding boardless game.
 * 
 * @author Clémentine.Sacré
 */
public class GrowingBoard
{
	
	private static HashMap<Integer, Integer> mappedPrevToNewIndexes;
	private static HashMap<Integer, Integer> mappedNewToPrevIndexes;
	private static HashSet<Integer> newAddedIndexes;
	
	private static int prevDimensionBoard;
	private static int prevAreaBoard;
	private static int prevTotalIndexes;
	private static int newDimensionBoard;
	private static int newAreaBoard;
	private static int newTotalIndexes;
	private static int diff;
	
	/** 
	 * Initializes data structures to map the previous indexes to the new indexes, 
	 * due to the change in board size, such as :
	 * mappedPrevToNewIndexes : mapping giving the previous index as key and the new index as value.
	 * mappedNewToPrevIndexes : mapping giving the new index as key and the previous index as value.
	 * newAddedIndexes : new indexes that don't have a mapping to the previous plate as they are new existing sites.
	 */
	private static void initMappingIndexes()
	{
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		newAddedIndexes = new HashSet<Integer>();
		int inter;
		int newIndex;
		for (int prevIndex = 0; prevIndex < prevTotalIndexes; prevIndex++)
		{
			inter = prevIndex / prevDimensionBoard;
			if (prevIndex < prevAreaBoard)
			{
				newIndex = prevIndex + prevDimensionBoard + Constants.GROWING_STEP + 1 + (2 * (inter));
			}
			else
			{
				newIndex = prevIndex + diff;
			}
			mappedPrevToNewIndexes.put(prevIndex, newIndex);
			mappedNewToPrevIndexes.put(newIndex, prevIndex);
		}
		for (int i = 0; i < newTotalIndexes; i++)
		{
			if (!mappedNewToPrevIndexes.containsKey(i))
			{
				newAddedIndexes.add(i);
			}
		}
	}
	
	/** 
	 * Initializes the main constants such as :
	 * prevAreaBoard : the area of the previous board (usually currDimensionBoard*currDimensionBoard).
	 * prevTotalIndexes : total number of indexes of the previous board (including players's hand).
	 * newDimensionBoard : dimension of the new board (size of one side of the board).
	 * newAreaBoard : the area of the new board.
	 * newTotalIndexes : total number of indexes of the new board.
	 * diff : number of indexes added compared to previous board.
	 * 
	 * @param context
	 * @param currDimensionBoard dimension of the current board (size of one side of the board), that has not changed size yet.
	 */
	private static void initMainConstants(Context context, int currDimensionBoard) {
		prevDimensionBoard = currDimensionBoard;
		prevAreaBoard = (int) Math.pow(prevDimensionBoard, 2);
		prevTotalIndexes = (int) Math.pow(prevDimensionBoard, 2)+context.sitesFrom().length-1;
		
		newDimensionBoard = prevDimensionBoard+Constants.GROWING_STEP;
		newAreaBoard = (int) Math.pow(newDimensionBoard, 2);
		newTotalIndexes = newAreaBoard+context.sitesFrom().length-1;
		
		diff = newAreaBoard - prevAreaBoard;
	}
	
	/** 
	 * Checks if player touched an edge of the board by performing a dichotomic search on a TopologyElement list.
	 * 
	 * @param topologyElements List into the search needs to be done. Made up of edges element.
	 * @param target Value we are looking for into the list.
	 * @return Index of the element in the list if found, -1 otherwise.
	 */
	public static boolean isTouchingEdge(List<TopologyElement> topologyElements, int target) {
		if (target == Constants.UNDEFINED) return false;
		
        int start = 0;
        int end = topologyElements.size() - 1;

        while (start <= end) {
            int midIndex = start + (end - start) / 2;
            int midValue = topologyElements.get(midIndex).index();

            if (midValue == target) {
                return true;
            } else if (midValue < target) {
            	start = midIndex + 1; // Check on right remaining side
            } else {
            	end = midIndex - 1; // Check on left remaining side
            }
        }

        return false;
    }
	
	/** 
	 * Updates the board dimensions and update the visual to reflect the new size.
	 * 
	 * @param app
	 * @param board
	 */
	private static void updateBoardDimensions(final PlayerApp app, Boardless board) 
	{		
		System.out.println("GrowingBoarc.java updateBoardDimensions() curr size : "+board.getDimension()+ " - new size : "+(board.getDimension() + Constants.GROWING_STEP));
		GraphFunction newGraphFunction = new RectangleOnSquare(new DimConstant(board.getDimension() + Constants.GROWING_STEP), null, null, null);
		board.setGraphFunction(newGraphFunction);
		
		board.setDimension(board.getDimension() + Constants.GROWING_STEP);
		//board.topology().pregenerate ... TODO
		
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
	 * Copy a HashedChunkSet by mapping the index to the index of the new board, 
	 * whose sized has changed compared to the previous one.
	 * 
	 * @param previousHCS
	 * @param generator
	 * @param maxChunkVal
	 * @param maxChunkVal
	 * @param mappedPrevToNewIndexes
	 */
	private static HashedChunkSet copyChunkWithNewBoardSize(HashedChunkSet previousHCS, ZobristHashGenerator generator, int maxChunkVal, int numChunks)
	{
		HashedChunkSet newHCS = new HashedChunkSet(generator, maxChunkVal, numChunks);
		if (previousHCS != null)
		{
			ChunkSet previousCS = previousHCS.internalState();
			ChunkSet newCS = (ChunkSet) newHCS.internalState();
			TIntArrayList nonzeroChunks = previousCS.getNonzeroChunks();
			for (int prevVal : nonzeroChunks.toArray()) 
				newCS.setChunk(mappedPrevToNewIndexes.get(prevVal), previousCS.getChunk(prevVal));
		}
		else
			newHCS = null;
		return newHCS;
	}
	
	/** 
	 * Updates the chunks of the first containerStates (container state of the board ?) 
	 * to include the new added sites, following the growth of the board.
	 * To precise the new playable sites, and the sites that should be empty.
	 * 
	 * @param context
	 */
	private static void updateChunks(Context context)
	{
		final Game game = context.game();
		final int numPlayers = game.players().count();
		int numSites = mappedNewToPrevIndexes.size()+newAddedIndexes.size();
		ContainerState[] containerStates = context.state().containerStates();
		
		// update each container state (container state 0 is the board state)
		for (int i=0; i<containerStates.length; i++)
		{	
			ContainerState containerState = containerStates[i];
			if (containerState instanceof other.state.container.ContainerFlatState) 
			{				
				ContainerFlatState containerFlatState = (ContainerFlatState) containerState;
				ZobristHashGenerator generator = containerFlatState.getGenerator();
				
				HashedChunkSet who = copyChunkWithNewBoardSize(containerFlatState.who(), generator, numPlayers+1, numSites);
				HashedChunkSet what = copyChunkWithNewBoardSize(containerFlatState.what(), generator, containerFlatState.getMaxWhatVal(), numSites);
				HashedChunkSet count = copyChunkWithNewBoardSize(containerFlatState.count(), generator, containerFlatState.getMaxCountVal(), numSites);
				HashedChunkSet state = copyChunkWithNewBoardSize(containerFlatState.state(), generator, containerFlatState.getMaxStateVal(), numSites);
				HashedChunkSet rotation = copyChunkWithNewBoardSize(containerFlatState.rotation(), generator, containerFlatState.getMaxRotationVal(), numSites);
				HashedChunkSet value = copyChunkWithNewBoardSize(containerFlatState.value(), generator, containerFlatState.getMaxPieceValue(), numSites);
				HashedBitSet playable = new HashedBitSet(generator, numSites);
				Region empty = new Region(numSites);
				
				// empty information - ?
				int[] emptySites = containerFlatState.emptySites().sites();
				int[] newEmptySites;
				if (emptySites.length > 0)
				{
					newEmptySites = new int[emptySites.length + newAddedIndexes.size()];
					int index = 0;
					for (int j=0; j<emptySites.length; j++)
					{
						newEmptySites[index] = mappedPrevToNewIndexes.get(emptySites[index]);
						index++;
					}
					for (Integer prevVal : newAddedIndexes) 
					{
			            newEmptySites[index] = prevVal;
			            index++;
			        }
					Arrays.sort(newEmptySites);
				}
				else 
					newEmptySites = new int[0];
				empty.setSites(newEmptySites);
				
				
				// playable information - specify which sites are playable
				BitSet playableSites = containerFlatState.playable().internalState();
				BitSet playableBS = playable.internalState();
				ArrayList<Integer> prevPlayableSites = new ArrayList<Integer>();
				int j = playableSites.nextSetBit(0);
		        if (j != -1) {
		        	prevPlayableSites.add(i);
		            while (true) {
		                if (++j < 0) break;
		                if ((j = playableSites.nextSetBit(j)) < 0) break;
		                int endOfRun = playableSites.nextClearBit(j);
		                do 
		                { 
		                	prevPlayableSites.add(j);
		                }
		                while (++j != endOfRun);
		            }
		        }
				for (Integer prevVal : prevPlayableSites)
					playableBS.flip(mappedPrevToNewIndexes.get(prevVal));
		        
				
				ContainerFlatState newContainerFlatState = new ContainerFlatState
				(
					game, 
					containerFlatState.container(), 
					numSites,
					who,
					what,
					count,
					state,
					rotation,
					value,
					playable,
					empty,
					numPlayers,
					containerFlatState.getMaxWhatVal(),
					containerFlatState.getMaxStateVal(),
					containerFlatState.getMaxCountVal(),
					containerFlatState.getMaxRotationVal(),
					containerFlatState.getMaxPieceValue(),
					generator
				);
				context.state().setContainerStates(i, newContainerFlatState);
			}
			else 
				throw new UnsupportedOperationException("Type " +containerStates[i].getClass().getName() + " not implement regarding growing state of the board.");
		}
	}
	
	/** 
	 * Cancel all the moves from the beginning, to have a fresh base with an empty board.
	 * Is equivalent to restore to initial state.
	 * Code taken from ToolView.jumpToMove().
	 * 
	 * @param app
	 * @param context
	 */
	private static void resetMoves(final PlayerApp app, final Context context)
	{
		app.manager().settingsManager().setAgentsPaused(app.manager(), true);
		app.settingsPlayer().setWebGameResultValid(false);
		
		// Store the previous saved trial, and reload it after resetting the game.
		final List<Move> allMoves = app.manager().ref().context().trial().generateCompleteMovesList();
		allMoves.addAll(app.manager().undoneMoves());
		System.out.println("GrowingBoard.java remakeTrial() legalMoves after14: "+context.trial().cachedLegalMoves());
		
		GameUtil.resetGame(app, true);
		System.out.println("GrowingBoard.java remakeTrial() legalMoves after15: "+context.trial().cachedLegalMoves());
		// also reset initial placement moves
		app.manager().ref().context().trial().setMoves(new MoveSequence(null), 0);
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
	 * Generates the new move based on a move, which means with the new indexes of the board
	 * (which means updating to() and from().
	 * 
	 * @param prevMove previous move to copy, except for the indexes.
	 * @return the new move.
	 */
	private static Move generateNewMove(Move prevMove)
	{
		List<Action> actions = prevMove.actions();
		List<Action> newActions = new ArrayList<Action>();
		for (int j=0; j<actions.size(); j++) 
		{
			Action action = actions.get(j);
			//System.out.println("GrowingBoard.java generateNewMove() newAction BEFORE : "+action+" - from : "+action.from()+" - to : "+action.to()+ " - type : "+action.getClass().getName() + " - action type : "+action.actionType());
			int to = action.to();
			int from = action.from();
			if (to != Constants.UNDEFINED)
				action.setTo(mappedPrevToNewIndexes.get(to));
			if (from != Constants.UNDEFINED)
				action.setFrom(mappedPrevToNewIndexes.get(from));
			
			newActions.add(action);
			//System.out.println("GrowingBoard.java generateNewMove() newAction AFTER : "+action+ " - type : "+action.getClass().getName() + " - action type : "+action.actionType());
		}
		
		Move newMove = new Move(newActions);
		return newMove;
	}
	
	/** 
	 * Replays the moves mapped to the proper new component (like tile or hand) index.
	 * 
	 * @param context
	 * @param movesDone
	 */
	private static void replayMoves(Context context, List<Move> movesDone)
	{
		Move move = null;
		System.out.println("GrowingBoard.java replayMoves() nmappedPrevToNewIndexes : "+mappedPrevToNewIndexes);
		for (int i = 0; i < movesDone.size(); i++)
		{
			move = movesDone.get(i);
			Move newMove = generateNewMove(move);
			System.out.println("GrowingBoard.java replayMoves() newMove : "+newMove + " - actions : "+newMove.actions());
			context.game().apply(context, newMove);
		}
		System.out.println("GrowingBoard.java replayMoves() generateCompleteMovesList movesDones after changing : "+context.trial().generateCompleteMovesList());
	}
	
	/**
	 * Generates the new legal moves based on the new indexes.
	 * 
	 * @param context
	 * @param legaleMoves previous list of legal moves, before the board changed size.
	 */
	private static void generateLegalMoves(Context context, Moves legaleMoves)
	{
		Moves newLegaleMoves = new BaseMoves(null);
		for (Move prevLegalMove : legaleMoves.moves())
		{
			Move newMove = generateNewMove(prevLegalMove);
			newLegaleMoves.moves().add(newMove);
		}
		System.out.println("GrowingBoard.java aa() - prevlegaleMoves : "+legaleMoves.moves());
		System.out.println("GrowingBoard.java aa() - newLegaleMoves : "+newLegaleMoves);
		
		context.trial().setLegalMoves(newLegaleMoves, context);
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
		System.out.println("GrowingBoard.java remakeTrial() legalMoves before: "+trial.cachedLegalMoves());
		List<Move> movesDone = trial.generateCompleteMovesList();
		Moves legalMoves = trial.cachedLegalMoves();
		System.out.println("GrowingBoard.java remakeTrial() generateCompleteMovesList movesDoneBeforRefresh : "+movesDone);
		
		resetMoves(app, context);
		initMappingIndexes();
		updateChunks(context);
		replayMoves(context, movesDone);
		generateLegalMoves(context, legalMoves);
		
		context = app.manager().ref().context();
		trial = context.trial();
	}

	/** 
	 * Update the indexes of the rest of the components, as the board has changed size, it uses more indexes.
	 * It is important to avoid two components (TopologyElement) having the same index.
	 * 
	 * @param context
	 * @param game
	 */
	private static void updateIndexes(Context context, final Game game) 
	{	
		// Updating context.containerId to contains the id of the new added tiles of the bigger board
		int[] prevContainerId = context.containerId();
		int[] newContainerId = new int[prevContainerId.length + diff];
		int indexNewContainerId = 0;
		int indePrevContainerId = 0;
		for (int i=0; i<newAreaBoard+context.sitesFrom().length-1; i++) 
		{
			if (i < newDimensionBoard)
			{
			// new bottom line of tiles
				newContainerId[indexNewContainerId] = 0;
				indexNewContainerId++;
			}
			else if (i >= newDimensionBoard && i < newDimensionBoard*(newDimensionBoard-1))
			{
			// middle lines of tiles
				if (i%newDimensionBoard == 0 || i%newDimensionBoard == newDimensionBoard-1)
			    {
				// tiles on one of the edges (left or right, which are new id
			        newContainerId[indexNewContainerId] = 0;
			        indexNewContainerId++;
			    }
			    else
			    {
			    // tiles on the middle of the board, which are the ones that were already there in the previous board
			    	newContainerId[indexNewContainerId] = prevContainerId[indePrevContainerId];
			        indePrevContainerId++;
			        indexNewContainerId++;
			    }
			}
			else if (i >= newDimensionBoard*(newDimensionBoard-1) && i < newAreaBoard)
			{
			// new top line of tiles
				newContainerId[indexNewContainerId] = 0;
		        indexNewContainerId++;
			}
			else {
			// containers outside of the board, such as players's hand
				newContainerId[indexNewContainerId] = prevContainerId[indePrevContainerId];
		        indePrevContainerId++;
		        indexNewContainerId++;
			}
			
		}
		game.equipment().setContainerId(newContainerId); // TODO quid if context use the containerId of its subcontext and not equipment

		// Updating context.sitesFrom to contains the values of the new added tiles of the bigger board
		int[] prevSitesFrom = game.equipment().sitesFrom();
		int[] newSitesFrom = new int[prevSitesFrom.length];
		newSitesFrom[0] = newSitesFrom[0];
		for (int i=1; i<prevSitesFrom.length; i++) 
		{
			newSitesFrom[i] = prevSitesFrom[i]+diff;
		}
		game.equipment().setSitesFrom(newSitesFrom); // TODO quid if context use the containerId of its subcontext and not equipment
		
		// Update index of the containers outside of the board (such as players's hand)
		Container[] containers = game.equipment().containers();
		for (int i=0; i<containers.length; i++)
		{
			TopologyElement topologyElement = game.equipment().containers()[i].topology().getGraphElements(SiteType.Cell).get(0);
			int index = topologyElement.index();
			topologyElement.setIndex(index+diff);
		}

	}
	
	/** 
	 * Check if the move was made on a boardless board and on one edge of the board. 
	 * If so, update the size of the  board.
	 * 
	 * @param app
	 * @param move
	 */
	public static void checkMoveImpactOnBoard(final PlayerApp app, final Move move) 
	{
		final Context context = app.manager().ref().context();
		Game game = context.game();
		
		if (game.isBoardless()) 
		{
			List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());
			System.out.println("\nGrowingBoard.java checkMoveImpactOnBoard() isTouchingEdge : "+isTouchingEdge(perimeter, move.to()));
			//System.out.println("GrowingBoard.java checkMoveImpactOnBoard() game.equipment.containers : "+game.equipment().containers().length);
			//System.out.println("GrowingBoard.java checkMoveImpactOnBoard() game.equipment.sitesFrom : "+Arrays.toString(game.equipment().sitesFrom()));
			//System.out.println("GrowingBoard.java checkMoveImpactOnBoard() context.containerId : "+Arrays.toString(context.containerId()));
			
			if (isTouchingEdge(perimeter, move.to())) 
			{
				Boardless board = (Boardless) game.board();
				initMainConstants(context, board.getDimension());
				
				// TODO check that the move is applied on a board type container
				System.out.println("GrowingBoard.java checkMoveImpactOnBoard() : touching an edge in a boardless game --> need to increase board size");
				updateBoardDimensions(app, board);
				updateIndexes(context, game);
				remakeTrial(app);
			}
		}
	}
}
