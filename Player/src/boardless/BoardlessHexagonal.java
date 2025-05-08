package boardless;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import game.equipment.container.board.Boardless;
import game.types.board.SiteType;
import game.types.board.TilingBoardlessType;
import game.util.graph.Edge;
import game.util.graph.Face;
import game.util.graph.Graph;
import main.math.Point3D;
import other.context.Context;
import other.move.Move;
import other.topology.Cell;
import other.topology.TopologyElement;
import other.topology.Vertex;

/**
 * Methods to create the mapping that will tell the board how to grow, specific for hexagonal tiles.
 * 
 * @author Clémentine.Sacré
 */
public class BoardlessHexagonal extends BoardlessAbstract  
{
	public BoardlessHexagonal() {
        super();
    }

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
	
	private Graph cc(final Context context, final Cell cell)
	{
		List<game.util.graph.Vertex> allo = context.board().graph().faces().get(cell.index()).vertices();
		//System.out.println("GrowingBoardHexagonal.java recalulteTout() graph before : "+context.board().graph());
		int initFace = context.board().graph().faces().size();
				
		// vertices
		game.util.graph.Vertex n = allo.get(2);
		game.util.graph.Vertex ne = allo.get(3);
		game.util.graph.Vertex se = allo.get(4);
		game.util.graph.Vertex s = allo.get(5);
		game.util.graph.Vertex sw = allo.get(0);
		game.util.graph.Vertex nw = allo.get(1);
		HashSet<game.util.graph.Vertex> initVertex = new HashSet<game.util.graph.Vertex>();
		initVertex.add(n);
		initVertex.add(ne);
		initVertex.add(ne);
		initVertex.add(s);
		initVertex.add(sw);
		initVertex.add(nw);
		
		double verticalSide = ne.pt().y() - se.pt().y();
		double verticalCenter = n.pt().y() - s.pt().y();
		double horizontalSide = se.pt().x() - sw.pt().x();
		
		Point3D pointA = new Point3D(s.pt().x() - (ne.pt().x() - sw.pt().x()), s.pt().y() - verticalSide, 0);
		Point3D pointB = new Point3D(sw.pt().x(), sw.pt().y() - verticalCenter, 0);
		Point3D pointC = new Point3D(s.pt().x(), s.pt().y() - verticalSide, 0);
		Point3D pointD = new Point3D(se.pt().x(), se.pt().y() - verticalCenter, 0);
		Point3D pointE = new Point3D(s.pt().x() + (se.pt().x() - nw.pt().x()), s.pt().y() - verticalSide, 0);
		Point3D pointF = new Point3D(sw.pt().x() - horizontalSide, sw.pt().y(), 0);
		Point3D pointG = new Point3D(s.pt().x() - horizontalSide, s.pt().y(), 0);
		Point3D pointH = new Point3D(s.pt().x() + horizontalSide, s.pt().y(), 0);
		Point3D pointI = new Point3D(se.pt().x() + horizontalSide, se.pt().y(), 0);
		Point3D pointJ = new Point3D(nw.pt().x() - horizontalSide, nw.pt().y(), 0);
		Point3D pointK = new Point3D(n.pt().x() - horizontalSide, n.pt().y(), 0);
		Point3D pointL = new Point3D(n.pt().x() + horizontalSide, n.pt().y(), 0);
		Point3D pointM = new Point3D(ne.pt().x() + horizontalSide, ne.pt().y(), 0);
		Point3D pointN = new Point3D(n.pt().x() - horizontalSide, n.pt().y() + verticalSide, 0);
		Point3D pointO = new Point3D(nw.pt().x(), nw.pt().y() + verticalCenter, 0);
		Point3D pointP = new Point3D(n.pt().x(), n.pt().y() + verticalSide, 0);
		Point3D pointQ = new Point3D(ne.pt().x(), nw.pt().y() + verticalCenter, 0);
		Point3D pointR = new Point3D(n.pt().x() + horizontalSide, n.pt().y() + verticalSide, 0);
		
		// faces

		game.util.graph.Face currCell = context.board().graph().faces().get(cell.index());

		double vertical = currCell.pt().y() - s.pt().y();
		double diagonalX = currCell.pt().x() - sw.pt().x();
		double diagonalY = currCell.pt().y() - sw.pt().y();
		
		Point3D pointCellA = new Point3D(currCell.pt().x() - diagonalX, currCell.pt().y() - vertical - diagonalY, 0);
		Point3D pointCellB = new Point3D(currCell.pt().x() + diagonalX, currCell.pt().y() - vertical - diagonalY, 0);
		Point3D pointCellC = new Point3D(currCell.pt().x() - (2*diagonalX), currCell.pt().y(), 0);
		Point3D pointCellD = new Point3D(currCell.pt().x() + (2*diagonalX), currCell.pt().y(), 0);
		Point3D pointCellE = new Point3D(currCell.pt().x() - diagonalX, currCell.pt().y() + vertical + diagonalY, 0);
		Point3D pointCellF = new Point3D(currCell.pt().x() + diagonalX, currCell.pt().y() + vertical + diagonalY, 0);
		
		
		// calculation
		Point3D[] newVertices = new Point3D[] {pointA, pointB, pointC, pointD, pointE, pointF, pointG, pointH, pointI, pointJ, pointK, pointL, pointM, pointN, pointO, pointP, pointQ, pointR};
		
		HashMap<Point3D, game.util.graph.Vertex> pointToVertex = new HashMap<Point3D, game.util.graph.Vertex>();
		pointToVertex.put(n.pt(), n);
		pointToVertex.put(ne.pt(), ne);
		pointToVertex.put(se.pt(), se);
		pointToVertex.put(s.pt(), s);
		pointToVertex.put(sw.pt(), sw);
		pointToVertex.put(nw.pt(), nw);
		HashMap<String, game.util.graph.Vertex> pointToVertexS = new HashMap<String, game.util.graph.Vertex>();
		pointToVertexS.put(point3dToString(n.pt()), n);
		pointToVertexS.put(point3dToString(ne.pt()), ne);
		pointToVertexS.put(point3dToString(se.pt()), se);
		pointToVertexS.put(point3dToString(s.pt()), s);
		pointToVertexS.put(point3dToString(sw.pt()), sw);
		pointToVertexS.put(point3dToString(nw.pt()), nw);
		
		HashMap<Integer, game.util.graph.Vertex> pointToVertexPre = new HashMap<Integer, game.util.graph.Vertex>();
		pointToVertexPre.put(n.id(), n);
		pointToVertexPre.put(ne.id(), ne);
		pointToVertexPre.put(se.id(), se);
		pointToVertexPre.put(s.id(), s);
		pointToVertexPre.put(sw.id(), sw);
		pointToVertexPre.put(nw.id(), nw);

		
		for (int p3d=0; p3d<newVertices.length; p3d++)
		{
			Point3D curr = newVertices[p3d];
			game.util.graph.Vertex isV = context.game().board().graph().findVertex(curr.x(), curr.y(), curr.z());
			if (isV != null)
			{
				pointToVertex.put(curr, isV);
				pointToVertexS.put(point3dToString(curr), isV);
				pointToVertexPre.put(isV.id(), isV);
			}	
		}		
		
		HashMap<Point3D, Point3D[]> linkVerticesWithEdges = new HashMap<Point3D, Point3D[]>();
		linkVerticesWithEdges.put(pointA, new Point3D[] {pointB, pointG});
		linkVerticesWithEdges.put(pointB, new Point3D[] {pointA, pointC});
		linkVerticesWithEdges.put(pointC, new Point3D[] {pointB, pointD, s.pt()});
		linkVerticesWithEdges.put(pointD, new Point3D[] {pointC, pointE});
		linkVerticesWithEdges.put(pointE, new Point3D[] {pointD, pointH});
		linkVerticesWithEdges.put(pointF, new Point3D[] {pointG, pointJ});
		linkVerticesWithEdges.put(pointG, new Point3D[] {pointA, pointF, sw.pt()});
		linkVerticesWithEdges.put(pointH, new Point3D[] {pointE, pointI, se.pt()});
		linkVerticesWithEdges.put(pointI, new Point3D[] {pointH, pointM});
		linkVerticesWithEdges.put(pointJ, new Point3D[] {pointF, pointK});
		linkVerticesWithEdges.put(pointK, new Point3D[] {pointJ, pointN, nw.pt()});
		linkVerticesWithEdges.put(pointL, new Point3D[] {pointM, pointR, ne.pt()});
		linkVerticesWithEdges.put(pointM, new Point3D[] {pointI, pointL});
		linkVerticesWithEdges.put(pointN, new Point3D[] {pointK, pointO});
		linkVerticesWithEdges.put(pointO, new Point3D[] {pointN, pointP});
		linkVerticesWithEdges.put(pointP, new Point3D[] {pointO, pointQ, n.pt()});
		linkVerticesWithEdges.put(pointQ, new Point3D[] {pointP, pointR});
		linkVerticesWithEdges.put(pointR, new Point3D[] {pointQ, pointL});
		
		HashSet<Point3D> addedEdges = new HashSet<Point3D>();
		for (Point3D p3d : linkVerticesWithEdges.keySet()) {
			// vertice does not exist yet
			if (!pointToVertexS.containsKey(point3dToString(p3d)))
            {
            	// create vertex
            	game.util.graph.Vertex newV = context.board().graph().addVertex(p3d);
				pointToVertex.put(p3d, newV);
				pointToVertexS.put(point3dToString(p3d), newV);
				
				// check for the neighbor vertices to create edge - creates also vertex if needed
            	for (Point3D p3dNeighbor : linkVerticesWithEdges.get(p3d)) {
            		if (!pointToVertexS.containsKey(point3dToString(p3dNeighbor)))
                    {
            			game.util.graph.Vertex newVNeighbor = context.board().graph().addVertex(p3dNeighbor);
            			Edge newEdge = context.board().graph().addEdge(newV, newVNeighbor);
            			addedEdges.add(newEdge.pt());
        				pointToVertex.put(p3dNeighbor, newVNeighbor);
        				pointToVertexS.put(point3dToString(p3dNeighbor), newVNeighbor);
                    }
            		else
            		{
            			Edge newEdge = context.board().graph().addEdge(newV, pointToVertex.get(p3dNeighbor));
            			addedEdges.add(newEdge.pt());
            		}
            	}
            }
            else
            {
            	game.util.graph.Vertex newV = pointToVertex.get(p3d);
				
				// check for the neighbor vertices to create edge - creates also vertex if needed
            	for (Point3D p3dNeighbor : linkVerticesWithEdges.get(p3d)) {
            		if (!pointToVertex.containsKey(p3dNeighbor))
                    {
            			game.util.graph.Vertex newVNeighbor = context.board().graph().addVertex(p3dNeighbor);
            			Edge newEdge = context.board().graph().addEdge(newV, newVNeighbor);
            			addedEdges.add(newEdge.pt());
        				pointToVertex.put(p3dNeighbor, newVNeighbor);
        				pointToVertexS.put(point3dToString(p3dNeighbor), newVNeighbor);
                    }
            		else 
            		{
            			Edge newEdgeX = new Edge(0, newV, pointToVertex.get(p3dNeighbor));
            			if (!addedEdges.contains(newEdgeX.pt()) && !(pointToVertexPre.containsKey(newV.id()) && pointToVertexPre.containsKey(pointToVertex.get(p3dNeighbor).id())))
            			{
            				Edge newEdge = context.board().graph().addEdge(newV, pointToVertex.get(p3dNeighbor));
                			addedEdges.add(newEdge.pt());
            			}
            		}
            	}
            }
        }
		
		// faces
		HashMap<Point3D, game.util.graph.Vertex[]> linkVerticestoFaces = new HashMap<Point3D, game.util.graph.Vertex[]>();
		linkVerticestoFaces.put(pointCellA, new game.util.graph.Vertex[] {pointToVertex.get(pointA), pointToVertex.get(pointG), sw, s, pointToVertex.get(pointC), pointToVertex.get(pointB)});
		linkVerticestoFaces.put(pointCellB, new game.util.graph.Vertex[] {pointToVertex.get(pointC), s, se, pointToVertex.get(pointH), pointToVertex.get(pointE), pointToVertex.get(pointD)});
		linkVerticestoFaces.put(pointCellC, new game.util.graph.Vertex[] {pointToVertex.get(pointF), pointToVertex.get(pointJ), pointToVertex.get(pointK), nw, sw, pointToVertex.get(pointG)});
		linkVerticestoFaces.put(pointCellD, new game.util.graph.Vertex[] {se, ne, pointToVertex.get(pointL), pointToVertex.get(pointM), pointToVertex.get(pointI), pointToVertex.get(pointH)});
		linkVerticestoFaces.put(pointCellE, new game.util.graph.Vertex[] {pointToVertex.get(pointK), pointToVertex.get(pointN), pointToVertex.get(pointO), pointToVertex.get(pointP), n, nw});
		linkVerticestoFaces.put(pointCellF, new game.util.graph.Vertex[] {n, pointToVertex.get(pointP), pointToVertex.get(pointQ), pointToVertex.get(pointR), pointToVertex.get(pointL), ne});
				
		HashSet<Face> addedFaces = new HashSet<Face>();

		for (Point3D p3dCell : linkVerticestoFaces.keySet())
		{
			Face f = context.board().graph().findFace(p3dCell.x(), p3dCell.y(), p3dCell.z());
			if (f == null)
			{	
				Face newF = context.board().graph().addFace(linkVerticestoFaces.get(p3dCell));
				addedFaces.add(newF);
			}
		}

		context.board().graph().reorder();
		
		// save face for going back if needed
		List<Integer> addedF = new ArrayList<Integer>();
		for(Face f: addedFaces)
			addedF.add(f.id());

        Collections.sort(addedF, Comparator.reverseOrder());
        addedFacesSinceBeginning().add(addedF);
		
		
        // Mappings current to new
        HashMap<Integer, Integer> mappedNewToInitIndexes2 = new HashMap<Integer, Integer>();
		int addedCells = 0;
		for (int i=0; i<context.board().graph().faces().size(); i++)
		{
			if (addedFaces.contains(context.board().graph().faces().get(i)))
			{
				if (i > initFace)
				{
					surplusIndexes().add(i);
					surplusInitIndexes().add(i);
				}
				else
				{
					surplusIndexes().add(i);
					surplusInitIndexes().add(i);
					addedCells += 1;
				}
			}
			else
			{
				if ((i-addedCells) <= initFace)
				{	
					mappedPrevToNewIndexes().put((i-addedCells), i);
					mappedNewToPrevIndexes().put(i, (i-addedCells));
					
					if (mappedNewToInitIndexes().containsKey(i-addedCells))
					{
						mappedInitToNewIndexes().put(mappedNewToInitIndexes().get(i-addedCells), i);
						mappedNewToInitIndexes2.put(i, mappedNewToInitIndexes().get(i-addedCells));
					}
					else
					{
						surplusInitIndexes().add(i);
					}
				}
			}
		}

		// map other containers that board TODO what if multiple cells inside a hand ? 
		int otherContainer = context.containers().length-1;
		int addedIndexes = surplusIndexes().size();
		int addedIndexesSinceBeginning = surplusInitIndexes().size();
		
		for (int i=0; i<otherContainer; i++)
		{
			int containerId = initFace+i;
			mappedPrevToNewIndexes().put(containerId, containerId+addedIndexes);
			mappedNewToPrevIndexes().put(containerId+addedIndexes, containerId);
		}
		
		for (int i=0; i<otherContainer; i++)
		{
			int containerId = mappedNewToInitIndexes().size()-otherContainer+i;
			mappedNewToInitIndexes2.put(containerId+addedIndexesSinceBeginning, containerId);
			mappedInitToNewIndexes().put(containerId, containerId+addedIndexesSinceBeginning);
		}
		mappedNewToInitIndexes = mappedNewToInitIndexes2;
		
		HashMap<Integer, Integer> copieMappedInitToNewIndexes = new HashMap<>(mappedInitToNewIndexes());
		HashMap<Integer, Integer> copieMappedNewToInitIndexes = new HashMap<>(mappedNewToInitIndexes());
		listCopieMappedInitToNewIndexes.add(copieMappedInitToNewIndexes);
		listCopieMappedNewToInitIndexes.add(copieMappedNewToInitIndexes);
        currentCopy += 1;
		
		HashSet<Integer> copiesurplusInitIndexes = new HashSet<>(surplusInitIndexes());
		listCopieSurplusInitIndexes.add(copiesurplusInitIndexes);

		return context.board().graph();
	}
	
