package app.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import game.equipment.container.board.Boardless;
import game.types.board.SiteType;
import game.types.board.TilingBoardlessType;
import other.context.Context;
import other.move.Move;
import other.topology.Cell;
import other.topology.Vertex;

/**
 * Methods to create the mapping that will tell the board how to grow.
 * 
 * @author Clémentine.Sacré
 */
public abstract class MappingBoardlessAbstract
{
	protected HashMap<Integer, Integer> mappedPrevToNewIndexes;
	protected HashMap<Integer, Integer> mappedNewToPrevIndexes;
	protected HashSet<Integer> surplusIndexes; // cells indexes that are being added from one board to another

	protected HashMap<Integer, Integer> mappedInitToNewIndexes;
	protected HashMap<Integer, Integer> mappedNewToInitIndexes;
	protected HashSet<Integer> surplusInitIndexes; // cells indexes that are being added from init board to new one
	
	//----------------------------Cells-----------------------------------------
	// remember the cells added at each step to be able to undo them easily when going back

	protected int nbAddedCellsFromStart = 0; // total number  of row/col (=cell) added since the beginning
	protected int[][] initialHands; // id initiaux des éléments qui ne sont pas sur le plateau (les mains des joueurs et autres)
	protected int[][] initialCells; // matrice du plateau initial pour suivre combien de cellules ont été rajoutées à chaque edge move pour chaque cellule bien précis
	protected int[][] initialCells2;
	protected int[][] initialVertices; // 0 if no vertex at this position on initial board, 1 else

	protected ArrayList<ArrayList<int[]>> cellsRowsColsAddedFromStart = new ArrayList<ArrayList<int[]>>(); // liste de listes de row,col relatives au plateau courant ajoutées pour un edge move
	protected ArrayList<int[]> lastCellsRowsColsAddedFromStart = new ArrayList<int[]>();
	protected ArrayList<int[]> cellsRowsColsAdded; // liste de row,col relatives au plateau courant ajoutées pour un edge move rowsColsAdded
	protected ArrayList<Integer> cellsAddedLeftCols = new ArrayList<Integer>(); // pour chaque edge move combien de colonne ont été crée en négatif
	protected ArrayList<Integer> cellsAddedDownRows = new ArrayList<Integer>(); // pour chaque edge move combien de lignes ont été crée en négatif
	protected ArrayList<Integer> cellsAddedRightCols = new ArrayList<Integer>(); // pour chaque edge move combien de colonne ont été crée en négatif
	protected ArrayList<Integer> cellsAddedUpRows = new ArrayList<Integer>();
	
	//----------------------------Vertices--------------------------------------
	// remember the vertices added at each step to be able to undo them easily when going back
	
	protected ArrayList<Integer> rowsNeighborsVertices; // liste de row en position relatif qui ont été ajoutés. Sont dans l'ordre croissant
	protected HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToColsVertices; // clé row ajoutées, valeur liste de col ajoutées pour cette row. Sont dans l'ordre croissant
	
	protected HashSet<Integer>[] nbAddedColPerRow; // for each row what column where added. 2 elem en plus, un pour une nouvelle row en bas, et un pour une row en haut // = list de row de liste de col
	
	public boolean vertexAddedLeftCol; // pour le dernier edge move, est ce qu'une colonne en vertex a été créée en négatif ? (pourrait utiliser celui des cells, ça va avec)

	
	public ArrayList<Boolean> vertexAddedLeftColFromStart = new ArrayList<Boolean>();
	
	protected ArrayList<HashSet<Integer>[]> nbAddedColPerRowFromStart = new ArrayList<HashSet<Integer>[]>();
	
	
	protected HashSet<Integer>[] lastNbAddedColPerRow;
	protected boolean lastVertexAddedLeftCol;
	
	protected int newTotalIndexesCells;
	
	
	//--------------------------------Getters----------------------------------
	
