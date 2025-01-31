package app.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import game.Game;
import game.equipment.component.Component;
import game.equipment.container.board.Boardless;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.ChunkSet;
import main.collections.FastTIntArrayList;
import other.action.Action;
import other.action.ActionType;
import other.context.Context;
import other.move.Move;
import other.move.MoveSequence;
import other.state.container.ContainerFlatState;
import other.state.container.ContainerState;
import other.state.owned.FlatCellOnlyOwned;
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
	private static HashSet<Integer> surplusIndexes; // indexes that are being added from one board to another

	private static int initDimensionBoard;
	private static int initAreaBoard;
	private static int initTotalIndexes;
	private static int diffInit;
	private static int prevDimensionBoard;
	private static int prevAreaBoard;
	private static int prevTotalIndexes;
	private static int newDimensionBoard;
	private static int newAreaBoard;
	private static int newTotalIndexes;
	private static int diff;
	
	
	private static HashMap<Integer, Integer> mappedInitToNewIndexes;
	private static HashMap<Integer, Integer> mappedNewToInitIndexes;
	private static HashSet<Integer> surplusInitIndexes; // indexes that are being added from init board to new one
	
	//--------------------------------Getters----------------------------------
	
	public static HashMap<Integer, Integer> mappedPrevToNewIndexes()
	{
		return mappedPrevToNewIndexes;
	}
	
	public static HashMap<Integer, Integer> mappedNewToPrevIndexes()
	{
		return mappedNewToPrevIndexes;
	}
	
	public static HashSet<Integer> surplusIndexes()
	{
		return surplusIndexes;
	}
	
	public static HashMap<Integer, Integer> mappedInitToNewIndexes()
	{
		return mappedInitToNewIndexes;
	}
	
	public static HashMap<Integer, Integer> mappedNewToInitIndexes()
	{
		return mappedNewToInitIndexes;
	}
	
	public static HashSet<Integer> surplusInitIndexes()
	{
		return surplusInitIndexes;
	}
	
	public static int prevDimensionBoard()
	{
		return prevDimensionBoard;
	}
	
	public static int prevAreaBoard()
	{
		return prevAreaBoard;
	}
	
	public static int prevTotalIndexes()
	{
		return prevTotalIndexes;
	}
	
	public static int newDimensionBoard()
	{
		return newDimensionBoard;
	}
	
	public static int newAreaBoard()
	{
		return newAreaBoard;
	}
	
	public static int newTotalIndexes()
	{
		return newTotalIndexes;
	}
	
	public static int diff()
	{
		return diff;
	}
	
	public static int initDimensionBoard()
	{
		return initDimensionBoard;
	}
	
	public static int initAreaBoard()
	{
		return initAreaBoard;
	}
	
	public static int initTotalIndexes()
	{
		return initTotalIndexes;
	}
	
	public static int diffInit()
	{
		return diffInit;
	}
	
	//----------------------------Initialization-------------------------------

	protected static void initInitConstants(Context context, int initDimension)
	{
		initDimensionBoard = initDimension;
		initAreaBoard = (int) Math.pow(initDimensionBoard(), 2);
		initTotalIndexes = initAreaBoard()+context.sitesFrom().length-1;
	}
	
	/** 
	 * Initializes data structures to map the previous indexes to the new indexes, 
	 * due to the change in board size, such as :
	 * mappedPrevToNewIndexes : mapping giving the previous index as key and the new index as value.
	 * mappedNewToPrevIndexes : mapping giving the new index as key and the previous index as value.
	 * newAddedIndexes : new indexes that don't have a mapping to the previous plate as they are new existing sites.
	 */
	protected static void initMappingIndexes1()
	{
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>();		
		
		if (prevDimensionBoard() < newDimensionBoard()) 
		{
			int inter;
			int newIndex;
			for (int prevIndex = 0; prevIndex < prevTotalIndexes(); prevIndex++)
			{
				inter = prevIndex / prevDimensionBoard();
				if (prevIndex < prevAreaBoard())
				{
					newIndex = prevIndex + prevDimensionBoard() + Constants.GROWING_STEP + 1 + (2 * (inter));
				}
				else
				{
					newIndex = prevIndex + diff;
				}
				mappedPrevToNewIndexes().put(prevIndex, newIndex);
				mappedNewToPrevIndexes().put(newIndex, prevIndex);
			}
			for (int i = 0; i < newTotalIndexes(); i++)
			{
				if (!mappedNewToPrevIndexes().containsKey(i))
				{
					surplusIndexes().add(i);
				}
			}
		}
		else
		{
			int inter;
			int newIndex;
			for (int prevIndex = 0; prevIndex < newTotalIndexes(); prevIndex++)
			{
				inter = prevIndex / newDimensionBoard();
				if (prevIndex < newAreaBoard())
				{
					newIndex = prevIndex + newDimensionBoard() + Constants.GROWING_STEP + 1 + (2 * (inter));
				}
				else
				{
					newIndex = prevIndex + diff;
				}
				mappedNewToPrevIndexes().put(prevIndex, newIndex);
				mappedPrevToNewIndexes().put(newIndex, prevIndex);
			}
			for (int i = 0; i < prevTotalIndexes(); i++)
			{
				if (!mappedPrevToNewIndexes().containsKey(i))
				{
					surplusIndexes().add(i);
				}
			}
		}
	}
	
	/** 
	 * Initializes data structures to map the previous indexes to the new indexes, 
	 * due to the change in board size, such as :
	 * mappedPrevToNewIndexes : mapping giving the previous index as key and the new index as value.
	 * mappedNewToPrevIndexes : mapping giving the new index as key and the previous index as value.
	 * newAddedIndexes : new indexes that don't have a mapping to the previous plate as they are new existing sites.
	 */
	protected static void initMappingIndexes()
	{
		// data structures to map between current plate and new plate
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>();		
		
		if (prevDimensionBoard() < newDimensionBoard()) 
		{
			int inter;
			int newIndex;
			for (int prevIndex = 0; prevIndex < prevTotalIndexes(); prevIndex++)
			{
				inter = prevIndex / prevDimensionBoard();
				if (prevIndex < prevAreaBoard())
					newIndex = prevIndex + prevDimensionBoard() + Constants.GROWING_STEP + 1 + (2 * (inter));
				else
					newIndex = prevIndex + diff();
				mappedPrevToNewIndexes().put(prevIndex, newIndex);
				mappedNewToPrevIndexes().put(newIndex, prevIndex);
			}
			for (int i = 0; i < newTotalIndexes(); i++)
				if (!mappedNewToPrevIndexes().containsKey(i))
					surplusIndexes().add(i);
		}
		else if (prevDimensionBoard() == newDimensionBoard())
		{
			for (int prevIndex = 0; prevIndex < newTotalIndexes(); prevIndex++)
			{
			    mappedNewToPrevIndexes().put(prevIndex, prevIndex);
				mappedPrevToNewIndexes().put(prevIndex, prevIndex);
			}
		}
		else
		{
			int newIndex;
			for (int prevIndex = 0; prevIndex < newTotalIndexes(); prevIndex++)
			{
				if (prevIndex < newAreaBoard()) {
					int col = prevIndex%newDimensionBoard();
					int line = prevIndex/newDimensionBoard();
					int offset = (prevDimensionBoard()-newDimensionBoard())/2;

					int mCol = col + offset;
					int mLine = line + offset;
					newIndex = mLine * prevDimensionBoard() + mCol;
				}
				else
					newIndex = prevIndex + diff();
				mappedNewToPrevIndexes().put(prevIndex, newIndex);
				mappedPrevToNewIndexes().put(newIndex, prevIndex);
			}
			for (int i = 0; i < prevTotalIndexes(); i++)
				if (!mappedPrevToNewIndexes().containsKey(i))
					surplusIndexes().add(i);
		}
		
		// data structures to map between initial plate and new plate
		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		surplusInitIndexes = new HashSet<Integer>();
		
		int newIndex;
		for (int prevIndex = 0; prevIndex < initTotalIndexes(); prevIndex++)
		{
			if (prevIndex < initAreaBoard())
			{
				int col = prevIndex%initDimensionBoard();
				int line = prevIndex/initDimensionBoard();
				int offset = (newDimensionBoard()-initDimensionBoard())/2;

				int mCol = col + offset;
				int mLine = line + offset;
				newIndex = mLine * newDimensionBoard() + mCol;
			}
			else
				newIndex = prevIndex + diffInit();
			mappedInitToNewIndexes().put(prevIndex, newIndex);
			mappedNewToInitIndexes().put(newIndex, prevIndex);
		}
		for (int i = 0; i < newTotalIndexes(); i++)
			if (!mappedNewToInitIndexes().containsKey(i))
				surplusInitIndexes().add(i);
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
	 * @param futureDimensionBoard dimension of the new board (size of one side of the board).
	 */
	public static void initMainConstants(Context context, int currDimensionBoard, int futureDimensionBoard) {
		
		prevDimensionBoard = currDimensionBoard;
		prevAreaBoard = (int) Math.pow(prevDimensionBoard(), 2);
		prevTotalIndexes = prevAreaBoard()+context.sitesFrom().length-1;
		
		newDimensionBoard = futureDimensionBoard; //prevDimensionBoard() + Constants.GROWING_STEP;
		newAreaBoard = (int) Math.pow(newDimensionBoard(), 2);
		newTotalIndexes = newAreaBoard()+context.sitesFrom().length-1;
		
		if (prevAreaBoard() < newAreaBoard())
			diff = newAreaBoard() - prevAreaBoard();
		else
			diff = prevAreaBoard() - newAreaBoard();

		if (initAreaBoard() < newAreaBoard())
			diffInit = newAreaBoard() - initAreaBoard();
		else
			diffInit = initAreaBoard() - newAreaBoard();
		
		if (initDimensionBoard() == 0)
			initInitConstants(context, currDimensionBoard);
		
		initMappingIndexes();
		
		/*System.out.println("GrowingBoard.java initMainConstants() mappedNewToPrevIndexes() : "+mappedNewToPrevIndexes());
		System.out.println("GrowingBoard.java initMainConstants() mappedPrevToNewIndexes() : "+mappedPrevToNewIndexes());
		System.out.println("GrowingBoard.java initMainConstants() surplusIndexes() : "+surplusIndexes());
		System.out.println("GrowingBoard.java initMainConstants() prevDimensionBoard() : "+prevDimensionBoard());
		System.out.println("GrowingBoard.java initMainConstants() prevAreaBoard() : "+prevAreaBoard());
		System.out.println("GrowingBoard.java initMainConstants() prevTotalIndexes() : "+prevTotalIndexes());
		System.out.println("GrowingBoard.java initMainConstants() newDimensionBoard() : "+newDimensionBoard());
		System.out.println("GrowingBoard.java initMainConstants() newAreaBoard() : "+newAreaBoard());
		System.out.println("GrowingBoard.java initMainConstants() newTotalIndexes() : "+newTotalIndexes());
		System.out.println("GrowingBoard.java initMainConstants() diff() : "+diff());
		System.out.println("GrowingBoard.java initMainConstants() initDimensionBoard() : "+initDimensionBoard());
		System.out.println("GrowingBoard.java initMainConstants() initAreaBoard() : "+initAreaBoard());
		System.out.println("GrowingBoard.java initMainConstants() initTotalIndexes() : "+initTotalIndexes());
		System.out.println("GrowingBoard.java initMainConstants() diffInit() : "+diffInit());
		System.out.println("GrowingBoard.java initMainConstants() mappedNewToInitIndexes() : "+mappedNewToInitIndexes());
		System.out.println("GrowingBoard.java initMainConstants() mappedInitToNewIndexes() : "+mappedInitToNewIndexes());
		System.out.println("GrowingBoard.java initMainConstants() surplusInitIndexes() : "+surplusInitIndexes());*/
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Updates the content of the Topology to match the new board.
	 * Re-launches pre-computations.
	 * 
	 * @param context
	 */
	protected static void updateTopology(Context context)
	{		
		/*System.out.println("GrowingBoard.java updateTopology() regions : "+Arrays.toString(game.equipment().regions()));
		System.out.println("GrowingBoard.java updateTopology() containers : "+Arrays.toString(game.equipment().containers()));
		System.out.println("GrowingBoard.java updateTopology() components : "+Arrays.toString(game.equipment().components()));
		System.out.println("GrowingBoard.java updateTopology() maps : "+Arrays.toString(game.equipment().maps()));
		System.out.println("GrowingBoard.java updateTopology() totalDefaultSites : "+game.equipment().totalDefaultSites());
		System.out.println("GrowingBoard.java updateTopology() containerId : "+Arrays.toString(game.equipment().containerId()));
		System.out.println("GrowingBoard.java updateTopology() offset : "+Arrays.toString(game.equipment().offset()));
		System.out.println("GrowingBoard.java updateTopology() sitesFrom : "+Arrays.toString(game.equipment().sitesFrom()));
		//System.out.println("GrowingBoard.java updateTopology() vertexWithHints : "+Arrays.toString(game.equipment().vertexWithHints()));
		//System.out.println("GrowingBoard.java updateTopology() cellWithHints : "+Arrays.toString(game.equipment().cellWithHints()));
		//System.out.println("GrowingBoard.java updateTopology() edgeWithHints : "+Arrays.toString(game.equipment().edgeWithHints()));
		System.out.println("GrowingBoard.java updateTopology() vertexHints : "+Arrays.toString(game.equipment().vertexHints()));
		System.out.println("GrowingBoard.java updateTopology() cellHints : "+Arrays.toString(game.equipment().cellHints()));
		System.out.println("GrowingBoard.java updateTopology() edgeHints : "+Arrays.toString(game.equipment().edgeHints()));
		System.out.println("GrowingBoard.java updateTopology() itemsToCreate : "+Arrays.toString(game.equipment().itemsToCreate()));*/
		context.game().update();
	}
	
	/** 
	 * Updates the board dimensions.
	 * 
	 * @param board
	 * @param newSize new size of the board.
	 */
	protected static void updateBoardDimensions(Context context, Boardless board, int newSize) 
	{
		System.out.println("GrowingBoard.java updateBoardDimensions() curr size : "+board.dimension()+ " - new size : "+newSize);
		GraphFunction newGraphFunction = new RectangleOnSquare(new DimConstant(newSize), null, null, null);
		board.setGraphFunction(newGraphFunction);
		
		board.setDimension(newSize);
		
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
	 * @param mappedPrevToNewIndexes
	 */
	protected static HashedChunkSet copyChunkWithNewBoardSize(HashedChunkSet previousHCS, ZobristHashGenerator generator, int maxChunkVal, int numChunks, HashMap<Integer, Integer> mappedPrevToNewIndexes2)
	{
		HashedChunkSet newHCS = new HashedChunkSet(generator, maxChunkVal, numChunks);
		if (previousHCS != null)
		{
			ChunkSet previousCS = previousHCS.internalState();
			ChunkSet newCS = (ChunkSet) newHCS.internalState();
			TIntArrayList nonzeroChunks = previousCS.getNonzeroChunks();
			for (int prevVal : nonzeroChunks.toArray()) { 
				newCS.setChunk(mappedInitToNewIndexes().get(prevVal), previousCS.getChunk(prevVal));
			}
		}
		else
			newHCS = null;
		return newHCS;
	}
	
	/** 
	 * TODO fix this method that only copy the chunkset
	 * Copy a HashedChunkSet by mapping the index to the index of the new board, 
	 * whose sized has changed compared to the previous one.
	 * 
	 * @param previousHCS
	 * @param generator
	 * @param maxChunkVal
	 * @param maxChunkVal
	 * @param mappedPrevToNewIndexes
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
	
	 /* Updates the chunks of the first containerStates (container state of the board ?) 
	 * to include the new added sites, following the growth of the board.
	 * To precise the new playable sites, and the sites that should be empty.
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
	public static void updateChunks(Context context, int prevDim, int newDim)
	{
		final Game game = context.game();
		final int numPlayers = game.players().count();
		
		int numSites = mappedNewToInitIndexes().size()+surplusInitIndexes().size();
		ContainerState[] containerStates = context.state().containerStates();

		// update each container state (container state 0 is the board state)
		ContainerState containerState = containerStates[0];
		if (containerState instanceof other.state.container.ContainerFlatState) 
		{				
			ContainerFlatState containerFlatState = (ContainerFlatState) containerState;
			ZobristHashGenerator generator = containerFlatState.getGenerator();
			
			// TODO maybe there is another way to copy a HashedChunkSet
			HashedChunkSet who = copyChunkWithNewBoardSize(containerFlatState.who(), generator, numPlayers+1, numSites, mappedInitToNewIndexes());
			HashedChunkSet what = copyChunkWithNewBoardSize(containerFlatState.what(), generator, containerFlatState.getMaxWhatVal(), numSites, mappedInitToNewIndexes());
			HashedChunkSet count = new HashedChunkSet(generator, containerFlatState.getMaxWhatVal(), numSites);//copyChunkWithNewBoardSize(containerFlatState.count(), generator, containerFlatState.getMaxCountVal(), numSites);
			HashedChunkSet state = copyChunkWithNewBoardSize(containerFlatState.state(), generator, containerFlatState.getMaxStateVal(), numSites, mappedInitToNewIndexes());
			HashedChunkSet rotation = copyChunkWithNewBoardSize(containerFlatState.rotation(), generator, containerFlatState.getMaxRotationVal(), numSites, mappedInitToNewIndexes());
			HashedChunkSet value = copyChunkWithNewBoardSize(containerFlatState.value(), generator, containerFlatState.getMaxPieceValue(), numSites, mappedInitToNewIndexes());
			HashedBitSet playable = new HashedBitSet(generator, numSites);
			Region empty = new Region(numSites);

			// empty information - place of the board where there is a site but nothing on it
			int[] emptySites = containerFlatState.emptySites().sites();
			int[] newEmptySites;
			if (emptySites.length > 0)
			{
				newEmptySites = new int[emptySites.length + surplusInitIndexes().size()];
				int index = 0;
				for (int j=0; j<emptySites.length; j++)
				{
					newEmptySites[index] = mappedInitToNewIndexes().get(emptySites[index]);
					index++;
				}
				for (Integer prevVal : surplusInitIndexes()) 
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
				playableBS.flip(mappedInitToNewIndexes().get(prevVal));
					        
			
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
			context.state().setContainerStates(0, newContainerFlatState);
		}
		else 
			throw new UnsupportedOperationException("Type " +containerStates[0].getClass().getName() + " not implement regarding growing state of the board.");
		
		
		for (int i=1; i<containerStates.length; i++)
		{	
			ContainerState containerStateI = containerStates[i];
			if (containerStateI instanceof other.state.container.ContainerFlatState) 
			{				
				ContainerFlatState containerFlatState = (ContainerFlatState) containerStateI;
				ZobristHashGenerator generator = containerFlatState.getGenerator();
				
				// TODO maybe there is another way to copy a HashedChunkSet
				HashedChunkSet who = copyChunk(containerFlatState.who(), generator, numPlayers+1, numSites);
				HashedChunkSet what = copyChunk(containerFlatState.what(), generator, containerFlatState.getMaxWhatVal(), numSites);
				HashedChunkSet count = new HashedChunkSet(generator, containerFlatState.getMaxWhatVal(), numSites); //copyChunkWithNewBoardSize2(containerFlatState.count(), generator, containerFlatState.getMaxCountVal(), numSites);
				HashedChunkSet state = copyChunk(containerFlatState.state(), generator, containerFlatState.getMaxStateVal(), numSites);
				HashedChunkSet rotation = copyChunk(containerFlatState.rotation(), generator, containerFlatState.getMaxRotationVal(), numSites);
				HashedChunkSet value = copyChunk(containerFlatState.value(), generator, containerFlatState.getMaxPieceValue(), numSites);
				HashedBitSet playable = new HashedBitSet(generator, numSites);
				Region empty = new Region(numSites);
				
				// empty information - ?
				int[] emptySites = containerFlatState.emptySites().sites();
				int[] newEmptySites;
				if (emptySites.length > 0)
				{
					newEmptySites = new int[emptySites.length + surplusIndexes().size()];
					int index = 0;
					for (int j=0; j<emptySites.length; j++)
					{
						newEmptySites[index] = emptySites[index];
						index++;
					}
					for (Integer prevVal : surplusIndexes()) 
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
			if (to != Constants.UNDEFINED) {
				newAction.setTo(mappedPrevToNewIndexes().get(to));
			}
			if (from != Constants.UNDEFINED)
				newAction.setFrom(mappedPrevToNewIndexes().get(from));
			
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
					action.setTo(mappedPrevToNewIndexes().get(to));
				if (from != Constants.UNDEFINED)
					action.setFrom(mappedPrevToNewIndexes().get(from));
				
				newActions.add(action);
			}
			
			// problem : this constructor does init the 'form' and 'to' of the Move, which are used to generate legal moves
			for (final Action a : actions)
				if (a.isDecision())
				{
					prevMove.setTo(mappedPrevToNewIndexes().get(a.to()));
					prevMove.setFrom(mappedPrevToNewIndexes().get(a.from()));
					break;
				}
		}

		if (isLastMoveDoneOnEdge)
			prevMove.setOnEdge(prevDimensionBoard());
		return prevMove;
	}
	
	/**
	 * Update the Owned (contains information about where the pieces are (indexes) for 
	 * a specific player / board).  Does it by updating Owned each time a move is 
	 * re-applied based on the new board, by removing previous index, and if needed 
	 * adding new index.
	 * 
	 * PROBLEM : the Owned structure does not restart to initial structure, so at the 
	 * first Move, I am already working with a structure that expect me to be on the 
	 * last Move. It creates inconsistencies.
	 * e.g. : if the player has a hand of 3 items, once his hand is empty, the index of
	 * his hand will no longer be linked to anything and will therefore no longer exist 
	 * in the Owned.
	 * 
	 * @param context
	 * @param move move that have just been re-apply.
	 * @param i position of the move in the list of move applied from the beginning.
	 * @param numInitialPlacementMoves number of initial placement moves.
	 */
	protected static void updateOwnedWithMove(Context context, Move move, int i, int numInitialPlacementMoves)
	{
		int to = move.to();
		int from = move.from();
		ActionType actionType = move.actionType();
		int what = move.what();
		
		System.out.println("GrowingBoard.java updateOwned() move : "+move);

		if (i < numInitialPlacementMoves)
		{
			for (final Action a : move.actions()) 
			{
				if (a.to() != Constants.UNDEFINED) 
				{
					to = a.to();
					break;
				}
			}
			for (final Action a : move.actions()) 
			{
				if (a.what() != 0) 
				{
					what = a.what();
					break;
				}
			}
			for (final Action a : move.actions()) 
			{
				if (a.actionType() != null) 
				{
					actionType = a.actionType();
					break;
				}
			}
		}
		System.out.println("GrowingBoard.java updateOwned() mappedPrevToNewIndexes : "+mappedPrevToNewIndexes);
		System.out.println(i+" - GrowingBoard.java updateOwned() before type : "+move.getClass().getName()+" - move : "+actionType+" - to : "+to+" - from : "+from+" - what : "+what+" - mapped to : "+mappedNewToPrevIndexes().get(to));
		
		if (actionType == ActionType.Add)
		{
			int containerId = context.containerId()[mappedPrevToNewIndexes().get(to)];
			Component piece = context.components()[what];
			final int owner = piece.owner();
			
			System.out.println("GrowingBoard.java updateOwned() ADD before sites : "+context.state().owned().sites(owner));
			int beforeSize = context.state().owned().sites(owner).size();

			context.state().owned().remove(owner, what, to, SiteType.Cell);
			
			int currSize = context.state().owned().sites(owner).size();
			System.out.println("GrowingBoard.java updateOwned() ADD afterr sites : "+context.state().owned().sites(owner));
			if (currSize < beforeSize && !context.state().owned().sites(owner).contains(mappedPrevToNewIndexes().get(to))) 
			{
				context.state().owned().add(owner, what, mappedPrevToNewIndexes().get(to), SiteType.Cell);
				System.out.println(i+" - GrowingBoard.java updateOwned() ADD add : "+mappedPrevToNewIndexes().get(to)+" - containerId to : "+context.containerId()[to]+ "- to owner : "+owner +" - cc : "+((FlatCellOnlyOwned) context.state().owned()).sites(owner)+" - curr size : "+newDimensionBoard());
			}
		}
		else if (actionType == ActionType.Move)
		{
			int containerId = context.containerId()[mappedPrevToNewIndexes().get(from)];
			System.out.println(i+" - GrowingBoard.java updateOwned() MOVE what before : "+what);
			if (containerId > 0)
			{
				what = context.state().containerStates()[containerId].what(mappedPrevToNewIndexes().get(from), SiteType.Cell);
				//what2 = context.state().containerStates()[containerId].what(move.from() - (prevAreaBoard - 1 + containerId), SiteType.Cell);
			}
			else
				what = context.state().containerStates()[containerId].what(from, SiteType.Cell);
			System.out.println(i+" - GrowingBoard.java updateOwned() MOVE what afterr : "+what);
			Component piece = context.components()[what];
			int owner = piece.owner();
			//owner = containerId;
			System.out.println(i+" - GrowingBoard.java updateOwned() MOVE containerId : "+containerId+" - owner : "+owner);
			
			context.state().owned().remove(owner, what, to, SiteType.Cell);
			System.out.println(i+" - GrowingBoard.java updateOwned() MOVE removed : "+to+" - of owner : "+owner +" - sites: "+((FlatCellOnlyOwned) context.state().owned()).sites(owner));
		
		}

		System.out.println(i+" - GrowingBoard.java updateOwned() afterr type : "+move.getClass().getName()+" - move : "+actionType+" - to : "+to+" - from : "+from+" - what : "+what);
				
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
		int mover = context.state().mover();
		int next = context.nextTo((context.state().mover()) % context.game().players().count() + 1);
		int numInitialPlacementMoves = context.trial().numInitialPlacementMoves();
		
		for (int i = 0; i < movesDone.size(); i++)
		{
			if (i == numInitialPlacementMoves)
			{
				context.state().setMover(mover);
				context.state().setNext(next);
			}
			
			move = movesDone.get(i);	
			generateNewMove(move, i == movesDone.size()-1);
			
			context.game().apply(context, move);
		}
		//context.game().moves(context); // help to initialize trial.cachedLegalMoves();
	}
	
	/**
	 * Generates the new legal moves based on the new indexes.
	 * 
	 * @param context
	 * @param legaleMoves previous list of legal moves, before the board changed size.
	 */
	protected static void generateLegalMoves(Context context, Moves legaleMoves)
	{
		/*Moves newLegaleMoves = new BaseMoves(null);
		for (Move prevLegalMove : legaleMoves.moves())
		{
			Move newMove = generateNewMove(prevLegalMove);
			newLegaleMoves.moves().add(newMove);
		}
		
		context.trial().setLegalMoves(newLegaleMoves, context);*/
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
					if (mappedInitToNewIndexes().containsKey(locations[i][j].get(k)))
						newFastTIntArrayList.add(mappedInitToNewIndexes().get(locations[i][j].get(k)));
				locations[i][j] = newFastTIntArrayList;
			}
	}
	
	/**
	 * Remove duplicates indexes from Owned.
	 * Created because of the problem of the updateOwned() method.
	 * 
	 * @param context
	 */
	public static void removeDuplicateInOwned(Context context)
	{
		FlatCellOnlyOwned owned = (FlatCellOnlyOwned) context.state().owned();
		FastTIntArrayList[][] locations = owned.locations();
		
		for (int i=0; i<locations.length; i++)
			for (int j=0; j<locations[i].length; j++)
			{
				FastTIntArrayList newFastTIntArrayList = new FastTIntArrayList();
				for (int k=0; k<locations[i][j].size(); k++)
				{
					int value = locations[i][j].get(k);
					if(!newFastTIntArrayList.contains(value))
						newFastTIntArrayList.add(value);
				}
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
	 * @param replayMoves
	 */
	protected static void remakeTrial(Context context, List<Move> movesDone, Moves legalMoves, final boolean replayMoves) 
	{
		
		updateChunks(context, prevDimensionBoard(), newDimensionBoard());
		updateOwned(context);
		
		if (replayMoves)
			replayMoves(context, movesDone);
		removeDuplicateInOwned(context);
		generateLegalMoves(context, legalMoves);
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
		//resetMoves(app);
		context.reset();
		context.state().initialise(context.currentInstanceContext().game());
		context.trial().setStatus(null);
		
		context.trial().setMoves(new MoveSequence(null), context.trial().numInitialPlacementMoves());
		//resetMoves(app);
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
		//int mover = context.state().mover();
		if (replayMoves)
			resetMoves(context);
		remakeTrial(context, movesDone, legalMoves, replayMoves);
		//context.state().setMover(mover);
	}
	
	/**
	 * Updates board by making it grow logically.
	 * 
	 * @param context
	 * @param fromSize TODO
	 * @param toSize TODO
	 * @param replayMove TODO
	 */
	public static void updateBoard(Context context, int fromSize, int toSize, final boolean replayMoves)
	{
		Game game = context.game();
		Boardless board = (Boardless) game.board();
		initMainConstants(context, fromSize, toSize);
		
		// TODO check that the move is applied on a board type container
		updateBoardDimensions(context, board, toSize);
		remakeTrial(context, replayMoves);
	}
	
	/** 
	 * Check if the move was made on a boardless board and on one edge of the board. 
	 * If so, update the size of the  board.
	 * 
	 * @param context
	 * @param move
	 * @param fromSize TODO
	 * @param toSize TODO
	 */
	public static void checkMoveImpactOnBoard(Context context, Move move, final int fromSize, final int toSize, final boolean replayMoves)
	{
		if (context.game().isBoardless()) 
		{
			List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());			
			if (isTouchingEdge(perimeter, move.to())) 
			{
				updateBoard(context, fromSize, toSize, replayMoves);
			}
		}
	}
}