	@Override
	protected Graph forward(final Context context, final Cell cell)
	{
		Graph newGraph = cc(context, cell);
		context.board().setGraphFunction(newGraph);
		
		
		return newGraph;
	}
	
	@Override
	public void keepSameSize(Context context)
	{
		int nbFaces = context.board().graph().faces().size();
		for (int i=0; i<nbFaces; i++)
		{
			mappedPrevToNewIndexes().put(i, i);
			mappedNewToPrevIndexes().put(i, i);
		}
		
		int otherContainer = context.containers().length-1;
		for (int i=nbFaces; i<nbFaces+otherContainer; i++)
		{
			mappedPrevToNewIndexes().put(i, i);
			mappedNewToPrevIndexes().put(i, i);
		}
		context.board().setGraphFunction(context.board().graph());
	}
	
	@Override
	public void rollback(Context context)
	{
		List<Integer> lastFacesAdded = addedFacesSinceBeginning().get(addedFacesSinceBeginning().size()-1);
		
		// create mapping prev to new
		int lastIndex = lastFacesAdded.size()-1;
		int count = 0;
		for (int i=0; i<context.board().graph().faces().size(); i++)
		{
			if (lastIndex >= 0 && i == lastFacesAdded.get(lastIndex))
			{
		        count += 1;
		        lastIndex -= 1;
		        surplusIndexes().add(i);
			}
		    else
		    {
				mappedPrevToNewIndexes().put(i, (i-count));
				mappedNewToPrevIndexes().put((i-count), i);
		    }
		}
		
		// surplus cell above the rest
		while (lastIndex >= 0)
		{
	        surplusIndexes().add(lastFacesAdded.get(lastIndex));
		    lastIndex -= 1;
		}
		
		int otherContainer = context.containers().length-1;
		int addedIndexes = surplusIndexes().size();
		int initFace = mappedPrevToNewIndexes().size();
		for (int i=0; i<otherContainer; i++)
		{
			int containerId = initFace+i;
			mappedPrevToNewIndexes().put(containerId+addedIndexes, containerId);
			mappedNewToPrevIndexes().put(containerId, containerId+addedIndexes);
		}
		    
		// remove face from the graph 
		for (Integer faceId : lastFacesAdded)
		{
			context.board().graph().removeFace(faceId, false);
		}
		context.board().setGraphFunction(context.board().graph());

		addedFacesSinceBeginning.remove(addedFacesSinceBeginning.size()-1);
		currentCopy -= 1;

		if (currentCopy >= 0)
		{
			mappedInitToNewIndexes = listCopieMappedInitToNewIndexes.get(currentCopy);
			mappedNewToInitIndexes = listCopieMappedNewToInitIndexes.get(currentCopy);
			surplusInitIndexes = listCopieSurplusInitIndexes.get(currentCopy);
		}
		else
		{
			mappedInitToNewIndexes = new HashMap<Integer, Integer>();
			mappedNewToInitIndexes = new HashMap<Integer, Integer>();
			surplusInitIndexes = new HashSet<Integer>();
			for (int i=0; i<context.board().graph().faces().size(); i++)
			{
				mappedInitToNewIndexes().put(i, i);
				mappedNewToInitIndexes().put(i, i);
			}
		}
		
	}
	
	
	@Override
	public void rollbackToInit(Context context)
	{
		if (addedFacesSinceBeginning().size() == 1)
		{
			// means the first move done on the board was done on an edge
			rollback(context);
		}
		else if(addedFacesSinceBeginning().size() == 0)
		{
			// means the first move done on the board was not done on an edge
			mappedInitToNewIndexes = new HashMap<Integer, Integer>();
			mappedNewToInitIndexes = new HashMap<Integer, Integer>();
			surplusInitIndexes = new HashSet<Integer>();
			int otherContainer = context.containers().length-1;
			for (int i=0; i<context.board().graph().faces().size()+otherContainer; i++)
			{
				mappedInitToNewIndexes().put(i, i);
				mappedNewToInitIndexes().put(i, i);
				mappedPrevToNewIndexes().put(i, i);
				mappedNewToPrevIndexes().put(i, i);
			}
			context.board().setGraphFunction(context.board().graph());
		}
		else
		{
			// means we are rolling back after doing multiple moves, with some done on edges
			
			// prev to new part
			mappedPrevToNewIndexes = listCopieMappedNewToInitIndexes.get(currentCopy);
			mappedNewToPrevIndexes = listCopieMappedInitToNewIndexes.get(currentCopy);
			surplusIndexes = listCopieSurplusInitIndexes.get(currentCopy);
			
			// TODO : reset graph
			for (int i=addedFacesSinceBeginning().size()-1; i>= 0; i--)
			{
				List<Integer> lastFacesAdded = addedFacesSinceBeginning().get(i);
				for (Integer faceId : lastFacesAdded)
				{
					context.board().graph().removeFace(faceId, false);
				}
			}
			context.board().setGraphFunction(context.board().graph());
			
			// init part
			mappedInitToNewIndexes = new HashMap<Integer, Integer>();
			mappedNewToInitIndexes = new HashMap<Integer, Integer>();
			surplusInitIndexes = new HashSet<Integer>();
			int otherContainer = context.containers().length-1;
			for (int i=0; i<context.board().graph().faces().size()+otherContainer; i++)
			{
				mappedInitToNewIndexes().put(i, i);
				mappedNewToInitIndexes().put(i, i);
			}
			
			// réinitialiser les autres structures de données pour qu'elles soient vides TODO : vérifier que ça fonctionne
			init(context);
		}
	}
}