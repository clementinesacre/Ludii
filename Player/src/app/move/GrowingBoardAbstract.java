package app.move;

import game.util.graph.Graph;
import other.context.Context;
import other.topology.Cell;

/**
 * Functions for handling the board growing regarding boardless game.
 * 
 * @author Clémentine.Sacré
 */
public abstract class GrowingBoardAbstract
{	
	protected abstract Graph recalculetoutInit(final Context context);

	protected abstract Graph recalculetoutRollBack(final Context context);
	
	protected abstract Graph recalculetout(final Context context, final Cell c);
}
