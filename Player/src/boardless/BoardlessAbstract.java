package boardless;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import game.util.graph.Edge;
import game.util.graph.Face;
import game.util.graph.Graph;
import main.math.Point3D;
import other.context.Context;
import other.topology.Cell;

/**
 * Methods to create the mapping that will tell the board how to grow.
 * 
 * @author Clémentine.Sacré
 */
public abstract class BoardlessAbstract
{
	// Mappings
	protected HashMap<Integer, Integer> mappedPrevToNewIndexes;
	protected HashSet<Integer> surplusIndexes; // cells indexes that are being added from one board to another

	protected HashMap<Integer, Integer> mappedInitToNewIndexes = new HashMap<Integer, Integer>();
	protected HashMap<Integer, Integer> mappedNewToInitIndexes = new HashMap<Integer, Integer>();
	protected HashSet<Integer> surplusInitIndexes = new HashSet<Integer>(); // cells indexes that are being added from init board to new one
	
	// Copy of the mappings - to be able to roll back from one or multiple moves
	protected List<HashMap<Integer, Integer>> mappedInitToNewIndexesHistory = new ArrayList<HashMap<Integer, Integer>>();
	protected List<HashMap<Integer, Integer>> mappedNewToInitIndexesHistory = new ArrayList<HashMap<Integer, Integer>>();
	protected List<HashSet<Integer>> surplusInitIndexesHistory = new ArrayList<HashSet<Integer>>();

	// data structures used to create new GraphElement based on the cell on which the last edge move was done
	protected game.util.graph.Face cellEdgeMove; // current cell on which the last edge move was done
	protected List<game.util.graph.Vertex> verticesOfCellEdgeMove;
	
	protected List<List<Integer>> addedCellsSinceInit;
	
	protected HashMap<String, game.util.graph.Vertex> point3dToVertex;
	protected HashMap<Integer, game.util.graph.Vertex> indexToVertex;
	protected HashMap<Point3D, game.util.graph.Vertex[]> cellPointToVerticesPoint;
	protected HashMap<Point3D, Point3D[]> vertexPointToNeighborsPoint;

	protected Point3D[] newVertices;
	
	// first time the class is called - this will allow initializing some specific data structures
	protected boolean firsTime = true;

	protected int newTotalIndexesCells;
	protected int initialNbCells;
	
	//--------------------------------Getters----------------------------------
	
	public HashMap<Integer, Integer> mappedPrevToNewIndexes()
	{
		return mappedPrevToNewIndexes;
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
	
	public int newTotalIndexesCells()
	{
		return newTotalIndexesCells;
	}
	
	//-------------------------------------------------------------------------
	
	public void initFromScratch(Context context)
	{	
		addedCellsSinceInit = new ArrayList<List<Integer>>();
		
		// initialize TODO
		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		surplusInitIndexes = new HashSet<Integer>();
		
		int nbCells = context.board().graph().faces().size();
		for (int i=0; i<nbCells; i++)
		{
			mappedInitToNewIndexes.put(i, i);
			mappedNewToInitIndexes.put(i, i);
		}
		// adding ids for the other containers than the board
		for (int i=nbCells; i<nbCells+context.containers().length-1; i++)
		{
			mappedInitToNewIndexes.put(i, i);
			mappedNewToInitIndexes.put(i, i);
		}
		
		mappedInitToNewIndexesHistory = new ArrayList<HashMap<Integer, Integer>>();
		mappedNewToInitIndexesHistory = new ArrayList<HashMap<Integer, Integer>>();
		surplusInitIndexesHistory = new ArrayList<HashSet<Integer>>();
	}
	
	public void init(Context context)
	{
		// data structures to map cells between current plate and new plate
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>();
		
		surplusInitIndexes = new HashSet<Integer>();
				
		if (firsTime)
		{
			initFromScratch(context);
			firsTime = false;
		}
	}
	
	public void clean()
	{
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>(); 

		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		surplusInitIndexes = new HashSet<Integer>(); 
	}
	
	public void updateNewTotalIndexesCells()
	{
		newTotalIndexesCells = mappedPrevToNewIndexes.size() + surplusIndexes.size();
	}
	
	//------------------------------ Utilities --------------------------------------

	public static String point3dToString(Point3D p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.x());
		sb.append("-");
		sb.append(p.y());
		sb.append("-");
		sb.append(p.z());
		
		String result = sb.toString();
		return result;
	}

	//--------------------------------------------------------------------
	
