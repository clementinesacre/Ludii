package app.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import game.equipment.container.board.Boardless;
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
	// remember the cells added at each step to be able to undo them easily when going back
	private static ArrayList<int[]> totalNbrNewCellsBeforeRow = new ArrayList<int[]>();
	private static ArrayList<Integer> addedRowPerEdgeMove = new ArrayList<Integer>(); 
	private static ArrayList<Integer> totalAddedRows = new ArrayList<Integer>();
	private static ArrayList<Integer> totalAddedCells = new ArrayList<Integer>(); 
	
	private static HashMap<Integer, Integer> mappedPrevToNewIndexes;
	private static HashMap<Integer, Integer> mappedNewToPrevIndexes;
	private static HashSet<Integer> surplusIndexes; // indexes that are being added from one board to another

	private static HashMap<Integer, Integer> mappedInitToNewIndexes;
	private static HashMap<Integer, Integer> mappedNewToInitIndexes;
	private static HashSet<Integer> surplusInitIndexes; // indexes that are being added from init board to new one
	
	private static ArrayList<Integer> rowsNeighbors; 
	private static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToCols; 
	private static int[] nbrNewCellsBeforeRow;
	private static int[] cumulNbrNewCellsBeforeRow;
	
	private static List<Cell> initCells;
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
	
	private static ArrayList<int[]> totalNbrNewCellsBeforeRow()
	{
		return totalNbrNewCellsBeforeRow;
	}
	
	private static ArrayList<Integer> addedRowPerEdgeMove()
	{
		return addedRowPerEdgeMove;
	}
	
	private static ArrayList<Integer> totalAddedRows()
	{
		return totalAddedRows;
	}
	
	private static ArrayList<Integer> totalAddedCells()
	{
		return totalAddedCells;
	}
	
	private static int[] clem()
	{
		return cumulNbrNewCellsBeforeRow;
	}
	
	//-------------------------------------------------------------------------
	
	public static void init()
	{
		totalNbrNewCellsBeforeRow = new ArrayList<int[]>();
		addedRowPerEdgeMove = new ArrayList<Integer>(); 
		totalAddedRows = new ArrayList<Integer>();
		
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>(); // indexes that are being added from one board to another
		
		rowsNeighbors = new ArrayList<Integer>(); 
		rowsNeighborsMapToCols = new HashMap<Integer, ArrayList<Integer>>(); 
		nbrNewCellsBeforeRow = new int[0];
	}

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
		int lastTotalAddedRows = totalAddedRows().size() > 0 ? totalAddedRows().get(totalAddedRows().size()-1) : 0;
		int[] inter = new int[context.topology().rows().get(SiteType.Cell).size()+3 + lastTotalAddedRows];
		boolean newRow = false;
		for (Integer r : rowsNeighbors())
		{
			ArrayList<Integer> colsNeighbors = rowsNeighborsMapToCols().get(r);
			Collections.sort(colsNeighbors);
			
			if (r >= 0) 
			{
				nbrNewCellsBeforeRow()[r+1] += nbrNewCellsBeforeRow()[r]; 
				inter[r+1+lastTotalAddedRows] += inter[r+lastTotalAddedRows] + clem()[r+1];
			}
			for (Integer c : colsNeighbors)
			{
				int newIndex;
				if (r < 0) //TODO
				{
					newIndex = nbrNewCellsBeforeRow()[r+2];
					nbrNewCellsBeforeRow()[r+2] += 1;
					inter[r+2+lastTotalAddedRows] += 1 + clem()[r+2];
				}
				else
				{
					// check on the right of the cell
					Cell rightNeighbor = context.topology().getCellWithCoords(r, c+1, 0); // TODO si r < 0 ou r >= size ça sert à rien de looper sur les coord
					if (rightNeighbor != null)
					{
						newIndex = rightNeighbor.index() + nbrNewCellsBeforeRow()[r+1];
						nbrNewCellsBeforeRow()[r+1] += 1;
						inter[r+1+lastTotalAddedRows] += 1 + clem()[r+1];
					}
					else
					{
						// check on the left of the cell
						Cell leftNeighbor = context.topology().getCellWithCoords(r, c-1, 0);
						if (leftNeighbor != null)
						{
							newIndex = leftNeighbor.index() + nbrNewCellsBeforeRow()[r+1] + 1;
							nbrNewCellsBeforeRow()[r+2] += 1;
							inter[r+2+lastTotalAddedRows] += 1 + clem()[r+2];
						}
						else
						{
							newIndex = context.topology().cells().size() + nbrNewCellsBeforeRow()[r+1];
							nbrNewCellsBeforeRow()[r+1] += 1;
							inter[r+1+lastTotalAddedRows] += 1 + clem()[r+1];
						}
					}
				}
				
				if (r < 0 || r >= context.topology().rows().get(SiteType.Cell).size())
				{
					newRow = true;
				}
				surplusIndexes().add(newIndex);
			}
		}
		int prevTotalAddedCells = 0;
		if (totalAddedCells().size()>0)
			prevTotalAddedCells = totalAddedCells().get(totalAddedCells().size()-1);
		totalAddedCells().add(surplusIndexes().size()+prevTotalAddedCells);

		System.out.println("MappingBoardless.java calculateNeighborsIndexes() totalAddedRows : "+totalAddedRows+" - lastTotalAddedRows : "+lastTotalAddedRows);
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() nbrNewCellsBeforeRow 1 : "+Arrays.toString(nbrNewCellsBeforeRow()));
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() i : "+rowsNeighbors().get(rowsNeighbors().size()-1)+2+" - < : "+context.topology().rows().get(SiteType.Cell).size()+3);
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() clem len : "+cumulNbrNewCellsBeforeRow.length+" - inter len : "+inter.length+" - nbrNewCellsBeforeRow len : "+nbrNewCellsBeforeRow.length);
		for (int i=rowsNeighbors().get(rowsNeighbors().size()-1)+2; i<context.topology().rows().get(SiteType.Cell).size()+3; i++)
		{
			System.out.println("MappingBoardless.java calculateNeighborsIndexes() inter["+i+"] += nbrNewCellsBeforeRow()["+(i-1)+"] + clem()["+(i-lastTotalAddedRows)+"];");
			inter[i] += nbrNewCellsBeforeRow()[i-1] + clem()[i-lastTotalAddedRows];
			nbrNewCellsBeforeRow()[i] = nbrNewCellsBeforeRow()[i-1] + nbrNewCellsBeforeRow()[i];
		}
		totalNbrNewCellsBeforeRow().add(nbrNewCellsBeforeRow());
		cumulNbrNewCellsBeforeRow = inter;
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() clem : "+Arrays.toString(cumulNbrNewCellsBeforeRow));
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() inter : "+Arrays.toString(inter));
		
		if (newRow)
			addedRowPerEdgeMove().add(1);
		else
			addedRowPerEdgeMove().add(0);
		
		totalAddedRows().add(lastTotalAddedRows + addedRowPerEdgeMove().get(addedRowPerEdgeMove().size()-1));
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() nbrNewCellsBeforeRow 2 : "+Arrays.toString(nbrNewCellsBeforeRow()));
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() newRow : "+newRow);
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
	 * Creates mapping between initial board and new board, by summing 
	 * the number of cells that were added before a row for each edge 
	 * move done from the start.
	 * @param context
	 * @param limit until which edge move the board is redone.
	 */
	private static void createInitMapping(Context context, int limit)
	{
		nbrNewCellsBeforeRow = new int[context.topology().rows().get(SiteType.Cell).size()+3+totalAddedRows().size()];
		
		for (int i=0; i<limit; i++)
		{
			int[] currTotalNbrNewCellsBeforeRow = totalNbrNewCellsBeforeRow().get(i);
			for (int j=0; j<currTotalNbrNewCellsBeforeRow.length; j++)
			{
				int offset = addedRowPerEdgeMove().get(i);
				nbrNewCellsBeforeRow()[j+offset] += currTotalNbrNewCellsBeforeRow[j];
			}
		}

		// data structures to map between initial plate and new plate
		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		surplusInitIndexes = new HashSet<Integer>();
		
		int maxAddedRow = totalAddedRows().get(totalAddedRows.size()-1);
		System.out.println("MappingBoardless.java cc() cells size : "+context.topology().cells().size());
		System.out.println("MappingBoardless.java cc() cells initDimension : "+((Boardless) context.game().board()).initDimension());
		int initAreaBoard = (int) Math.pow(((Boardless) context.game().board()).initDimension(), 2);
		//for (int prevIndex=0; prevIndex<initAreaBoard; prevIndex++)
		for (Cell c : context.topology().cells())
		{
			int prevIndex = c.index();
			int newIndex = prevIndex + nbrNewCellsBeforeRow()[c.row()+1+maxAddedRow];
			
			mappedInitToNewIndexes().put(prevIndex, newIndex);
			mappedNewToInitIndexes().put(newIndex, prevIndex);
		}
		
		for (int i=0; i<context.topology().cells().size(); i++)
			if (!mappedInitToNewIndexes().containsKey(i))
				surplusInitIndexes().add(i);
	}
	
	private static void undoLastMove(Context context)
	{
		int[] lastNbrNewCellsBeforeRow = totalNbrNewCellsBeforeRow().get(totalNbrNewCellsBeforeRow().size()-1);
		int[] xx = new int[nbrNewCellsBeforeRow().length];
		int offset = addedRowPerEdgeMove().get(totalNbrNewCellsBeforeRow().size()-1);
		int maxAddedRow = totalAddedRows().get(totalAddedRows().size()-1);
		for (int i=0; i<lastNbrNewCellsBeforeRow.length; i++)
		{
			xx[i+offset] = nbrNewCellsBeforeRow()[i+offset] - lastNbrNewCellsBeforeRow[i];
		}
		
		for (Cell c : context.topology().cells())
		{
			int prevIndex = c.index();
			int newIndex = prevIndex + xx[c.row()+1+maxAddedRow];
			
			mappedPrevToNewIndexes().put(prevIndex, newIndex);
			mappedNewToPrevIndexes().put(newIndex, prevIndex);
		}
		
		for (int i=0; i<context.topology().cells().size()+totalAddedCells().get(totalAddedCells().size()-1); i++)
			if (!mappedPrevToNewIndexes().containsKey(i))
				surplusIndexes().add(i);
	}
	
	private static void prt()
	{
		System.out.print("MappingBoardless.java prt() totalNbrNewCellsBeforeRow : [");
		for (int[] i : totalNbrNewCellsBeforeRow)
			System.out.print(Arrays.toString(i)+", ");
		System.out.println("]");
		System.out.println("MappingBoardless.java prt() addedRowPerEdgeMove : "+addedRowPerEdgeMove());
		System.out.println("MappingBoardless.java prt() totalAddedRows : "+totalAddedRows());
		System.out.println("MappingBoardless.java prt() totalAddedCells : "+totalAddedCells());
		System.out.println("MappingBoardless.java prt() mappedPrevToNewIndexes : "+mappedPrevToNewIndexes());
		System.out.println("MappingBoardless.java prt() mappedNewToPrevIndexes : "+mappedNewToPrevIndexes());
		System.out.println("MappingBoardless.java prt() surplusIndexes : "+surplusIndexes());
		System.out.println("MappingBoardless.java prt() mappedInitToNewIndexes : "+mappedInitToNewIndexes());
		System.out.println("MappingBoardless.java prt() mappedNewToInitIndexes : "+mappedNewToInitIndexes());
		System.out.println("MappingBoardless.java prt() surplusInitIndexes : "+surplusInitIndexes());
		System.out.println("MappingBoardless.java prt() rowsNeighbors : "+rowsNeighbors());
		System.out.println("MappingBoardless.java prt() rowsNeighborsMapToCols : "+rowsNeighborsMapToCols());
		System.out.println("MappingBoardless.java prt() addedRowPerEdgeMove : "+Arrays.toString(nbrNewCellsBeforeRow()));
	}
	
	private static void rollback()
	{
		// removing last data TODO : depend until where the rollback is happening
		addedRowPerEdgeMove().remove(addedRowPerEdgeMove().size() - 1);
		totalAddedRows().remove(totalAddedRows().size() - 1);
		totalNbrNewCellsBeforeRow().remove(totalNbrNewCellsBeforeRow().size() - 1);
		totalAddedCells().remove(totalAddedCells().size() - 1);
	}
	
	/**
	 * Creates a mapping between the indexes of the previous board and the 
	 * new board.
	 * @param context
	 * @param move edge move just applied that will help telling what cells 
	 * will be added and so what is going to be the new mapping.
	 * @param fromSize
	 * @param toSize
	 */
	public static void createMappings(Context context, Move move, final int fromSize, final int toSize)
	{

		// data structures to map between current plate and new plate
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>();	
		if (initCells == null) 
		{
			/*for (Cell obj : context.topology().cells()) {
				initCells.add(new Cell(obj)); // Copie indépendante
			}*/
			initCells = new ArrayList<Cell>();
			cumulNbrNewCellsBeforeRow = new int[context.topology().rows().get(SiteType.Cell).size()+3];
		}
		
		if (fromSize < toSize)
		{
			Cell cell = (Cell) context.topology().getGraphElement(SiteType.Cell, move.to());
			
			calculateNeighborsCoordinates(context, cell);
			calculateNeighborsIndexes(context);
			calculateCurrentCellsIndexes(context); // prev to new
			createInitMapping(context, totalNbrNewCellsBeforeRow().size()); // init to new
		}
		else
		{
			createInitMapping(context, totalNbrNewCellsBeforeRow().size()-1); // init to new
			undoLastMove(context); // prev to new
			
			rollback();
		}

		prt();
	}
}