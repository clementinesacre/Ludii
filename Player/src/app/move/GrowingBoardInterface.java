package app.move;

import game.equipment.container.board.Boardless;
import game.types.board.TilingBoardlessType;
import game.util.graph.Graph;
import other.context.Context;
import other.topology.Cell;

/**
 * Functions for handling the board growing regarding boardless game.
 * 
 * @author Clémentine.Sacré
 */
public class GrowingBoardInterface
{	
	private static GrowingBoardAbstract growingBoard;
	
	private static GrowingBoardAbstract growingBoard()
	{
		return growingBoard;
	}
	
	public static Graph generateGraph(Context context, int boardSizeChange, Cell c)
	{
		if (growingBoard() == null)
			switch(((Boardless) context.game().board()).tiling()) {
				case Square:
					growingBoard = new GrowingBoardSquare();
					break;
				case Hexagonal:
					growingBoard = new GrowingBoardHexagonal();
					break;
				default:
					throw new UnsupportedOperationException("Tiling "+((Boardless) context.game().board()).tiling()+" not implement for boardless games.");
			}
		
		Graph newGraph;
		switch(boardSizeChange) {
			case 1:
				// Board needs to grow
				newGraph = growingBoard().recalculetout(context, c);
			    break;
			case -1:
				// Board needs to shrink / go back from 1 step
				newGraph = growingBoard().recalculetoutRollBack(context);
				break;
			default:
				// Board is re-initialize / go back from all steps
				newGraph = growingBoard().recalculetoutInit(context);
		}
		
		return newGraph;
	}
}
