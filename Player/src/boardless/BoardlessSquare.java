package boardless;

import java.util.HashMap;
import java.util.HashSet;

import game.util.graph.Graph;
import main.math.Point3D;

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
	protected void calculateNewGraphElements(final Graph graph)
	{				
		// getting the 4 vertices around the cell on which the edge move was made
		HashSet<game.util.graph.Vertex> mainVertices = new HashSet<game.util.graph.Vertex>();
		game.util.graph.Vertex ne = verticesOfCellEdgeMove.get(2);
		game.util.graph.Vertex se = verticesOfCellEdgeMove.get(3);
		game.util.graph.Vertex sw = verticesOfCellEdgeMove.get(0);
		game.util.graph.Vertex nw = verticesOfCellEdgeMove.get(1);
		mainVertices.add(ne);
		mainVertices.add(se);
		mainVertices.add(sw);
		mainVertices.add(nw);

		// calculate the 12 vertices around, will help know which one exist already or not
		double verticalSide = nw.pt().y() - sw.pt().y();
		double horizontalSide = ne.pt().x() - nw.pt().x();
		
		Point3D pointVertexA = new Point3D(sw.pt().x() - horizontalSide, sw.pt().y() - verticalSide, 0);
		Point3D pointVertexB = new Point3D(sw.pt().x(), sw.pt().y() - verticalSide, 0);
		Point3D pointVertexC = new Point3D(se.pt().x(), se.pt().y() - verticalSide, 0);
		Point3D pointVertexD = new Point3D(se.pt().x() + horizontalSide, se.pt().y() - verticalSide, 0);
		Point3D pointVertexE = new Point3D(sw.pt().x() - horizontalSide, sw.pt().y(), 0);
		Point3D pointVertexF = new Point3D(se.pt().x() + horizontalSide, se.pt().y(), 0);
		Point3D pointVertexG = new Point3D(nw.pt().x() - horizontalSide, nw.pt().y(), 0);
		Point3D pointVertexH = new Point3D(ne.pt().x() + horizontalSide, ne.pt().y(), 0);
		Point3D pointVertexI = new Point3D(nw.pt().x() - horizontalSide, nw.pt().y() + verticalSide, 0);
		Point3D pointVertexJ = new Point3D(nw.pt().x(), nw.pt().y() + verticalSide, 0);
		Point3D pointVertexK = new Point3D(ne.pt().x(), ne.pt().y() + verticalSide, 0);
		Point3D pointVertexL = new Point3D(ne.pt().x() + horizontalSide, ne.pt().y() + verticalSide, 0);
		
		// calculation
		newPoint3dVertices = new Point3D[] {pointVertexA, pointVertexB, pointVertexC, pointVertexD, pointVertexE, pointVertexF, pointVertexG, pointVertexH, pointVertexI, pointVertexJ, pointVertexK, pointVertexL};
		
		point3dToVertex = new HashMap<String, game.util.graph.Vertex>();
		point3dToVertex.put(point3dToString(ne.pt()), ne);
		point3dToVertex.put(point3dToString(se.pt()), se);
		point3dToVertex.put(point3dToString(sw.pt()), sw);
		point3dToVertex.put(point3dToString(nw.pt()), nw);
		
		indexToVertex = new HashMap<Integer, game.util.graph.Vertex>();
		indexToVertex.put(ne.id(), ne);
		indexToVertex.put(se.id(), se);
		indexToVertex.put(sw.id(), sw);
		indexToVertex.put(nw.id(), nw);
		
		detectNewVertices(graph);
		
		// link each vertex to its neighbors, in order to create the edges
		vertexPointToNeighborsPoint = new HashMap<Point3D, Point3D[]>();
		vertexPointToNeighborsPoint.put(pointVertexA, new Point3D[] {pointVertexB, pointVertexE});
		vertexPointToNeighborsPoint.put(pointVertexB, new Point3D[] {pointVertexA, pointVertexC, sw.pt()});
		vertexPointToNeighborsPoint.put(pointVertexC, new Point3D[] {pointVertexB, pointVertexD, se.pt()});
		vertexPointToNeighborsPoint.put(pointVertexD, new Point3D[] {pointVertexC, pointVertexF});
		vertexPointToNeighborsPoint.put(pointVertexE, new Point3D[] {pointVertexA, sw.pt(), pointVertexG});
		vertexPointToNeighborsPoint.put(pointVertexF, new Point3D[] {pointVertexD, se.pt(), pointVertexH});
		vertexPointToNeighborsPoint.put(pointVertexG, new Point3D[] {pointVertexE, nw.pt(), pointVertexI});
		vertexPointToNeighborsPoint.put(pointVertexH, new Point3D[] {pointVertexF, ne.pt(), pointVertexL});
		vertexPointToNeighborsPoint.put(pointVertexI, new Point3D[] {pointVertexG, pointVertexJ});
		vertexPointToNeighborsPoint.put(pointVertexJ, new Point3D[] {pointVertexI, nw.pt(), pointVertexK});
		vertexPointToNeighborsPoint.put(pointVertexK, new Point3D[] {pointVertexJ, ne.pt(), pointVertexL});
		vertexPointToNeighborsPoint.put(pointVertexL, new Point3D[] {pointVertexK, pointVertexH});
		
		createVandE(graph);
		
		// calculate the 8 cells around, will help know which one exist already or not
		double diagonal = cellEdgeMove.pt().x() - sw.pt().x();
		
		Point3D pointCellA = new Point3D(cellEdgeMove.pt().x() - (2*diagonal), cellEdgeMove.pt().y() - (2*diagonal), 0);
		Point3D pointCellB = new Point3D(cellEdgeMove.pt().x(), cellEdgeMove.pt().y() - (2*diagonal), 0);
		Point3D pointCellC = new Point3D(cellEdgeMove.pt().x() + (2*diagonal), cellEdgeMove.pt().y() - (2*diagonal), 0);
		Point3D pointCellD = new Point3D(cellEdgeMove.pt().x() - (2*diagonal), cellEdgeMove.pt().y(), 0);
		Point3D pointCellE = new Point3D(cellEdgeMove.pt().x() + (2*diagonal), cellEdgeMove.pt().y(), 0);
		Point3D pointCellF = new Point3D(cellEdgeMove.pt().x() - (2*diagonal), cellEdgeMove.pt().y() + (2*diagonal), 0);
		Point3D pointCellG = new Point3D(cellEdgeMove.pt().x(), cellEdgeMove.pt().y() + (2*diagonal), 0);
		Point3D pointCellH = new Point3D(cellEdgeMove.pt().x() + (2*diagonal), cellEdgeMove.pt().y() + (2*diagonal), 0);
				
		// link each cell to its vertices
		cellPointToVerticesPoint = new HashMap<Point3D, game.util.graph.Vertex[]>();
		cellPointToVerticesPoint.put(pointCellA, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexA)), point3dToVertex.get(point3dToString(pointVertexE)), sw, point3dToVertex.get(point3dToString(pointVertexB))});
		cellPointToVerticesPoint.put(pointCellB, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexB)), sw, se, point3dToVertex.get(point3dToString(pointVertexC))});
		cellPointToVerticesPoint.put(pointCellC, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexC)), se, point3dToVertex.get(point3dToString(pointVertexF)), point3dToVertex.get(point3dToString(pointVertexD))});
		cellPointToVerticesPoint.put(pointCellD, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexE)), point3dToVertex.get(point3dToString(pointVertexG)), nw, sw});
		cellPointToVerticesPoint.put(pointCellE, new game.util.graph.Vertex[] {se, ne, point3dToVertex.get(point3dToString(pointVertexH)), point3dToVertex.get(point3dToString(pointVertexF))});
		cellPointToVerticesPoint.put(pointCellF, new game.util.graph.Vertex[] {point3dToVertex.get(point3dToString(pointVertexG)), point3dToVertex.get(point3dToString(pointVertexI)), point3dToVertex.get(point3dToString(pointVertexJ)), nw});
		cellPointToVerticesPoint.put(pointCellG, new game.util.graph.Vertex[] {nw, point3dToVertex.get(point3dToString(pointVertexJ)), point3dToVertex.get(point3dToString(pointVertexK)), ne});
		cellPointToVerticesPoint.put(pointCellH, new game.util.graph.Vertex[] {ne, point3dToVertex.get(point3dToString(pointVertexK)), point3dToVertex.get(point3dToString(pointVertexL)), point3dToVertex.get(point3dToString(pointVertexH))});
	}
}