package boardless;

import java.util.HashMap;
import java.util.HashSet;

import game.util.graph.Graph;
import main.math.Point3D;

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
	
	@Override
	protected void calculateNewGraphElements(final Graph graph)
	{	
		// getting the 6 vertices around the cell on which the edge move was made
		HashSet<game.util.graph.Vertex> mainVertices = new HashSet<game.util.graph.Vertex>();
		game.util.graph.Vertex n = verticesOfCellEdgeMove.get(2);
		game.util.graph.Vertex ne = verticesOfCellEdgeMove.get(3);
		game.util.graph.Vertex se = verticesOfCellEdgeMove.get(4);
		game.util.graph.Vertex s = verticesOfCellEdgeMove.get(5);
		game.util.graph.Vertex sw = verticesOfCellEdgeMove.get(0);
		game.util.graph.Vertex nw = verticesOfCellEdgeMove.get(1);
		mainVertices.add(n);
		mainVertices.add(ne);
		mainVertices.add(se);
		mainVertices.add(s);
		mainVertices.add(sw);
		mainVertices.add(nw);

		// calculate the 18 vertices around, will help know which one exist already or not
		double verticalSide = ne.pt().y() - se.pt().y();
		double verticalCenter = n.pt().y() - s.pt().y();
		double horizontalSide = se.pt().x() - sw.pt().x();
		
		Point3D pointVertexA = new Point3D(s.pt().x() - (ne.pt().x() - sw.pt().x()), s.pt().y() - verticalSide, 0);
		Point3D pointVertexB = new Point3D(sw.pt().x(), sw.pt().y() - verticalCenter, 0);
		Point3D pointVertexC = new Point3D(s.pt().x(), s.pt().y() - verticalSide, 0);
		Point3D pointVertexD = new Point3D(se.pt().x(), se.pt().y() - verticalCenter, 0);
		Point3D pointVertexE = new Point3D(s.pt().x() + (se.pt().x() - nw.pt().x()), s.pt().y() - verticalSide, 0);
		Point3D pointVertexF = new Point3D(sw.pt().x() - horizontalSide, sw.pt().y(), 0);
		Point3D pointVertexG = new Point3D(s.pt().x() - horizontalSide, s.pt().y(), 0);
		Point3D pointVertexH = new Point3D(s.pt().x() + horizontalSide, s.pt().y(), 0);
		Point3D pointVertexI = new Point3D(se.pt().x() + horizontalSide, se.pt().y(), 0);
		Point3D pointVertexJ = new Point3D(nw.pt().x() - horizontalSide, nw.pt().y(), 0);
		Point3D pointVertexK = new Point3D(n.pt().x() - horizontalSide, n.pt().y(), 0);
		Point3D pointVertexL = new Point3D(n.pt().x() + horizontalSide, n.pt().y(), 0);
		Point3D pointVertexM = new Point3D(ne.pt().x() + horizontalSide, ne.pt().y(), 0);
		Point3D pointVertexN = new Point3D(n.pt().x() - horizontalSide, n.pt().y() + verticalSide, 0);
		Point3D pointVertexO = new Point3D(nw.pt().x(), nw.pt().y() + verticalCenter, 0);
		Point3D pointVertexP = new Point3D(n.pt().x(), n.pt().y() + verticalSide, 0);
		Point3D pointVertexQ = new Point3D(ne.pt().x(), nw.pt().y() + verticalCenter, 0);
		Point3D pointVertexR = new Point3D(n.pt().x() + horizontalSide, n.pt().y() + verticalSide, 0);
		
		newPoint3dVertices = new Point3D[] {pointVertexA, pointVertexB, pointVertexC, pointVertexD, pointVertexE, pointVertexF, pointVertexG, pointVertexH, pointVertexI, pointVertexJ, pointVertexK, pointVertexL, pointVertexM, pointVertexN, pointVertexO, pointVertexP, pointVertexQ, pointVertexR};
		
		point3dToVertex = new HashMap<String, game.util.graph.Vertex>();
		point3dToVertex.put(point3dToString(n.pt()), n);
		point3dToVertex.put(point3dToString(ne.pt()), ne);
		point3dToVertex.put(point3dToString(se.pt()), se);
		point3dToVertex.put(point3dToString(s.pt()), s);
		point3dToVertex.put(point3dToString(sw.pt()), sw);
		point3dToVertex.put(point3dToString(nw.pt()), nw);
		
		indexToVertex = new HashMap<Integer, game.util.graph.Vertex>();
		indexToVertex.put(n.id(), n);
		indexToVertex.put(ne.id(), ne);
		indexToVertex.put(se.id(), se);
		indexToVertex.put(s.id(), s);
		indexToVertex.put(sw.id(), sw);
		indexToVertex.put(nw.id(), nw);

		detectNewVertices(graph);	
		
		// link each vertex to its neighbors, in order to create the edges
		vertexPointToNeighborsPoint = new HashMap<Point3D, Point3D[]>();
		vertexPointToNeighborsPoint.put(pointVertexA, new Point3D[] {pointVertexB, pointVertexG});
		vertexPointToNeighborsPoint.put(pointVertexB, new Point3D[] {pointVertexA, pointVertexC});
		vertexPointToNeighborsPoint.put(pointVertexC, new Point3D[] {pointVertexB, pointVertexD, s.pt()});
		vertexPointToNeighborsPoint.put(pointVertexD, new Point3D[] {pointVertexC, pointVertexE});
		vertexPointToNeighborsPoint.put(pointVertexE, new Point3D[] {pointVertexD, pointVertexH});
		vertexPointToNeighborsPoint.put(pointVertexF, new Point3D[] {pointVertexG, pointVertexJ});
		vertexPointToNeighborsPoint.put(pointVertexG, new Point3D[] {pointVertexA, pointVertexF, sw.pt()});
		vertexPointToNeighborsPoint.put(pointVertexH, new Point3D[] {pointVertexE, pointVertexI, se.pt()});
		vertexPointToNeighborsPoint.put(pointVertexI, new Point3D[] {pointVertexH, pointVertexM});
		vertexPointToNeighborsPoint.put(pointVertexJ, new Point3D[] {pointVertexF, pointVertexK});
		vertexPointToNeighborsPoint.put(pointVertexK, new Point3D[] {pointVertexJ, pointVertexN, nw.pt()});
		vertexPointToNeighborsPoint.put(pointVertexL, new Point3D[] {pointVertexM, pointVertexR, ne.pt()});
		vertexPointToNeighborsPoint.put(pointVertexM, new Point3D[] {pointVertexI, pointVertexL});
		vertexPointToNeighborsPoint.put(pointVertexN, new Point3D[] {pointVertexK, pointVertexO});
		vertexPointToNeighborsPoint.put(pointVertexO, new Point3D[] {pointVertexN, pointVertexP});
		vertexPointToNeighborsPoint.put(pointVertexP, new Point3D[] {pointVertexO, pointVertexQ, n.pt()});
		vertexPointToNeighborsPoint.put(pointVertexQ, new Point3D[] {pointVertexP, pointVertexR});
		vertexPointToNeighborsPoint.put(pointVertexR, new Point3D[] {pointVertexQ, pointVertexL});
		
		createVandE(graph);
		
		// calculate the 6 cells around, will help know which one exist already or not
		double vertical = cellEdgeMove.pt().y() - s.pt().y();
		double diagonalX = cellEdgeMove.pt().x() - sw.pt().x();
		double diagonalY = cellEdgeMove.pt().y() - sw.pt().y();
		
		Point3D pointCellA = new Point3D(cellEdgeMove.pt().x() - diagonalX, cellEdgeMove.pt().y() - vertical - diagonalY, 0);
		Point3D pointCellB = new Point3D(cellEdgeMove.pt().x() + diagonalX, cellEdgeMove.pt().y() - vertical - diagonalY, 0);
		Point3D pointCellC = new Point3D(cellEdgeMove.pt().x() - (2*diagonalX), cellEdgeMove.pt().y(), 0);
		Point3D pointCellD = new Point3D(cellEdgeMove.pt().x() + (2*diagonalX), cellEdgeMove.pt().y(), 0);
		Point3D pointCellE = new Point3D(cellEdgeMove.pt().x() - diagonalX, cellEdgeMove.pt().y() + vertical + diagonalY, 0);
		Point3D pointCellF = new Point3D(cellEdgeMove.pt().x() + diagonalX, cellEdgeMove.pt().y() + vertical + diagonalY, 0);
				
		// link each cell to its vertices
		cellPointToVerticesPoint = new HashMap<Point3D, game.util.graph.Vertex[]>();
		cellPointToVerticesPoint.put(pointCellA, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexA)), point3dToVertex.get(point3dToString(pointVertexG)), sw, s, point3dToVertex.get(point3dToString(pointVertexC)), point3dToVertex.get(point3dToString(pointVertexB))});
		cellPointToVerticesPoint.put(pointCellB, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexC)), s, se, point3dToVertex.get(point3dToString(pointVertexH)), point3dToVertex.get(point3dToString(pointVertexE)), point3dToVertex.get(point3dToString(pointVertexD))});
		cellPointToVerticesPoint.put(pointCellC, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexF)), point3dToVertex.get(point3dToString(pointVertexJ)), point3dToVertex.get(point3dToString(pointVertexK)), nw, sw, point3dToVertex.get(point3dToString(pointVertexG))});
		cellPointToVerticesPoint.put(pointCellD, new game.util.graph.Vertex[] {se, ne, point3dToVertex.get(point3dToString(pointVertexL)), point3dToVertex.get(point3dToString(pointVertexM)), point3dToVertex.get(point3dToString(pointVertexI)), point3dToVertex.get(point3dToString(pointVertexH))});
		cellPointToVerticesPoint.put(pointCellE, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexK)), point3dToVertex.get(point3dToString(pointVertexN)), point3dToVertex.get(point3dToString(pointVertexO)), point3dToVertex.get(point3dToString(pointVertexP)), n, nw});
		cellPointToVerticesPoint.put(pointCellF, new game.util.graph.Vertex[] {n, point3dToVertex.get(point3dToString(pointVertexP)), point3dToVertex.get(point3dToString(pointVertexQ)), point3dToVertex.get(point3dToString(pointVertexR)), point3dToVertex.get(point3dToString(pointVertexL)), ne});
				
	}
}