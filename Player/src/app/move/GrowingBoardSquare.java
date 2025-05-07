package app.move;

import java.awt.geom.Point2D;

import game.functions.graph.generators.basis.square.Square;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.context.Context;
import other.topology.Cell;

/**
 * Functions for handling the board growing regarding boardless game.
 * 
 * @author Clémentine.Sacré
 */
public class GrowingBoardSquare extends GrowingBoardAbstract
{	
	protected Graph recalculetoutInit(final Context context)
	{
		System.out.println("GrowingBoard.java recalculetoutInit()");
		final Graph graph = new Graph();

		int[][] initialVertices = MappingBoardless.initialVertices();
		for (int row = 0; row < initialVertices.length; row++) 
		{
			for (int col = 0; col < initialVertices[row].length; col++)
			{
				if (initialVertices[row][col] == 1)
				{
					final Point2D pt = new Point2D.Double(col, row);
					graph.addVertex(pt);
				}
			}
		}
		
		for (int row = 0; row < initialVertices.length; row++) 
		{
			for (int col = 0; col < initialVertices[row].length; col++)
			{
				if (initialVertices[row][col] == 1)
				{
					final game.util.graph.Vertex vertexA = graph.findVertex(col, row);
					
					for (int dirn = 0; dirn < Square.steps.length / 2; dirn++)
					{
						final int rr = row + Square.steps[dirn][0];
						final int cc = col + Square.steps[dirn][1];
						
						if (rr < 0 || rr >= initialVertices.length || cc < 0 || cc >= initialVertices[row].length)
							continue;

						final game.util.graph.Vertex vertexB = graph.findVertex(cc, rr);
					
						if (vertexA != null && vertexB != null)
							graph.findOrAddEdge(vertexA, vertexB);
					}
				}
			}
		}

		graph.makeFaces(false);
		
		//graph.setBasisAndShape(basis, shape);
		graph.reorder();


		return graph; 
	}

	protected Graph recalculetoutRollBack(final Context context)
	{
		System.out.println("GrowingBoard.java recalculetoutRollBack()");
		final Graph graph = new Graph();

		// Add 1 if playing on the cells, as the number of cells in each 
		// direction is 1 less than the number of vertices.
		final int newColsNbr = context.topology().columns().get(SiteType.Vertex).size()+1;
		final int newRowsNbr = context.topology().rows().get(SiteType.Vertex).size()+1;

		int offsetRow = MappingBoardless.lastNbAddedColPerRow()[0] == null ? 0 : 1;
		int last = MappingBoardless.lastNbAddedColPerRow().length-1;
		int offsetCol = 0;
		if (MappingBoardless.lastVertexAddedLeftCol())
			offsetCol = 1;
		
		int startRow = MappingBoardless.lastNbAddedColPerRow()[0] == null ? 1 : 0;
		int offsetEndRow = MappingBoardless.lastNbAddedColPerRow()[last] == null ? 1 : 0;
		int endRow = MappingBoardless.lastNbAddedColPerRow().length - offsetEndRow;
		for (int row = startRow; row < endRow; row++) {
			if (MappingBoardless.lastNbAddedColPerRow()[row] == null)
			{
				for (int i=0; i<context.topology().rows().get(SiteType.Vertex).get(row-startRow).size(); i++)
				{
					int col = context.topology().rows().get(SiteType.Vertex).get(row-startRow).get(i).col();
					final Point2D pt = new Point2D.Double(col-offsetCol, row-offsetRow);
					graph.addVertex(pt);
				}
			}
			else
			{
				for (int i=0; i<context.topology().rows().get(SiteType.Vertex).get(row-startRow).size(); i++)
				{
					int col = context.topology().rows().get(SiteType.Vertex).get(row-startRow).get(i).col();
					if (!MappingBoardless.lastNbAddedColPerRow()[row].contains(col-offsetCol))
					{
						final Point2D pt = new Point2D.Double(col-offsetCol, row-offsetRow);
						graph.addVertex(pt);
					}
				}
			}
		}
		
		
		for (int row = startRow; row < endRow; row++) {
			
			if (MappingBoardless.lastNbAddedColPerRow()[row] == null)
			{
				for (int i=0; i<context.topology().rows().get(SiteType.Vertex).get(row-startRow).size(); i++)
				{
					int col = context.topology().rows().get(SiteType.Vertex).get(row-startRow).get(i).col();
					final game.util.graph.Vertex vertexA = graph.findVertex(col-offsetCol, row-offsetRow);
					
					for (int dirn = 0; dirn < Square.steps.length / 2; dirn++)
					{
						final int rr = row-offsetRow + Square.steps[dirn][0];
						final int cc = col-offsetCol + Square.steps[dirn][1];
						
						if (rr < 0 || rr >= newRowsNbr || cc < 0 || cc >= newColsNbr)
							continue;
	
						final game.util.graph.Vertex vertexB = graph.findVertex(cc, rr);
					
						if (vertexA != null && vertexB != null)
							graph.findOrAddEdge(vertexA, vertexB);
					}
				}
			}
			else
			{
				for (int i=0; i<context.topology().rows().get(SiteType.Vertex).get(row-startRow).size(); i++)
				{
					int col = context.topology().rows().get(SiteType.Vertex).get(row-startRow).get(i).col();
					if (!MappingBoardless.lastNbAddedColPerRow()[row].contains(col-offsetCol))
					{
						final game.util.graph.Vertex vertexA = graph.findVertex(col-offsetCol, row-offsetRow);
						
						for (int dirn = 0; dirn < Square.steps.length / 2; dirn++)
						{
							final int rr = row-offsetRow + Square.steps[dirn][0];
							final int cc = col-offsetCol + Square.steps[dirn][1];
							
							if (rr < 0 || rr >= newRowsNbr || cc < 0 || cc >= newColsNbr)
								continue;
		
							final game.util.graph.Vertex vertexB = graph.findVertex(cc, rr);
						
							if (vertexA != null && vertexB != null)
								graph.findOrAddEdge(vertexA, vertexB);
						}
					}
				}
			}
		}
		

		graph.makeFaces(false);
		
		//graph.setBasisAndShape(basis, shape);
		graph.reorder();


		return graph; 
	}
	
