package app.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;

import game.equipment.container.board.Boardless;
import game.types.board.SiteType;
import other.context.Context;
import other.move.Move;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Vertex;

/**
 * Methods to create the mapping that will tell the board how to grow.
 * 
 * @author Clémentine.Sacré
 */
public class MappingBoardless
{
	
	private static HashMap<Integer, Integer> mappedPrevToNewIndexes;
	private static HashMap<Integer, Integer> mappedNewToPrevIndexes;
	private static HashSet<Integer> surplusIndexes; // cells indexes that are being added from one board to another

	private static HashMap<Integer, Integer> mappedInitToNewIndexes;
	private static HashMap<Integer, Integer> mappedNewToInitIndexes;
	private static HashSet<Integer> surplusInitIndexes; // cells indexes that are being added from init board to new one
	
	//----------------------------Cells-----------------------------------------
	// remember the cells added at each step to be able to undo them easily when going back

	private static int nbAddedCellsFromStart = 0; // total number  of row/col (=cell) added since the beginning
	private static int[][] initialHands; // id initiaux des éléments qui ne sont pas sur le plateau (les mains des joueurs et autres)
	private static int[][] intialCells; // matrice du plateau initial pour suivre combien de cellules ont été rajoutées à chaque edge move pour chaque cellule bien précis

	private static ArrayList<ArrayList<int[]>> cellsRowsColsAddedFromStart = new ArrayList<ArrayList<int[]>>(); // liste de listes de row,col relatives au plateau courant ajoutées pour un edge move
	private static ArrayList<int[]> cellsRowsColsAdded; // liste de row,col relatives au plateau courant ajoutées pour un edge move rowsColsAdded
	private static ArrayList<Integer> cellsAddedLeftCols = new ArrayList<Integer>(); // pour chaque edge move combien de colonne ont été crée en négatif
	private static ArrayList<Integer> cellsAddedDownRows = new ArrayList<Integer>(); // pour chaque edge move combien de lignes ont été crée en négatif

	//----------------------------Vertices--------------------------------------
	// remember the vertices added at each step to be able to undo them easily when going back
	
	private static ArrayList<Integer> rowsNeighborsVertices; // liste de row en position relatif qui ont été ajoutés. Sont dans l'ordre croissant
	private static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToColsVertices; // clé row ajoutées, valeur liste de col ajoutées pour cette row. Sont dans l'ordre croissant
	
	private static HashSet<Integer>[] nbAddedColPerRow; // for each row what column where added. 2 elem en plus, un pour une nouvelle row en bas, et un pour une row en haut // = list de row de liste de col
	
	public static boolean vertexAddedLeftCol; // pour le dernier edge move, est ce qu'une colonne en vertex a été créée en négatif ? (pourrait utiliser celui des cells, ça va avec)

	
	public static ArrayList<Boolean> vertexAddedLeftColFromStart = new ArrayList<Boolean>();
	
	private static ArrayList<HashSet<Integer>[]> nbAddedColPerRowFromStart = new ArrayList<HashSet<Integer>[]>();
	
	
	private static HashSet<Integer>[] lastNbAddedColPerRow;
	private static boolean lastVertexAddedLeftCol;
	
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
	
	private static ArrayList<Integer> rowsNeighborsVertices()
	{
		return rowsNeighborsVertices;
	}
	
	public static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToColsVertices()
	{
		return rowsNeighborsMapToColsVertices;
	}
	
	public static HashSet<Integer>[] nbAddedColPerRow()
	{
		return nbAddedColPerRow;
	}
	
	public static int[][] initialHands()
	{
		return initialHands;
	}
	
	public static ArrayList<Integer> cellsAddedLeftCols()
	{
		return cellsAddedLeftCols;
	}
	
	public static ArrayList<Integer> cellsAddedDownRows()
	{
		return cellsAddedDownRows;
	}
	
	public static ArrayList<int[]> cellsRowsColsAdded()
	{
		return cellsRowsColsAdded;
	}
	
	public static ArrayList<ArrayList<int[]>> cellsRowsColsAddedFromStart()
	{
		return cellsRowsColsAddedFromStart;
	}
	
	public static int[][] initialCells()
	{
		return intialCells;
	}
	
	public static int nbAddedCellsFromStart()
	{
		return nbAddedCellsFromStart;
	}
	
	public static boolean vertexAddedLeftCol()
	{
		return vertexAddedLeftCol;
	}
	
	public static ArrayList<Boolean> vertexAddedLeftColFromStart()
	{
		return vertexAddedLeftColFromStart;
	}
	
	public static ArrayList<HashSet<Integer>[]> nbAddedColPerRowFromStart()
	{
		return nbAddedColPerRowFromStart;
	}
	
