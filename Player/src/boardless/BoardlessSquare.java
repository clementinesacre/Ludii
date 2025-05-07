package boardless;

import game.util.graph.Graph;
import other.context.Context;
import other.topology.Cell;

/**
 * Methods to create the mapping that will tell the board how to grow, specific for square tiles.
 * 
 * @author Clémentine.Sacré
 */
public class BoardlessSquare extends BoardlessAbstract  
{
	public BoardlessSquare() {
        super();
    }
	
	@Override
	protected Graph forward(final Context context, final Cell cell)
	{
		return null;
	}
	
	@Override
	public void keepSameSize(Context context)
	{}
	
	@Override
	public void rollback(Context context)
	{}
	
	@Override
	public void rollbackToInit(Context context)
	{}
}