	public HashMap<Integer, Integer> mappedPrevToNewIndexes()
	{
		return mappedPrevToNewIndexes;
	}
	
	public HashMap<Integer, Integer> mappedNewToPrevIndexes()
	{
		return mappedNewToPrevIndexes;
	}
	
	public HashSet<Integer> surplusIndexes()
	{
		return surplusIndexes;
	}
	
	public HashMap<Integer, Integer> mappedInitToNewIndexes()
	{
		return mappedInitToNewIndexes;
	}
	
	public HashMap<Integer, Integer> mappedNewToInitIndexes()
	{
		return mappedNewToInitIndexes;
	}
	
	public HashSet<Integer> surplusInitIndexes()
	{
		return surplusInitIndexes;
	}
	
	protected ArrayList<Integer> rowsNeighborsVertices()
	{
		return rowsNeighborsVertices;
	}
	
	public HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToColsVertices()
	{
		return rowsNeighborsMapToColsVertices;
	}
	
	public HashSet<Integer>[] nbAddedColPerRow()
	{
		return nbAddedColPerRow;
	}
	
	public int[][] initialHands()
	{
		return initialHands;
	}
	
	public ArrayList<Integer> cellsAddedLeftCols()
	{
		return cellsAddedLeftCols;
	}
	
	public ArrayList<Integer> cellsAddedDownRows()
	{
		return cellsAddedDownRows;
	}
	
	public ArrayList<Integer> cellsAddedRightCols()
	{
		return cellsAddedRightCols;
	}
	
	public ArrayList<Integer> cellsAddedUpRows()
	{
		return cellsAddedUpRows;
	}
	
	public ArrayList<int[]> cellsRowsColsAdded()
	{
		return cellsRowsColsAdded;
	}
	
	public ArrayList<ArrayList<int[]>> cellsRowsColsAddedFromStart()
	{
		return cellsRowsColsAddedFromStart;
	}
	
	public ArrayList<int[]> lastCellsRowsColsAddedFromStart()
	{
		return lastCellsRowsColsAddedFromStart;
	}
	
	public int[][] initialCells()
	{
		return initialCells;
	}
	
	public int[][] initialCells2()
	{
		return initialCells2;
	}
	
	public int nbAddedCellsFromStart()
	{
		return nbAddedCellsFromStart;
	}
	
	public boolean vertexAddedLeftCol()
	{
		return vertexAddedLeftCol;
	}
	
	public ArrayList<Boolean> vertexAddedLeftColFromStart()
	{
		return vertexAddedLeftColFromStart;
	}
	
	public ArrayList<HashSet<Integer>[]> nbAddedColPerRowFromStart()
	{
		return nbAddedColPerRowFromStart;
	}
	
	public boolean lastVertexAddedLeftCol()
	{
		return lastVertexAddedLeftCol;
	}
	
	public HashSet<Integer>[] lastNbAddedColPerRow()
	{
		return lastNbAddedColPerRow;
	}
	
	public int newTotalIndexesCells()
	{
		return newTotalIndexesCells;
	}
	
	public int[][] initialVertices()
	{
		return initialVertices;
	}
	
	//-------------------------------------------------------------------------
	