	/**
	 * Create new vertices and edges on the graph. Based on an hashmap 
	 * containing each vertices that might need to be created as key, 
	 * and the neighbors vertices as value.
	 * 
	 * @param context
	 */
	protected void createVandE(Context context)
	{
		HashSet<Point3D> newEdges = new HashSet<Point3D>();
		for (Point3D p3dVertex : vertexPointToNeighborsPoint.keySet()) {
			// vertice does not exist yet
			if (!point3dToVertex.containsKey(point3dToString(p3dVertex)))
            {
            	game.util.graph.Vertex newVertex = context.board().graph().addVertex(p3dVertex);
				point3dToVertex.put(point3dToString(p3dVertex), newVertex);
				
				// check for the neighbor vertices to create edge - creates also neighbor vertices if needed
            	for (Point3D p3dVertexNeighbor : vertexPointToNeighborsPoint.get(p3dVertex)) {
            		if (!point3dToVertex.containsKey(point3dToString(p3dVertexNeighbor)))
                    {
            			game.util.graph.Vertex newVertexNeighbor = context.board().graph().addVertex(p3dVertexNeighbor);
            			Edge newEdge = context.board().graph().addEdge(newVertex, newVertexNeighbor);
            			newEdges.add(newEdge.pt());
        				point3dToVertex.put(point3dToString(p3dVertexNeighbor), newVertexNeighbor);
                    }
            		else
            		{
            			Edge newEdge = context.board().graph().addEdge(newVertex, point3dToVertex.get(point3dToString(p3dVertexNeighbor)));
            			newEdges.add(newEdge.pt());
            		}
            	}
            }
            else
            {
            	game.util.graph.Vertex newVertex = point3dToVertex.get(point3dToString(p3dVertex));
				
				// check for the neighbor vertices to create edge - creates also neighbor vertices if needed
            	for (Point3D p3dNeighbor : vertexPointToNeighborsPoint.get(p3dVertex))
            	{
                	if (!point3dToVertex.containsKey(point3dToString(p3dNeighbor)))
                    {
            			game.util.graph.Vertex newVertexNeighbor = context.board().graph().addVertex(p3dNeighbor);
            			Edge newEdge = context.board().graph().addEdge(newVertex, newVertexNeighbor);
            			newEdges.add(newEdge.pt());
        				point3dToVertex.put(point3dToString(p3dNeighbor), newVertexNeighbor);
                    }
            		else 
            		{
            			Edge newEdgeX = new Edge(0, newVertex, point3dToVertex.get(point3dToString(p3dNeighbor)));
            			// if edge does not exist yet and both of the vertices do not exist yet
                		if (!newEdges.contains(newEdgeX.pt()) && !(indexToVertex.containsKey(newVertex.id()) && indexToVertex.containsKey(point3dToVertex.get(point3dToString(p3dNeighbor)).id())))
            			{
            				Edge newEdge = context.board().graph().addEdge(newVertex, point3dToVertex.get(point3dToString(p3dNeighbor)));
                			newEdges.add(newEdge.pt());
            			}
            		}
            	}
            }
        }
	}
	
	/**
	 * Check which vertices in the ones that should exist already exists.
	 * 
	 * @param context
	 */
	protected void detectNewVertices(Context context)
	{
		for (int i=0; i<newVertices.length; i++)
		{
			Point3D point3dVertex = newVertices[i];
			game.util.graph.Vertex vertex = context.game().board().graph().findVertex(point3dVertex.x(), point3dVertex.y(), point3dVertex.z());
			if (vertex != null)
			{
				point3dToVertex.put(point3dToString(point3dVertex), vertex);
				indexToVertex.put(vertex.id(), vertex);
			}	
		}
	}
	
