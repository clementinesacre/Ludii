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
	
	private static HashMap<Integer, Integer> mappedPrevToNewIndexesVertices;
	private static HashMap<Integer, Integer> mappedNewToPrevIndexesVertices;
	private static HashSet<Integer> surplusIndexesVertices; // vertices indexes that are being added from one board to another

	private static HashMap<Integer, Integer> mappedInitToNewIndexesVertices;
	private static HashMap<Integer, Integer> mappedNewToInitIndexesVertices;
	private static HashSet<Integer> surplusInitIndexesVertices; // vertices indexes that are being added from init board to new one
	
	private static HashMap<Integer, Integer> mappedPrevToNewIndexesEdges;
	private static HashMap<Integer, Integer> mappedNewToPrevIndexesEdges;
	private static HashSet<Integer> surplusIndexesEdges; // edges indexes that are being added from one board to another

	private static HashMap<Integer, Integer> mappedInitToNewIndexesEdges;
	private static HashMap<Integer, Integer> mappedNewToInitIndexesEdges;
	private static HashSet<Integer> surplusInitIndexesEdges; // edges indexes that are being added from init board to new one
	
	private static int[][] cc = new int[][] { // coordonnées relatives des edges
		{-3, -1}, {-3, 1}, {-3, 3}, 
		{-2, -2}, {-2, 0}, {-2, 2}, {-2, 4}, 
		{-1, -1}, {-1, 3}, 
		{0, -2}, {0, 4}, 
		{1, 3}, {1, -1}, 
		{2, -2}, {2, 0}, {2, 2}, {2, 4}, 
		{3, -1}, {3, 1}, {3, 3}};

	private static int[][][] bb = new int[][][] { // pour chaque edge dans cc, les deux vertex liés et ce qu'il faut appliqué comme calcul par rapport au vertice en bas a gauche de la cell
		{{-1, 0}, {-1, -1}}, {{-1, 0}, {-1, 0}}, {{-1, 2}, {-1, 1}}, 
		{{-1, -1}, {0, -1}}, {{0, 0}, {-1, 0}}, {{-1, 1}, {0, 1}}, {{0, 2}, {-1, 2}}, 
		{{0, 0}, {0 ,-1}}, {{0, 1}, {0, 2}}, 
		{{0, -1}, {1, -1}}, {{1, 2}, {0, 2}}, 
		{{1, 2}, {1, 1}}, {{1, -1}, {1, 0}}, 
		{{1, -1}, {2, -1}}, {{2, 0}, {1, 0}}, {{1, 1}, {2, 1}}, {{2, 2}, {1, 2}}, 
		{{2, -1}, {2, 0}}, {{2, 0}, {2, 1}}, {{2, 1}, {2, 2}}};
	

	//----------------------------Cells-----------------------------------------
	// remember the cells added at each step to be able to undo them easily when going back
	private static ArrayList<int[]> totalNbrNewCellsBeforeRow = new ArrayList<int[]>();
	private static ArrayList<Integer> addedRowPerEdgeMove = new ArrayList<Integer>(); 
	private static ArrayList<Integer> totalAddedRows = new ArrayList<Integer>();
	private static ArrayList<Integer> totalAddedCells = new ArrayList<Integer>(); 
	
	private static ArrayList<Integer> rowsNeighbors; 
	private static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToCols; 
	private static int[] nbrNewCellsBeforeRow;
	private static int[] cumulNbrNewCellsBeforeRow;

	private static List<Cell> initCells;

	//----------------------------Vertices--------------------------------------
	// remember the vertices added at each step to be able to undo them easily when going back
	private static ArrayList<int[]> totalNbrNewVerticesBeforeRow = new ArrayList<int[]>();
	private static ArrayList<Integer> addedRowPerEdgeMoveVertices = new ArrayList<Integer>(); 
	public static ArrayList<Integer> totalAddedRowsVertices = new ArrayList<Integer>();
	public static ArrayList<Integer> totalAddedColsVertices = new ArrayList<Integer>();
	private static ArrayList<Integer> totalAddedVertices = new ArrayList<Integer>(); 
	
	private static ArrayList<Integer> rowsNeighborsVertices; 
	private static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToColsVertices; 
	private static int[] nbrNewVerticesBeforeRow;
	private static int[] cumulNbrNewVerticesBeforeRow;
	
	static HashMap<Integer, int[]> xx; 
	private static HashMap<Integer, HashMap<Integer, Integer>> xxretourne; 
	static HashMap<Integer, int[]> vertexToCoord; 
	public static ArrayList<Integer>[] nbAddedColPerRow;

	//----------------------------Edges--------------------------------------
	// remember the vertices added at each step to be able to undo them easily when going back
	private static ArrayList<int[]> totalNbrNewEdgesBeforeRow = new ArrayList<int[]>();
	private static ArrayList<Integer> addedRowPerEdgeMoveEdges = new ArrayList<Integer>(); 
	private static ArrayList<Integer> totalAddedRowsEdges = new ArrayList<Integer>();
	private static ArrayList<Integer> totalAddedEdges = new ArrayList<Integer>(); 
	
	private static ArrayList<Integer> rowsNeighborsEdges; 
	private static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToColsEdges; 
	private static int[] nbrNewEdgesBeforeRow;
	private static int[] cumulNbrNewEdgesBeforeRow;
	
	static HashMap<Integer, int[]> xx2; 
	private static HashMap<Integer, HashMap<Integer, Integer>> mo; 
	static HashMap<Integer, int[]> edgeToV; 
	static HashMap<Integer, ArrayList<Integer>> stru; 

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
	
	public static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToCols()
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
	
	private static int[] cumulNbrNewCellsBeforeRow()
	{
		return cumulNbrNewCellsBeforeRow;
	}
	

	
	public static HashMap<Integer, Integer> mappedPrevToNewIndexesVertices()
	{
		return mappedPrevToNewIndexesVertices;
	}
	
	public static HashMap<Integer, Integer> mappedNewToPrevIndexesVertices()
	{
		return mappedNewToPrevIndexesVertices;
	}
	
	public static HashSet<Integer> surplusIndexesVertices()
	{
		return surplusIndexesVertices;
	}
	
	public static HashMap<Integer, Integer> mappedInitToNewIndexesVertices()
	{
		return mappedInitToNewIndexesVertices;
	}
	
	public static HashMap<Integer, Integer> mappedNewToInitIndexesVertices()
	{
		return mappedNewToInitIndexesVertices;
	}
	
	public static HashSet<Integer> surplusInitIndexesVertices()
	{
		return surplusInitIndexesVertices;
	}
	
	private static ArrayList<Integer> rowsNeighborsVertices()
	{
		return rowsNeighborsVertices;
	}
	
	public static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToColsVertices()
	{
		return rowsNeighborsMapToColsVertices;
	}
	
	private static int[] nbrNewVerticesBeforeRow()
	{
		return nbrNewVerticesBeforeRow;
	}
	
	private static ArrayList<int[]> totalNbrNewVerticesBeforeRow()
	{
		return totalNbrNewVerticesBeforeRow;
	}
	
	private static ArrayList<Integer> addedRowPerEdgeMoveVertices()
	{
		return addedRowPerEdgeMoveVertices;
	}
	
	private static ArrayList<Integer> totalAddedRowsVertices()
	{
		return totalAddedRowsVertices;
	}
	
	private static ArrayList<Integer> totalAddedVertices()
	{
		return totalAddedVertices;
	}
	
	private static int[] cumulNbrNewVerticesBeforeRow()
	{
		return cumulNbrNewVerticesBeforeRow;
	}
	

	
	public static HashMap<Integer, Integer> mappedPrevToNewIndexesEdges()
	{
		return mappedPrevToNewIndexesEdges;
	}
	
	public static HashMap<Integer, Integer> mappedNewToPrevIndexesEdges()
	{
		return mappedNewToPrevIndexesEdges;
	}
	
	public static HashSet<Integer> surplusIndexesEdges()
	{
		return surplusIndexesEdges;
	}
	
	public static HashMap<Integer, Integer> mappedInitToNewIndexesEdges()
	{
		return mappedInitToNewIndexesEdges;
	}
	
	public static HashMap<Integer, Integer> mappedNewToInitIndexesEdges()
	{
		return mappedNewToInitIndexesEdges;
	}
	
	public static HashSet<Integer> surplusInitIndexesEdges()
	{
		return surplusInitIndexesEdges;
	}
	
	private static ArrayList<Integer> rowsNeighborsEdges()
	{
		return rowsNeighborsEdges;
	}
	
	public static HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToColsEdges()
	{
		return rowsNeighborsMapToColsEdges;
	}
	
	private static int[] nbrNewEdgesBeforeRow()
	{
		return nbrNewEdgesBeforeRow;
	}
	
	private static ArrayList<int[]> totalNbrNewEdgesBeforeRow()
	{
		return totalNbrNewEdgesBeforeRow;
	}
	
	private static ArrayList<Integer> addedRowPerEdgeMoveEdges()
	{
		return addedRowPerEdgeMoveEdges;
	}
	
	private static ArrayList<Integer> totalAddedRowsEdges()
	{
		return totalAddedRowsEdges;
	}
	
	private static ArrayList<Integer> totalAddedEdges()
	{
		return totalAddedEdges;
	}
	
	private static int[] cumulNbrNewEdgesBeforeRow()
	{
		return cumulNbrNewEdgesBeforeRow;
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
		int row = cell.row();
		int col = cell.col();
		System.out.println("MappingBoardless.java calculateNeighborsCoordinates() vertex cell : "+cell.vertices());
		System.out.println("MappingBoardless.java calculateNeighborsCoordinates() edges cell : "+cell.edges());
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
				inter[r+1+lastTotalAddedRows] += inter[r+lastTotalAddedRows] + cumulNbrNewCellsBeforeRow()[r+1];
			}
			for (Integer c : colsNeighbors)
			{
				int newIndex;
				if (r < 0) //TODO
				{
					newIndex = nbrNewCellsBeforeRow()[r+2];
					nbrNewCellsBeforeRow()[r+2] += 1;
					inter[r+2+lastTotalAddedRows] += 1 + cumulNbrNewCellsBeforeRow()[r+2];
				}
				else
				{
					// check on the right of the cell
					Cell rightNeighbor = context.topology().getCellWithCoords(r, c+1, 0); // TODO si r < 0 ou r >= size ça sert à rien de looper sur les coord
					if (rightNeighbor != null)
					{
						newIndex = rightNeighbor.index() + nbrNewCellsBeforeRow()[r+1];
						nbrNewCellsBeforeRow()[r+1] += 1;
						inter[r+1+lastTotalAddedRows] += 1 + cumulNbrNewCellsBeforeRow()[r+1];
					}
					else
					{
						// check on the left of the cell
						Cell leftNeighbor = context.topology().getCellWithCoords(r, c-1, 0);
						if (leftNeighbor != null)
						{
							newIndex = leftNeighbor.index() + nbrNewCellsBeforeRow()[r+1] + 1;
							nbrNewCellsBeforeRow()[r+2] += 1;
							inter[r+2+lastTotalAddedRows] += 1 + cumulNbrNewCellsBeforeRow()[r+2];
						}
						else
						{
							newIndex = context.topology().cells().size() + nbrNewCellsBeforeRow()[r+1];
							nbrNewCellsBeforeRow()[r+1] += 1;
							inter[r+1+lastTotalAddedRows] += 1 + cumulNbrNewCellsBeforeRow()[r+1];
						}
					}
				}
				
				if (r < 0 || r >= context.topology().rows().get(SiteType.Cell).size())
				{
					newRow = true;
				}
				surplusIndexes().add(newIndex);
				surplusInitIndexes().add(newIndex);
			}
		}
		int prevTotalAddedCells = 0;
		if (totalAddedCells().size()>0)
			prevTotalAddedCells = totalAddedCells().get(totalAddedCells().size()-1);
		totalAddedCells().add(surplusIndexes().size()+prevTotalAddedCells);

		System.out.println("MappingBoardless.java calculateNeighborsIndexes() totalAddedRows : "+totalAddedRows+" - lastTotalAddedRows : "+lastTotalAddedRows);
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() nbrNewCellsBeforeRow 1 : "+Arrays.toString(nbrNewCellsBeforeRow()));
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() i : "+rowsNeighbors().get(rowsNeighbors().size()-1)+2+" - < : "+context.topology().rows().get(SiteType.Cell).size()+3);
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() cumulNbrNewCellsBeforeRow len : "+cumulNbrNewCellsBeforeRow.length+" - inter len : "+inter.length+" - nbrNewCellsBeforeRow len : "+nbrNewCellsBeforeRow.length);
		for (int i=rowsNeighbors().get(rowsNeighbors().size()-1)+2; i<context.topology().rows().get(SiteType.Cell).size()+3; i++)
		{
			System.out.println("MappingBoardless.java calculateNeighborsIndexes() inter["+i+"] += nbrNewCellsBeforeRow()["+(i-1)+"] + cumulNbrNewCellsBeforeRow()["+(i-lastTotalAddedRows)+"];");
			inter[i] += nbrNewCellsBeforeRow()[i-1] + cumulNbrNewCellsBeforeRow()[i-lastTotalAddedRows];
			nbrNewCellsBeforeRow()[i] = nbrNewCellsBeforeRow()[i-1] + nbrNewCellsBeforeRow()[i];
		}
		totalNbrNewCellsBeforeRow().add(nbrNewCellsBeforeRow());
		cumulNbrNewCellsBeforeRow = inter;
		System.out.println("MappingBoardless.java calculateNeighborsIndexes() cumulNbrNewCellsBeforeRow : "+Arrays.toString(cumulNbrNewCellsBeforeRow));
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
		

		
		int diff = nbrNewCellsBeforeRow()[nbrNewCellsBeforeRow().length-2];
		System.out.println("MappingBoard.java createInitMapping() diff : "+diff);
		System.out.println("MappingBoard.java createInitMapping() nbrNewCellsBeforeRow : "+Arrays.toString(nbrNewCellsBeforeRow));
		for (int i=1; i<context.containers().length; i++)
		{
			System.out.println("MappingBoard.java createInitMapping() container : "+context.containers()[i]);
			for (Cell c : context.containers()[i].topology().cells())
			{
				System.out.println("MappingBoard.java createInitMapping() c : "+c);
				int prevIndex = c.index();
				int newIndex = prevIndex + diff;
				mappedPrevToNewIndexes().put(prevIndex, newIndex);
				mappedNewToPrevIndexes().put(newIndex, prevIndex);
				System.out.println("MappingBoard.java createInitMapping() mappedInitToNewIndexes : "+mappedInitToNewIndexes);
			}
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
		//surplusInitIndexes = new HashSet<Integer>();
		
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
		
		int diff = nbrNewCellsBeforeRow()[nbrNewCellsBeforeRow().length-2];
		System.out.println("MappingBoard.java createInitMapping() diff : "+diff);
		System.out.println("MappingBoard.java createInitMapping() nbrNewCellsBeforeRow : "+Arrays.toString(nbrNewCellsBeforeRow));
		for (int i=1; i<context.containers().length; i++)
		{
			System.out.println("MappingBoard.java createInitMapping() container : "+context.containers()[i]);
			for (Cell c : context.containers()[i].topology().cells())
			{
				System.out.println("MappingBoard.java createInitMapping() c : "+c);
				int prevIndex = c.index();
				int newIndex = prevIndex + diff;
				mappedInitToNewIndexes().put(prevIndex, newIndex);
				mappedNewToInitIndexes().put(newIndex, prevIndex);
				System.out.println("MappingBoard.java createInitMapping() mappedInitToNewIndexes : "+mappedInitToNewIndexes);
			}
		}	
		
		//newIndex = prevIndex + diff();
		
		for (int i=0; i<context.topology().cells().size(); i++)
			if (!mappedInitToNewIndexes().containsKey(i))
				surplusInitIndexes().add(i);
	}
	
	private static void undoLastMoveCells(Context context)
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

	//----------------------------Vertices--------------------------------------
	

	/**
	 * Calculates the coordinates of the neighbor of a cell, by only keeping the 
	 * ones that do not exist yet on the current board.
	 * rowsNeighborsMapToCols is a HashMap, with the keys being the rows of these 
	 * neighbors, and the value is a list of the columns of these neighbors.
	 * rowsNeighbors is just a list containing the rows of these neighbors.
	 * @param context
	 * @param cell
	 */
	private static void calculateVerticesNeighborsCoordinates(Context context, Cell cell)
	{
		Vertex vertex = cell.vertices().get(0); //bottom left vertex of the cell
		int row = vertex.row();
		int col = vertex.col();
		System.out.println("MappingBoardless.java calculateVerticesNeighborsCoordinates() vertex cell : "+cell.vertices());
		System.out.println("MappingBoardless.java calculateVerticesNeighborsCoordinates() edges cell : "+cell.edges());
		for (Edge e : cell.edges())
		{
			System.out.println("MappingBoardless.java calculateVerticesNeighborsCoordinates() e row : "+e.row()+" - col : "+e.col());
		}
		rowsNeighborsMapToColsVertices = new HashMap<Integer, ArrayList<Integer>>();
		rowsNeighborsVertices = new ArrayList<Integer>();
		
		for (int r=0; r<=1; r++)
		{
			for (int c=-1; c<=2; c+=3)
			{
				int newRow = row + r;
				int newCol = col + c;
				Vertex newVertex = context.topology().getVertexWithCoords(newRow, newCol, 0); //TODO : 0 for only flat game?
				if (newVertex == null)
				{
					if (rowsNeighborsMapToColsVertices().containsKey(newRow))
					{
						rowsNeighborsMapToColsVertices().get(newRow).add(newCol);
					}
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
		
		for (int r=-1; r<=2; r+=3)
		{
			for (int c=-1; c<=2; c++)
			{
				int newRow = row + r;
				int newCol = col + c;
				Vertex newVertex = context.topology().getVertexWithCoords(newRow, newCol, 0); //TODO : 0 for only flat game?
				if (newVertex == null)
				{
					if (rowsNeighborsMapToColsVertices().containsKey(newRow))
					{
						rowsNeighborsMapToColsVertices().get(newRow).add(newCol);
					}
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
	
		Collections.sort(rowsNeighborsVertices());
	}
	
	/**
	 * Calculates the indexes of the neighbors of the cell. They do not exist yet, 
	 * this mean this will shift the current cells, and though help create the new 
	 * mapping. At the same time, nbrNewCellsBeforeRow is filled in order to remember 
	 * the number of cells added before a row.
	 * @param context
	 */
	private static void calculateVerticesNeighborsIndexes(Context context)
	{
		nbrNewVerticesBeforeRow = new int[context.topology().rows().get(SiteType.Vertex).size()+3];
		int lastTotalAddedRowsVertices = totalAddedRowsVertices().size() > 0 ? totalAddedRowsVertices().get(totalAddedRowsVertices().size()-1) : 0;
		int lastTotalAddedColsVertices = totalAddedColsVertices.size() > 0 ? totalAddedColsVertices.get(totalAddedColsVertices.size()-1) : 0;
		int[] inter = new int[context.topology().rows().get(SiteType.Vertex).size()+3 + lastTotalAddedRowsVertices];
		boolean newRow = false;
		boolean newCol = false;
		for (Integer r : rowsNeighborsVertices())
		{
			ArrayList<Integer> colsNeighbors = rowsNeighborsMapToColsVertices().get(r);
			Collections.sort(colsNeighbors);
			
			if (r >= 0) 
			{
				nbrNewVerticesBeforeRow()[r+1] += nbrNewVerticesBeforeRow()[r]; 
				inter[r+1+lastTotalAddedRowsVertices] += inter[r+lastTotalAddedRowsVertices] + cumulNbrNewVerticesBeforeRow()[r+1];
			}
			for (Integer c : colsNeighbors)
			{
				int newIndex;
				if (r < 0) //TODO
				{
					newIndex = nbrNewVerticesBeforeRow()[r+2];
					nbrNewVerticesBeforeRow()[r+2] += 1;
					inter[r+2+lastTotalAddedRowsVertices] += 1 + cumulNbrNewVerticesBeforeRow()[r+2];
				}
				else
				{
					// check on the right of the cell
					Vertex rightNeighbor = context.topology().getVertexWithCoords(r, c+1, 0); // TODO si r < 0 ou r >= size ça sert à rien de looper sur les coord
					if (rightNeighbor != null)
					{
						newIndex = rightNeighbor.index() + nbrNewVerticesBeforeRow()[r+1];
						nbrNewVerticesBeforeRow()[r+1] += 1;
						inter[r+1+lastTotalAddedRowsVertices] += 1 + cumulNbrNewVerticesBeforeRow()[r+1];
					}
					else
					{
						// check on the left of the cell
						Vertex leftNeighbor = context.topology().getVertexWithCoords(r, c-1, 0);
						if (leftNeighbor != null)
						{
							newIndex = leftNeighbor.index() + nbrNewVerticesBeforeRow()[r+1] + 1;
							nbrNewVerticesBeforeRow()[r+2] += 1;
							inter[r+2+lastTotalAddedRowsVertices] += 1 + cumulNbrNewVerticesBeforeRow()[r+2];
						}
						else
						{
							newIndex = context.topology().vertices().size() + nbrNewVerticesBeforeRow()[r+1];
							nbrNewVerticesBeforeRow()[r+1] += 1;
							inter[r+1+lastTotalAddedRowsVertices] += 1 + cumulNbrNewVerticesBeforeRow()[r+1];
						}
					}
				}
				

				Vertex newV = new Vertex(newIndex, r, c, 0);
				
				if (r < 0 || r >= context.topology().rows().get(SiteType.Vertex).size())
				{
					newRow = true;
				}
				if (c < 0 || c >= context.topology().columns().get(SiteType.Vertex).size())
				{
					newCol = true;
					if (nbAddedColPerRow[r+1] != null)
						nbAddedColPerRow[r+1].add(c);
					else
					{
						ArrayList<Integer> abcd = new ArrayList<Integer>();
						abcd.add(c);
						nbAddedColPerRow[r+1] = abcd;
					}
				}
				surplusIndexesVertices().add(newIndex);
				xx.put(newIndex, new int[]{r, c});
				
				if (xxretourne.containsKey(r))
				{
					xxretourne.get(r).put(c, newIndex);
				}
				else
				{
					HashMap<Integer, Integer> pq = new HashMap<Integer, Integer>();
					pq.put(c, newIndex);
					xxretourne.put(r, pq);
				}
				vertexToCoord.put(newIndex, new int[]{r, c});
			}
		}
		int prevTotalAddedVertices = 0;
		if (totalAddedVertices().size()>0)
			prevTotalAddedVertices = totalAddedVertices().get(totalAddedVertices().size()-1);
		totalAddedVertices().add(surplusIndexesVertices().size()+prevTotalAddedVertices);

		System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() nbAddedColPerRow : "+Arrays.toString(nbAddedColPerRow));
		System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() totalAddedRows : "+totalAddedRows+" - lastTotalAddedRows : "+lastTotalAddedRowsVertices);
		System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() nbrNewCellsBeforeRow 1 : "+Arrays.toString(nbrNewCellsBeforeRow()));
		System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() i : "+rowsNeighbors().get(rowsNeighbors().size()-1)+2+" - < : "+context.topology().rows().get(SiteType.Cell).size()+3);
		System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() clem len : "+cumulNbrNewCellsBeforeRow.length+" - inter len : "+inter.length+" - nbrNewCellsBeforeRow len : "+nbrNewCellsBeforeRow.length);
		for (int i=rowsNeighborsVertices().get(rowsNeighborsVertices().size()-1)+2; i<context.topology().rows().get(SiteType.Vertex).size()+3; i++)
		{
			System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() inter["+i+"] += nbrNewVerticesBeforeRow()["+(i-1)+"] + clem()["+(i-lastTotalAddedRowsVertices)+"];");
			inter[i] += nbrNewVerticesBeforeRow()[i-1] + cumulNbrNewVerticesBeforeRow()[i-lastTotalAddedRowsVertices];
			nbrNewVerticesBeforeRow()[i] = nbrNewVerticesBeforeRow()[i-1] + nbrNewVerticesBeforeRow()[i];
		}
		totalNbrNewVerticesBeforeRow().add(nbrNewVerticesBeforeRow());
		cumulNbrNewVerticesBeforeRow = inter;
		System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() clem : "+Arrays.toString(cumulNbrNewVerticesBeforeRow));
		System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() inter : "+Arrays.toString(inter));
		
		if (newRow)
			addedRowPerEdgeMoveVertices().add(1);
		else
			addedRowPerEdgeMoveVertices().add(0);
		
		totalAddedRowsVertices().add(lastTotalAddedRowsVertices + addedRowPerEdgeMoveVertices().get(addedRowPerEdgeMoveVertices().size()-1));
		

		if (newRow)
			totalAddedColsVertices.add(1);
		else
			totalAddedColsVertices.add(0);
		System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() nbrNewVerticesBeforeRow 2 : "+Arrays.toString(nbrNewVerticesBeforeRow()));
		System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() newRow : "+newRow);
	}
	
	/**
	 * For each cells of the previous board, calculates the new index.
	 * @param context
	 */
	private static void calculateCurrentVerticesIndexes(Context context)
	{
		for (Vertex v : context.topology().vertices())
		{
			int prevIndex = v.index();
			int newIndex = prevIndex + nbrNewVerticesBeforeRow()[v.row()+1]; 
			mappedPrevToNewIndexesVertices().put(prevIndex, newIndex);
			mappedNewToPrevIndexesVertices().put(newIndex, prevIndex);
		}
	}

	
	private static void undoLastMoveVertices(Context context)
	{
		int[] lastNbrNewVerticesBeforeRow = totalNbrNewVerticesBeforeRow().get(totalNbrNewVerticesBeforeRow().size()-1);
		int[] xx = new int[nbrNewVerticesBeforeRow().length];
		int offset = addedRowPerEdgeMoveVertices().get(totalNbrNewVerticesBeforeRow().size()-1);
		int maxAddedRow = totalAddedRowsVertices().get(totalAddedRowsVertices().size()-1);
		for (int i=0; i<lastNbrNewVerticesBeforeRow.length; i++)
		{
			xx[i+offset] = nbrNewVerticesBeforeRow()[i+offset] - lastNbrNewVerticesBeforeRow[i];
		}
		
		for (Vertex v : context.topology().vertices())
		{
			int prevIndex = v.index();
			int newIndex = prevIndex + xx[v.row()+1+maxAddedRow];
			
			mappedPrevToNewIndexesVertices().put(prevIndex, newIndex);
			mappedNewToPrevIndexesVertices().put(newIndex, prevIndex);
		}
		
		for (int i=0; i<context.topology().vertices().size()+totalAddedVertices().get(totalAddedVertices().size()-1); i++)
			if (!mappedPrevToNewIndexesVertices().containsKey(i))
				surplusIndexesVertices().add(i);
	}
	
	


	//----------------------------Edges--------------------------------------
	

	/**
	 * Calculates the coordinates of the neighbor of a cell, by only keeping the 
	 * ones that do not exist yet on the current board.
	 * rowsNeighborsMapToCols is a HashMap, with the keys being the rows of these 
	 * neighbors, and the value is a list of the columns of these neighbors.
	 * rowsNeighbors is just a list containing the rows of these neighbors.
	 * @param context
	 * @param cell
	 */
	private static void calculateEdgesNeighborsCoordinates(Context context, Cell cell)
	{
		Edge edge = cell.edges().get(0); //bottom left vertex of the cell
		int row = edge.row();
		int col = edge.col();
		System.out.println("MappingBoardless.java calculateEdgesNeighborsCoordinates() vertex cell : "+cell.vertices());
		System.out.println("MappingBoardless.java calculateEdgesNeighborsCoordinates() edges cell : "+cell.edges());
		rowsNeighborsMapToColsEdges = new HashMap<Integer, ArrayList<Integer>>();
		rowsNeighborsEdges = new ArrayList<Integer>();
		
		int index = 0;
		for (int[] coords : cc)
		{
			int r = coords[0];
			int c = coords[1];
				int newRow = row + r;
				int newCol = col + c;
				Edge newEdge = context.topology().getEdgeWithCoords(newRow, newCol, 0); //TODO : 0 for only flat game?
				if (newEdge == null)
				{
					if (rowsNeighborsMapToColsEdges().containsKey(newRow))
					{
						rowsNeighborsMapToColsEdges().get(newRow).add(newCol);
						mo.get(newRow).put(newCol, index);
					}
					else
					{
						ArrayList<Integer> set = new ArrayList<Integer>();
						set.add(newCol);
						rowsNeighborsMapToColsEdges().put(newRow, set);
						rowsNeighborsEdges().add(newRow);
						

						HashMap<Integer, Integer> pq = new HashMap<Integer, Integer>();
						pq.put(newCol, index);
						mo.put(newRow, pq);
					}
				}
				
				index += 1;
		}
	
		Collections.sort(rowsNeighborsEdges());
	}
	
	/**
	 * Calculates the indexes of the neighbors of the cell. They do not exist yet, 
	 * this mean this will shift the current cells, and though help create the new 
	 * mapping. At the same time, nbrNewCellsBeforeRow is filled in order to remember 
	 * the number of cells added before a row.
	 * @param context
	 */
	private static void calculateEdgesNeighborsIndexes(Context context, Cell cell)
	{
		nbrNewEdgesBeforeRow = new int[context.topology().rows().get(SiteType.Edge).size()+3];
		int lastTotalAddedRowsEdges = totalAddedRowsEdges().size() > 0 ? totalAddedRowsEdges().get(totalAddedRowsEdges().size()-1) : 0;
		int[] inter = new int[context.topology().rows().get(SiteType.Edge).size()+3 + lastTotalAddedRowsEdges];
		boolean newRow = false;
		for (Integer r : rowsNeighborsEdges())
		{
			ArrayList<Integer> colsNeighbors = rowsNeighborsMapToColsEdges().get(r);
			Collections.sort(colsNeighbors);
			
			if (r >= 0) 
			{
				nbrNewEdgesBeforeRow()[r+1] += nbrNewEdgesBeforeRow()[r]; 
				inter[r+1+lastTotalAddedRowsEdges] += inter[r+lastTotalAddedRowsEdges] + cumulNbrNewEdgesBeforeRow()[r+1];
			}
			for (Integer c : colsNeighbors)
			{
				int newIndex;
				if (r < 0) //TODO
				{
					newIndex = nbrNewEdgesBeforeRow()[r+2];
					nbrNewEdgesBeforeRow()[r+2] += 1;
					inter[r+2+lastTotalAddedRowsEdges] += 1 + cumulNbrNewVerticesBeforeRow()[r+2];
				}
				else
				{
					// check on the right of the cell
					Edge rightNeighbor = context.topology().getEdgeWithCoords(r, c+2, 0); // TODO si r < 0 ou r >= size ça sert à rien de looper sur les coord
					if (rightNeighbor != null)
					{
						newIndex = rightNeighbor.index() + nbrNewEdgesBeforeRow()[r+1];
						nbrNewEdgesBeforeRow()[r+1] += 1;
						inter[r+1+lastTotalAddedRowsEdges] += 1 + cumulNbrNewEdgesBeforeRow()[r+1];
					}
					else
					{
						// check on the left of the cell
						Edge leftNeighbor = context.topology().getEdgeWithCoords(r, c-2, 0);
						if (leftNeighbor != null)
						{
							newIndex = leftNeighbor.index() + nbrNewEdgesBeforeRow()[r+1] + 1;
							nbrNewEdgesBeforeRow()[r+2] += 1;
							inter[r+2+lastTotalAddedRowsEdges] += 1 + cumulNbrNewEdgesBeforeRow()[r+2];
						}
						else
						{
							newIndex = context.topology().edges().size() + nbrNewEdgesBeforeRow()[r+1];
							nbrNewEdgesBeforeRow()[r+1] += 1;
							inter[r+1+lastTotalAddedRowsEdges] += 1 + cumulNbrNewEdgesBeforeRow()[r+1];
						}
					}
				}
				
				if (r < 0 || r >= context.topology().rows().get(SiteType.Edge).size())
				{
					newRow = true;
				}
				surplusIndexesEdges().add(newIndex);
				xx.put(newIndex, new int[]{r, c});

				int[][] vs = bb[mo.get(r).get(c)];
				int[] v1 = vs[0];
				int[] v2 = vs[1];
				int rowV1Relative = v1[0];
				int colV1Relative = v1[1];
				int rowV2Relative = v2[0];
				int colV2Relative = v2[1];
				int rowV1Absolute = rowV1Relative+cell.vertices().get(0).row();
				int rowV2Absolute = rowV2Relative+cell.vertices().get(0).row();
				int colV1Absolute = colV1Relative+cell.vertices().get(0).col();
				int colV2Absolute = colV2Relative+cell.vertices().get(0).col();

				int gh1;
				if (xxretourne.containsKey(rowV1Absolute) && xxretourne.get(rowV1Absolute).containsKey(colV1Absolute))
				{
					gh1 = xxretourne.get(rowV1Absolute).get(colV1Absolute);
				}
				else
				{
					gh1 = context.topology().getVertexWithCoords(rowV1Absolute, colV1Absolute, 0).index()+nbrNewVerticesBeforeRow[rowV1Absolute+1];
				}
				
				int gh2;
				if (xxretourne.containsKey(rowV2Absolute) && xxretourne.get(rowV2Absolute).containsKey(colV2Absolute))
				{
					gh2 = xxretourne.get(rowV2Absolute).get(colV2Absolute);
				}
				else
				{
					gh2 = context.topology().getVertexWithCoords(rowV2Absolute, colV2Absolute, 0).index()+nbrNewVerticesBeforeRow[rowV2Absolute+1];
				}
				
				if (stru.containsKey(gh1))
					stru.get(gh1).add(newIndex);
				else
				{
					ArrayList<Integer> pazlf = new ArrayList<Integer>();
					pazlf.add(newIndex);
					stru.put(gh1, pazlf);		
				}
				if (stru.containsKey(gh2))
					stru.get(gh2).add(newIndex);
				else
				{
					ArrayList<Integer> pazlf = new ArrayList<Integer>();
					pazlf.add(newIndex);
					stru.put(gh2, pazlf);		
				}

				edgeToV.put(newIndex, new int[]{gh1, gh2});
			}
		}
		int prevTotalAddedEdges = 0;
		if (totalAddedEdges().size()>0)
			prevTotalAddedEdges = totalAddedEdges().get(totalAddedEdges().size()-1);
		totalAddedEdges().add(surplusIndexesEdges().size()+prevTotalAddedEdges);

		System.out.println("MappingBoardless.java calculateEdgesNeighborsIndexes() totalAddedRows : "+totalAddedRows+" - lastTotalAddedRowsEdges : "+lastTotalAddedRowsEdges);
		System.out.println("MappingBoardless.java calculateEdgesNeighborsIndexes() nbrNewCellsBeforeRow 1 : "+Arrays.toString(nbrNewEdgesBeforeRow()));
		System.out.println("MappingBoardless.java calculateEdgesNeighborsIndexes() i : "+rowsNeighborsEdges().get(rowsNeighborsEdges().size()-1)+2+" - < : "+context.topology().rows().get(SiteType.Cell).size()+3);
		System.out.println("MappingBoardless.java calculateEdgesNeighborsIndexes() clem len : "+cumulNbrNewCellsBeforeRow.length+" - inter len : "+inter.length+" - nbrNewCellsBeforeRow len : "+nbrNewCellsBeforeRow.length);
		for (int i=rowsNeighborsEdges().get(rowsNeighborsEdges().size()-1)+2; i<context.topology().rows().get(SiteType.Edge).size()+3; i++)
		{
			System.out.println("MappingBoardless.java calculateVerticesNeighborsIndexes() inter["+i+"] += nbrNewVerticesBeforeRow()["+(i-1)+"] + clem()["+(i-lastTotalAddedRowsEdges)+"];");
			inter[i] += nbrNewEdgesBeforeRow()[i-1] + cumulNbrNewEdgesBeforeRow()[i-lastTotalAddedRowsEdges];
			nbrNewEdgesBeforeRow()[i] = nbrNewEdgesBeforeRow()[i-1] + nbrNewEdgesBeforeRow()[i];
		}
		totalNbrNewEdgesBeforeRow().add(nbrNewEdgesBeforeRow());
		cumulNbrNewEdgesBeforeRow = inter;
		System.out.println("MappingBoardless.java calculateEdgesNeighborsIndexes() clem : "+Arrays.toString(cumulNbrNewEdgesBeforeRow));
		System.out.println("MappingBoardless.java calculateEdgesNeighborsIndexes() inter : "+Arrays.toString(inter));
		
		if (newRow)
			addedRowPerEdgeMoveEdges().add(1);
		else
			addedRowPerEdgeMoveEdges().add(0);
		
		totalAddedRowsEdges().add(lastTotalAddedRowsEdges + addedRowPerEdgeMoveEdges().get(addedRowPerEdgeMoveEdges().size()-1));
		System.out.println("MappingBoardless.java calculateEdgesNeighborsIndexes() nbrNewEdgesBeforeRow 2 : "+Arrays.toString(nbrNewEdgesBeforeRow()));
		System.out.println("MappingBoardless.java calculateEdgesNeighborsIndexes() newRow : "+newRow);
	}
	
	/**
	 * For each cells of the previous board, calculates the new index.
	 * @param context
	 */
	private static void calculateCurrentEdgesIndexes(Context context)
	{
		for (Edge e : context.topology().edges())
		{
			int prevIndex = e.index();
			int newIndex = prevIndex + nbrNewEdgesBeforeRow()[e.row()+1]; 
			mappedPrevToNewIndexesEdges().put(prevIndex, newIndex);
			mappedNewToPrevIndexesEdges().put(newIndex, prevIndex);
		}
	}

	
	private static void undoLastMoveEdges(Context context)
	{
		int[] lastNbrNewEdgesBeforeRow = totalNbrNewEdgesBeforeRow().get(totalNbrNewEdgesBeforeRow().size()-1);
		int[] xx2 = new int[nbrNewEdgesBeforeRow().length];
		int offset = addedRowPerEdgeMoveEdges().get(totalNbrNewEdgesBeforeRow().size()-1);
		int maxAddedRow = totalAddedRowsEdges().get(totalAddedRowsEdges().size()-1);
		for (int i=0; i<lastNbrNewEdgesBeforeRow.length; i++)
		{
			xx2[i+offset] = nbrNewEdgesBeforeRow()[i+offset] - lastNbrNewEdgesBeforeRow[i];
		}
		
		for (Edge e : context.topology().edges())
		{
			int prevIndex = e.index();
			int newIndex = prevIndex + xx2[e.row()+1+maxAddedRow];
			
			mappedPrevToNewIndexesEdges().put(prevIndex, newIndex);
			mappedNewToPrevIndexesEdges().put(newIndex, prevIndex);
		}
		
		for (int i=0; i<context.topology().edges().size()+totalAddedEdges().get(totalAddedEdges().size()-1); i++)
			if (!mappedPrevToNewIndexesEdges().containsKey(i))
				surplusIndexesEdges().add(i);
	}
	
	//--------------------------------------------------------------------------
	
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
		System.out.println("MappingBoardless.java prt() nbrNewCellsBeforeRow : "+Arrays.toString(nbrNewCellsBeforeRow()));
		System.out.print("MappingBoardless.java prt() totalNbrNewVerticesBeforeRow : [");
		for (int[] i : totalNbrNewVerticesBeforeRow)
			System.out.print(Arrays.toString(i)+", ");
		System.out.println("]");
		System.out.println("MappingBoardless.java prt() addedRowPerEdgeMoveVertices : "+addedRowPerEdgeMoveVertices());
		System.out.println("MappingBoardless.java prt() totalAddedRowsVertices : "+totalAddedRowsVertices());
		System.out.println("MappingBoardless.java prt() totalAddedVertices : "+totalAddedVertices());
		System.out.println("MappingBoardless.java prt() mappedPrevToNewIndexesVertices : "+mappedPrevToNewIndexesVertices());
		System.out.println("MappingBoardless.java prt() mappedNewToPrevIndexesVertices : "+mappedNewToPrevIndexesVertices());
		System.out.println("MappingBoardless.java prt() surplusIndexesVertices : "+surplusIndexesVertices());
		System.out.println("MappingBoardless.java prt() mappedInitToNewIndexesVertices : "+mappedInitToNewIndexesVertices());
		System.out.println("MappingBoardless.java prt() mappedNewToInitIndexesVertices : "+mappedNewToInitIndexesVertices());
		System.out.println("MappingBoardless.java prt() surplusInitIndexesVertices : "+surplusInitIndexesVertices());
		System.out.println("MappingBoardless.java prt() rowsNeighborsVertices : "+rowsNeighborsVertices());
		System.out.println("MappingBoardless.java prt() rowsNeighborsMapToCols Vertices: "+rowsNeighborsMapToColsVertices());
		System.out.println("MappingBoardless.java prt() nbrNewVerticesBeforeRow : "+Arrays.toString(nbrNewVerticesBeforeRow()));
		for (int[] i : totalNbrNewEdgesBeforeRow)
			System.out.print(Arrays.toString(i)+", ");
		System.out.println("]");
		System.out.println("MappingBoardless.java prt() addedRowPerEdgeMoveEdges : "+addedRowPerEdgeMoveEdges());
		System.out.println("MappingBoardless.java prt() totalAddedRowsEdges : "+totalAddedRowsEdges());
		System.out.println("MappingBoardless.java prt() totalAddedEdges : "+totalAddedEdges());
		System.out.println("MappingBoardless.java prt() mappedPrevToNewIndexesEdges : "+mappedPrevToNewIndexesEdges());
		System.out.println("MappingBoardless.java prt() mappedNewToPrevIndexesEdges : "+mappedNewToPrevIndexesEdges());
		System.out.println("MappingBoardless.java prt() surplusIndexesEdges : "+surplusIndexesEdges());
		System.out.println("MappingBoardless.java prt() mappedInitToNewIndexesEdges : "+mappedInitToNewIndexesEdges());
		System.out.println("MappingBoardless.java prt() mappedNewToInitIndexesEdges : "+mappedNewToInitIndexesEdges());
		System.out.println("MappingBoardless.java prt() surplusInitIndexesEdges : "+surplusInitIndexesEdges());
		System.out.println("MappingBoardless.java prt() rowsNeighborsEdges : "+rowsNeighborsEdges());
		System.out.println("MappingBoardless.java prt() rowsNeighborsMapToColsEdges : "+rowsNeighborsMapToColsEdges());
		System.out.println("MappingBoardless.java prt() nbrNewEdgesBeforeRow : "+Arrays.toString(nbrNewEdgesBeforeRow()));
	}
	
	private static void rollback()
	{
		// removing last data TODO : depend until where the rollback is happening
		addedRowPerEdgeMove().remove(addedRowPerEdgeMove().size() - 1);
		totalAddedRows().remove(totalAddedRows().size() - 1);
		totalNbrNewCellsBeforeRow().remove(totalNbrNewCellsBeforeRow().size() - 1);
		totalAddedCells().remove(totalAddedCells().size() - 1);
	}
	
	public static void createMappingVertex(Context context)
	{
		
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
		surplusInitIndexes = new HashSet<Integer>();
		// data structures to map vertices between current plate and new plate
		mappedPrevToNewIndexesVertices = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexesVertices = new HashMap<Integer, Integer>();
		surplusIndexesVertices = new HashSet<Integer>();
		xx = new HashMap<Integer, int[]>();
		xxretourne = new HashMap<Integer, HashMap<Integer, Integer>>();
		vertexToCoord = new HashMap<Integer, int[]>();
		nbAddedColPerRow = new ArrayList[context.topology().rows().get(SiteType.Vertex).size()+2];
		System.out.println("MappingBoardless.java prt() nbAddedColPerRow : "+Arrays.toString(nbAddedColPerRow));
		// data structures to map edges between current plate and new plate
		mappedPrevToNewIndexesEdges = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexesEdges = new HashMap<Integer, Integer>();
		surplusIndexesEdges = new HashSet<Integer>();
		xx2 = new HashMap<Integer, int[]>();
		mo = new HashMap<Integer, HashMap<Integer, Integer>>();
		stru = new HashMap<Integer, ArrayList<Integer>>();
		edgeToV = new HashMap<Integer, int[]>();
		
		if (initCells == null) 
		{
			/*for (Cell obj : context.topology().cells()) {
				initCells.add(new Cell(obj)); // Copie indépendante
			}*/
			initCells = new ArrayList<Cell>();
			cumulNbrNewCellsBeforeRow = new int[context.topology().rows().get(SiteType.Cell).size()+3];
			cumulNbrNewVerticesBeforeRow = new int[context.topology().rows().get(SiteType.Vertex).size()+3];
			cumulNbrNewEdgesBeforeRow = new int[context.topology().rows().get(SiteType.Edge).size()+3];
		}
		
		if (fromSize < toSize)
		{
			Cell cell = (Cell) context.topology().getGraphElement(SiteType.Cell, move.to());
			
			calculateNeighborsCoordinates(context, cell);
			calculateNeighborsIndexes(context);
			calculateCurrentCellsIndexes(context); // prev to new
			createInitMapping(context, totalNbrNewCellsBeforeRow().size()); // init to new
			
			calculateVerticesNeighborsCoordinates(context, cell);
			calculateVerticesNeighborsIndexes(context);
			calculateCurrentVerticesIndexes(context); // prev to new
			
			calculateEdgesNeighborsCoordinates(context, cell);
			calculateEdgesNeighborsIndexes(context, cell);
			calculateCurrentEdgesIndexes(context); // prev to new
		}
		else
		{
			createInitMapping(context, totalNbrNewCellsBeforeRow().size()-1); // init to new
			undoLastMoveCells(context); // prev to new
			
			undoLastMoveVertices(context);
			
			undoLastMoveEdges(context);
			
			rollback();
		}

		prt();
	}
}