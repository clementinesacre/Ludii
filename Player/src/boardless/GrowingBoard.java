package boardless;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import game.Game;
import game.equipment.container.board.Boardless;
import game.functions.graph.generators.basis.square.Square;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.board.TilingBoardlessType;
import game.util.equipment.Region;
import game.util.graph.Graph;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.ChunkSet;
import main.collections.FastTIntArrayList;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerFlatState;
import other.state.container.ContainerState;
import other.state.owned.FlatCellOnlyOwned;
import other.state.zhash.HashedBitSet;
import other.state.zhash.HashedChunkSet;
import other.state.zhash.ZobristHashGenerator;
import other.topology.Cell;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Functions for handling the board growing regarding boardless game.
 * 
 * @author Clémentine.Sacré
 */
public class GrowingBoard
{	
	
	//-------------------------------------------------------------------------
	
	/**
	 * Updates the content of the Topology to match the new board.
	 * Re-launches pre-computations.
	 * 
	 * @param context
	 */
	protected static void updateTopology(Context context)
	{
		context.game().update();
	}
	
	/** 
	 * Updates the board dimensions.
	 * 
	 * @param context
	 * @param board
	 * @param boardSizeChange Determines how the board size should be adjusted based on the last move.
	 * -2: Reset the board to its initial size ; -1: Reduce the board size based on last move ;
	 *  0: Keep the board at its current size ; 1: Expand the board size based on the last move.
	 */
	protected static void updateBoardDimensions(Context context, Boardless board, int boardSizeChange, Move move) 
	{			
		System.out.println("GrowingBoard.java updateBoardDimensions() boardSizeChange : "+boardSizeChange);
		
		
		//Graph newGraph = GrowingBoardInterface.generateGraph(context, boardSizeChange, (Cell) context.topology().getGraphElement(SiteType.Cell, move.to()));
		//board.setGraphFunction(newGraph);
		
		UpdateBoard.createMappings(context, move, boardSizeChange);
		
		//board.setGraphFunction(newGraphFunction);
		//board.setDimension(newSize);
		
		updateTopology(context);
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
	 * Copy a HashedChunkSet by mapping the index to the index of the new board, 
	 * whose sized has changed compared to the previous one.
	 * 
	 * @param previousHCS
	 * @param generator
	 * @param maxChunkVal
	 * @param maxChunkVal
	 */
	protected static HashedChunkSet copyChunkWithNewBoardSize(HashedChunkSet previousHCS, ZobristHashGenerator generator, int maxChunkVal, int numChunks)
	{
		HashedChunkSet newHCS = new HashedChunkSet(generator, maxChunkVal, numChunks);
		if (previousHCS != null)
		{
			ChunkSet previousCS = previousHCS.internalState();
			ChunkSet newCS = (ChunkSet) newHCS.internalState();
			TIntArrayList nonzeroChunks = previousCS.getNonzeroChunks();
			for (int prevVal : nonzeroChunks.toArray()) { 
				newCS.setChunk(UpdateBoard.mappedInitToNewIndexes().get(prevVal), previousCS.getChunk(prevVal));
			}
		}
		else
			newHCS = null;
		return newHCS;
	}
	
	/** 
	 * TODO fix this method that only copy the chunkset
	 * Copy a HashedChunkSet by giving it a new size.
	 * 
	 * @param previousHCS
	 * @param generator
	 * @param maxChunkVal
	 * @param maxChunkVal
	 */
	protected static HashedChunkSet copyChunk(HashedChunkSet previousHCS, ZobristHashGenerator generator, int maxChunkVal, int numChunks)
	{
		HashedChunkSet newHCS = new HashedChunkSet(generator, maxChunkVal, numChunks);
		if (previousHCS != null)
		{
			ChunkSet previousCS = previousHCS.internalState();
			ChunkSet newCS = (ChunkSet) newHCS.internalState();
			TIntArrayList nonzeroChunks = previousCS.getNonzeroChunks();
			for (int prevVal : nonzeroChunks.toArray()) 
				newCS.setChunk(prevVal, previousCS.getChunk(prevVal));
		}
		else
			newHCS = null;
		return newHCS;
	}
	
	 /* Updates the chunks of the first containerStates to include the new 
	 * added sites, following the growth of the board. To precise the new 
	 * playable sites, and the sites that should be empty.
	 * 
	 * what : index of a component at a specific location - 0 if no component
	 * who : index of the owner of a component at a specific location - 0 if no component
	 * count : number of the same component at a specific location
	 * state : value of the state at a specific location
	 * rotation : value for the rotation direction at a specific location (0 for to the first supported direction)
	 * playable : For boardless games, returning if a location is playable (1) or not (0)
	 * 
	 * @param context
	 */
	public static void updateChunks(Context context)
	{
		final Game game = context.game();
		final int numPlayers = game.players().count();
		int numSites = UpdateBoard.newTotalIndexesCells();
		ContainerState[] containerStates = context.state().containerStates();
		
		for (int i=0; i<containerStates.length; i++)
		{
			ContainerState containerState = containerStates[i];
			if (containerState instanceof other.state.container.ContainerFlatState) 
			{				
				ContainerFlatState containerFlatState = (ContainerFlatState) containerState;
				ZobristHashGenerator generator = containerFlatState.getGenerator();
				
				HashedChunkSet who;
				HashedChunkSet what;
				HashedChunkSet count;
				HashedChunkSet state;
				HashedChunkSet rotation;
				HashedChunkSet value;
				HashedBitSet playable;
				Region empty;
				
				// update each container state (container state 0 is the board state)
				if (i == 0) {
					// TODO maybe there is another way to copy a HashedChunkSet
					who = copyChunkWithNewBoardSize(containerFlatState.who(), generator, numPlayers+1, numSites);
					what = copyChunkWithNewBoardSize(containerFlatState.what(), generator, containerFlatState.getMaxWhatVal(), numSites);
					count = copyChunkWithNewBoardSize(containerFlatState.count(), generator, containerFlatState.getMaxWhatVal(), numSites);
					state = copyChunkWithNewBoardSize(containerFlatState.state(), generator, containerFlatState.getMaxStateVal(), numSites);
					rotation = copyChunkWithNewBoardSize(containerFlatState.rotation(), generator, containerFlatState.getMaxRotationVal(), numSites);
					value = copyChunkWithNewBoardSize(containerFlatState.value(), generator, containerFlatState.getMaxPieceValue(), numSites);
					playable = new HashedBitSet(generator, numSites);
					empty = new Region(numSites);
		
					// empty information - place of the board where there is a site but nothing on it
					int[] emptySites = containerFlatState.emptySites().sites();
					int[] newEmptySites;
					if (emptySites.length > 0)
					{
						newEmptySites = new int[emptySites.length + UpdateBoard.surplusInitIndexes().size()];
						int index = 0;
						for (int j=0; j<emptySites.length; j++)
						{
							newEmptySites[index] = UpdateBoard.mappedInitToNewIndexes().get(emptySites[index]);
							index++;
						}
						for (Integer prevVal : UpdateBoard.surplusInitIndexes()) 
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
			        	prevPlayableSites.add(j);
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
						playableBS.flip(UpdateBoard.mappedInitToNewIndexes().get(prevVal));
				}
				else 
				{
					// TODO maybe there is another way to copy a HashedChunkSet
					who = copyChunk(containerFlatState.who(), generator, numPlayers+1, numSites);
					what = copyChunk(containerFlatState.what(), generator, containerFlatState.getMaxWhatVal(), numSites);
					count = copyChunk(containerFlatState.count(), generator, containerFlatState.getMaxWhatVal(), numSites);
					state = copyChunk(containerFlatState.state(), generator, containerFlatState.getMaxStateVal(), numSites);
					rotation = copyChunk(containerFlatState.rotation(), generator, containerFlatState.getMaxRotationVal(), numSites);
					value = copyChunk(containerFlatState.value(), generator, containerFlatState.getMaxPieceValue(), numSites);
					playable = new HashedBitSet(generator, numSites);
					empty = new Region(numSites);
					
					// empty information - place of the board where there is a site but nothing on it
					int[] emptySites = containerFlatState.emptySites().sites();
					int[] newEmptySites;
					if (emptySites.length > 0)
					{
						newEmptySites = new int[emptySites.length + UpdateBoard.surplusIndexes().size()];
						int index = 0;
						for (int j=0; j<emptySites.length; j++)
						{
							newEmptySites[index] = emptySites[index];
							index++;
						}
						for (Integer prevVal : UpdateBoard.surplusIndexes()) 
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
			        	prevPlayableSites.add(j);
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
						playableBS.flip(prevVal);
				}
						        
				
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
	 * Generates the new move based on a move, which means with the new indexes of the board
	 * (which means updating to() and from().
	 * 
	 * @param prevMove previous move to copy, except for the indexes.
	 * @return the new move.
	 */
	public static Move generateNewMove(Move prevMove, boolean isLastMoveDoneOnEdge)
	{		
		List<Action> actions = prevMove.actions();
		if (actions.size() == 1)
		{
			Action newAction = actions.get(0);
			int to = newAction.to();
			int from = newAction.from();
			if (to != Constants.UNDEFINED)
				newAction.setTo(UpdateBoard.mappedPrevToNewIndexes().get(to));
			if (from != Constants.UNDEFINED)
				newAction.setFrom(UpdateBoard.mappedPrevToNewIndexes().get(from));
			
			prevMove.setTo(prevMove.to());
			prevMove.setFrom(prevMove.from());
		}
		else 
		{
			List<Action> newActions = new ArrayList<Action>();
			for (int j=0; j<actions.size(); j++) 
			{
				Action action = actions.get(j);
				int to = action.to();
				int from = action.from();
				if (to != Constants.UNDEFINED)
					action.setTo(UpdateBoard.mappedPrevToNewIndexes().get(to));
				if (from != Constants.UNDEFINED)
					action.setFrom(UpdateBoard.mappedPrevToNewIndexes().get(from));
				
				newActions.add(action);
			}
			
			// problem : this constructor does init the 'form' and 'to' of the Move, which are used to generate legal moves
			for (final Action a : actions)
				if (a.isDecision())
				{
					prevMove.setTo(a.to());
					prevMove.setFrom(a.from());
					break;
				}
		}

		if (isLastMoveDoneOnEdge)
			prevMove.setOnEdge(1);
		return prevMove;
	}
	
	/** 
	 * Replays the moves mapped to the proper new component (like tile or hand) index.
	 * 
	 * @param context
	 * @param movesDone
	 */
	protected static void replayMoves(Context context, List<Move> movesDone)
	{
		Move move = null;
		int numInitialPlacementMoves = context.trial().numInitialPlacementMoves();
		
		for (int i = 0; i < movesDone.size(); i++)
		{
			move = movesDone.get(i);	
			generateNewMove(move, i == movesDone.size()-1);
			
			if (i>=numInitialPlacementMoves)
				context.game().apply(context, move);
		}
	}
	
	/**
	 * Update the Owned (contains information about where the pieces are (indexes) for 
	 * a specific player / board).  Does it by updating Owned at once by mapping previous 
	 * indexes to new ones.
	 * 
	 * PROBLEM : this method is called before re-applying moves. When re-applying moves, 
	 * it is (highly) likely that the same indexes will be added to Owned, so we are going 
	 * to have duplicates.
	 * 
	 * @param context
	 */
	protected static void updateOwned(Context context)
	{	
		FlatCellOnlyOwned owned = (FlatCellOnlyOwned) context.state().owned();
		FastTIntArrayList[][] locations = owned.locations();
		
		for (int i=0; i<locations.length; i++)
			for (int j=0; j<locations[i].length; j++)
			{
				FastTIntArrayList newFastTIntArrayList = new FastTIntArrayList();
				for (int k=0; k<locations[i][j].size(); k++)
					if (UpdateBoard.mappedInitToNewIndexes().containsKey(locations[i][j].get(k)))
						newFastTIntArrayList.add(UpdateBoard.mappedInitToNewIndexes().get(locations[i][j].get(k)));
				locations[i][j] = newFastTIntArrayList;
			}
	}
	
	/**
	 * Update the Owned (contains information about where the pieces are (indexes) for 
	 * a specific player / board).  Does it by reseting the Owned structure, as moves, 
	 * when being re-apply, will add proper information to the structure.
	 * 
	 * PROBLEM : When re-creating moves with the proper index for the new board size, 
	 * I don't really re-create Move, but I update the previous ones. And Add Move does'nt 
	 * re-add data in the Owned, there is a code preventing it, it is only done at 
	 * initialization of the Object, and it is only initialize once in my current implementation.
	 * 
	 * @param context
	 */
	protected static void resetOwned(Context context) 
	{
		context.state().setOwned(new FlatCellOnlyOwned(context.game())); 
	}
	
	/** 
	 * Start over the game on the new board and apply the historic of move mapped to the new board.
	 * 
	 * @param app
	 * @param movesDone
	 * @param legalMoves
	 * @param replayMoves
	 */
	protected static void remakeTrial(Context context, List<Move> movesDone, Moves legalMoves, final boolean replayMoves) 
	{
		updateChunks(context);
		updateOwned(context);
		
		if (replayMoves)
			replayMoves(context, movesDone);
	}
	
	/**
	 * Reset the state, which also reset the mover.
	 * 
	 * @param context
	 */
	protected static void resetState(final Context context)
	{	
		context.state().reset(context.game());
	}
	
	/** 
	 * Cancel all the moves from the beginning, to have a fresh base with an empty board.
	 */
	protected static void resetMoves(Context context)
	{
		// TODO - how reset moves properly? problem with legal moves when doing this
		context.reset();
		context.state().initialise(context.currentInstanceContext().game());
		context.trial().setStatus(null);
		
		resetState(context);
	}
	
	/** 
	 * Start over the game on the new board and apply the historic of move mapped to the new board.
	 * 
	 * @param app
	 */
	protected static void remakeTrial(Context context, final boolean replayMoves) 
	{
		Trial trial = context.trial();
		List<Move> movesDone = trial.generateCompleteMovesList();
		Moves legalMoves = trial.cachedLegalMoves();
		if (replayMoves)
			resetMoves(context);
		remakeTrial(context, movesDone, legalMoves, replayMoves);
	}
	
	/**
	 * Updates board by making it grow logically.
	 * 
	 * @param context
	 * @param boardSizeChange Determines how the board size should be adjusted based on the last move.
	 * -2: Reset the board to its initial size ; -1: Reduce the board size based on last move ;
	 *  0: Keep the board at its current size ; 1: Expand the board size based on the last move.
	 * @param replayMove TODO
	 */
	public static void updateBoard(Context context, Move move, int boardSizeChange, final boolean replayMoves)
	{
		Game game = context.game();
		Boardless board = (Boardless) game.board();
		
		// TODO check that the move is applied on a board type container
		// update dimensions only if board change size
		updateBoardDimensions(context, board, boardSizeChange, move);
		remakeTrial(context, replayMoves);
	}
	
	/** 
	 * Check if the move was made on a boardless board and on one edge of the board. 
	 * If so, update the size of the  board.
	 * 
	 * @param context
	 * @param move
	 * @param boardSizeChange Determines how the board size should be adjusted based on the last move.
	 * -2: Reset the board to its initial size ; -1: Reduce the board size based on last move ;
	 *  0: Keep the board at its current size ; 1: Expand the board size based on the last move.
	 */
	public static void checkMoveImpactOnBoard(Context context, Move move, final int boardSizeChange, final boolean replayMoves)
	{
		if (context.game().isBoardless()) 
		{
			List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());			
			if (isTouchingEdge(perimeter, move.to())) 
			{
				updateBoard(context, move, boardSizeChange, replayMoves);
			}
		}
	}
}