	/**
	 * Update the graph and the board by creating the required cells, edges and vertices.
	 * 
	 * @param context
	 * @return the new graph
	 */
	protected Graph updateBoard(Context context)
	{
		calculateNewGraphElements(context);
		
		HashSet<Face> newCells = new HashSet<Face>();
		for (Point3D p3dCell : cellPointToVerticesPoint.keySet())
		{
			Face cell = context.board().graph().findFace(p3dCell.x(), p3dCell.y(), p3dCell.z());
			if (cell == null)
			{	
				Face newCell = context.board().graph().addFace(cellPointToVerticesPoint.get(p3dCell));
				newCells.add(newCell);
			}
		}
		
		context.board().graph().reorder();
		
		// save cells for going back if needed
		List<Integer> newCellsIndex = new ArrayList<Integer>();
		for(Face f: newCells)
			newCellsIndex.add(f.id());

		// descending order as if these cells need to be deleting, we want to delete the one with the highest id first
        Collections.sort(newCellsIndex, Comparator.reverseOrder());
        addedCellsSinceInit.add(newCellsIndex);
		
        // Mappings current to new
        HashMap<Integer, Integer> mappedNewToInitIndexesTmp = new HashMap<Integer, Integer>();
		int nbAddedCells = 0;
		for (int i=0; i<context.board().graph().faces().size(); i++)
		{
			int prevCellId = i-nbAddedCells;
			if (newCells.contains(context.board().graph().faces().get(i)))
			{
				if (i > initialNbCells)
				{
					surplusIndexes.add(i);
					surplusInitIndexes.add(i);

					if (mappedNewToInitIndexes.containsKey(prevCellId))
		                nbAddedCells += 1;
				}
				else
				{
					surplusIndexes.add(i);
					surplusInitIndexes.add(i);
					nbAddedCells += 1;
				}
			}
			else
				if (prevCellId <= initialNbCells)
				{	
					mappedPrevToNewIndexes.put(prevCellId, i);
					
					if (mappedNewToInitIndexes.containsKey(prevCellId))
					{
						mappedInitToNewIndexes.put(mappedNewToInitIndexes.get(prevCellId), i);
						mappedNewToInitIndexesTmp.put(i, mappedNewToInitIndexes.get(prevCellId));
					}
					else
						surplusInitIndexes.add(i);
				}
		}

		// map other containers that board TODO what if multiple cells inside a hand ? 
		int nbContainers = context.containers().length-1;
		int nbNewIndexes = surplusIndexes.size();
		int nbNewIndexesSinceInit = surplusInitIndexes.size();
		
		for (int i=0; i<nbContainers; i++)
		{
			int containerIdPrevToNew = initialNbCells+i;
			mappedPrevToNewIndexes.put(containerIdPrevToNew, containerIdPrevToNew+nbNewIndexes);

			int containerIdInitToNew = mappedNewToInitIndexes.size()-nbContainers+i;
			mappedNewToInitIndexesTmp.put(containerIdInitToNew+nbNewIndexesSinceInit, containerIdInitToNew);
			mappedInitToNewIndexes.put(containerIdInitToNew, containerIdInitToNew+nbNewIndexesSinceInit);
		}
		mappedNewToInitIndexes = mappedNewToInitIndexesTmp;
		
		HashMap<Integer, Integer> copieMappedInitToNewIndexes = new HashMap<>(mappedInitToNewIndexes);
		HashMap<Integer, Integer> copieMappedNewToInitIndexes = new HashMap<>(mappedNewToInitIndexes);
		mappedInitToNewIndexesHistory.add(copieMappedInitToNewIndexes);
		mappedNewToInitIndexesHistory.add(copieMappedNewToInitIndexes);
		
		HashSet<Integer> copiesurplusInitIndexes = new HashSet<>(surplusInitIndexes);
		surplusInitIndexesHistory.add(copiesurplusInitIndexes);

		return context.board().graph();
	}
	
	/**
	 * Handles the impact of a move made on an edge, by making the board grow accordingly.
	 * 
	 * @param context
	 * @return the new graph
	 */
	protected Graph forward(final Context context, final Cell cell)
	{
		init(context);
		
		cellEdgeMove = context.board().graph().faces().get(cell.index());
		verticesOfCellEdgeMove = context.board().graph().faces().get(cell.index()).vertices();
		initialNbCells = context.board().graph().faces().size();
		
		Graph newGraph = updateBoard(context);
		context.board().setGraphFunction(newGraph);
		
		return newGraph;
	}
	
	/**
	 * Update the mappings so a cell id is equal to its current cell id.
	 * 
	 * @param context
	 */
	public void keepSameSize(Context context)
	{
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		int nbCells = context.board().graph().faces().size();
		int nbContainers = context.containers().length-1;
		for (int i=0; i<nbCells+nbContainers; i++)
			mappedPrevToNewIndexes.put(i, i);
		
		context.board().setGraphFunction(context.board().graph());
	}
	
