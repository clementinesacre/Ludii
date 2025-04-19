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
public class MappingBoardlessHexagonal extends MappingBoardlessAbstract
{

	protected int[][] coordCellsHexagonal = new int[][]{{-1, -1}, {-1, 0}, {0, -1}, {0, 1}, {1, 0}, {1, 1}};
	
	protected int[][] coordVeticesHexagonal = new int[][]{
		{-2, -1}, {-2, 1}, 
		{-1, -2}, {-1, 0}, {-1, 2}, 
		{0, -2}, {0, 2}, 
		{1, -3}, {1, 3}, 
		{2, -3}, {2, 3}, 
		{3, -2}, {3, 2}, 
		{4, -2}, {4, 0}, {4, 2}, 
		{5, -1}, {5, 1}};
		

	public MappingBoardlessHexagonal() {
        super();
    }
		
	//--------------------------------Getters----------------------------------
	
	public int[][] coordVeticesHexagonal()
	{
		return coordVeticesHexagonal;
	}
	
	public int[][] coordCellsHexagonal()
	{
		return coordCellsHexagonal;
	}
	
	//----------------------------Cells-----------------------------------------
	
	@Override
	public void calculateNeighborsCoordinates(Context context, Cell cell)
	{
		boolean newColB = false;
		boolean newRowB = false;
		boolean newRightColB = false;
		boolean newUpperRowB = false;
		int row = cell.row();
		int col = cell.col();
		
		for (int i=0; i<coordCellsHexagonal().length; i++)
		{
			int r = coordCellsHexagonal()[i][0];
			int c = coordCellsHexagonal()[i][1];
			
			int newRow = row + r;
			int newCol = col + c;
			if (newCol < 0 && !newColB)
				newColB = true;
			if (newRow < 0 && !newRowB)
				newRowB = true;
			if (newCol >= context.topology().columns().get(SiteType.Cell).size() && !newUpperRowB)
				newUpperRowB = true;
			if (newRow >= context.topology().rows().get(SiteType.Cell).size() && !newRightColB)
				newRightColB = true;
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
			cellsAddedRightCols().add(newRightColB ? 1 : 0); 
			cellsAddedUpRows().add(newUpperRowB ? 1 : 0); 
		}
		else
		{
			int last = cellsAddedLeftCols().size()-1; // TODO en faire une variable globale, utilisée à pas mal d'endroit
			cellsAddedLeftCols().add(cellsAddedLeftCols().get(last) + (newColB ? 1 : 0)); 
			cellsAddedDownRows().add(cellsAddedDownRows().get(last) + (newRowB ? 1 : 0)); 
			cellsAddedRightCols().add(cellsAddedRightCols().get(last) + (newRightColB ? 1 : 0)); 
			cellsAddedUpRows().add(cellsAddedUpRows().get(last) + (newUpperRowB ? 1 : 0)); 
		}
		for (int i=0; i<cellsRowsColsAdded().size(); i++)
			System.out.println("MappingBoardlessHexagonal.java calculateNeighborsCoordinates() cellsRowsColsAdded.get("+i+") : "+Arrays.toString(cellsRowsColsAdded().get(i)));
	}

	@Override
	protected void prevToNewRollBack(Context context)
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
	
	@Override
	protected void initToNewRollBack(Context context)
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
				if (initialCells()[r][c] != -1)
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
	
	@Override
	protected void prevToNewInit(Context context)
	{	
		System.out.println("MappingBoardlessHexagonal.java prevToNewInit() mappedPrevToNewIndexes BEFORE : "+mappedPrevToNewIndexes);

		if (initialCells != null)
			for (int[] i : initialCells)
				System.out.println("MappingBoardlessHexagonal.java prevToNewInit() initialCells i : "+Arrays.toString(i));
		
		for (Cell c : context.topology().cells())
			System.out.println("MappingBoardlessHexagonal.java prevToNewInit() cell index : "+c.index()+" - row : "+c.row()+" - col : "+c.col());
		for (Cell c : context.topology().cells())
		{
			int row = c.row();
			int col = c.col();
			System.out.println("MappingBoardlessHexagonal.java prevToNewInit() row : "+row+" - col : "+col+" - index : "+c.index());
			if (row >= 0 && row < initialCells().length)
			{
				if (col >= 0 && col < initialCells()[row].length && initialCells()[row][col] != -1)
				{
					int prevIndex = c.index();
					int newIndex = prevIndex - initialCells()[row][col];
					mappedPrevToNewIndexes().put(prevIndex, newIndex);
					mappedNewToPrevIndexes().put(newIndex, prevIndex);
					System.out.println("MappingBoardlessHexagonal.java prevToNewInit() prevIndex : "+prevIndex+" - newIndex : "+newIndex);
				}
				else
					surplusIndexes().add(c.index());
			}
			else
				surplusIndexes().add(c.index());
		}
		
		for (int i=0; i<initialHands().length; i++)
			for (int j=0; j<initialHands()[i].length; j++)
			{
				int newIndex = initialHands()[i][j];
				int prevIndex = newIndex + nbAddedCellsFromStart();
				mappedPrevToNewIndexes().put(prevIndex, newIndex);
				mappedNewToPrevIndexes().put(newIndex, prevIndex);
				System.out.println("MappingBoardlessHexagonal.java prevToNewInit()2 prevIndex : "+prevIndex+" - newIndex : "+newIndex);
			}
		
		System.out.println("MappingBoardlessHexagonal.java prevToNewInit() mappedPrevToNewIndexes : "+mappedPrevToNewIndexes);
		System.out.println("MappingBoardlessHexagonal.java prevToNewInit() mappedNewToPrevIndexes : "+mappedNewToPrevIndexes);
	}
	
	@Override
	protected void initToNewInit(Context context)
	{
		int cellIndex = 0;
		nbAddedCellsFromStart = 0;
		// mapping existing cells
		for (int r=0; r<initialCells().length; r++)
			for (int c=0; c<initialCells()[r].length; c++)
			{
				if (initialCells()[r][c] != -1)
				{
					initialCells()[r][c] = 0;
					mappedInitToNewIndexes().put(cellIndex, cellIndex);
					mappedNewToInitIndexes().put(cellIndex, cellIndex);
	
					cellIndex ++;
				}
			}
		
		// mapping other containers than board
		for (int i=0; i<initialHands().length; i++)
			for (int j=0; j<initialHands()[i].length; j++)
			{
				int prevIndex = initialHands()[i][j];
				mappedInitToNewIndexes().put(prevIndex, prevIndex);
				mappedNewToInitIndexes().put(prevIndex, prevIndex);
			}
	}
	
	//----------------------------Vertices--------------------------------------
	
	@Override
	public void calculateVerticesNeighborsCoordinates(Context context, Cell cell)
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
		
		
		for (int i=0; i<coordVeticesHexagonal().length; i++)
		{
			int r = coordVeticesHexagonal()[i][0];
			int c = coordVeticesHexagonal()[i][1];
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
		
		for (int i=0; i<coordVeticesHexagonal().length; i++)
			System.out.println("MappingBoardlessHexagonal.java calculateVerticesNeighborsCoordinates() coordVeticesHexagonal["+i+"] : "+Arrays.toString(coordVeticesHexagonal()[i]));
	}
	
	//--------------------------------------------------------------------------
	
	@Override
	public void forward(Context context, Cell cell)
	{
		calculateNeighborsCoordinates(context, cell);
		
		prevToNew(context);
		initToNew(context);
		
		calculateVerticesNeighborsCoordinates(context, cell);
	}
	
}