	protected Graph recalculetout(final Context context, final Cell c)
	{
		System.out.println("GrowingBoard.java recalculetout()");
		final Graph graph = new Graph();

		// Add 1 if playing on the cells, as the number of cells in each 
		// direction is 1 less than the number of vertices.
		final int newColsNbr = context.topology().columns().get(SiteType.Vertex).size()+1;
		final int newRowsNbr = context.topology().rows().get(SiteType.Vertex).size()+1;
		
		int offsetRow = MappingBoardless.nbAddedColPerRow()[0] == null ? 1 : 0;
		int offsetCol = 0;
		if (MappingBoardless.vertexAddedLeftCol())
			offsetCol = 1;
		for (int row = 0; row < MappingBoardless.nbAddedColPerRow().length; row++) {
			if (row == 0 || row == MappingBoardless.nbAddedColPerRow().length-1)
			{
				if (MappingBoardless.nbAddedColPerRow()[row] != null)
				{
					for (Integer col : MappingBoardless.nbAddedColPerRow()[row])
					{
						final Point2D pt = new Point2D.Double(col+offsetCol, row-offsetRow);
						graph.addVertex(pt);
					}
				}
			}
			else
			{
				for (int i=0; i<context.topology().rows().get(SiteType.Vertex).get(row-1).size(); i++)
				{
					int col = context.topology().rows().get(SiteType.Vertex).get(row-1).get(i).col();
					final Point2D pt = new Point2D.Double(col+offsetCol, row-offsetRow);
					graph.addVertex(pt);
				}
				
				if (MappingBoardless.nbAddedColPerRow()[row] != null)
				{
					//adding new vertices if new cols
					for (Integer col : MappingBoardless.nbAddedColPerRow()[row])
					{
						final Point2D pt = new Point2D.Double(col+offsetCol, row-offsetRow);
						graph.addVertex(pt);
					}
				}
			}
		}
		
		
		for (int row = 0; row < MappingBoardless.nbAddedColPerRow().length; row++) {
			if (row == 0 || row == MappingBoardless.nbAddedColPerRow().length-1)
			{
				if (MappingBoardless.nbAddedColPerRow()[row] != null)
				{
					for (Integer col : MappingBoardless.nbAddedColPerRow()[row])
					{
						final game.util.graph.Vertex vertexA = graph.findVertex(col+offsetCol, row-offsetRow);
						
						for (int dirn = 0; dirn < Square.steps.length / 2; dirn++)
						{
							final int rr = row-offsetRow + Square.steps[dirn][0];
							final int cc = col+offsetCol + Square.steps[dirn][1];
							
							if (rr < 0 || rr >= newRowsNbr || cc < 0 || cc >= newColsNbr)
								continue;
	
							final game.util.graph.Vertex vertexB = graph.findVertex(cc, rr);
						
							if (vertexA != null && vertexB != null)
								graph.findOrAddEdge(vertexA, vertexB);
						}
					}
				}
			}
			else
			{
				for (int i=0; i<context.topology().rows().get(SiteType.Vertex).get(row-1).size(); i++)
				{
					int col = context.topology().rows().get(SiteType.Vertex).get(row-1).get(i).col();
					final game.util.graph.Vertex vertexA = graph.findVertex(col+offsetCol, row-offsetRow);
					
					for (int dirn = 0; dirn < Square.steps.length / 2; dirn++)
					{
						final int rr = row-offsetRow + Square.steps[dirn][0];
						final int cc = col+offsetCol + Square.steps[dirn][1];
						
						if (rr < 0 || rr >= newRowsNbr || cc < 0 || cc >= newColsNbr)
							continue;

						final game.util.graph.Vertex vertexB = graph.findVertex(cc, rr);
					
						if (vertexA != null && vertexB != null)
							graph.findOrAddEdge(vertexA, vertexB);
					}
				}
				
				if (MappingBoardless.nbAddedColPerRow()[row] != null)
				{
					for (Integer col : MappingBoardless.nbAddedColPerRow()[row])
					{
						final game.util.graph.Vertex vertexA = graph.findVertex(col+offsetCol, row-offsetRow);
						
						for (int dirn = 0; dirn < Square.steps.length / 2; dirn++)
						{
							final int rr = row-offsetRow + Square.steps[dirn][0];
							final int cc = col+offsetCol + Square.steps[dirn][1];
							
							if (rr < 0 || rr >= newRowsNbr || cc < 0 || cc >= newColsNbr)
								continue;
	
							final game.util.graph.Vertex vertexB = graph.findVertex(cc, rr);
						
							if (vertexA != null && vertexB != null)
								graph.findOrAddEdge(vertexA, vertexB);
						}
					}
				}
			}
		}
		
		

		graph.makeFaces(false);
		
		//graph.setBasisAndShape(basis, shape);
		graph.reorder();

		
		return graph; 
	}
}