	/**
	 * Update the board to make it shrink in case the last edge 
	 * was done on an edge, by removing the required cells, vertices 
	 * and edges.
	 * 
	 * @param context
	 */
	public void rollback(Context context)
	{
		init(context);
		
		initialNbCells = context.board().graph().faces().size();
		List<Integer> lastFacesAdded = addedCellsSinceInit.get(addedCellsSinceInit.size()-1);
		
		// mapping previous board to new board
		int lastIndex = lastFacesAdded.size()-1;
		int count = 0;
		for (int i=0; i<initialNbCells; i++)
		{
			if (lastIndex >= 0 && i == lastFacesAdded.get(lastIndex))
			{
		        count += 1;
		        lastIndex -= 1;
		        surplusIndexes.add(i);
			}
		    else
				mappedPrevToNewIndexes.put(i, (i-count));
		}
		
		// check for new cells that would have been added at the top of the board
		while (lastIndex >= 0)
		{
	        surplusIndexes.add(lastFacesAdded.get(lastIndex));
		    lastIndex -= 1;
		}
		
		// also map indexes for the other containers that the board
		int nbContainers = context.containers().length-1;
		int nbNewIndexes = surplusIndexes.size();
		int nbNewIndexesSinceInit = mappedPrevToNewIndexes.size();
		for (int i=0; i<nbContainers; i++)
		{
			int containerId = nbNewIndexesSinceInit+i;
			mappedPrevToNewIndexes.put(containerId+nbNewIndexes, containerId);
		}
		    
		// remove unwanted cells from the graph 
		for (Integer cellId : lastFacesAdded)
			context.board().graph().removeFace(cellId, false);
		context.board().setGraphFunction(context.board().graph());

		addedCellsSinceInit.remove(addedCellsSinceInit.size()-1);
		mappedInitToNewIndexesHistory.remove(mappedInitToNewIndexesHistory.size()-1);
		mappedNewToInitIndexesHistory.remove(mappedNewToInitIndexesHistory.size()-1);
		surplusInitIndexesHistory.remove(surplusInitIndexesHistory.size()-1);

		if (mappedInitToNewIndexesHistory.size() > 0)
		{
			mappedInitToNewIndexes = mappedInitToNewIndexesHistory.get(mappedInitToNewIndexesHistory.size()-1);
			mappedNewToInitIndexes = mappedNewToInitIndexesHistory.get(mappedNewToInitIndexesHistory.size()-1);
			surplusInitIndexes = surplusInitIndexesHistory.get(surplusInitIndexesHistory.size()-1);
		}
		else
		{
			mappedInitToNewIndexes = new HashMap<Integer, Integer>();
			mappedNewToInitIndexes = new HashMap<Integer, Integer>();
			surplusInitIndexes = new HashSet<Integer>();
			for (int i=0; i<context.board().graph().faces().size(); i++)
			{
				mappedInitToNewIndexes.put(i, i);
				mappedNewToInitIndexes.put(i, i);
			}
		}
	}
	
	/**
	 * Reinitialise the board to its initial size. Mappings need to 
	 * be updated to map between the current board that might be different 
	 * from the initial board.
	 * 
	 * @param context
	 */
	public void rollbackToInit(Context context)
	{
		init(context);
		
		int nbCellsAddedSinceInit = addedCellsSinceInit.size();
		initialNbCells = context.board().graph().faces().size();
		
		if (nbCellsAddedSinceInit == 1)
		{
			// the first move done on the board was done on an edge
			rollback(context);
		}
		else if(nbCellsAddedSinceInit == 0)
		{
			// the first move done on the board was not done on an edge
			mappedInitToNewIndexes = new HashMap<Integer, Integer>();
			mappedNewToInitIndexes = new HashMap<Integer, Integer>();
			surplusInitIndexes = new HashSet<Integer>();
			int otherContainer = context.containers().length-1;
			for (int i=0; i<initialNbCells+otherContainer; i++)
			{
				mappedInitToNewIndexes.put(i, i);
				mappedNewToInitIndexes.put(i, i);
				mappedPrevToNewIndexes.put(i, i);
			}
			context.board().setGraphFunction(context.board().graph());
		}
		else
		{
			// rolling back to initial board after doing multiple moves, with some done on edges
			mappedPrevToNewIndexes = mappedNewToInitIndexesHistory.get(mappedNewToInitIndexesHistory.size()-1);
			surplusIndexes = surplusInitIndexesHistory.get(surplusInitIndexesHistory.size()-1);
			
			for (int i=nbCellsAddedSinceInit-1; i>= 0; i--)
			{
				List<Integer> addedCells = addedCellsSinceInit.get(i);
				for (Integer cellId : addedCells)
					context.board().graph().removeFace(cellId, false);
			}
			context.board().setGraphFunction(context.board().graph());
			
			mappedInitToNewIndexes = new HashMap<Integer, Integer>();
			mappedNewToInitIndexes = new HashMap<Integer, Integer>();
			surplusInitIndexes = new HashSet<Integer>();
			int otherContainer = context.containers().length-1;
			for (int i=0; i<initialNbCells+otherContainer; i++)
			{
				mappedInitToNewIndexes.put(i, i);
				mappedNewToInitIndexes.put(i, i);
			}
			
			initFromScratch(context);
		}
	}
	
	/**
	 * Creates point3D of the GraphElement that should exist, and make links between them.
	 * 
	 * @param context
	 */
	protected abstract void calculateNewGraphElements(Context context);
}