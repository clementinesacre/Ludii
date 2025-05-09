package boardless;

import java.util.HashMap;
import java.util.HashSet;

import main.math.Point3D;
import other.context.Context;

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
	
	@Override
	// calculateNewVCE
	protected void calculateNewGraphElements(final Context context)
	{		
		// check if top of the triangle is top of triangle is facing up or down - as this will impact the calulcation of the other graph elements
		boolean triangleUpsideDown = false;
		if (verticesOfCellEdgeMove.get(0).pt().x() > verticesOfCellEdgeMove.get(1).pt().x())
			triangleUpsideDown = true;
		
		vertexPointToNeighborsPoint = new HashMap<Point3D, Point3D[]>();
		point3dToVertex = new HashMap<String, game.util.graph.Vertex>();
		indexToVertex = new HashMap<Integer, game.util.graph.Vertex>();
		cellPointToVerticesPoint = new HashMap<Point3D, game.util.graph.Vertex[]>();

		HashSet<game.util.graph.Vertex> mainVertices = new HashSet<game.util.graph.Vertex>();
		
		Point3D pointVertexA;
		Point3D pointVertexB;
		Point3D pointVertexC;
		Point3D pointVertexD;
		Point3D pointVertexE;
		Point3D pointVertexF;
		Point3D pointVertexG;
		Point3D pointVertexH;
		Point3D pointVertexI;
		
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
			// getting the 3 vertices around the cell on which the edge move was made
			game.util.graph.Vertex s = verticesOfCellEdgeMove.get(0);
			game.util.graph.Vertex w = verticesOfCellEdgeMove.get(1);
			game.util.graph.Vertex e = verticesOfCellEdgeMove.get(2);
			mainVertices.add(s);
			mainVertices.add(w);
			mainVertices.add(e);

			// calculate the 9 vertices around, will help know which one exist already or not
			double rightDiagonalX = e.pt().x() - s.pt().x();
			double rightDiagonalY = e.pt().y() - s.pt().y();
			double horizontalSide = e.pt().x() - w.pt().x();
			
			pointVertexA = new Point3D(s.pt().x() - rightDiagonalX, s.pt().y() - rightDiagonalY, 0);
			pointVertexB = new Point3D(s.pt().x() + rightDiagonalX, s.pt().y() - rightDiagonalY, 0);
			pointVertexC = new Point3D(s.pt().x() - horizontalSide, s.pt().y(), 0);
			pointVertexD = new Point3D(s.pt().x() + horizontalSide, s.pt().y(), 0);
			pointVertexE = new Point3D(w.pt().x() - horizontalSide, w.pt().y(), 0);
			pointVertexF = new Point3D(e.pt().x() + horizontalSide, e.pt().y(), 0);
			pointVertexG = new Point3D(w.pt().x() - rightDiagonalX, w.pt().y() + rightDiagonalY, 0);
			pointVertexH = new Point3D(e.pt().x() - rightDiagonalX, e.pt().y() + rightDiagonalY, 0);
			pointVertexI = new Point3D(e.pt().x() + rightDiagonalX, e.pt().y() + rightDiagonalY, 0);
			
			// calculation
			newVertices = new Point3D[] {pointVertexA, pointVertexB, pointVertexC, pointVertexD, pointVertexE, pointVertexF, pointVertexG, pointVertexH, pointVertexI};
			
			point3dToVertex.put(point3dToString(s.pt()), s);
			point3dToVertex.put(point3dToString(w.pt()), w);
			point3dToVertex.put(point3dToString(e.pt()), e);
			
			indexToVertex.put(s.id(), s);
			indexToVertex.put(w.id(), w);
			indexToVertex.put(e.id(), e);
	
			detectNewVertices(context);
			
			// link each vertex to its neighbors, in order to create the edges
			vertexPointToNeighborsPoint.put(pointVertexA, new Point3D[] {pointVertexC, s.pt(), pointVertexB});
			vertexPointToNeighborsPoint.put(pointVertexB, new Point3D[] {pointVertexA, s.pt(), pointVertexD});
			vertexPointToNeighborsPoint.put(pointVertexC, new Point3D[] {pointVertexA, s.pt(), pointVertexE, w.pt()});
			vertexPointToNeighborsPoint.put(pointVertexD, new Point3D[] {s.pt(), e.pt(), pointVertexF});
			vertexPointToNeighborsPoint.put(pointVertexE, new Point3D[] {pointVertexC, w.pt(), pointVertexG});
			vertexPointToNeighborsPoint.put(pointVertexF, new Point3D[] {pointVertexD, e.pt(), pointVertexI});
			vertexPointToNeighborsPoint.put(pointVertexG, new Point3D[] {pointVertexE, w.pt(), pointVertexH});
			vertexPointToNeighborsPoint.put(pointVertexH, new Point3D[] {w.pt(), e.pt(), pointVertexG, pointVertexI});
			vertexPointToNeighborsPoint.put(pointVertexI, new Point3D[] {e.pt(), pointVertexF, pointVertexH});
		}
		else
		{
			// getting the 3 vertices around the cell on which the edge move was made
			game.util.graph.Vertex n = verticesOfCellEdgeMove.get(1);
			game.util.graph.Vertex e = verticesOfCellEdgeMove.get(2);
			game.util.graph.Vertex w = verticesOfCellEdgeMove.get(0);
			mainVertices.add(n);
			mainVertices.add(e);
			mainVertices.add(w);
		
			// calculate the 9 vertices around, will help know which one exist already or not
			double leftDiagonalX = n.pt().x() - w.pt().x();
			double leftDiagonalY = n.pt().y() - w.pt().y();
			double rightDiagonalX = e.pt().x() - n.pt().x();
			double rightDiagonalY = n.pt().y() - e.pt().y();
			double horizontalSide = e.pt().x() - w.pt().x();
			
			pointVertexA = new Point3D(w.pt().x() - leftDiagonalX, w.pt().y() - leftDiagonalY, 0);
			pointVertexB = new Point3D(w.pt().x() + rightDiagonalX, w.pt().y() - rightDiagonalY, 0);
			pointVertexC = new Point3D(e.pt().x() + rightDiagonalX, e.pt().y() - rightDiagonalY, 0);
			pointVertexD = new Point3D(w.pt().x() - horizontalSide, w.pt().y(), 0);
			pointVertexE = new Point3D(e.pt().x() + horizontalSide, e.pt().y(), 0);
			pointVertexF = new Point3D(n.pt().x() - horizontalSide, n.pt().y(), 0);
			pointVertexG = new Point3D(n.pt().x() + horizontalSide, n.pt().y(), 0);
			pointVertexH = new Point3D(n.pt().x() - leftDiagonalX, n.pt().y() + leftDiagonalY, 0);
			pointVertexI = new Point3D(n.pt().x() + rightDiagonalX, n.pt().y() + rightDiagonalY, 0);
			
			// calculation
			newVertices = new Point3D[] {pointVertexA, pointVertexB, pointVertexC, pointVertexD, pointVertexE, pointVertexF, pointVertexG, pointVertexH, pointVertexI};
			
			point3dToVertex.put(point3dToString(n.pt()), n);
			point3dToVertex.put(point3dToString(e.pt()), e);
			point3dToVertex.put(point3dToString(w.pt()), w);
			
			indexToVertex.put(n.id(), n);
			indexToVertex.put(e.id(), e);
			indexToVertex.put(w.id(), w);
	
			detectNewVertices(context);	
			
			vertexPointToNeighborsPoint.put(pointVertexA, new Point3D[] {pointVertexD, w.pt(), pointVertexB});
			vertexPointToNeighborsPoint.put(pointVertexB, new Point3D[] {pointVertexA, w.pt(), e.pt(), pointVertexC});
			vertexPointToNeighborsPoint.put(pointVertexC, new Point3D[] {pointVertexB, e.pt(), pointVertexE});
			vertexPointToNeighborsPoint.put(pointVertexD, new Point3D[] {pointVertexA, w.pt(), pointVertexF});
			vertexPointToNeighborsPoint.put(pointVertexE, new Point3D[] {pointVertexC, e.pt(), pointVertexG});
			vertexPointToNeighborsPoint.put(pointVertexF, new Point3D[] {pointVertexD, w.pt(), n.pt(), pointVertexH});
			vertexPointToNeighborsPoint.put(pointVertexG, new Point3D[] {e.pt(), pointVertexE, n.pt(), pointVertexI});
			vertexPointToNeighborsPoint.put(pointVertexH, new Point3D[] {pointVertexF, n.pt(), pointVertexI});
			vertexPointToNeighborsPoint.put(pointVertexI, new Point3D[] {n.pt(), pointVertexG, pointVertexH});
		}
		
		createVandE(context);
		
		if (triangleUpsideDown)
		{
			game.util.graph.Vertex s = verticesOfCellEdgeMove.get(0);
			game.util.graph.Vertex w = verticesOfCellEdgeMove.get(1);
			game.util.graph.Vertex e = verticesOfCellEdgeMove.get(2);
			
			// calculate the 12 cells around, will help know which one exist already or not
			double centreToS = cellEdgeMove.pt().y() - s.pt().y();
			double centreToWx = cellEdgeMove.pt().x() - w.pt().x();
			double centreToWy = w.pt().y() - cellEdgeMove.pt().y();
			
			pointCellA = new Point3D(cellEdgeMove.pt().x() - centreToWx, cellEdgeMove.pt().y() - centreToWy - centreToS, 0);
			pointCellB = new Point3D(cellEdgeMove.pt().x(), cellEdgeMove.pt().y() - (2*centreToS), 0);
			pointCellC = new Point3D(cellEdgeMove.pt().x() + centreToWx, cellEdgeMove.pt().y() - centreToWy - centreToS, 0);
			pointCellD = new Point3D(cellEdgeMove.pt().x() - (2*centreToWx), cellEdgeMove.pt().y(), 0);
			pointCellE = new Point3D(cellEdgeMove.pt().x() - centreToWx, cellEdgeMove.pt().y() + centreToWy - centreToS, 0);
			pointCellF = new Point3D(cellEdgeMove.pt().x() + centreToWx, cellEdgeMove.pt().y() + centreToWy - centreToS, 0);
			pointCellG = new Point3D(cellEdgeMove.pt().x() + (2*centreToWx), cellEdgeMove.pt().y(), 0);
			pointCellH = new Point3D(cellEdgeMove.pt().x() - (2*centreToWx), cellEdgeMove.pt().y() + (2*centreToWy), 0);
			pointCellI = new Point3D(cellEdgeMove.pt().x() - centreToWx, cellEdgeMove.pt().y() + centreToWy + centreToS, 0);
			pointCellJ = new Point3D(cellEdgeMove.pt().x(), cellEdgeMove.pt().y() + (2*centreToWy), 0);
			pointCellK = new Point3D(cellEdgeMove.pt().x() + centreToWx, cellEdgeMove.pt().y() + centreToWy + centreToS, 0);
			pointCellL = new Point3D(cellEdgeMove.pt().x() + (2*centreToWx), cellEdgeMove.pt().y() + (2*centreToWy), 0);
						
			// link each cell to its vertices
			cellPointToVerticesPoint.put(pointCellA, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexA)), point3dToVertex.get(point3dToString(pointVertexC)), s});
			cellPointToVerticesPoint.put(pointCellB, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexA)), s, point3dToVertex.get(point3dToString(pointVertexB))});
			cellPointToVerticesPoint.put(pointCellC, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexB)), s, point3dToVertex.get(point3dToString(pointVertexD))});
			cellPointToVerticesPoint.put(pointCellD, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexC)), point3dToVertex.get(point3dToString(pointVertexE)), w});
			cellPointToVerticesPoint.put(pointCellE, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexC)), w, s});
			cellPointToVerticesPoint.put(pointCellF, new game.util.graph.Vertex[] {s, e, point3dToVertex.get(point3dToString(pointVertexD))});
			cellPointToVerticesPoint.put(pointCellG, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexD)), e, point3dToVertex.get(point3dToString(pointVertexF))});
			cellPointToVerticesPoint.put(pointCellH, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexE)), point3dToVertex.get(point3dToString(pointVertexG)), w});
			cellPointToVerticesPoint.put(pointCellI, new game.util.graph.Vertex[] {w, point3dToVertex.get(point3dToString(pointVertexG)), point3dToVertex.get(point3dToString(pointVertexH))});
			cellPointToVerticesPoint.put(pointCellJ, new game.util.graph.Vertex[] {w, point3dToVertex.get(point3dToString(pointVertexH)), e});
			cellPointToVerticesPoint.put(pointCellK, new game.util.graph.Vertex[] {e, point3dToVertex.get(point3dToString(pointVertexH)), point3dToVertex.get(point3dToString(pointVertexI))});
			cellPointToVerticesPoint.put(pointCellL, new game.util.graph.Vertex[] {e, point3dToVertex.get(point3dToString(pointVertexI)), point3dToVertex.get(point3dToString(pointVertexF))});
		}
		else
		{		
			game.util.graph.Vertex n = verticesOfCellEdgeMove.get(1);
			game.util.graph.Vertex e = verticesOfCellEdgeMove.get(2);
			game.util.graph.Vertex w = verticesOfCellEdgeMove.get(0);
			
			// calculate the 12 cells around, will help know which one exist already or not
			double centreToN = n.pt().y() - cellEdgeMove.pt().y();
			double centreToWx = cellEdgeMove.pt().x() - w.pt().x();
			double centreToWy = cellEdgeMove.pt().y() - w.pt().y();
			
			pointCellA = new Point3D(cellEdgeMove.pt().x() - (2*centreToWx), cellEdgeMove.pt().y() - (2*centreToWy), 0);
			pointCellB = new Point3D(cellEdgeMove.pt().x() - centreToWx, cellEdgeMove.pt().y() - centreToWy - centreToN, 0);
			pointCellC = new Point3D(cellEdgeMove.pt().x(), cellEdgeMove.pt().y() - (2*centreToWy), 0);
			pointCellD = new Point3D(cellEdgeMove.pt().x() + centreToWx, cellEdgeMove.pt().y() - centreToWy - centreToN, 0);
			pointCellE = new Point3D(cellEdgeMove.pt().x() + (2*centreToWx), cellEdgeMove.pt().y() - (2*centreToWy), 0);
			pointCellF = new Point3D(cellEdgeMove.pt().x() - (2*centreToWx), cellEdgeMove.pt().y(), 0);
			pointCellG = new Point3D(cellEdgeMove.pt().x() - centreToWx, cellEdgeMove.pt().y() - centreToWy + centreToN, 0);
			pointCellH = new Point3D(cellEdgeMove.pt().x() + centreToWx, cellEdgeMove.pt().y() - centreToWy + centreToN, 0);
			pointCellI = new Point3D(cellEdgeMove.pt().x() + (2*centreToWx), cellEdgeMove.pt().y(), 0);
			pointCellJ = new Point3D(cellEdgeMove.pt().x() - centreToWx, cellEdgeMove.pt().y() + centreToN + centreToWy, 0);
			pointCellK = new Point3D(cellEdgeMove.pt().x() + centreToWx, cellEdgeMove.pt().y() + centreToN + centreToWy, 0);
			pointCellL = new Point3D(cellEdgeMove.pt().x(), cellEdgeMove.pt().y() + (2*centreToN), 0);
					
			// link each cell to its vertices
			cellPointToVerticesPoint.put(pointCellA, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexA)), point3dToVertex.get(point3dToString(pointVertexD)), w});
			cellPointToVerticesPoint.put(pointCellB, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexA)), w, point3dToVertex.get(point3dToString(pointVertexB))});
			cellPointToVerticesPoint.put(pointCellC, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexB)), w, e});
			cellPointToVerticesPoint.put(pointCellD, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexB)), e, point3dToVertex.get(point3dToString(pointVertexC))});
			cellPointToVerticesPoint.put(pointCellE, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexC)), e, point3dToVertex.get(point3dToString(pointVertexE))});
			cellPointToVerticesPoint.put(pointCellF, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexD)), point3dToVertex.get(point3dToString(pointVertexF)), w});
			cellPointToVerticesPoint.put(pointCellG, new game.util.graph.Vertex[] {w, point3dToVertex.get(point3dToString(pointVertexF)), n});
			cellPointToVerticesPoint.put(pointCellH, new game.util.graph.Vertex[] {e, n, point3dToVertex.get(point3dToString(pointVertexG))});
			cellPointToVerticesPoint.put(pointCellI, new game.util.graph.Vertex[] {e, point3dToVertex.get(point3dToString(pointVertexG)), point3dToVertex.get(point3dToString(pointVertexE))});
			cellPointToVerticesPoint.put(pointCellJ, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexF)), point3dToVertex.get(point3dToString(pointVertexH)), n});
			cellPointToVerticesPoint.put(pointCellK, new game.util.graph.Vertex[] {n, point3dToVertex.get(point3dToString(pointVertexI)), point3dToVertex.get(point3dToString(pointVertexG))});
			cellPointToVerticesPoint.put(pointCellL, new game.util.graph.Vertex[] {n, point3dToVertex.get(point3dToString(pointVertexH)), point3dToVertex.get(point3dToString(pointVertexI))});
		}
	}
}