	public void init(Context context)
	{
		initialHands = new int[context.containers().length-1][];
		for (int i=1; i<context.containers().length; i++)
		{
			int[] initialHand = new int[context.containers()[i].topology().cells().size()];
			for (int j=0; j<context.containers()[i].topology().cells().size(); j++)
			{
				Cell c = context.containers()[i].topology().cells().get(j);
				int prevIndex = c.index();
				initialHand[j] = prevIndex;
				
			}
			initialHands()[i-1] = initialHand;
		}
		
		initialCells = new int[context.topology().rows().get(SiteType.Cell).size()][];
		for (int i=0; i<context.topology().rows().get(SiteType.Cell).size(); i++)
		{
			int nbCol = context.topology().rows().get(SiteType.Cell).get(i).size();
			initialCells[i] = new int[nbCol];
		}
		
		initialCells = new int[context.topology().rows().get(SiteType.Cell).size()][context.topology().columns().get(SiteType.Cell).size()];
		for (Cell c : context.topology().cells())
		{
			
			initialCells[c.row()][c.col()] = 1;
		}
		for (int i=0; i<initialCells.length; i++)
		{
			for (int j=0; j<initialCells[i].length; j++)
			{
				if (initialCells[i][j] == 1)
					initialCells[i][j] = 0;
				else
					initialCells[i][j] = -1;
			}
		}
		
		initialVertices = new int[context.topology().rows().get(SiteType.Vertex).size()][context.topology().columns().get(SiteType.Vertex).size()];
		for (Vertex v : context.topology().vertices())
		{
			initialVertices[v.row()][v.col()] = 1;
		}
		
		initialCells2 = new int[context.topology().rows().get(SiteType.Cell).size()][context.topology().columns().get(SiteType.Cell).size()];
		for (Cell c : context.topology().cells())
		{
			initialCells2[c.row()][c.col()] = 1;
		}
		
		
		if (initialCells != null)
			for (int[] i : initialCells)
				System.out.println("MappingBoardless.java init() initialCells i : "+Arrays.toString(i));
		
	}
	
	public void clean()
	{

		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>(); 

		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		surplusInitIndexes = new HashSet<Integer>(); 
		
		initialHands = null;
		initialCells = null;
		initialCells2 = null;
		
		nbAddedCellsFromStart = 0;
		
		cellsRowsColsAddedFromStart = new ArrayList<ArrayList<int[]>>();
		cellsRowsColsAdded = new ArrayList<int[]>();
		cellsAddedLeftCols = new ArrayList<Integer>();
		cellsAddedDownRows = new ArrayList<Integer>();
		cellsAddedRightCols = new ArrayList<Integer>();
		cellsAddedUpRows = new ArrayList<Integer>();

		rowsNeighborsVertices = new ArrayList<Integer>(); 
		rowsNeighborsMapToColsVertices = new HashMap<Integer, ArrayList<Integer>>();		
		nbAddedColPerRow = null;
		
		vertexAddedLeftCol = false; 
		
	}
	
	public void initForNewBoard(Context context)
	{
		// data structures to map cells between current plate and new plate
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>();
		

		// data structures to map between initial plate and new plate
		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		surplusInitIndexes = new HashSet<Integer>();

		// data structures to map vertices between current plate and new plate
		nbAddedColPerRow = new HashSet[context.topology().rows().get(SiteType.Vertex).size()+2];
		
		vertexAddedLeftCol = false;
		
		cellsRowsColsAdded = new ArrayList<int[]>();
		
		
		
		if (initialCells == null)	
			init(context);
	}
	
	//----------------------------utils-----------------------------------------
	
	public int lastNbColAddedCells()
	{
		if (cellsAddedLeftCols().size() == 0)
			return 0;
		else if (cellsAddedLeftCols().size() == 1)
			return cellsAddedLeftCols().get(0);
		else
		{
			int last = cellsAddedLeftCols().size()-1;
			return cellsAddedLeftCols().get(last) - cellsAddedLeftCols().get(last-1);
		}
		
	}
	public int lastNbRowAddedCells()
	{
		if (cellsAddedDownRows().size() == 0)
			return 0;
		else if (cellsAddedDownRows().size() == 1)
			return cellsAddedDownRows().get(0);
		else
		{
			int last = cellsAddedDownRows().size()-1;
			return cellsAddedDownRows().get(last) - cellsAddedDownRows().get(last-1);
		}
		
	}

	//----------------------------Cells-----------------------------------------
	
