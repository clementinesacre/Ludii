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
 * Methods to create the mapping that will tell the board how to grow, specific for square tiles.
 * 
 * @author Clémentine.Sacré
 */
public class BoardlessSquare extends BoardlessAbstract  
{
	public BoardlessSquare() {
        super();
    }
	
	private Graph cc(final Context context, final Cell cell)
	{
		List<game.util.graph.Vertex> allo = context.board().graph().faces().get(cell.index()).vertices();
		//System.out.println("GrowingBoardHexagonal.java recalulteTout() graph before : "+context.board().graph());
		int initFace = context.board().graph().faces().size();
				
		// vertices
		game.util.graph.Vertex ne = allo.get(2);
		game.util.graph.Vertex se = allo.get(3);
		game.util.graph.Vertex sw = allo.get(0);
		game.util.graph.Vertex nw = allo.get(1);
		HashSet<game.util.graph.Vertex> initVertex = new HashSet<game.util.graph.Vertex>();
		initVertex.add(ne);
		initVertex.add(se);
		initVertex.add(sw);
		initVertex.add(nw);
		
		double verticalSide = nw.pt().y() - sw.pt().y();
		double horizontalSide = ne.pt().x() - nw.pt().x();
		
		Point3D pointA = new Point3D(sw.pt().x() - horizontalSide, sw.pt().y() - verticalSide, 0);
		Point3D pointB = new Point3D(sw.pt().x(), sw.pt().y() - verticalSide, 0);
		Point3D pointC = new Point3D(se.pt().x(), se.pt().y() - verticalSide, 0);
		Point3D pointD = new Point3D(se.pt().x() + horizontalSide, se.pt().y() - verticalSide, 0);
		Point3D pointE = new Point3D(sw.pt().x() - horizontalSide, sw.pt().y(), 0);
		Point3D pointF = new Point3D(se.pt().x() + horizontalSide, se.pt().y(), 0);
		Point3D pointG = new Point3D(nw.pt().x() - horizontalSide, nw.pt().y(), 0);
		Point3D pointH = new Point3D(ne.pt().x() + horizontalSide, ne.pt().y(), 0);
		Point3D pointI = new Point3D(nw.pt().x() - horizontalSide, nw.pt().y() + verticalSide, 0);
		Point3D pointJ = new Point3D(nw.pt().x(), nw.pt().y() + verticalSide, 0);
		Point3D pointK = new Point3D(ne.pt().x(), ne.pt().y() + verticalSide, 0);
		Point3D pointL = new Point3D(ne.pt().x() + horizontalSide, ne.pt().y() + verticalSide, 0);
		
		// faces

		game.util.graph.Face currCell = context.board().graph().faces().get(cell.index());

		double diagonal = currCell.pt().x() - sw.pt().x();
		//double diagonalX = currCell.pt().x() - sw.pt().x();
		//double diagonalY = currCell.pt().y() - sw.pt().y();
		
		Point3D pointCellA = new Point3D(currCell.pt().x() - (2*diagonal), currCell.pt().y() - (2*diagonal), 0);
		Point3D pointCellB = new Point3D(currCell.pt().x(), currCell.pt().y() - (2*diagonal), 0);
		Point3D pointCellC = new Point3D(currCell.pt().x() + (2*diagonal), currCell.pt().y() - (2*diagonal), 0);
		Point3D pointCellD = new Point3D(currCell.pt().x() - (2*diagonal), currCell.pt().y(), 0);
		Point3D pointCellE = new Point3D(currCell.pt().x() + (2*diagonal), currCell.pt().y(), 0);
		Point3D pointCellF = new Point3D(currCell.pt().x() - (2*diagonal), currCell.pt().y() + (2*diagonal), 0);
		Point3D pointCellG = new Point3D(currCell.pt().x(), currCell.pt().y() + (2*diagonal), 0);
		Point3D pointCellH = new Point3D(currCell.pt().x() + (2*diagonal), currCell.pt().y() + (2*diagonal), 0);
		
		
		// calculation
		Point3D[] newVertices = new Point3D[] {pointA, pointB, pointC, pointD, pointE, pointF, pointG, pointH, pointI, pointJ, pointK, pointL};
		
		HashMap<Point3D, game.util.graph.Vertex> pointToVertex = new HashMap<Point3D, game.util.graph.Vertex>();
		pointToVertex.put(ne.pt(), ne);
		pointToVertex.put(se.pt(), se);
		pointToVertex.put(sw.pt(), sw);
		pointToVertex.put(nw.pt(), nw);
		HashMap<String, game.util.graph.Vertex> pointToVertexS = new HashMap<String, game.util.graph.Vertex>();
		pointToVertexS.put(point3dToString(ne.pt()), ne);
		pointToVertexS.put(point3dToString(se.pt()), se);
		pointToVertexS.put(point3dToString(sw.pt()), sw);
		pointToVertexS.put(point3dToString(nw.pt()), nw);
		
		HashMap<Integer, game.util.graph.Vertex> pointToVertexPre = new HashMap<Integer, game.util.graph.Vertex>();
		pointToVertexPre.put(ne.id(), ne);
		pointToVertexPre.put(se.id(), se);
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
		linkVerticesWithEdges.put(pointA, new Point3D[] {pointB, pointE});
		linkVerticesWithEdges.put(pointB, new Point3D[] {pointA, pointC, sw.pt()});
		linkVerticesWithEdges.put(pointC, new Point3D[] {pointB, pointD, se.pt()});
		linkVerticesWithEdges.put(pointD, new Point3D[] {pointC, pointF});
		linkVerticesWithEdges.put(pointE, new Point3D[] {pointA, sw.pt(), pointG});
		linkVerticesWithEdges.put(pointF, new Point3D[] {pointD, se.pt(), pointH});
		linkVerticesWithEdges.put(pointG, new Point3D[] {pointE, nw.pt(), pointI});
		linkVerticesWithEdges.put(pointH, new Point3D[] {pointF, ne.pt(), pointL});
		linkVerticesWithEdges.put(pointI, new Point3D[] {pointG, pointJ});
		linkVerticesWithEdges.put(pointJ, new Point3D[] {pointI, nw.pt(), pointK});
		linkVerticesWithEdges.put(pointK, new Point3D[] {pointJ, ne.pt(), pointL});
		linkVerticesWithEdges.put(pointL, new Point3D[] {pointK, pointH});
		
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
		linkVerticestoFaces.put(pointCellA, new game.util.graph.Vertex[] {pointToVertex.get(pointA), pointToVertex.get(pointE), sw, pointToVertex.get(pointB)});
		linkVerticestoFaces.put(pointCellB, new game.util.graph.Vertex[] {pointToVertex.get(pointB), sw, se, pointToVertex.get(pointC)});
		linkVerticestoFaces.put(pointCellC, new game.util.graph.Vertex[] {pointToVertex.get(pointC), se, pointToVertex.get(pointF), pointToVertex.get(pointD)});
		linkVerticestoFaces.put(pointCellD, new game.util.graph.Vertex[] {pointToVertex.get(pointE), pointToVertex.get(pointG), nw, sw});
		linkVerticestoFaces.put(pointCellE, new game.util.graph.Vertex[] {se, ne, pointToVertex.get(pointH), pointToVertex.get(pointF)});
		linkVerticestoFaces.put(pointCellF, new game.util.graph.Vertex[] {pointToVertex.get(pointG), pointToVertex.get(pointI), pointToVertex.get(pointJ), nw});
		linkVerticestoFaces.put(pointCellG, new game.util.graph.Vertex[] {nw, pointToVertex.get(pointJ), pointToVertex.get(pointK), ne});
		linkVerticestoFaces.put(pointCellH, new game.util.graph.Vertex[] {ne, pointToVertex.get(pointK), pointToVertex.get(pointL), pointToVertex.get(pointH)});
			
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
					
					if (mappedNewToInitIndexes().containsKey(i-addedCells))
					{
		                addedCells += 1;
					}
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
}