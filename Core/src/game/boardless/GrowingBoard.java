package game.boardless;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import game.Game;
import game.equipment.container.board.Boardless;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.hex.HexagonOnHex;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.functions.graph.generators.basis.tri.TriangleOnTri;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.board.TilingBoardlessType;
import game.util.equipment.Region;
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
		
	// Remember information about the initial plate in case of hexagonal board
	private static HashMap<Integer, Integer> initMaxPerColRow;
	private static HashMap<Integer, Integer> initMinPerColRow;
	private static ArrayList<Integer> initRowsSizeCumul;

	// Remember information about the initial plate in case of triangular board
	private static ArrayList<Integer> initMinColPerRowList;
	private static ArrayList<Integer> initRowsSizeCumulTriangular;
	private static int initMaxIndexRowOrCol;
	private static HashMap<Integer, Integer> initMinColPerRow;
	private static HashMap<Integer, Integer> initMaxColPerRow;
	private static HashMap<Integer, Integer> initMaxRowPerCol;
	
	public static List<TopologyElement> perimeter; // perimeter of the current board
	public static boolean isVisual; // is the app used through a visual like PlayerDesktop
	
	public static List<Move> movesDone; // the last moves done
	
	private static int nbInitialTiles = -1;
	//--------------------------------Getters----------------------------------
	
	/**
	 * @return mappedPrevToNewIndexes
	 */
	public static HashMap<Integer, Integer> mappedPrevToNewIndexes()
	{
		return mappedPrevToNewIndexes;
	}
	
	/**
	 * @return mappedNewToPrevIndexes
	 */
	public static HashMap<Integer, Integer> mappedNewToPrevIndexes()
	{
		return mappedNewToPrevIndexes;
	}
	
	/**
	 * @return surplusIndexes
	 */
	public static HashSet<Integer> surplusIndexes()
	{
		return surplusIndexes;
	}
	
	/**
	 * @return mappedInitToNewIndexes
	 */
	public static HashMap<Integer, Integer> mappedInitToNewIndexes()
	{
		return mappedInitToNewIndexes;
	}
	
	/**
	 * @return mappedNewToInitIndexes
	 */
	public static HashMap<Integer, Integer> mappedNewToInitIndexes()
	{
		return mappedNewToInitIndexes;
	}
	
	/**
	 * @return surplusInitIndexes
	 */
	public static HashSet<Integer> surplusInitIndexes()
	{
		return surplusInitIndexes;
	}
	
	/**
	 * @return prevDimensionBoard
	 */
	public static int prevDimensionBoard()
	{
		return prevDimensionBoard;
	}
	
	/**
	 * @return prevAreaBoard
	 */
	public static int prevAreaBoard()
	{
		return prevAreaBoard;
	}
	
	/**
	 * @return prevTotalIndexes
	 */
	public static int prevTotalIndexes()
	{
		return prevTotalIndexes;
	}
	
	/**
	 * @return newDimensionBoard
	 */
	public static int newDimensionBoard()
	{
		return newDimensionBoard;
	}
	
	/**
	 * @return newAreaBoard
	 */
	public static int newAreaBoard()
	{
		return newAreaBoard;
	}
	
	/**
	 * @return newTotalIndexes
	 */
	public static int newTotalIndexes()
	{
		return newTotalIndexes;
	}
	
	/**
	 * @return diff
	 */
	public static int diff()
	{
		return diff;
	}
	
	/**
	 * @return initDimensionBoard
	 */
	public static int initDimensionBoard()
	{
		return initDimensionBoard;
	}
	
	/**
	 * @return initAreaBoard
	 */
	public static int initAreaBoard()
	{
		return initAreaBoard;
	}
	
	/**
	 * @return initTotalIndexes
	 */
	public static int initTotalIndexes()
	{
		return initTotalIndexes;
	}
	
	/**
	 * @return diffInit
	 */
	public static int diffInit()
	{
		return diffInit;
	}
	
	/**
	 * @return initMaxPerColRow
	 */
	public static HashMap<Integer, Integer> initMaxPerColRow()
	{
		return initMaxPerColRow;
	}
	
	/**
	 * @return initMinPerColRow
	 */
	public static HashMap<Integer, Integer> initMinPerColRow()
	{
		return initMinPerColRow;
	}
	
	/**
	 * @return initRowsSizeCumul
	 */
	public static ArrayList<Integer> initRowsSizeCumul()
	{
		return initRowsSizeCumul;
	}

	/**
	 * @return initMinColPerRowList
	 */
	public static ArrayList<Integer> initMinColPerRowList()
	{
		return initMinColPerRowList;
	}

	/**
	 * @return initRowsSizeCumulTriangular
	 */
	public static ArrayList<Integer> initRowsSizeCumulTriangular()
	{
		return initRowsSizeCumulTriangular;
	}

	/**
	 * @return initMaxIndexRowOrCol
	 */
	public static int initMaxIndexRowOrCol()
	{
		return initMaxIndexRowOrCol;
	}
	
	/**
	 * @return initMinColPerRow
	 */
	public static HashMap<Integer, Integer> initMinColPerRow()
	{
		return initMinColPerRow;
	}
	
	/**
	 * @return initMaxColPerRow
	 */
	public static HashMap<Integer, Integer> initMaxColPerRow()
	{
		return initMaxColPerRow;
	}
	
	/**
	 * @return initMaxRowPerCol
	 */
	public static HashMap<Integer, Integer> initMaxRowPerCol()
	{
		return initMaxRowPerCol;
	}
	
	/**
	 * 
	 * @param context
	 * @return step(s) from which the board will be growing / shrinking
	 */
	public static int growingStep(final Context context)
	{
		return ((Boardless) context.game().board()).tiling() == TilingBoardlessType.Square
		? Constants.GROWING_STEP_SQUARE_BOARDLESS :  ((Boardless) context.game().board()).tiling() == TilingBoardlessType.Hexagonal 
		? Constants.GROWING_STEP_HEX_BOARDLESS : Constants.GROWING_STEP_TRIANGLE_BOARDLESS;
	}
	
	//----------------------------Initialization-------------------------------


	/**
	 * Reset attributes of the class.
	 */
	public static void reset()
	{
		mappedPrevToNewIndexes = null;
		mappedNewToPrevIndexes = null;
		surplusIndexes = null; 
		
		initDimensionBoard = 0;
		initAreaBoard = 0;
		initTotalIndexes = 0;
		diffInit = 0;
		prevDimensionBoard = 0;
		prevAreaBoard = 0;
		prevTotalIndexes = 0;
		newDimensionBoard = 0;
		newAreaBoard = 0;
		newTotalIndexes = 0;
		diff = 0;
		
		mappedInitToNewIndexes = null;
		mappedNewToInitIndexes = null;
		surplusInitIndexes = null;
				
		initMaxPerColRow = null;
		initMinPerColRow = null;
		initRowsSizeCumul = null;

		initMinColPerRowList = null;
		initRowsSizeCumulTriangular = null;
		initMaxIndexRowOrCol = 0;
		initMinColPerRow = null;
		initMaxColPerRow = null;
		initMaxRowPerCol = null;
		
		perimeter = null;
		isVisual = false;
		
		movesDone = null;
		
		nbInitialTiles = -1;
	}
	
	protected static void initInitConstants(final Context context, final int initDimension)
	{
		initDimensionBoard = initDimension;
		
		if (((Boardless) context.game().board()).tiling() == TilingBoardlessType.Square) {
			initAreaBoard = (int) Math.pow(initDimensionBoard(), 2);
		}
		else if (((Boardless) context.game().board()).tiling() == TilingBoardlessType.Hexagonal)
		{
			initAreaBoard = (initDimensionBoard() * ((initDimensionBoard() - 1) * 3)) + 1;
			
			int initNbrRowsOrCols = (initDimensionBoard()*2)-1;
			initMaxPerColRow = new HashMap<Integer, Integer>();
			initMinPerColRow = new HashMap<Integer, Integer>();
			initRowsSizeCumul = new ArrayList<Integer>();
			
			initRowsSizeCumul().add(0);		        
			for (int i=0; i<initNbrRowsOrCols; i++) 
			{
			    if (i<=(initDimensionBoard()-1)) 
			    	initRowsSizeCumul().add(initDimensionBoard()+i+initRowsSizeCumul().get(i));
			    else 
			    	initRowsSizeCumul().add(initNbrRowsOrCols-1-(i%initDimensionBoard()) +initRowsSizeCumul().get(i));
			    
			    if (i<initDimensionBoard()-1)
			    {
					initMaxPerColRow().put(i, (initDimensionBoard()-1) + i);
			    	initMinPerColRow.put(i, 0);
			    }
			    else
			    {
					initMaxPerColRow().put(i, initDimensionBoard()+1);
			    	initMinPerColRow().put(i, i - (initDimensionBoard()-1));
			    }
			}
			
		}
		else if (((Boardless) context.game().board()).tiling() == TilingBoardlessType.Triangular)
		{
			initAreaBoard = (int) Math.pow(initDimensionBoard(), 2);
			
			initMinColPerRowList = new ArrayList<Integer>();
			initRowsSizeCumulTriangular = new ArrayList<Integer>();
			initMinColPerRowList().add(0);
			initRowsSizeCumulTriangular().add(0);
			initRowsSizeCumulTriangular().add(initDimensionBoard());
			
			for (int i=1; i<initDimensionBoard(); i++)
			{
				initMinColPerRowList().add(i);
				initMinColPerRowList().add(i);
				
				initRowsSizeCumulTriangular().add(initDimensionBoard()-i + initRowsSizeCumulTriangular().get(initRowsSizeCumulTriangular().size()-1));
				initRowsSizeCumulTriangular().add(initDimensionBoard()-i + initRowsSizeCumulTriangular().get(initRowsSizeCumulTriangular().size()-1));
			}
			initRowsSizeCumulTriangular().remove(initRowsSizeCumulTriangular().size() - 1);
			
			
			initMaxIndexRowOrCol = (initDimensionBoard()*2) - 2;
			initMinColPerRow = new HashMap<Integer, Integer>();
			initMaxColPerRow = new HashMap<Integer, Integer>();
			initMaxRowPerCol = new HashMap<Integer, Integer>();
			initMinColPerRow().put(0, 0);
			initMaxColPerRow().put(0, initMaxIndexRowOrCol());
			initMaxRowPerCol().put(0, 0);
			initMaxRowPerCol().put(initMaxIndexRowOrCol(), 0);
			for (int i=1; i<initDimensionBoard(); i++) 
			{
				initMinColPerRow().put((i*2)-1, i);
				initMinColPerRow().put(i*2, i);

				initMaxColPerRow().put((i*2)-1, initMaxIndexRowOrCol() - i);
				initMaxColPerRow().put(i*2, initMaxIndexRowOrCol() - i);

				initMaxRowPerCol().put(i, i*2);
				initMaxRowPerCol().put(initMaxIndexRowOrCol()-i, i*2);
			}
		}
		else
		{
			throw new UnsupportedOperationException("Tiling "+((Boardless) context.game().board()).tiling()+" not implement for boardless games.");
		}
	
		initTotalIndexes = context.game().equipment().offset().length;
	}
	
	/** 
	 * Initializes data structures to map the previous indexes to the new indexes, 
	 * due to the change in board size. For a Rectangle / Square board.
	 */
	protected static void initMappingIndexesSquare()
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
					newIndex = prevIndex + newDimensionBoard() + 1 + (2 * (inter));
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
	 * Translates coordinates into index for a hexagonal board.
	 * 
	 * @param row row of the coordinate.
	 * @param col column of the coordinate.
	 * @param rowsSizeCumul helps to know how many cells there are before the beginning of a new row.
	 * So if size of the board is 3, newRowsSizeCumul will look like [0, 3, 7, 12, 16, 19], showing 
	 * that at there are 0 cells before the first row, 7 cells before the third row, ...
	 * @param dim dimension of the board
	 *
	 * @return the index based on the coordinates.
	 */
	protected static int coordToIndexHex(final int row, final int col, final int rowSizeCumul, final int dim)
	{
		int newRow = row - (dim - 1);
		newRow = newRow > 0 ? newRow : 0;
	    return rowSizeCumul + col - newRow;
	}
	
	
	protected static int get_min(final int val, final int dim)
	{
		if (val < dim)
	        return 0;
	    else
	        return val - dim + 1;
	}
	
	protected static int get_max(final int val, final int dim)
	{
		if (val >= dim)
	        return (dim * 2) - 2;
	    else
	        return val + (dim - 1);
	}
	 
	/**
	 * Initializes data structures to map the previous indexes to the new indexes, 
	 * due to the change in board size. For a Hexagonal board.
	 * 
	 * @param context
	 */
	protected static void initMappingIndexesHexagonal(final Context context)
	{
		// data structures to map between current plate and new plate
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>();	
	
		int nbCells = context.topology().cells().size();
		int nbOtherContainers = prevTotalIndexes() - nbCells;
		int newNbrRowsOrCols = (newDimensionBoard()*2)-1;
		ArrayList<Integer> newRowsSizeCumul = new ArrayList<Integer>();
		newRowsSizeCumul.add(0);
		for (int i=0; i<newNbrRowsOrCols; i++) 
		{
		    if (i<=(newDimensionBoard()-1)) 
		    	newRowsSizeCumul.add(newDimensionBoard()+i+newRowsSizeCumul.get(i));
		    else 
		    	newRowsSizeCumul.add(newNbrRowsOrCols-1-(i%newDimensionBoard()) +newRowsSizeCumul.get(i));
		}	

		if (prevDimensionBoard() < newDimensionBoard()) 
		{
			for (Cell prevIndex : context.topology().cells()) 
			{ 
				int newCol = prevIndex.col()+1;
				int newRow = prevIndex.row()+1;
				int newIndex = coordToIndexHex(newRow, newCol, newRowsSizeCumul.get(newRow), newDimensionBoard());
				mappedPrevToNewIndexes().put(prevIndex.index(), newIndex);
				mappedNewToPrevIndexes().put(newIndex, prevIndex.index());
			}
			// mapping container outside of the board
			for (int i=nbCells; i<nbCells+nbOtherContainers; i++)
			{
				int newIndex = i + diff();
				mappedPrevToNewIndexes().put(i, newIndex);
				mappedNewToPrevIndexes().put(newIndex, i);
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
			int newMaxIndexRowOrCol = (newDimensionBoard()*2) - 2;
			for (Cell prevIndex : context.topology().cells()) 
			{ 
				int newCol = prevIndex.col()-1;
				int newRow = prevIndex.row()-1;
				if (newCol >= 0 && newRow >= 0 && newCol <= newMaxIndexRowOrCol && newRow <= newMaxIndexRowOrCol) 
				{
					if (newCol >= get_min(newRow, newDimensionBoard()) && newRow >= get_min(newCol, newDimensionBoard()) && newCol <= get_max(newRow, newDimensionBoard()) && newRow <= get_max(newCol, newDimensionBoard())) 
					
					{
						int newIndex = coordToIndexHex(newRow, newCol, newRowsSizeCumul.get(newRow), newDimensionBoard());
						mappedPrevToNewIndexes().put(prevIndex.index(), newIndex);
						mappedNewToPrevIndexes().put(newIndex, prevIndex.index());
					}
				}
			}
			// mapping container outside of the board
			for (int i=nbCells; i<nbCells+nbOtherContainers; i++)
			{
				int newIndex = i - diff();
				mappedPrevToNewIndexes().put(i, newIndex);
				mappedNewToPrevIndexes().put(newIndex, i);
			}
			for (int i = 0; i < prevTotalIndexes(); i++)
				if (!mappedPrevToNewIndexes().containsKey(i))
					surplusIndexes().add(i);
		}
		
		// data structures to map between initial plate and new plate
		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		surplusInitIndexes = new HashSet<Integer>();
				    
		if (newDimensionBoard() != initTotalIndexes()) 
		{
			int diffInitToPrevDimensionBoard = prevDimensionBoard() - initDimensionBoard();
			int initMaxIndexRowOrCol = (initDimensionBoard()*2) - 2;
			for (Cell prevIndex : context.topology().cells()) 
			{ 
				int newCol = prevIndex.col()-diffInitToPrevDimensionBoard;
				int newRow = prevIndex.row()-diffInitToPrevDimensionBoard;
				if (newCol >= 0 && newRow >= 0 && newCol <= initMaxIndexRowOrCol && newRow <= initMaxIndexRowOrCol)
					if (newCol >= initMinPerColRow().get(newRow) && newRow >= initMinPerColRow().get(newCol) && newCol <= initMaxPerColRow().get(newRow) && newRow <= initMaxPerColRow().get(newCol)) 
					{
						int newIndex = coordToIndexHex(newRow, newCol, initRowsSizeCumul().get(newRow), initDimensionBoard());
						mappedInitToNewIndexes().put(newIndex, mappedPrevToNewIndexes().get(prevIndex.index()));
						mappedNewToInitIndexes().put(mappedPrevToNewIndexes().get(prevIndex.index()), newIndex);
					}
			}
			
			// mapping container outside of the board
			int prevIndex = mappedInitToNewIndexes().size();
			for (int i=prevIndex; i<prevIndex+nbOtherContainers; i++)
			{
				int newIndex = i + diffInit();
				mappedInitToNewIndexes().put(i, newIndex);
				mappedNewToInitIndexes().put(newIndex, i);
			}
		}
		else
			for (int i=0; i<initTotalIndexes(); i++)
			{
				mappedInitToNewIndexes().put(i, i);
				mappedNewToInitIndexes().put(i, i);
			}
		
		// handling surplus
		for (int i = 0; i < newTotalIndexes(); i++)
			if (!mappedNewToInitIndexes().containsKey(i))
				surplusInitIndexes().add(i);
	}
	
	/**
	 * Translates coordinates into index for a triangular board.
	 * 
	 * @param col column of the coordinate.
	 * @param minColPerRow minimum possible column coordinate for a the row.
	 * @param rowsSizeCumul helps to know how many cells there are before the beginning of a new row.
	 *
	 * @return the index based on the coordinates.
	 */
	protected static int coordToIndexTriangle(final int col, final int minColPerRow, final int rowsSizeCumul)
	{
		return rowsSizeCumul + (col - minColPerRow)/2;
	}
	
	/**
	 * Initializes data structures to map the previous indexes to the new indexes, 
	 * due to the change in board size. For a Triangular board.
	 * 
	 * @param context
	 */
	protected static void initMappingIndexesTriangle(final Context context)
	{
		// data structures to map between current plate and new plate
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>();	
		
		ArrayList<Integer> newMinColPerRowList = new ArrayList<Integer>();
		ArrayList<Integer> newRowsSizeCumul = new ArrayList<Integer>();
		newMinColPerRowList.add(0);
		newRowsSizeCumul.add(0);
		newRowsSizeCumul.add(newDimensionBoard());
		for (int i=1; i<newDimensionBoard(); i++)
		{
			newMinColPerRowList.add(i);
			newMinColPerRowList.add(i);
			
			newRowsSizeCumul.add(newDimensionBoard()-i + newRowsSizeCumul.get(newRowsSizeCumul.size()-1));
		    newRowsSizeCumul.add(newDimensionBoard()-i + newRowsSizeCumul.get(newRowsSizeCumul.size()-1));
		}
		newRowsSizeCumul.remove(newRowsSizeCumul.size() - 1);
		
		if (prevDimensionBoard() < newDimensionBoard()) 
		{
			for (Cell prevIndex : context.topology().cells()) 
			{ 
				int newCol = prevIndex.col()+Constants.GROWING_STEP_TRIANGLE_BOARDLESS;
				int newRow = prevIndex.row()+Constants.GROWING_STEP_TRIANGLE_BOARDLESS-1;
				int newIndex = coordToIndexTriangle(newCol, newMinColPerRowList.get(newRow), newRowsSizeCumul.get(newRow));
				mappedPrevToNewIndexes().put(prevIndex.index(), newIndex);
				mappedNewToPrevIndexes().put(newIndex, prevIndex.index());
			}
			// creating map for the cells outside the main board
			for (int prevIndex=prevAreaBoard(); prevIndex<prevTotalIndexes(); prevIndex++) 
			{
				int newIndex = prevIndex + diff();
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
			int newMaxIndexRowOrCol = (newDimensionBoard()*2) - 2;
			HashMap<Integer, Integer> minColPerRow = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> maxColPerRow = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> maxRowPerCol = new HashMap<Integer, Integer>();
			minColPerRow.put(0, 0);
			maxColPerRow.put(0, newMaxIndexRowOrCol);
			maxRowPerCol.put(0, 0);
			maxRowPerCol.put(newMaxIndexRowOrCol, 0);
			for (int i=1; i<newDimensionBoard(); i++) 
			{
				minColPerRow.put((i*2)-1, i);
				minColPerRow.put(i*2, i);

				maxColPerRow.put((i*2)-1, newMaxIndexRowOrCol - i);
				maxColPerRow.put(i*2, newMaxIndexRowOrCol - i);

				maxRowPerCol.put(i, i*2);
				maxRowPerCol.put(newMaxIndexRowOrCol-i, i*2);
			}
			for (Cell prevIndex : context.topology().cells()) 
			{ 
				int newCol = prevIndex.col()-Constants.GROWING_STEP_TRIANGLE_BOARDLESS;
				int newRow = prevIndex.row()-(Constants.GROWING_STEP_TRIANGLE_BOARDLESS-1);
				if (newCol >= 0 && newRow >= 0 && newCol <= newMaxIndexRowOrCol && newRow <= newMaxIndexRowOrCol) {
					// make sure new coordinates exist on the smaller board
					if (newCol >= minColPerRow.get(newRow) && newCol <= maxColPerRow.get(newRow) && (maxColPerRow.get(newRow)-newCol)%2 == 0 && newRow <= maxRowPerCol.get(newCol))
					{
						int newIndex = coordToIndexTriangle(newCol, newMinColPerRowList.get(newRow), newRowsSizeCumul.get(newRow));
						mappedPrevToNewIndexes().put(prevIndex.index(), newIndex);
						mappedNewToPrevIndexes().put(newIndex, prevIndex.index());
					}
				}
			}
			// creating map for the cells outside the main board
			for (int prevIndex=prevAreaBoard(); prevIndex<prevTotalIndexes(); prevIndex++) 
			{
				int newIndex = prevIndex - diff();
				mappedPrevToNewIndexes().put(prevIndex, newIndex);
				mappedNewToPrevIndexes().put(newIndex, prevIndex);
			}
			for (int i = 0; i < prevTotalIndexes(); i++)
				if (!mappedPrevToNewIndexes().containsKey(i))
					surplusIndexes().add(i);
		}
		
		// data structures to map between initial plate and new plate
		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		surplusInitIndexes = new HashSet<Integer>();
		
		if (newDimensionBoard() != initDimensionBoard()) 
		{
			int diffInitToPrevDimensionBoard = (prevDimensionBoard() - initDimensionBoard())/Constants.GROWING_STEP_TRIANGLE_BOARDLESS;
			for (Cell prevIndex : context.topology().cells()) 
			{ 
				int newCol = prevIndex.col()-(Constants.GROWING_STEP_TRIANGLE_BOARDLESS*diffInitToPrevDimensionBoard);
				int newRow = prevIndex.row()-((Constants.GROWING_STEP_TRIANGLE_BOARDLESS-1)*diffInitToPrevDimensionBoard);
				if (newCol >= 0 && newRow >= 0 && newCol <= initMaxIndexRowOrCol() && newRow <= initMaxIndexRowOrCol()) {
					// make sure new coordinates exist on the smaller board
					if (newCol >= initMinColPerRow().get(newRow) && newCol <= initMaxColPerRow().get(newRow) && (initMaxColPerRow().get(newRow)-newCol)%2 == 0 && newRow <= initMaxRowPerCol().get(newCol))
					{
						int newIndex = coordToIndexTriangle(newCol, initMinColPerRowList().get(newRow), initRowsSizeCumulTriangular().get(newRow));
						mappedInitToNewIndexes().put(newIndex, mappedPrevToNewIndexes().get(prevIndex.index()));
						mappedNewToInitIndexes().put(mappedPrevToNewIndexes().get(prevIndex.index()), newIndex);
					}
				}
			}
			// creating map for the cells outside the main board
			for (int prevIndex=initAreaBoard(); prevIndex<initTotalIndexes(); prevIndex++) 
			{
				int newIndex = prevIndex + diffInit();
				mappedInitToNewIndexes().put(prevIndex, newIndex);
				mappedNewToInitIndexes().put(newIndex, prevIndex);
			}
		}
		else
			for (int i=0; i<initTotalIndexes(); i++)
			{
				mappedInitToNewIndexes().put(i, i);
				mappedNewToInitIndexes().put(i, i);
			}

		for (int i = 0; i < newTotalIndexes(); i++)
			if (!mappedNewToInitIndexes().containsKey(i))
				surplusInitIndexes().add(i);
	}
	/** 
	 * Initializes data structures to map the previous indexes to the new indexes, 
	 * due to the change in board size, such as :
	 * mappedPrevToNewIndexes : mapping giving the previous index as key and the new index as value.
	 * mappedNewToPrevIndexes : mapping giving the new index as key and the previous index as value.
	 * surplusIndexes : new indexes that don't have a mapping to the previous plate as they are new existing sites.
	 * 
	 * mappedInitToNewIndexes : mapping giving the init index as key and the new index as value.
	 * mappedNewToInitIndexes : mapping giving the new index as key and the init index as value.
	 * surplusInitIndexes : new indexes that don't have a mapping to the init plate as they are new existing sites.
	 */
	protected static void initMappingIndexes(Context context)
	{
		if (((Boardless) context.game().board()).tiling() == TilingBoardlessType.Square)
			initMappingIndexesSquare();
		else if (((Boardless) context.game().board()).tiling() == TilingBoardlessType.Hexagonal)
			initMappingIndexesHexagonal(context);
		else if (((Boardless) context.game().board()).tiling() == TilingBoardlessType.Triangular)
			initMappingIndexesTriangle(context);
		else
			throw new UnsupportedOperationException("Tiling "+((Boardless) context.game().board()).tiling()+" not implement for boardless games.");
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
	public static void initMainConstants(final Context context, final int currDimensionBoard, final int futureDimensionBoard) {
		
		if (initDimensionBoard() == 0)
			initInitConstants(context, currDimensionBoard);
		
		prevDimensionBoard = currDimensionBoard;
		newDimensionBoard = futureDimensionBoard;
		
		if (((Boardless) context.game().board()).tiling() == TilingBoardlessType.Square) {
			prevAreaBoard = (int) Math.pow(prevDimensionBoard(), 2);
			newAreaBoard = (int) Math.pow(newDimensionBoard(), 2);
		}
		else if (((Boardless) context.game().board()).tiling() == TilingBoardlessType.Hexagonal)
		{
			prevAreaBoard = (prevDimensionBoard() * ((prevDimensionBoard() - 1) * 3)) + 1;
			newAreaBoard = (newDimensionBoard() * ((newDimensionBoard() - 1) * 3)) + 1;
		}
		else if (((Boardless) context.game().board()).tiling() == TilingBoardlessType.Triangular)
		{
			prevAreaBoard = (int) Math.pow(prevDimensionBoard(), 2);
			newAreaBoard = (int) Math.pow(newDimensionBoard(), 2);
		}
		else
			throw new UnsupportedOperationException("Tiling "+((Boardless) context.game().board()).tiling()+" not implement for boardless games.");

		int nbOtherContainers = 0;
		for (int i=1; i<context.sitesFrom().length; i++)
			nbOtherContainers += context.game().equipment().containers()[i].topology().getGraphElements(SiteType.Cell).size();
		
		prevTotalIndexes = prevAreaBoard()+nbOtherContainers;
		newTotalIndexes = newAreaBoard()+nbOtherContainers;
		
		if (prevAreaBoard() < newAreaBoard())
			diff = newAreaBoard() - prevAreaBoard();
		else
			diff = prevAreaBoard() - newAreaBoard();

		diffInit = newAreaBoard() - initAreaBoard();
		
		
		initMappingIndexes(context);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param game
	 * @return the number of initial tiles on the main board.
	 */
	public static int nbInitialTiles(final Game game)
	{
		if (nbInitialTiles == -1)
			nbInitialTiles = game.rules().start().nbInitialTilesOnBoard();
		return nbInitialTiles;
	}
	
	/**
	 * Updates the content of the Topology to match the new board.
	 * Re-launches pre-computations.
	 * 
	 * @param context
	 */
	protected static void updateTopology(final Context context)
	{
		context.game().update();
	}
	
	/** 
	 * Updates the board dimensions.
	 * 
	 * @param context
	 * @param board
	 * @param newSize new size of the board.
	 */
	public static void updateBoardDimensions(final Context context, final Boardless board, final int newSize) 
	{
		GraphFunction newGraphFunction = board.tiling() == TilingBoardlessType.Square
				? new RectangleOnSquare(new DimConstant(newSize), null, null, null) : board.tiling() == TilingBoardlessType.Hexagonal 
				? new HexagonOnHex(new DimConstant(newSize)) : new TriangleOnTri(new DimConstant(newSize));

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
	public static boolean isTouchingEdge(final int target) {
		if (target == Constants.UNDEFINED) return false;
		
        int start = 0;
        int end = perimeter.size() - 1;

        while (start <= end) {
            int midIndex = start + (end - start) / 2;
            int midValue = perimeter.get(midIndex).index();

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
	protected static HashedChunkSet copyChunkWithNewBoardSize(final HashedChunkSet previousHCS, final ZobristHashGenerator generator, final int maxChunkVal, final int numChunks)
	{
		HashedChunkSet newHCS = new HashedChunkSet(generator, maxChunkVal, numChunks);
		if (previousHCS != null)
		{
			ChunkSet previousCS = previousHCS.internalState();
			ChunkSet newCS = (ChunkSet) newHCS.internalState();
			TIntArrayList nonzeroChunks = previousCS.getNonzeroChunks();
			for (int prevVal : nonzeroChunks.toArray()) 
			{ 
				newCS.setChunk(mappedInitToNewIndexes().get(prevVal), previousCS.getChunk(prevVal));
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
	 * @param mappedPrevToNewIndexes
	 */
	protected static HashedChunkSet copyChunk(final HashedChunkSet previousHCS, final ZobristHashGenerator generator, final int maxChunkVal, final int numChunks)
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
	
	 /**
	 * Updates the chunks of the first containerStates to include the new 
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
	 * @param prevDim
	 * @param newDim
	 */
	public static void updateChunks(final Context context, final int prevDim, final int newDim)
	{
		final Game game = context.game();
		final int numPlayers = game.players().count();
		int numSites = newTotalIndexes();
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
				if (i == 0) 
				{
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
	 * Generates the new move based on a move, which means with the new 
	 * indexes of the board (which means updating to() and from()).
	 * 
	 * @param prevMove previous move to copy, except for the indexes.
	 * @param isMoveDoneOnEdge is the move done on an edge of board which 
	 * lead to an increase of the board size. 
	 * @param mapping Mapping to use to map move from previous to new board.
	 * @return the new move.
	 */
	public static Move generateNewMove(final Move prevMove, final boolean isMoveDoneOnEdge, final HashMap<Integer, Integer> mapping)
	{		
		List<Action> actions = prevMove.actions();

		if (actions.size() == 1)
		{
			Action newAction = actions.get(0);
			int to = newAction.to();
			int from = newAction.from();
			if (to != Constants.UNDEFINED)
			{
				newAction.setTo(mapping.get(to));
			}
			if (from != Constants.UNDEFINED)
				newAction.setFrom(mapping.get(from));
			
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
					action.setTo(mapping.get(to));
				if (from != Constants.UNDEFINED)
					action.setFrom(mapping.get(from));
				
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

		if (isMoveDoneOnEdge)
			prevMove.setOnEdge(prevDimensionBoard());
		return prevMove;
	}
	
	/**
	 * Generates the new move based on a move, which means with the new indexes 
	 * of the board (which means updating to() and from()), by using the default 
	 * mapping which is between the current board and the new board.
	 * 
	 * @param prevMove previous move to copy, except for the indexes.
	 * @param isMoveDoneOnEdge is the move done on an edge of board which 
	 * lead to an increase of the board size. 
	 * @return the new move.
	 */
	public static Move generateNewMove(final Move prevMove, final boolean isMoveDoneOnEdge)
	{
		return generateNewMove(prevMove, isMoveDoneOnEdge, mappedPrevToNewIndexes());
	}
	
	/** 
	 * Replays the moves mapped to the proper new component (like tile or hand) index.
	 * 
	 * @param context
	 * @param movesDone
	 */
	protected static void replayMoves(final Context context, final List<Move> movesDone)
	{
		Move move = null;
		int numInitialPlacementMoves = context.trial().numInitialPlacementMoves();
		
		for (int i = 0; i < movesDone.size(); i++)
		{
			move = movesDone.get(i);
			generateNewMove(move, false);
			
			if (i>=numInitialPlacementMoves)
				context.game().apply(context, move, false);
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
	protected static void updateOwned(final Context context)
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
	 * Update the chunks and the owned based on the new board. 
	 * Also apply the historic of move mapped to the new board.
	 * 
	 * @param app
	 * @param movesDone
	 * @param legalMoves
	 * @param replayMoves
	 */
	public static void remakeTrial(final Context context, final List<Move> movesDone, Moves legalMoves, final boolean replayMoves) 
	{
		updateChunks(context, prevDimensionBoard(), newDimensionBoard());
		updateOwned(context);
		
		if (replayMoves)
			replayMoves(context, movesDone);
	}
	
	/** 
	 * Update the chunks and the owned based on the new board.
	 * 
	 * @param context
	 */
	public static void updateChunksAndOwned(final Context context) 
	{	
		remakeTrial(context, null, null, false);
	}
	
	public static void redoneAllButLast(final Context context)
	{
		
		/*for (int i=0; i<movesDone.size(); i++) 
			movesDone.set(i, generateNewMove(movesDone.get(i), false));*/
		
		Move move = null;
		int numInitialPlacementMoves = context.trial().numInitialPlacementMoves();
		
		for (int i = 0; i < movesDone.size(); i++)
		{
			move = movesDone.get(i);
			generateNewMove(move, false);
			
			if (i>=numInitialPlacementMoves)
				context.game().apply(context, move, false);
		}
	}
	
	/**
	 * Reset the state, which also reset the mover.
	 * 
	 * @param context
	 */
	public static void resetState(final Context context)
	{	
		context.state().reset(context.game());
	}
	
	/** 
	 * Cancel all the moves from the beginning, to have a fresh base with an empty board.
	 * 
	 * @param context
	 */
	public static void resetMoves(final Context context)
	{
		context.reset();
		context.game().start(context, true);
		context.game().incrementGameStartCount();
	}
	
	/** 
	 * Start over the game on the new board and apply the historic of move mapped to the new board.
	 * 
	 * @param app
	 */
	protected static void remakeTrial(final Context context, final boolean replayMoves) 
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
	 * @param fromSize TODO
	 * @param toSize
	 * @param replayMoves
	 */
	public static void updateBoard(final Context context, final int fromSize, final int toSize, final boolean replayMoves)
	{
		Game game = context.game();
		Boardless board = (Boardless) game.board();
		initMainConstants(context, fromSize, toSize);
		
		// TODO check that the move is applied on a board type container
		updateBoardDimensions(context, board, toSize);

		Trial trial = context.trial();
		movesDone = trial.generateCompleteMovesList();
		if (replayMoves) // TODO does not change if we call it or not - test that
			resetMoves(context);
	}
	
	/** 
	 * Check if the move was made on a boardless board and on one edge of the board. 
	 * If so, update the size of the  board.
	 * 
	 * @param context
	 * @param move
	 * @param fromSize TODO
	 * @param toSize 
	 * @param replayMoves 
	 */
	public static void checkMoveImpactOnBoard(final Context context, final Move move, final int fromSize, final int toSize, final boolean replayMoves) 
	{
		if (context.game().isBoardless()) 
		{
			perimeter = new ArrayList<>(context.topology().perimeter(context.board().defaultSite()));
			if (isTouchingEdge(move.to()))
				updateBoard(context, fromSize, toSize, replayMoves);
		}
	}
	
}