	public static boolean lastVertexAddedLeftCol()
	{
		return lastVertexAddedLeftCol;
	}
	
	public static HashSet<Integer>[] lastNbAddedColPerRow()
	{
		return lastNbAddedColPerRow;
	}
	
	//-------------------------------------------------------------------------
	
	public static void init(Context context)
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
		
		intialCells = new int[context.topology().rows().get(SiteType.Cell).size()][]; // TODO : ne pas harcoder la taille
		for (int i=0; i<context.topology().rows().get(SiteType.Cell).size(); i++)
		{
			int nbCol = context.topology().rows().get(SiteType.Cell).get(i).size();
			intialCells[i] = new int[nbCol];
		}
	}
	
	public static void clean()
	{

		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>(); 

		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		surplusInitIndexes = new HashSet<Integer>(); 
		
		initialHands = null;
		intialCells = null;
		
		nbAddedCellsFromStart = 0;
		
		cellsRowsColsAddedFromStart = new ArrayList<ArrayList<int[]>>();
		cellsRowsColsAdded = new ArrayList<int[]>();
		cellsAddedLeftCols = new ArrayList<Integer>();
		cellsAddedDownRows = new ArrayList<Integer>();

		rowsNeighborsVertices = new ArrayList<Integer>(); 
		rowsNeighborsMapToColsVertices = new HashMap<Integer, ArrayList<Integer>>();		
		nbAddedColPerRow = null;
		
		vertexAddedLeftCol = false; 
		
	}

	//----------------------------Cells-----------------------------------------
	
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
		boolean newColB = false;
		boolean newRowB = false;
		int row = cell.row();
		int col = cell.col();
		for (int r=-1; r<=1; r++)
			for (int c=-1; c<=1; c++)
			{
				int newRow = row + r;
				int newCol = col + c;
				if (newCol < 0 && !newColB)
					newColB = true;
				if (newRow < 0 && !newRowB)
					newRowB = true;
				Cell newCell = context.topology().getCellWithCoords(newRow, newCol, 0); //TODO : 0 for only flat game?
				if (newCell == null)
				{
					cellsRowsColsAdded().add(new int[] {newRow, newCol});
				}
			}
		cellsRowsColsAddedFromStart().add(cellsRowsColsAdded());
		

		// store if an under row of left column have been created
		if (cellsAddedLeftCols().size() == 0) 
		{
			cellsAddedLeftCols().add(newColB ? 1 : 0); 
			cellsAddedDownRows().add(newRowB ? 1 : 0); 
		}
		else
		{
			int last = cellsAddedLeftCols().size()-1; // TODO en faire une variable globale, utilisée à pas mal d'endroit
			cellsAddedLeftCols().add(cellsAddedLeftCols().get(last) + (newColB ? 1 : 0)); 
			cellsAddedDownRows().add(cellsAddedDownRows().get(last) + (newRowB ? 1 : 0)); 
		}
	}

	private static void prevToNew(Context context)
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

	private static void initToNew(Context context)
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

				initialCells()[r][c] += initAddedCells;
				int newIndex = cellIndex + initialCells()[r][c];
				mappedInitToNewIndexes().put(cellIndex, newIndex);
				mappedNewToInitIndexes().put(newIndex, cellIndex);

				cellIndex ++;
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
	}
	

	private static void prevToNewRollBack(Context context)
	{
		// prev to new
		int last = cellsAddedLeftCols().size()-1;
		int addedCells = 0;
		ArrayList<int[]> lastCellsRowsColsAdded = cellsRowsColsAddedFromStart().get(last);
		int[] currAddedRowCol = lastCellsRowsColsAdded.size() > 0 ? lastCellsRowsColsAdded.get(0) : new int[]{};
		int nbAddedCol = cellsAddedLeftCols().get(last) > 0 ? 1 : 0;
		int nbAddedRow = cellsAddedDownRows().get(last) > 0 ? 1 : 0;
		int rowColIndex = lastCellsRowsColsAdded.size() > 0 ? 0 : 1;
		// mapping existing cells
		for (Cell c : context.topology().cells())
		{
			boolean flag = true;
			while (rowColIndex < lastCellsRowsColsAdded.size())
			{
				if (c.row() == currAddedRowCol[0]+nbAddedRow)
				{
					if (c.col() == currAddedRowCol[1]+nbAddedCol)
					{
						flag = false;
						addedCells += 1;
						rowColIndex ++;
						if (rowColIndex < lastCellsRowsColsAdded.size())
							currAddedRowCol = lastCellsRowsColsAdded.get(rowColIndex);
						break;
					}
					else if (c.col() > currAddedRowCol[1]+nbAddedCol)
					{
						addedCells += 1;
						rowColIndex ++;
						if (rowColIndex < lastCellsRowsColsAdded.size())
							currAddedRowCol = lastCellsRowsColsAdded.get(rowColIndex);
						else
							break;
					}
					else
						break;
				}	
				else if (c.row() > currAddedRowCol[0]+nbAddedRow)
				{
					addedCells += 1;
					rowColIndex ++;
					if (rowColIndex < lastCellsRowsColsAdded.size())
						currAddedRowCol = lastCellsRowsColsAdded.get(rowColIndex);
					else
						break;
				}
				else
					break;
			}
			
			
			if (flag)
			{
				int newIndex = c.index() - addedCells;
				mappedPrevToNewIndexes().put(c.index(), newIndex);
				mappedNewToPrevIndexes().put(newIndex, c.index());
			}
		}
		
		// mapping other containers than board
		for (int i=1; i<context.containers().length; i++)
			for (int j=0; j<context.containers()[i].topology().cells().size(); j++)
			{
				int prevIndex = context.containers()[i].topology().cells().get(j).index();
				int newIndex = prevIndex - lastCellsRowsColsAdded.size();
				mappedPrevToNewIndexes().put(prevIndex, newIndex);
				mappedNewToPrevIndexes().put(newIndex, prevIndex);
			}
		
		// saving new cells that cannot be mapped from previous board as they are new
		for (int i=0; i<context.topology().cells().size()-lastCellsRowsColsAdded.size(); i++)
			if (!mappedNewToPrevIndexes().containsKey(i))
				surplusIndexes().add(i);
	}
	
	private static void initToNewRollBack(Context context)
	{
		// init to new
		int last = cellsAddedLeftCols().size()-1;
		int nbAddedCol = cellsAddedLeftCols().get(last);
		int nbAddedRow = cellsAddedDownRows().get(last);
		int initAddedCells = 0;
		ArrayList<int[]> lastCellsRowsColsAdded = cellsRowsColsAddedFromStart().get(last);
		int[] currRowCol = lastCellsRowsColsAdded.size() > 0 ? lastCellsRowsColsAdded.get(0) : new int[]{};
		int rowColIndex = lastCellsRowsColsAdded.size() > 0 ? 0 : 1;
		int cellIndex = 0;
		nbAddedCellsFromStart -= lastCellsRowsColsAdded.size();
		// mapping existing cells
		for (int r=0; r<initialCells().length; r++)
			for (int c=0; c<initialCells()[r].length; c++)
			{
				while (rowColIndex < lastCellsRowsColsAdded.size())
				{
					if (r == (currRowCol[0]-nbAddedRow))
					{
						if (c > (currRowCol[1]-nbAddedCol))
						{
							initAddedCells += 1;
							rowColIndex ++;
							if (rowColIndex < lastCellsRowsColsAdded.size())
								currRowCol = lastCellsRowsColsAdded.get(rowColIndex);
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
						if (rowColIndex < lastCellsRowsColsAdded.size())
							currRowCol = lastCellsRowsColsAdded.get(rowColIndex);
						else
							break;
					}
					else
						break;
				}

				initialCells()[r][c] -= initAddedCells;
				int newIndex = cellIndex + initialCells()[r][c];
				mappedInitToNewIndexes().put(cellIndex, newIndex);
				mappedNewToInitIndexes().put(newIndex, cellIndex);

				cellIndex ++;
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
	}
	
	private static void prevToNewSame(Context context)
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
	
	private static void removeLast()
	{
		int last = cellsAddedLeftCols().size()-1;
		cellsRowsColsAddedFromStart().remove(last);
		cellsAddedLeftCols().remove(last);
		cellsAddedDownRows().remove(last);
		
		lastNbAddedColPerRow = nbAddedColPerRowFromStart().get(last).clone();
		lastVertexAddedLeftCol = vertexAddedLeftColFromStart().get(last);
		nbAddedColPerRowFromStart().remove(last);
		vertexAddedLeftColFromStart().remove(last);
	}
	
	//----------------------------Vertices--------------------------------------
	
	private static void calculateVerticesNeighborsCoordinates(Context context, Cell cell)
	{
		Vertex vertex = cell.vertices().get(0); 
		// make sure to have bottom left vertex of the cell
		for (Vertex v : cell.vertices())
			if (v.index() < vertex.index())
				vertex = v;
		int row = vertex.row();
		int col = vertex.col();
		rowsNeighborsMapToColsVertices = new HashMap<Integer, ArrayList<Integer>>();
		rowsNeighborsVertices = new ArrayList<Integer>();
		
		for (int r=0; r<=1; r++)
			for (int c=-1; c<=2; c+=3)
			{
				int newRow = row + r;
				int newCol = col + c;
				Vertex newVertex = context.topology().getVertexWithCoords(newRow, newCol, 0); //TODO : 0 for only flat game?
				if (newVertex == null)
				{
					if (rowsNeighborsMapToColsVertices().containsKey(newRow))
						rowsNeighborsMapToColsVertices().get(newRow).add(newCol);
					else
					{
						ArrayList<Integer> set = new ArrayList<Integer>();
						set.add(newCol);
						rowsNeighborsMapToColsVertices().put(newRow, set);
						rowsNeighborsVertices().add(newRow);
					}
				}
			}
		
		for (int r=-1; r<=2; r+=3)
			for (int c=-1; c<=2; c++)
			{
				int newRow = row + r;
				int newCol = col + c;
				Vertex newVertex = context.topology().getVertexWithCoords(newRow, newCol, 0); //TODO : 0 for only flat game?
				if (newVertex == null)
				{
					if (rowsNeighborsMapToColsVertices().containsKey(newRow))
						rowsNeighborsMapToColsVertices().get(newRow).add(newCol);
					else
					{
						ArrayList<Integer> set = new ArrayList<Integer>();
						set.add(newCol);
						rowsNeighborsMapToColsVertices().put(newRow, set);
						rowsNeighborsVertices().add(newRow);
					}
				}
			}
	}
	
	
	private static void calculateVerticesNeighborsIndexes(Context context)
	{

		for (Integer r : rowsNeighborsVertices())
		{
			ArrayList<Integer> colsNeighbors = rowsNeighborsMapToColsVertices().get(r);
			Collections.sort(colsNeighbors);
			
			for (Integer c : colsNeighbors)
			{	
				if (nbAddedColPerRow()[r+1] != null)
					nbAddedColPerRow()[r+1].add(c);
				else
				{
					HashSet<Integer> arr = new HashSet<Integer>();
					arr.add(c);
					nbAddedColPerRow()[r+1] = arr;
				}
				
				if (c<0 && !vertexAddedLeftCol())
					vertexAddedLeftCol = true;
			}
		}
		nbAddedColPerRowFromStart().add(nbAddedColPerRow());
		vertexAddedLeftColFromStart().add(vertexAddedLeftCol());
	}
	
	
	//--------------------------------------------------------------------------
	
	private static void prt()
	{
		System.out.println("MappingBoardless.java prt() mappedPrevToNewIndexes : "+mappedPrevToNewIndexes());
		System.out.println("MappingBoardless.java prt() mappedNewToPrevIndexes : "+mappedNewToPrevIndexes());
		System.out.println("MappingBoardless.java prt() surplusIndexes : "+surplusIndexes());
		System.out.println("MappingBoardless.java prt() mappedInitToNewIndexes : "+mappedInitToNewIndexes());
		System.out.println("MappingBoardless.java prt() mappedNewToInitIndexes : "+mappedNewToInitIndexes());
		System.out.println("MappingBoardless.java prt() surplusInitIndexes : "+surplusInitIndexes());
		
		System.out.println("MappingBoardless.java prt() rowsNeighborsVertices : "+rowsNeighborsVertices());
		System.out.println("MappingBoardless.java prt() rowsNeighborsMapToCols Vertices: "+rowsNeighborsMapToColsVertices());
		System.out.println("MappingBoardless.java prt() rowsNeighborsMapToCols nbAddedColPerRow: "+nbAddedColPerRow());
		

		for (HashSet<Integer>[] cc : MappingBoardless.nbAddedColPerRowFromStart())
			System.out.println("MappingBoardless.java prt() nbAddedColPerRowFromStart nb : "+Arrays.toString(cc));
		
		for (int[] i : intialCells)
			System.out.println("MappingBoardless.java prt() intialCells i : "+Arrays.toString(i));
	}
	
	private static void rollback(Context context)
	{
		// removing last data TODO : depend until where the rollback is happening
		prevToNewRollBack(context);
		initToNewRollBack(context);
		removeLast();
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
		
		if (intialCells == null)	
			init(context);
		
		if (fromSize < toSize)
		{
			Cell cell = (Cell) context.topology().getGraphElement(SiteType.Cell, move.to());
			
			calculateNeighborsCoordinates(context, cell);

			prevToNew(context);
			initToNew(context);
			
			calculateVerticesNeighborsCoordinates(context, cell);
			calculateVerticesNeighborsIndexes(context);
		}
		else if(fromSize == toSize)
		{
			prevToNewSame(context);
			initToNew(context);
		}
		else
		{							
			rollback(context);
		}

		prt();
	}
}