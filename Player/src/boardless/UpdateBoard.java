package boardless;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
public class UpdateBoard
{
	private static BoardlessAbstract mappingBoardlessBoard;
	
	public static BoardlessAbstract mappingBoardlessBoard()
	{
		return mappingBoardlessBoard;
	}
	
	public static HashMap<Integer, Integer> mappedPrevToNewIndexes()
	{
		return mappingBoardlessBoard().mappedPrevToNewIndexes();
	}
	
	public static HashMap<Integer, Integer> mappedInitToNewIndexes()
	{
		return mappingBoardlessBoard().mappedInitToNewIndexes();
	}
	
	public static HashSet<Integer> surplusIndexes()
	{
		return mappingBoardlessBoard().surplusIndexes();
	}
	
	public static HashSet<Integer> surplusInitIndexes()
	{
		return mappingBoardlessBoard().surplusInitIndexes();
	}
	
	public static int newTotalIndexesCells()
	{
		return mappingBoardlessBoard().newTotalIndexesCells();
	}
	
	public static HashSet<Integer>[] nbAddedColPerRow()
	{
		return mappingBoardlessBoard().nbAddedColPerRow();
	}
	
	public static boolean vertexAddedLeftCol()
	{
		return mappingBoardlessBoard().vertexAddedLeftCol();
	}
	
	public static HashSet<Integer>[] lastNbAddedColPerRow()
	{
		return mappingBoardlessBoard().lastNbAddedColPerRow();
	}
	
	public static boolean lastVertexAddedLeftCol()
	{
		return mappingBoardlessBoard().lastVertexAddedLeftCol();
	}
	
	public static int[][] initialVertices()
	{
		return mappingBoardlessBoard().initialVertices();
	}
	
	/*public static int lastNbRowAddedCells()
	{
		return mappingBoardlessBoard().lastNbRowAddedCells();
	}*/

	public static ArrayList<int[]> cellsRowsColsAdded()
	{
		return mappingBoardlessBoard().cellsRowsColsAdded();
	}
	
	/*public static int lastNbColAddedCells()
	{
		return mappingBoardlessBoard().lastNbColAddedCells();
	}*/
	
	public static int[][] initialCells2()
	{
		return mappingBoardlessBoard().initialCells2();
	}
	
	
	//--------------------------------------------------------------------------
	
	/**
	 * Creates a mapping between the indexes of the previous board and the 
	 * new board.
	 * @param context
	 * @param move edge move just applied that will help telling what cells 
	 * will be added and so what is going to be the new mapping.
	 * @param boardSizeChange Determines how the board size should be adjusted based on the last move.
	 * -2: Reset the board to its initial size ; -1: Reduce the board size based on last move ;
	 *  0: Keep the board at its current size ; 1: Expand the board size based on the last move.
	 */
	public static void createMappings(Context context, Move move, final int boardSizeChange)
	{
		if (mappingBoardlessBoard() == null)
			switch(((Boardless) context.game().board()).tiling()) {
				case Square:
					mappingBoardlessBoard = new BoardlessSquare();
					break;
				case Hexagonal:
					mappingBoardlessBoard = new BoardlessHexagonal();
					break;
				default:
					throw new UnsupportedOperationException("Tiling "+((Boardless) context.game().board()).tiling()+" not implement for boardless games.");
			}

		mappingBoardlessBoard().initForNewBoard(context);


		switch(boardSizeChange) {
			case 1:
			    // Board needs to grow
				Cell cell = (Cell) context.topology().getGraphElement(SiteType.Cell, move.to());
				mappingBoardlessBoard().forward(context, cell);
			    break;
			case 0:
			    // Board keeps same size
				mappingBoardlessBoard().keepSameSize(context);
			    break;
			case -1:
			    // Board needs to shrink / go back from 1 step
				mappingBoardlessBoard().rollback(context);
			    break;
			case -2:
			    // Board is re-initialize / go back from all steps
				mappingBoardlessBoard().rollbackToInit(context);
			    break;
			default:
				// code block
		}

		mappingBoardlessBoard().updateNewTotalIndexesCells();
	}
}