	protected void prevToNew(Context context)
	{
		// prev to new
		int addedCells = 0;
		int[] currAddedRowCol = cellsRowsColsAdded().size() > 0 ? cellsRowsColsAdded().get(0) : new int[]{};
		int rowColIndex = cellsRowsColsAdded().size() > 0 ? 0 : 1;
		// mapping existing cells
		for (Cell c : context.topology().cells())
		{
			while (rowColIndex < cellsRowsColsAdded().size())
			{
				if (c.row() == currAddedRowCol[0])
				{
					if (c.col() > currAddedRowCol[1])
					{
						addedCells += 1;
						rowColIndex ++;
						if (rowColIndex < cellsRowsColsAdded().size())
							currAddedRowCol = cellsRowsColsAdded().get(rowColIndex);
						else
							break;
					}
					else
						break;
				}	
				else if (c.row() > currAddedRowCol[0])
				{
					addedCells += 1;
					rowColIndex ++;
					if (rowColIndex < cellsRowsColsAdded().size())
						currAddedRowCol = cellsRowsColsAdded().get(rowColIndex);
					else
						break;
				}
				else
					break;
			}
			
			int newIndex = c.index() + addedCells;
			mappedPrevToNewIndexes().put(c.index(), newIndex);
			mappedNewToPrevIndexes().put(newIndex, c.index());
		}
		

		// mapping other containers than board
		for (int i=1; i<context.containers().length; i++)
			for (int j=0; j<context.containers()[i].topology().cells().size(); j++)
			{
				int prevIndex = context.containers()[i].topology().cells().get(j).index();
				int newIndex = prevIndex + cellsRowsColsAdded().size();
				mappedPrevToNewIndexes().put(prevIndex, newIndex);
				mappedNewToPrevIndexes().put(newIndex, prevIndex);
			}
		
		// saving new cells that cannot be mapped from previous board as they are new
		for (int i=0; i<context.topology().cells().size()+cellsRowsColsAdded().size(); i++)
			if (!mappedNewToPrevIndexes().containsKey(i))
				surplusIndexes().add(i);
		
		nbAddedCellsFromStart += cellsRowsColsAdded().size();
		
	}

	protected void initToNew(Context context)
	{
		// init to new
		int last = cellsAddedLeftCols().size()-1;
		int nbAddedCol = cellsAddedLeftCols().get(last);
		int nbAddedRow = cellsAddedDownRows().get(last);
		int initAddedCells = 0;
		int[] currRowCol = cellsRowsColsAdded().size() > 0 ? cellsRowsColsAdded().get(0) : new int[]{};
		int rowColIndex = cellsRowsColsAdded().size() > 0 ? 0 : 1;
		int cellIndex = 0;
		// mapping existing cells
		for (int r=0; r<initialCells().length; r++)
			for (int c=0; c<initialCells()[r].length; c++)
			{
				if (initialCells()[r][c] != -1) 
				{
					while (rowColIndex < cellsRowsColsAdded().size())
					{
						if (r == (currRowCol[0]-nbAddedRow))
						{
							if (c > (currRowCol[1]-nbAddedCol))
							{
								initAddedCells += 1;
								rowColIndex ++;
								if (rowColIndex < cellsRowsColsAdded().size())
									currRowCol = cellsRowsColsAdded().get(rowColIndex);
								else
									break;
							}
							else
								break;
						}	
						else if (r > (currRowCol[0]-nbAddedRow))
						{
							initAddedCells += 1;
							rowColIndex ++;
							if (rowColIndex < cellsRowsColsAdded().size())
								currRowCol = cellsRowsColsAdded().get(rowColIndex);
							else
								break;
						}
						else
							break;
					}

					System.out.println("MappingBoardlessAbstract.java initToNew() initialCells()[r][c] : "+initialCells()[r][c]+" - r : "+r+" - c : "+c+" - becomes : "+(cellIndex + initialCells()[r][c]));
					initialCells()[r][c] += initAddedCells;
					int newIndex = cellIndex + initialCells()[r][c];
					mappedInitToNewIndexes().put(cellIndex, newIndex);
					mappedNewToInitIndexes().put(newIndex, cellIndex);
	
					cellIndex ++;
				}
			}
		
		// mapping other containers than board
		for (int i=0; i<initialHands().length; i++)
			for (int j=0; j<initialHands()[i].length; j++)
			{
				int prevIndex = initialHands()[i][j];
				int newIndex = prevIndex + nbAddedCellsFromStart();
				mappedInitToNewIndexes().put(prevIndex, newIndex);
				mappedNewToInitIndexes().put(newIndex, prevIndex);
			}
		
		// saving new cells that cannot be mapped from previous board as they are new
		for (int i=0; i<cellIndex+nbAddedCellsFromStart(); i++)
			if (!mappedNewToInitIndexes().containsKey(i))
				surplusInitIndexes().add(i);
		if (initialCells != null)
			for (int[] i : initialCells)
				System.out.println("MappingBoardlessAbstract.java initToNew() initialCells i : "+Arrays.toString(i));
	}
	
