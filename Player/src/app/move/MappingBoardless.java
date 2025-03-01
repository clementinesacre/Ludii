package app.move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import game.types.board.SiteType;
import other.context.Context;
import other.move.Move;
import other.topology.Cell;

/**
 * Methods to create the mapping that will tell the board how to grow.
 * 
 * @author Clémentine.Sacré
 */
public class MappingBoardless
{
	private static Stack<ArrayList<Integer>> addedCoordinatesPerEdgeMove = new Stack<ArrayList<Integer>>(); //TODO add coordinates
	
	private static HashMap<Integer, Integer> mappedPrevToNewIndexes;
	private static HashMap<Integer, Integer> mappedNewToPrevIndexes;
	private static HashSet<Integer> surplusIndexes; // indexes that are being added from one board to another
	
	private static ArrayList<Integer> rowsNeighbors; 
	private static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToCols; 
	private static int[] nbrNewCellsBeforeRow;
	
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
	
	private static ArrayList<Integer> rowsNeighbors()
	{
		return rowsNeighbors;
	}
	
	private static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToCols()
	{
		return rowsNeighborsMapToCols;
	}
	
	private static int[] nbrNewCellsBeforeRow()
	{
		return nbrNewCellsBeforeRow;
	}
			
	//-------------------------------------------------------------------------

	/**
	 * Calculates the coordinates of the neighbor of a cell, by only keeping the 
	 * ones that do not exist yet on the current board.
	 * rowsNeighborsMapToCols is a HashMap, with the keys being the rows of these 
	 * neighbors, and the value is a list of the columns of these neighbors.
	 * rowsNeighbors is just a list containing the rows of these neighbors.
	 * @param context
	 * @param cell
	 */
	private static void calculateNeighborsCoordinates(Context context, Cell cell)
	{
		int row = cell.row();
		int col = cell.col();
		rowsNeighborsMapToCols = new HashMap<Integer, ArrayList<Integer>>();
		rowsNeighbors = new ArrayList<Integer>();
		for (int r=-1; r<=1; r++)
		{
			for (int c=-1; c<=1; c++)
			{
				int newRow = row + r;
				int newCol = col + c;
				Cell newCell = context.topology().getCellWithCoords(newRow, newCol, 0); //TODO : 0 for only flat game?
				if (newCell == null)
				{
					if (rowsNeighborsMapToCols().containsKey(newRow))
					{
						rowsNeighborsMapToCols().get(newRow).add(newCol);
					}
					else
					{
						ArrayList<Integer> set = new ArrayList<Integer>();
						set.add(newCol);
						rowsNeighborsMapToCols().put(newRow, set);
						rowsNeighbors().add(newRow);
					}
				}
			}
		}
		Collections.sort(rowsNeighbors());
	}
	
	/**
	 * Calculates the indexes of the neighbors of the cell. They do not exist yet, 
	 * this mean this will shift the current cells, and though help create the new 
	 * mapping. At the same time, nbrNewCellsBeforeRow is filled in order to remember 
	 * the number of cells added before a row.
	 * @param context
	 */
	private static void calculateNeighborsIndexes(Context context)
	{
		nbrNewCellsBeforeRow = new int[context.topology().rows().get(SiteType.Cell).size()+3];
		for (Integer r : rowsNeighbors())
		{
			ArrayList<Integer> colsNeighbors = rowsNeighborsMapToCols().get(r);
			Collections.sort(colsNeighbors);
			
			if (r >= 0)
				nbrNewCellsBeforeRow()[r+1] += nbrNewCellsBeforeRow()[r]; 
			for (Integer c : colsNeighbors)
			{
				int newIndex;
				if (r < 0) //TODO
				{
					newIndex = nbrNewCellsBeforeRow()[r+1];
					nbrNewCellsBeforeRow()[r+1] += 1;
				}
				else
				{
					// check on the right of the cell
					Cell rightNeighbor = context.topology().getCellWithCoords(r, c+1, 0); // TODO si r < 0 ou r >= size ça sert à rien de looper sur les coord
					if (rightNeighbor != null)
					{
						newIndex = rightNeighbor.index() + nbrNewCellsBeforeRow()[r+1];
						nbrNewCellsBeforeRow()[r+1] += 1;
					}
					else
					{
						// check on the left of the cell
						Cell leftNeighbor = context.topology().getCellWithCoords(r, c-1, 0);
						if (leftNeighbor != null)
						{
							newIndex = leftNeighbor.index() + nbrNewCellsBeforeRow()[r+1] + 1;
							nbrNewCellsBeforeRow()[r+2] += 1;
						}
						else
						{
							newIndex = context.topology().cells().size() + nbrNewCellsBeforeRow()[r+1];
							nbrNewCellsBeforeRow()[r+1] += 1;
						}
					}
				}
				surplusIndexes().add(newIndex);
			}
		}

		for (int i=rowsNeighbors().get(rowsNeighbors().size()-1)+2; i<context.topology().rows().get(SiteType.Cell).size()+2; i++)
			nbrNewCellsBeforeRow()[i] = nbrNewCellsBeforeRow()[i-1] + nbrNewCellsBeforeRow()[i];
	}
	
	/**
	 * For each cells of the previous board, calculates the new index.
	 * @param context
	 */
	private static void calculateCurrentCellsIndexes(Context context)
	{
		for (Cell c : context.topology().cells())
		{
			int prevIndex = c.index();
			int newIndex = prevIndex + nbrNewCellsBeforeRow()[c.row()+1]; 
			mappedPrevToNewIndexes().put(prevIndex, newIndex);
			mappedNewToPrevIndexes().put(newIndex, prevIndex);
		}
	}
	
	/**
	 * Creates a mapping between the indexes of the previous board and the 
	 * new board.
	 * @param context
	 * @param move edge move just applied that will help telling what cells 
	 * will be added and so what is going to be the new mapping.
	 */
	public static void createMapping(Context context, Move move)
	{
		Cell cell = (Cell) context.topology().getGraphElement(SiteType.Cell, move.to());

		// data structures to map between current plate and new plate
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>();	
		
		calculateNeighborsCoordinates(context, cell);
		calculateNeighborsIndexes(context);
		calculateCurrentCellsIndexes(context);
	}
}