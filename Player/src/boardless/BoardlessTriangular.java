package boardless;

import java.util.ArrayList;
import java.util.Arrays;
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
 * Methods to create the mapping that will tell the board how to grow, specific for triangular tiles.
 * 
 * @author Clémentine.Sacré
 */
public class BoardlessTriangular extends BoardlessAbstract  
{
	public BoardlessTriangular() {
        super();
    }
	
	private Graph cc(final Context context, final Cell cell)
	{
		List<game.util.graph.Vertex> allo = context.board().graph().faces().get(cell.index()).vertices();
		int initFace = context.board().graph().faces().size();
		
		boolean triangleUpsideDown = false;
		if (allo.get(0).pt().x() > allo.get(1).pt().x())
			triangleUpsideDown = true;
		
		HashMap<Point3D, Point3D[]> linkVerticesWithEdges = new HashMap<Point3D, Point3D[]>();
		HashMap<Point3D, game.util.graph.Vertex> pointToVertex = new HashMap<Point3D, game.util.graph.Vertex>();
		HashMap<String, game.util.graph.Vertex> pointToVertexS = new HashMap<String, game.util.graph.Vertex>();
		HashMap<Integer, game.util.graph.Vertex> pointToVertexPre = new HashMap<Integer, game.util.graph.Vertex>();
		HashMap<Point3D, game.util.graph.Vertex[]> linkVerticestoFaces = new HashMap<Point3D, game.util.graph.Vertex[]>();

		Point3D pointA;
		Point3D pointB;
		Point3D pointC;
		Point3D pointD;
		Point3D pointE;
		Point3D pointF;
		Point3D pointG;
		Point3D pointH;
		Point3D pointI;
		Point3D pointCellA;
		Point3D pointCellB;
		Point3D pointCellC;
		Point3D pointCellD;
		Point3D pointCellE;
		Point3D pointCellF;
		Point3D pointCellG;
		Point3D pointCellH;
		Point3D pointCellI;
		Point3D pointCellJ;
		Point3D pointCellK;
		Point3D pointCellL;
		
		if (triangleUpsideDown)
		{
			System.out.println("BoardlessTriangular.java forward() if1");
			// vertices
			game.util.graph.Vertex s = allo.get(0);
			game.util.graph.Vertex w = allo.get(1);
			game.util.graph.Vertex e = allo.get(2);
			HashSet<game.util.graph.Vertex> initVertex = new HashSet<game.util.graph.Vertex>();
			initVertex.add(s);
			initVertex.add(w);
			initVertex.add(e);
			
			double rightDiagonalX = e.pt().x() - s.pt().x();
			double rightDiagonalY = e.pt().y() - s.pt().y();
			double horizontalSide = e.pt().x() - w.pt().x();
			
			
			pointA = new Point3D(s.pt().x() - rightDiagonalX, s.pt().y() - rightDiagonalY, 0);
			pointB = new Point3D(s.pt().x() + rightDiagonalX, s.pt().y() - rightDiagonalY, 0);
			pointC = new Point3D(s.pt().x() - horizontalSide, s.pt().y(), 0);
			pointD = new Point3D(s.pt().x() + horizontalSide, s.pt().y(), 0);
			pointE = new Point3D(w.pt().x() - horizontalSide, w.pt().y(), 0);
			pointF = new Point3D(e.pt().x() + horizontalSide, e.pt().y(), 0);
			pointG = new Point3D(w.pt().x() - rightDiagonalX, w.pt().y() + rightDiagonalY, 0);
			pointH = new Point3D(e.pt().x() - rightDiagonalX, e.pt().y() + rightDiagonalY, 0);
			pointI = new Point3D(e.pt().x() + rightDiagonalX, e.pt().y() + rightDiagonalY, 0);
			
			// faces
	
			game.util.graph.Face currCell = context.board().graph().faces().get(cell.index());
	
			
			double centreToS = currCell.pt().y() - s.pt().y();
			double centreToWx = currCell.pt().x() - w.pt().x();
			double centreToWy = w.pt().y() - currCell.pt().y();
			
			pointCellA = new Point3D(currCell.pt().x() - centreToWx, currCell.pt().y() - centreToWy - centreToS, 0);
			pointCellB = new Point3D(currCell.pt().x(), currCell.pt().y() - (2*centreToS), 0);
			pointCellC = new Point3D(currCell.pt().x() + centreToWx, currCell.pt().y() - centreToWy - centreToS, 0);
			pointCellD = new Point3D(currCell.pt().x() - (2*centreToWx), currCell.pt().y(), 0);
			pointCellE = new Point3D(currCell.pt().x() - centreToWx, currCell.pt().y() + centreToWy - centreToS, 0);
			pointCellF = new Point3D(currCell.pt().x() + centreToWx, currCell.pt().y() + centreToWy - centreToS, 0);
			pointCellG = new Point3D(currCell.pt().x() + (2*centreToWx), currCell.pt().y(), 0);
			pointCellH = new Point3D(currCell.pt().x() - (2*centreToWx), currCell.pt().y() + (2*centreToWy), 0);
			pointCellI = new Point3D(currCell.pt().x() - centreToWx, currCell.pt().y() + centreToWy + centreToS, 0);
			pointCellJ = new Point3D(currCell.pt().x(), currCell.pt().y() + (2*centreToWy), 0);
			pointCellK = new Point3D(currCell.pt().x() + centreToWx, currCell.pt().y() + centreToWy + centreToS, 0);
			pointCellL = new Point3D(currCell.pt().x() + (2*centreToWx), currCell.pt().y() + (2*centreToWy), 0);
			
			// calculation
			Point3D[] newVertices = new Point3D[] {pointA, pointB, pointC, pointD, pointE, pointF, pointG, pointH, pointI};
			
			pointToVertex.put(s.pt(), s);
			pointToVertex.put(w.pt(), w);
			pointToVertex.put(e.pt(), e);
			
			pointToVertexS.put(point3dToString(s.pt()), s);
			pointToVertexS.put(point3dToString(w.pt()), w);
			pointToVertexS.put(point3dToString(e.pt()), e);
			
			pointToVertexPre.put(s.id(), s);
			pointToVertexPre.put(w.id(), w);
			pointToVertexPre.put(e.id(), e);
	
			
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
			
			
			linkVerticesWithEdges.put(pointA, new Point3D[] {pointC, s.pt(), pointB});
			linkVerticesWithEdges.put(pointB, new Point3D[] {pointA, s.pt(), pointD});
			linkVerticesWithEdges.put(pointC, new Point3D[] {pointA, s.pt(), pointE, w.pt()});
			linkVerticesWithEdges.put(pointD, new Point3D[] {s.pt(), e.pt(), pointF});
			linkVerticesWithEdges.put(pointE, new Point3D[] {pointC, w.pt(), pointG});
			linkVerticesWithEdges.put(pointF, new Point3D[] {pointD, e.pt(), pointI});
			linkVerticesWithEdges.put(pointG, new Point3D[] {pointE, w.pt(), pointH});
			linkVerticesWithEdges.put(pointH, new Point3D[] {w.pt(), e.pt(), pointG, pointI});
			linkVerticesWithEdges.put(pointI, new Point3D[] {e.pt(), pointF, pointH});
		}
		else
		{
			// vertices
			game.util.graph.Vertex n = allo.get(1);
			game.util.graph.Vertex e = allo.get(2);
			game.util.graph.Vertex w = allo.get(0);
			HashSet<game.util.graph.Vertex> initVertex = new HashSet<game.util.graph.Vertex>();
			initVertex.add(n);
			initVertex.add(e);
			initVertex.add(w);
			
			
			double leftDiagonalX = n.pt().x() - w.pt().x();
			double leftDiagonalY = n.pt().y() - w.pt().y();
			double rightDiagonalX = e.pt().x() - n.pt().x();
			double rightDiagonalY = n.pt().y() - e.pt().y();
			double horizontalSide = e.pt().x() - w.pt().x();
			
			
			pointA = new Point3D(w.pt().x() - leftDiagonalX, w.pt().y() - leftDiagonalY, 0);
			pointB = new Point3D(w.pt().x() + rightDiagonalX, w.pt().y() - rightDiagonalY, 0);
			pointC = new Point3D(e.pt().x() + rightDiagonalX, e.pt().y() - rightDiagonalY, 0);
			pointD = new Point3D(w.pt().x() - horizontalSide, w.pt().y(), 0);
			pointE = new Point3D(e.pt().x() + horizontalSide, e.pt().y(), 0);
			pointF = new Point3D(n.pt().x() - horizontalSide, n.pt().y(), 0);
			pointG = new Point3D(n.pt().x() + horizontalSide, n.pt().y(), 0);
			pointH = new Point3D(n.pt().x() - leftDiagonalX, n.pt().y() + leftDiagonalY, 0);
			pointI = new Point3D(n.pt().x() + rightDiagonalX, n.pt().y() + rightDiagonalY, 0);
			
			// faces
	
			game.util.graph.Face currCell = context.board().graph().faces().get(cell.index());
	
			
			double centreToN = n.pt().y() - currCell.pt().y();
			double centreToWx = currCell.pt().x() - w.pt().x();
			double centreToWy = currCell.pt().y() - w.pt().y();
			
			pointCellA = new Point3D(currCell.pt().x() - (2*centreToWx), currCell.pt().y() - (2*centreToWy), 0);
			pointCellB = new Point3D(currCell.pt().x() - centreToWx, currCell.pt().y() - centreToWy - centreToN, 0);
			pointCellC = new Point3D(currCell.pt().x(), currCell.pt().y() - (2*centreToWy), 0);
			pointCellD = new Point3D(currCell.pt().x() + centreToWx, currCell.pt().y() - centreToWy - centreToN, 0);
			pointCellE = new Point3D(currCell.pt().x() + (2*centreToWx), currCell.pt().y() - (2*centreToWy), 0);
			pointCellF = new Point3D(currCell.pt().x() - (2*centreToWx), currCell.pt().y(), 0);
			pointCellG = new Point3D(currCell.pt().x() - centreToWx, currCell.pt().y() - centreToWy + centreToN, 0);
			pointCellH = new Point3D(currCell.pt().x() + centreToWx, currCell.pt().y() - centreToWy + centreToN, 0);
			pointCellI = new Point3D(currCell.pt().x() + (2*centreToWx), currCell.pt().y(), 0);
			pointCellJ = new Point3D(currCell.pt().x() - centreToWx, currCell.pt().y() + centreToN + centreToWy, 0);
			pointCellK = new Point3D(currCell.pt().x() + centreToWx, currCell.pt().y() + centreToN + centreToWy, 0);
			pointCellL = new Point3D(currCell.pt().x(), currCell.pt().y() + (2*centreToN), 0);
			
			// calculation
			Point3D[] newVertices = new Point3D[] {pointA, pointB, pointC, pointD, pointE, pointF, pointG, pointH, pointI};
			
			pointToVertex.put(n.pt(), n);
			pointToVertex.put(e.pt(), e);
			pointToVertex.put(w.pt(), w);
			
			pointToVertexS.put(point3dToString(n.pt()), n);
			pointToVertexS.put(point3dToString(e.pt()), e);
			pointToVertexS.put(point3dToString(w.pt()), w);
			
			pointToVertexPre.put(n.id(), n);
			pointToVertexPre.put(e.id(), e);
			pointToVertexPre.put(w.id(), w);
	
			
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
			
			
			linkVerticesWithEdges.put(pointA, new Point3D[] {pointD, w.pt(), pointB});
			linkVerticesWithEdges.put(pointB, new Point3D[] {pointA, w.pt(), e.pt(), pointC});
			linkVerticesWithEdges.put(pointC, new Point3D[] {pointB, e.pt(), pointE});
			linkVerticesWithEdges.put(pointD, new Point3D[] {pointA, w.pt(), pointF});
			linkVerticesWithEdges.put(pointE, new Point3D[] {pointC, e.pt(), pointG});
			linkVerticesWithEdges.put(pointF, new Point3D[] {pointD, w.pt(), n.pt(), pointH});
			linkVerticesWithEdges.put(pointG, new Point3D[] {e.pt(), pointE, n.pt(), pointI});
			linkVerticesWithEdges.put(pointH, new Point3D[] {pointF, n.pt(), pointI});
			linkVerticesWithEdges.put(pointI, new Point3D[] {n.pt(), pointG, pointH});
		}
		
		System.out.println("BoardlessTriangular.java forward() pointCellA : "+pointCellA);
		System.out.println("BoardlessTriangular.java forward() pointCellB : "+pointCellB);
		System.out.println("BoardlessTriangular.java forward() pointCellC : "+pointCellC);
		System.out.println("BoardlessTriangular.java forward() pointCellD : "+pointCellD);
		System.out.println("BoardlessTriangular.java forward() pointCellE : "+pointCellE);
		System.out.println("BoardlessTriangular.java forward() pointCellF : "+pointCellF);
		System.out.println("BoardlessTriangular.java forward() pointCellG : "+pointCellG);
		System.out.println("BoardlessTriangular.java forward() pointCellH : "+pointCellH);
		System.out.println("BoardlessTriangular.java forward() pointCellI : "+pointCellI);
		System.out.println("BoardlessTriangular.java forward() pointCellJ : "+pointCellJ);
		System.out.println("BoardlessTriangular.java forward() pointCellK : "+pointCellK);
		System.out.println("BoardlessTriangular.java forward() pointCellL : "+pointCellL);
		
		
		// making links
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
		
		if (triangleUpsideDown)
		{
			game.util.graph.Vertex s = allo.get(0);
			game.util.graph.Vertex w = allo.get(1);
			game.util.graph.Vertex e = allo.get(2);
			
			// faces
			linkVerticestoFaces.put(pointCellA, new game.util.graph.Vertex[] {pointToVertex.get(pointA), pointToVertex.get(pointC), s});
			linkVerticestoFaces.put(pointCellB, new game.util.graph.Vertex[] {pointToVertex.get(pointA), s, pointToVertex.get(pointB)});
			linkVerticestoFaces.put(pointCellC, new game.util.graph.Vertex[] {pointToVertex.get(pointB), s, pointToVertex.get(pointD)});
			linkVerticestoFaces.put(pointCellD, new game.util.graph.Vertex[] {pointToVertex.get(pointC), pointToVertex.get(pointE), w});
			linkVerticestoFaces.put(pointCellE, new game.util.graph.Vertex[] {pointToVertex.get(pointC), w, s});
			linkVerticestoFaces.put(pointCellF, new game.util.graph.Vertex[] {s, e, pointToVertex.get(pointD)});
			linkVerticestoFaces.put(pointCellG, new game.util.graph.Vertex[] {pointToVertex.get(pointD), e, pointToVertex.get(pointF)});
			linkVerticestoFaces.put(pointCellH, new game.util.graph.Vertex[] {pointToVertex.get(pointE), pointToVertex.get(pointG), w});
			linkVerticestoFaces.put(pointCellI, new game.util.graph.Vertex[] {w, pointToVertex.get(pointG), pointToVertex.get(pointH)});
			linkVerticestoFaces.put(pointCellJ, new game.util.graph.Vertex[] {w, pointToVertex.get(pointH), e});
			linkVerticestoFaces.put(pointCellK, new game.util.graph.Vertex[] {e, pointToVertex.get(pointH), pointToVertex.get(pointI)});
			linkVerticestoFaces.put(pointCellL, new game.util.graph.Vertex[] {e, pointToVertex.get(pointI), pointToVertex.get(pointF)});
		}
		else
		{		
			game.util.graph.Vertex n = allo.get(1);
			game.util.graph.Vertex e = allo.get(2);
			game.util.graph.Vertex w = allo.get(0);
			// faces
			linkVerticestoFaces.put(pointCellA, new game.util.graph.Vertex[] {pointToVertex.get(pointA), pointToVertex.get(pointD), w});
			linkVerticestoFaces.put(pointCellB, new game.util.graph.Vertex[] {pointToVertex.get(pointA), w, pointToVertex.get(pointB)});
			linkVerticestoFaces.put(pointCellC, new game.util.graph.Vertex[] {pointToVertex.get(pointB), w, e});
			linkVerticestoFaces.put(pointCellD, new game.util.graph.Vertex[] {pointToVertex.get(pointB), e, pointToVertex.get(pointC)});
			linkVerticestoFaces.put(pointCellE, new game.util.graph.Vertex[] {pointToVertex.get(pointC), e, pointToVertex.get(pointE)});
			linkVerticestoFaces.put(pointCellF, new game.util.graph.Vertex[] {pointToVertex.get(pointD), pointToVertex.get(pointF), w});
			linkVerticestoFaces.put(pointCellG, new game.util.graph.Vertex[] {w, pointToVertex.get(pointF), n});
			linkVerticestoFaces.put(pointCellH, new game.util.graph.Vertex[] {e, n, pointToVertex.get(pointG)});
			linkVerticestoFaces.put(pointCellI, new game.util.graph.Vertex[] {e, pointToVertex.get(pointG), pointToVertex.get(pointE)});
			linkVerticestoFaces.put(pointCellJ, new game.util.graph.Vertex[] {pointToVertex.get(pointF), pointToVertex.get(pointH), n});
			linkVerticestoFaces.put(pointCellK, new game.util.graph.Vertex[] {n, pointToVertex.get(pointI), pointToVertex.get(pointG)});
			linkVerticestoFaces.put(pointCellL, new game.util.graph.Vertex[] {n, pointToVertex.get(pointH), pointToVertex.get(pointI)});
		}
		
		HashSet<Face> addedFaces = new HashSet<Face>();
		for (Point3D p3dCell : linkVerticestoFaces.keySet())
		{
			Face f = context.board().graph().findFace(p3dCell.x(), p3dCell.y(), p3dCell.z());
			System.out.println("BoardlessTriangular.java forward() p3dCell : "+p3dCell);
			if (f == null)
			{	
				Face newF = context.board().graph().addFace(linkVerticestoFaces.get(p3dCell));
				System.out.println("BoardlessTriangular.java forward() newF : "+newF);
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