	protected void prevToNewSame(Context context)
	{
		for (Cell c : context.topology().cells())
		{			
			mappedPrevToNewIndexes().put(c.index(), c.index());
			mappedNewToPrevIndexes().put(c.index(), c.index());
		}

		// mapping other containers than board
		for (int i=1; i<context.containers().length; i++)
			for (int j=0; j<context.containers()[i].topology().cells().size(); j++)
			{
				int prevIndex = context.containers()[i].topology().cells().get(j).index();
				mappedPrevToNewIndexes().put(prevIndex, prevIndex);
				mappedNewToPrevIndexes().put(prevIndex, prevIndex);
			}
	}
	
	protected void removeLast(Context context)
	{
		int last = cellsAddedLeftCols().size()-1;
		lastCellsRowsColsAddedFromStart = (ArrayList<int[]>) cellsRowsColsAddedFromStart.get(last).clone();
		cellsRowsColsAddedFromStart().remove(last);
		cellsAddedLeftCols().remove(last);
		cellsAddedDownRows().remove(last);
		
		// TODO fix this not normal there should be something (because in hexa we dont fill this structure)
		lastNbAddedColPerRow = nbAddedColPerRowFromStart().size() > 0 ? nbAddedColPerRowFromStart().get(last).clone() : new HashSet[context.topology().rows().get(SiteType.Vertex).size()];
		lastVertexAddedLeftCol = vertexAddedLeftColFromStart().size() > 0 ? vertexAddedLeftColFromStart().get(last) : false;
		if (nbAddedColPerRowFromStart().size()>0)
			nbAddedColPerRowFromStart().remove(last);
		if (vertexAddedLeftColFromStart().size()>0)
			vertexAddedLeftColFromStart().remove(last);
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
	public abstract void calculateNeighborsCoordinates(Context context, Cell cell);
	
	public abstract void calculateVerticesNeighborsCoordinates(Context context, Cell cell);
	
	protected abstract void prevToNewInit(Context context);
	
	protected abstract void initToNewInit(Context context);
	
	protected abstract void initToNewRollBack(Context context);
	
	protected abstract void prevToNewRollBack(Context context);
	
	public void rollback(Context context)
	{
		// removing last data TODO : depend until where the rollback is happening
		prevToNewRollBack(context);
		initToNewRollBack(context);
		removeLast(context);
	}
	
	public void rollbackToInit(Context context)
	{
		prevToNewInit(context);
		initToNewInit(context);
		initialCells = null;
	}
	
	public abstract void forward(Context context, Cell cell);
	
	public void updateNewTotalIndexesCells()
	{
		newTotalIndexesCells = mappedPrevToNewIndexes().size() + surplusIndexes.size();
	}
	
	public void keepSameSize(Context context)
	{
		prevToNewSame(context);
		initToNew(context);
	}
}