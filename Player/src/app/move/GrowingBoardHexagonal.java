package app.move;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


import game.functions.graph.generators.basis.hex.Hex;
import game.types.board.SiteType;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import other.context.Context;
import other.topology.Cell;
import other.topology.TopologyElement;

/**
 * Functions for handling the board growing regarding boardless game.
 * 
 * @author Clémentine.Sacré
 */
public class GrowingBoardHexagonal
{	
	
	
	protected static Graph recalculetoutInitHexagonal(final Context context)
	{
		System.out.println("GrowingBoard.java recalculetoutInitHexagonal()");
		final Graph graph = new Graph();

		HashMap<String, Vertex> all = new HashMap<String, Vertex>();
		final DecimalFormat df = new DecimalFormat("#.###");
		int[][] initialCells = MappingBoardless.initialCells2();
		//for (int i=0; i<initialCells.length; i++)
		//	System.out.println("GrowingBoard.java recalculetoutInitHexagonal() initialCells2["+i+"] : "+Arrays.toString(initialCells[i]));
			
		HashSet<String> existingEdge = new HashSet<String>();
		int offsetRow = -1;
		for (int row = 0; row < initialCells.length; row++) 
		{
			int offsetCol = -1;
			for (int col = 0; col < initialCells[row].length; col++)
			{
				if (initialCells[row][col] == 1)
				{
					if (offsetCol == -1)
						offsetCol = col;
					if (offsetRow == -1)
						offsetRow = row;
					//System.out.println("GrowingBoard.java recalculetoutInitHexagonal() row : "+row+" - col : "+col);
					final Point2D ptRef = Hex.xy(row, col); //ajout offset
					
					int[] exist = new int[Hex.ref.length];
					Vertex[] vertices = new Vertex[Hex.ref.length];
					for (int n = 0; n < Hex.ref.length; n++)
					{
						
						double x = ptRef.getX() + Hex.ref[n][0];
						double y = ptRef.getY() + Hex.ref[n][1];
						
						String concatenated = df.format(x) + "-" + df.format(y);
						

						if (!all.containsKey(concatenated))
						{
							Vertex a = graph.addVertex(x, y);
							all.put(concatenated, a);
							vertices[n] = a;
						}
						else
						{
							exist[n] = 1;
							vertices[n] = all.get(concatenated);
						}
					}

					//edges
					int l = exist.length;
					for (int i=0; i<l; i++)
					{					
						int verticesA = vertices[i%l].id();
						int verticesB = vertices[(i+1)%l].id();
						String concatenated = verticesA + "-" + verticesB;
						String concatenated1 = verticesB + "-" + verticesA;
						if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
						{
							graph.addEdge(vertices[i%l], vertices[(i+1)%l]);
							existingEdge.add(concatenated);
						}
					}
					
				}
			}
			
		}
		
		

		graph.makeFaces(false);
		
		//graph.setBasisAndShape(basis, shape);
		graph.reorder();
		
		//System.out.println("GrowingBoard.java recalculetoutInitHexagonal() graph after : "+graph);


		return graph; 
	}

	protected static Graph recalculetoutRollBackHexagonal(final Context context)
	{
		System.out.println("GrowingBoard.java recalculetoutRollBackHexagonal()");
		final Graph graph = new Graph();	

		int offsetRow = MappingBoardless.lastNbRowAddedCells();
		
		
		int offsetCol = MappingBoardless.lastNbColAddedCells();
		System.out.println("GrowingBoard.java recalculetoutHexagonal() offsetCol : "+offsetCol);
		for (int i=0; i<MappingBoardless.cellsRowsColsAdded().size(); i++)
			System.out.println("GrowingBoard.java recalculetoutHexagonal() MappingBoardless.cellsRowsColsAdded().get(i) : "+Arrays.toString(MappingBoardless.cellsRowsColsAdded().get(i)));
		HashMap<String, Vertex> all = new HashMap<String, Vertex>();
		final DecimalFormat df = new DecimalFormat("#.###");
		
		HashSet<String> existingEdge = new HashSet<String>();
		
		System.out.println("GrowingBoard.java recalculetoutHexagonal() vertices : "+context.topology().vertices());
		int[] bas = new int[]{4, 3, 2};
		int[] haut = new int[]{5, 0, 1};
		
		HashMap<String, ArrayList<Vertex>> allo = new HashMap<String, ArrayList<Vertex>>();
		
		// premiere ligne
		ArrayList<Vertex> clem = new ArrayList<Vertex>();
		for (TopologyElement c : context.topology().rows().get(SiteType.Cell).get(0))
		{
			int col = c.col();
			int row = c.row();
			final Point2D ptRef = Hex.xy(row+offsetRow, col+offsetCol);
			
			int[] exist = new int[Hex.ref.length];
			Vertex[] vertices = new Vertex[Hex.ref.length];
			for (int n : bas)
			{
				
				double x = ptRef.getX() + Hex.ref[n][0];
				double y = ptRef.getY() + Hex.ref[n][1];
				
				String concatenated = df.format(x) + "-" + df.format(y);
				

				if (!all.containsKey(concatenated))
				{
					Vertex a = graph.addVertex(x, y);
					all.put(concatenated, a);
					vertices[n] = a;
					clem.add(vertices[n]);
					
					if (!allo.containsKey(df.format(x)))
						allo.put(df.format(x), new ArrayList<Vertex>());
					allo.get(df.format(x)).add(a);
				}
				else
				{
					exist[n] = 1;
					vertices[n] = all.get(concatenated);
				}
			}
		}
		
		// edges
		for (int i=0; i<clem.size()-1; i++)
		{
			String concatenated = clem.get(i).id() + "-" + clem.get(i+1).id();
			String concatenated1 = clem.get(i+1).id() + "-" + clem.get(i).id();
			if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
			{
				graph.addEdge(clem.get(i), clem.get(i+1));
				existingEdge.add(concatenated);
			}
		}
		
		
		//lignes centrales
		for (int r=0; r<context.topology().rows().get(SiteType.Cell).size()-1;r++)
		{
			clem = new ArrayList<Vertex>();
			if (context.topology().rows().get(SiteType.Cell).get(r).size() > context.topology().rows().get(SiteType.Cell).get(r+1).size())
			{
				for (TopologyElement c : context.topology().rows().get(SiteType.Cell).get(r))
				{
					int col = c.col();
					int row = c.row();
					final Point2D ptRef = Hex.xy(row+offsetRow, col+offsetCol);
					
					int[] exist = new int[Hex.ref.length];
					Vertex[] vertices = new Vertex[Hex.ref.length];
					for (int n : haut)
					{
						
						double x = ptRef.getX() + Hex.ref[n][0];
						double y = ptRef.getY() + Hex.ref[n][1];
						
						String concatenated = df.format(x) + "-" + df.format(y);
						

						if (!all.containsKey(concatenated))
						{
							Vertex a = graph.addVertex(x, y);
							all.put(concatenated, a);
							vertices[n] = a;
							clem.add(vertices[n]);
							
							if (!allo.containsKey(df.format(x)))
								allo.put(df.format(x), new ArrayList<Vertex>());
							allo.get(df.format(x)).add(a);
						}
						else
						{
							exist[n] = 1;
							vertices[n] = all.get(concatenated);
						}
					}
				}
			}
			else
			{
				for (TopologyElement c : context.topology().rows().get(SiteType.Cell).get(r+1))
				{
					int col = c.col();
					int row = c.row();
					final Point2D ptRef = Hex.xy(row+offsetRow, col+offsetCol);
					
					int[] exist = new int[Hex.ref.length];
					Vertex[] vertices = new Vertex[Hex.ref.length];
					for (int n : bas)
					{
						
						double x = ptRef.getX() + Hex.ref[n][0];
						double y = ptRef.getY() + Hex.ref[n][1];
						
						String concatenated = df.format(x) + "-" + df.format(y);
						

						if (!all.containsKey(concatenated))
						{
							Vertex a = graph.addVertex(x, y);
							all.put(concatenated, a);
							vertices[n] = a;
							clem.add(vertices[n]);
							
							if (!allo.containsKey(df.format(x)))
								allo.put(df.format(x), new ArrayList<Vertex>());
							allo.get(df.format(x)).add(a);
						}
						else
						{
							exist[n] = 1;
							vertices[n] = all.get(concatenated);
						}
					}
				}
			}
			
			//edges
			for (int i=0; i<clem.size()-1; i++)
			{
				String concatenated = clem.get(i).id() + "-" + clem.get(i+1).id();
				String concatenated1 = clem.get(i+1).id() + "-" + clem.get(i).id();
				if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
				{
					graph.addEdge(clem.get(i), clem.get(i+1));
					existingEdge.add(concatenated);
				}
			}
		}
		
		// ligne tout en haut
		clem = new ArrayList<Vertex>();
		for (TopologyElement c : context.topology().rows().get(SiteType.Cell).get(context.topology().rows().get(SiteType.Cell).size()-1))
		{
			int col = c.col();
			int row = c.row();
			final Point2D ptRef = Hex.xy(row+offsetRow, col+offsetCol);
			
			int[] exist = new int[Hex.ref.length];
			Vertex[] vertices = new Vertex[Hex.ref.length];
			for (int n : haut)
			{
				
				double x = ptRef.getX() + Hex.ref[n][0];
				double y = ptRef.getY() + Hex.ref[n][1];
				
				String concatenated = df.format(x) + "-" + df.format(y);
				

				if (!all.containsKey(concatenated))
				{
					Vertex a = graph.addVertex(x, y);
					all.put(concatenated, a);
					vertices[n] = a;
					clem.add(vertices[n]);
					
					if (!allo.containsKey(df.format(x)))
						allo.put(df.format(x), new ArrayList<Vertex>());
					allo.get(df.format(x)).add(a);
				}
				else
				{
					exist[n] = 1;
					vertices[n] = all.get(concatenated);
				}
			}
		}
		
		//edges
		for (int i=0; i<clem.size()-1; i++)
		{
			String concatenated = clem.get(i).id() + "-" + clem.get(i+1).id();
			String concatenated1 = clem.get(i+1).id() + "-" + clem.get(i).id();
			if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
			{
				graph.addEdge(clem.get(i), clem.get(i+1));
				existingEdge.add(concatenated);
			}
		}
		
		
		// vertical edges
		for (Cell c : context.topology().cells())
		{
			
			int row = c.row();
			int col = c.col();
			final Point2D ptRef = Hex.xy(row+offsetRow, col+offsetCol);
			
			double xtr = ptRef.getX() + Hex.ref[1][0];
			double ytr = ptRef.getY() + Hex.ref[1][1];
			double xdr = ptRef.getX() + Hex.ref[2][0];
			double ydr = ptRef.getY() + Hex.ref[2][1];
			
			double xtl = ptRef.getX() + Hex.ref[5][0];
			double ytl = ptRef.getY() + Hex.ref[5][1];
			double xdl = ptRef.getX() + Hex.ref[4][0];
			double ydl = ptRef.getY() + Hex.ref[4][1];
			
			final Vertex vtr = graph.findVertex(xtr, ytr, 0);
			final Vertex vdr = graph.findVertex(xdr, ydr, 0);
			final Vertex vtl = graph.findVertex(xtl, ytl, 0);
			final Vertex vdl = graph.findVertex(xdl, ydl, 0);
			
			String concatenated = vtr.id() + "-" + vdr.id();
			String concatenated1 = vdr.id() + "-" + vtr.id();
			if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
			{
				graph.addEdge(vtr, vdr);
				existingEdge.add(concatenated);
			}
			
			
			String concatenated2 = vtl.id() + "-" + vdl.id();
			String concatenated3 = vdl.id() + "-" + vtl.id();
			if (!existingEdge.contains(concatenated2) && !existingEdge.contains(concatenated3))
			{
				graph.addEdge(vtl, vdl);
				existingEdge.add(concatenated2);
			}
		}


		graph.makeFaces(false);
		graph.reorder();


		return graph; 
	}
	
	
	protected static void loop(int[] nbr, Graph graph, int row, int col, HashMap<String, Vertex> all, HashMap<String, ArrayList<Vertex>> allo, ArrayList<Vertex> clem)
	{
		final DecimalFormat df = new DecimalFormat("#.###");
		
		int[] exist = new int[Hex.ref.length];
		Vertex[] vertices = new Vertex[Hex.ref.length];
		Point2D ptRef = Hex.xy(row, col);
		
		for (int n : nbr)
		{
			
			double x = ptRef.getX() + Hex.ref[n][0];
			double y = ptRef.getY() + Hex.ref[n][1];
			
			String concatenated = df.format(x) + "-" + df.format(y);
			

			if (!all.containsKey(concatenated))
			{
				Vertex a = graph.addVertex(x, y);
				all.put(concatenated, a);
				vertices[n] = a;
				
				if (!allo.containsKey(df.format(x)))
					allo.put(df.format(x), new ArrayList<Vertex>());
				allo.get(df.format(x)).add(a);
			}
			else
			{
				exist[n] = 1;
				vertices[n] = all.get(concatenated);
			}
			if (clem.size() == 0 || (clem.size() > 0 && clem.get(clem.size()-1) != vertices[n]))
			{
				clem.add(vertices[n]);
				System.out.println("GrowingBoardHexagonal.java loop() clem add v : "+vertices[n]+" - with hex n : "+n+" - for cell row : "+row+" - col : "+col);
			}
		}
	}
	
	protected static void loopBas(Graph graph, int row, int col, HashMap<String, Vertex> all, HashMap<String, ArrayList<Vertex>> allo, ArrayList<Vertex> clem)
	{		
		int[] bas = new int[]{4, 3, 2};
		loop(bas, graph, row, col, all, allo, clem);
	}
	
	protected static void loopHaut(Graph graph, int row, int col, HashMap<String, Vertex> all, HashMap<String, ArrayList<Vertex>> allo, ArrayList<Vertex> clem)
	{		
		int[] haut = new int[]{5, 0, 1};
		loop(haut, graph, row, col, all, allo, clem);
	}
	
	protected static void linkingEdges(Graph graph, ArrayList<Vertex> clem, HashSet<String> existingEdge)
	{
		for (int m=0; m<clem.size()-1; m++)
		{
			String concatenated = clem.get(m).id() + "-" + clem.get(m+1).id();
			String concatenated1 = clem.get(m+1).id() + "-" + clem.get(m).id();
			if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
			{
				graph.addEdge(clem.get(m), clem.get(m+1));
				existingEdge.add(concatenated);
				System.out.println("GrowingBoardHexagonal.java linkingEdges() edge between v1 : "+clem.get(m).id()+" - v2 : "+clem.get(m+1).id());
			}
		}
	}
	
	protected static Graph recalculetoutHexagonal(final Context context)
	{
		System.out.println("GrowingBoard.java recalculetoutHexagonal()");
		final Graph graph = new Graph();	

		int offsetRow = MappingBoardless.lastNbRowAddedCells();
		int lastIndex = 0;
		
		
		int offsetCol = MappingBoardless.lastNbColAddedCells();
		HashMap<String, Vertex> all = new HashMap<String, Vertex>();
		
		HashSet<String> existingEdge = new HashSet<String>();
		
		for (int i=0; i<MappingBoardless.cellsRowsColsAdded().size(); i++)
			System.out.println("GrowingBoard.java recalculetoutHexagonal() MappingBoardless.cellsRowsColsAdded() : "+Arrays.toString(MappingBoardless.cellsRowsColsAdded().get(i)));
		
		System.out.println("GrowingBoard.java recalculetoutHexagonal() context.topology().rows() : "+context.topology().rows());
		System.out.println("GrowingBoard.java recalculetoutHexagonal() vertices : "+context.topology().vertices());

		HashMap<String, ArrayList<Vertex>> allo = new HashMap<String, ArrayList<Vertex>>();

		ArrayList<Vertex> clem = new ArrayList<Vertex>();
		
	
		// if row down
		clem = new ArrayList<Vertex>();
		while (lastIndex < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == -1)
		{
		
			int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
			int row =MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0];
			System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas1");
			loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
			lastIndex++;
		}

		linkingEdges(graph, clem, existingEdge);
		

		System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() lastIndex : "+lastIndex);
		if (lastIndex > 0)// TODO : pas juste regarder le premier élément à gauche ? pour les trous
		{
			clem = new ArrayList<Vertex>();
			
			int tmp = lastIndex;
			while (tmp < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(tmp)[1] <= MappingBoardless.cellsRowsColsAdded().get(0)[1])
			{
				int otherCol = MappingBoardless.cellsRowsColsAdded().get(tmp)[1];
				int otherRow = MappingBoardless.cellsRowsColsAdded().get(tmp)[0];
				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas-2");
				loopBas(graph, otherRow+offsetRow, otherCol+offsetCol, all, allo, clem);
				
				tmp++;
			}
			
			int curr = 0;
			System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() curr : "+curr);
			System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() context.topology().rows().get(SiteType.Cell).get(0).get(curr).col() : "+context.topology().rows().get(SiteType.Cell).get(0).get(curr).col()+" - MappingBoardless.cellsRowsColsAdded().get(0)[1] : "+ MappingBoardless.cellsRowsColsAdded().get(0)[1]);
			while (curr <= context.topology().rows().get(SiteType.Cell).get(0).size()-1 && context.topology().rows().get(SiteType.Cell).get(0).get(curr).col() <= MappingBoardless.cellsRowsColsAdded().get(0)[1])
			{
				int otherCol = context.topology().rows().get(SiteType.Cell).get(0).get(curr).col();
				int otherRow = context.topology().rows().get(SiteType.Cell).get(0).get(curr).row();
				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas-3");
				loopBas(graph, otherRow+offsetRow, otherCol+offsetCol, all, allo, clem);
				
				curr ++;
			}
		}
		
		lastIndex = 0;
		while (lastIndex < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == -1)
		{
		
			int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
			int row =MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0];

			System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopHaut3");
			loopHaut(graph, row+offsetRow, col+offsetCol, all, allo, clem);
			lastIndex++;
		}

		linkingEdges(graph, clem, existingEdge);
		
		
		int firstUpper = lastIndex;
		
		
		
		
		
		// NORMAL
		for (int i=0; i<context.topology().rows().get(SiteType.Cell).size(); i++)
		{
			System.out.println("\n\nGrowingBoardHexagonal.java recalculetoutHexagonal() NEW LOOP ROW : i : "+i);
			// BAS
			clem = new ArrayList<Vertex>();
			// bas - nouvelles cellules à gauche
			int bas = lastIndex;
			while (bas < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(bas)[0] == i && MappingBoardless.cellsRowsColsAdded().get(bas)[1] < context.topology().rows().get(SiteType.Cell).get(i).get(0).col())
			{
			
				int col = MappingBoardless.cellsRowsColsAdded().get(bas)[1];
				int row =MappingBoardless.cellsRowsColsAdded().get(bas)[0];

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas1");
				loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
				bas++;
			}
			
			
			
			// bas cellules existantes - centrales
			for (int j=0; j<context.topology().rows().get(SiteType.Cell).get(i).size(); j++)
			{
				
				other.topology.Cell c = (other.topology.Cell) context.topology().rows().get(SiteType.Cell).get(i).get(j);
				int col = c.col();
				int row = c.row();				

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas2");
				loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
					
			}
			
			// bas - cellules à droite
			while (bas < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(bas)[0] == i && MappingBoardless.cellsRowsColsAdded().get(bas)[1] > context.topology().rows().get(SiteType.Cell).get(i).get(0).col())
			{
			
				int col = MappingBoardless.cellsRowsColsAdded().get(bas)[1];
				int row =MappingBoardless.cellsRowsColsAdded().get(bas)[0];

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas3");
				loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
				bas++;
			}
			

			linkingEdges(graph, clem, existingEdge);
			
			
			// if there are new cell on the next row
			
			while (firstUpper < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(firstUpper)[0] == i)
			{
				firstUpper += 1;
			}
			
			

			System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() firstUpper : "+firstUpper);
			// HAUT
			clem = new ArrayList<Vertex>();
			// haut - nouvelles cellules à gauche
			int haut = lastIndex;
			if (i < context.topology().rows().get(SiteType.Cell).size()-1 && haut < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(haut)[0] == i && MappingBoardless.cellsRowsColsAdded().get(haut)[1] < context.topology().rows().get(SiteType.Cell).get(i+1).get(0).col())
			{
				// of new cells on the top left
				int tmpCurr = firstUpper;
				while (tmpCurr < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(tmpCurr)[0] == MappingBoardless.cellsRowsColsAdded().get(firstUpper)[0] && MappingBoardless.cellsRowsColsAdded().get(tmpCurr)[1] <= MappingBoardless.cellsRowsColsAdded().get(haut)[1])
				{
					int col = context.topology().rows().get(SiteType.Cell).get(i+1).get(tmpCurr).col();
					int row = context.topology().rows().get(SiteType.Cell).get(i+1).get(tmpCurr).row();

					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas40");
					loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
					tmpCurr++;
				}
				
				// if existing cells on the top left
				int curr = 0;
				while (curr < context.topology().rows().get(SiteType.Cell).get(i+1).size() && context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).col() <= MappingBoardless.cellsRowsColsAdded().get(haut)[1])
				{
					int col = context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).col();
					int row = context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).row();

					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas4");
					loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
					curr++;
				}
			}
			
			while (haut < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(haut)[0] == i && MappingBoardless.cellsRowsColsAdded().get(haut)[1] < context.topology().rows().get(SiteType.Cell).get(i).get(0).col())
			{
			
				int col = MappingBoardless.cellsRowsColsAdded().get(haut)[1];
				int row =MappingBoardless.cellsRowsColsAdded().get(haut)[0];

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas5");
				loopHaut(graph, row+offsetRow, col+offsetCol, all, allo, clem);
				haut++;
			}
			
			
			
			
			
			
			// haut cellules existantes - centrales
			if (i < context.topology().rows().get(SiteType.Cell).size()-1)
			{
				// of new cells on the top left
				int tmpCurr = firstUpper;
				while (tmpCurr < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(tmpCurr)[0] == MappingBoardless.cellsRowsColsAdded().get(firstUpper)[0] && MappingBoardless.cellsRowsColsAdded().get(tmpCurr)[1] <= context.topology().rows().get(SiteType.Cell).get(i).get(0).col())
				{
					int col = context.topology().rows().get(SiteType.Cell).get(i+1).get(tmpCurr).col();
					int row = context.topology().rows().get(SiteType.Cell).get(i+1).get(tmpCurr).row();

					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas60");
					loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
					tmpCurr++;
				}
				
				int curr = 0;
				while (curr < context.topology().rows().get(SiteType.Cell).get(i+1).size() && context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).col() <= context.topology().rows().get(SiteType.Cell).get(i).get(0).col())
				{
					int col = context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).col();
					int row = context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).row();

					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas6");
					loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
					curr++;
				}
			}	
			for (int j=0; j<context.topology().rows().get(SiteType.Cell).get(i).size(); j++)
			{
				
				other.topology.Cell c = (other.topology.Cell) context.topology().rows().get(SiteType.Cell).get(i).get(j);
				int col = c.col();
				int row = c.row();				

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas7");
				loopHaut(graph, row+offsetRow, col+offsetCol, all, allo, clem);
					
			}
			
			
			
			

			System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() haut : "+haut+" - i : "+i);
			// haut - cellules à droite
			/*if (i < context.topology().rows().get(SiteType.Cell).size()-1 && haut < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(haut)[0] == i && MappingBoardless.cellsRowsColsAdded().get(haut)[1] > context.topology().rows().get(SiteType.Cell).get(i+1).get(0).col())
			{
				
				// of new cells on the top left
				int tmpCurr = firstUpper;
				while (tmpCurr < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(tmpCurr)[0] == MappingBoardless.cellsRowsColsAdded().get(firstUpper)[0] && MappingBoardless.cellsRowsColsAdded().get(tmpCurr)[1] <= MappingBoardless.cellsRowsColsAdded().get(haut)[1])
				{
					int col = context.topology().rows().get(SiteType.Cell).get(i+1).get(tmpCurr).col();
					int row = context.topology().rows().get(SiteType.Cell).get(i+1).get(tmpCurr).row();

					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas80");
					loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
					tmpCurr++;
				}
				
				int curr = context.topology().rows().get(SiteType.Cell).get(i).get(context.topology().rows().get(SiteType.Cell).get(i).size()-1).col()+1;
				while (curr < context.topology().rows().get(SiteType.Cell).get(i+1).size() && context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).col() <= MappingBoardless.cellsRowsColsAdded().get(haut)[1])
				{
					int col = context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).col();
					int row = context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).row();

					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas8");
					loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
					curr++;
				}
			}*/

			System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas9 haut : "+haut);
			while (haut < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(haut)[0] == i && MappingBoardless.cellsRowsColsAdded().get(haut)[1] > context.topology().rows().get(SiteType.Cell).get(i).get(0).col())
			{
			
				int col = MappingBoardless.cellsRowsColsAdded().get(haut)[1];
				int row =MappingBoardless.cellsRowsColsAdded().get(haut)[0];

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas9");
				loopHaut(graph, row+offsetRow, col+offsetCol, all, allo, clem);
				haut++;
			}
			
			lastIndex = haut;

			linkingEdges(graph, clem, existingEdge);
		}
		
		
		
		/*for (int i=0; i<context.topology().rows().get(SiteType.Cell).size(); i++)
		{
			int tmp = lastIndex;
			// BAS
			// si nouvelles cell à gauche
			while (lastIndex < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == i && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1] < context.topology().rows().get(SiteType.Cell).get(i).get(0).col())
			{
			
				int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
				int row =MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0];

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas4");
				loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
				lastIndex++;
				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() incremented0 lastIndex : "+lastIndex);
			}
			
			// cell de base
			clem = new ArrayList<Vertex>();
			for (int j=0; j<context.topology().rows().get(SiteType.Cell).get(i).size(); j++)
			{
				
				other.topology.Cell c = (other.topology.Cell) context.topology().rows().get(SiteType.Cell).get(i).get(j);
				int col = c.col();
				int row = c.row();				

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas5");
				loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
					
			}
			
			// si nouvelles cell à droite
			while (lastIndex < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == i && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1] > context.topology().rows().get(SiteType.Cell).get(i).get(context.topology().rows().get(SiteType.Cell).get(i).size()-1).col())
			{
			
				int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
				int row = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0];

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas6");
				loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
				lastIndex++;
				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() incremented1 lastIndex : "+lastIndex);
			}
			//lastIndex = tmp;
			linkingEdges(graph, clem, existingEdge);
			

			//lastIndex = tmp;
			// HAUT - dépend de la longueur de la ligne pour savoir si on regarde vertex du haut des cells ou du bas des cells ( de row différentes)
			int alloooo = lastIndex < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == i ? (context.topology().rows().get(SiteType.Cell).get(i).get(0).col() > MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] ? MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] : context.topology().rows().get(SiteType.Cell).get(i).get(0).col()) : context.topology().rows().get(SiteType.Cell).get(i).get(0).col();
			clem = new ArrayList<Vertex>();
			if (i<context.topology().rows().get(SiteType.Cell).size()-1 && context.topology().rows().get(SiteType.Cell).get(i+1).get(0).col() <= alloooo)
			{
				
				int tmp2 = firstUpper;
				int coucou = tmp;
				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() before loopBas-7 tmp2 : "+tmp2+" - coucou :"+coucou+" - MappingBoardless.cellsRowsColsAdded().get(tmp2+1)[1] : "+MappingBoardless.cellsRowsColsAdded().get(tmp2)[1]+" - MappingBoardless.cellsRowsColsAdded().get(tmp2)[1] : "+MappingBoardless.cellsRowsColsAdded().get(coucou)[1]);
				for (int popi=0; popi<MappingBoardless.cellsRowsColsAdded().size(); popi++)
					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() before loopBas-7 MappingBoardless.cellsRowsColsAdded() : "+Arrays.toString(MappingBoardless.cellsRowsColsAdded().get(popi)));
				while (coucou < tmp2 && tmp2 < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(tmp2)[0] == i+1 & MappingBoardless.cellsRowsColsAdded().get(tmp2)[1] <= MappingBoardless.cellsRowsColsAdded().get(coucou)[1]) //TODO nul, si l'élément après est sur la mêmle ligne que la nouvelle cell, il faudra quand même voir sur la ligne du haut
				{
					int otherCol = MappingBoardless.cellsRowsColsAdded().get(tmp2)[1];
					int otherRow = MappingBoardless.cellsRowsColsAdded().get(tmp2)[0];
					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas-7");
					loopBas(graph, otherRow+offsetRow, otherCol+offsetCol, all, allo, clem);
					
					coucou++;
				}
				
				int tmp3 = tmp;
				//lastIndex = tmp2;
				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() lavant oopBas--7 tmp3 : "+tmp3+" - i : "+i+" - MappingBoardless.cellsRowsColsAdded().get(tmp3)[1] : "+MappingBoardless.cellsRowsColsAdded().get(tmp3)[1]+" - context.topology().rows().get(SiteType.Cell).get(i).get(0).col() : "+context.topology().rows().get(SiteType.Cell).get(i).get(0).col());
				while (tmp3 <= MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(tmp3)[0] == i+1 && MappingBoardless.cellsRowsColsAdded().get(tmp3)[1] <= context.topology().rows().get(SiteType.Cell).get(i).get(0).col())
				{
					int otherCol = MappingBoardless.cellsRowsColsAdded().get(tmp3)[1];
					int otherRow = MappingBoardless.cellsRowsColsAdded().get(tmp3)[0];

					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas--7");
					loopBas(graph, otherRow+offsetRow, otherCol+offsetCol, all, allo, clem);
					
					tmp3 ++;
				}
				//lastIndex = tmp3;
				
				
				int curr = 0;
				while (curr <= context.topology().rows().get(SiteType.Cell).get(i+1).size()-1 && context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).col() <= alloooo)
				{
					int otherCol = context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).col();
					int otherRow = context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).row();

					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopBas7");
					loopBas(graph, otherRow+offsetRow, otherCol+offsetCol, all, allo, clem);
					
					curr ++;
				}
				
			}
			
			//lastIndex = tmp;
			lastIndex = tmp;
			while (firstUpper < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(firstUpper)[0] == i+1)
			{
				firstUpper += 1;
			}
			System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() firstUpper : "+firstUpper);
			// si nouvelles cell à gauche
			
			// si nouvelle cellule à gauche et il n'y a aucune cellule en haut à gauche
			//while (( firstUpper >= MappingBoardless.cellsRowsColsAdded().size() || (firstUpper < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1] >= MappingBoardless.cellsRowsColsAdded().get(firstUpper)[1])) && lastIndex < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == i && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1] < context.topology().rows().get(SiteType.Cell).get(i).get(context.topology().rows().get(SiteType.Cell).get(i).size()-1).col())
			{
			
				int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
				int row = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0];

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopHaut-10");
				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() 4incremented lastIndex : "+lastIndex);
				loopHaut(graph, row+offsetRow, col+offsetCol, all, allo, clem);
				lastIndex++;
			}
			

			System.out.println("CLEM1 : lastIndex : "+lastIndex);
			if (lastIndex < MappingBoardless.cellsRowsColsAdded().size())
			{
				System.out.println("CLEM2 : MappingBoardless.cellsRowsColsAdded().get(lastIndex) : "+Arrays.toString(MappingBoardless.cellsRowsColsAdded().get(lastIndex))+" - i : "+i);
			}
			
			if (lastIndex < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == i)
			{
				int tmpX = firstUpper;
				int curr = 0;
				System.out.println("CLEM3 : lastIndex : "+lastIndex+" -  MappingBoardless.cellsRowsColsAdded().get(lastIndex) : "+Arrays.toString( MappingBoardless.cellsRowsColsAdded().get(lastIndex))+" - i : "+i+" - context.topology().rows().get(SiteType.Cell) : "+context.topology().rows().get(SiteType.Cell));
				
				if (tmpX < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1] >= MappingBoardless.cellsRowsColsAdded().get(tmpX)[1])
				{
					//bas
					int col = MappingBoardless.cellsRowsColsAdded().get(tmpX)[1];
					int row = MappingBoardless.cellsRowsColsAdded().get(tmpX)[0];

					System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopHaut23");
					loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
					tmpX += 1;
				}
	
				else if (i+1 < context.topology().rows().get(SiteType.Cell).size() && curr < context.topology().rows().get(SiteType.Cell).get(i+1).size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1] < context.topology().rows().get(SiteType.Cell).get(i+1).get(curr).col())
				{
					//bas
					
				}
				
				else
				{
					while(lastIndex < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == i) 
					{
						int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
						int row = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0];

						System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopHaut22");
						loopHaut(graph, row+offsetRow, col+offsetCol, all, allo, clem);
						lastIndex++;
						System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() 3incremented lastIndex : "+lastIndex);
					}
				}
			}
			
			// centre
			for (int j=0; j<context.topology().rows().get(SiteType.Cell).get(i).size(); j++)
			{
				other.topology.Cell c = (other.topology.Cell) context.topology().rows().get(SiteType.Cell).get(i).get(j);
				int col = c.col();
				int row = c.row();

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopHaut9");
				loopHaut(graph, row+offsetRow, col+offsetCol, all, allo, clem);
			}
			
			// si nouvelles cell à droite
			while (lastIndex < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == i && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1] > context.topology().rows().get(SiteType.Cell).get(i).get(context.topology().rows().get(SiteType.Cell).get(i).size()-1).col())
			{
			
				int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
				int row = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0];

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopHaut10");
				loopHaut(graph, row+offsetRow, col+offsetCol, all, allo, clem);
				lastIndex++;
				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() 3incremented lastIndex : "+lastIndex);
			}
			linkingEdges(graph, clem, existingEdge);
			
			while (lastIndex < MappingBoardless.cellsRowsColsAdded().size() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == i)
			{
				lastIndex += 1;
			}
		}	*/
		
		// if cell in a new upper row
		// bas
		clem = new ArrayList<Vertex>();
		if (lastIndex < MappingBoardless.cellsRowsColsAdded().size())
		{
			int lastRow = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0];
			int tmp = lastIndex;
			// bas
			for (int i=lastIndex; i<MappingBoardless.cellsRowsColsAdded().size(); i++)
			{
				if (lastRow != MappingBoardless.cellsRowsColsAdded().get(i)[0])
				{
					linkingEdges(graph, clem, existingEdge);
					
					// haut
					for (int j=tmp; j<i; j++)
					{
						int col = MappingBoardless.cellsRowsColsAdded().get(j)[1];
						int row = MappingBoardless.cellsRowsColsAdded().get(j)[0];

						System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopHaut11");
						loopHaut(graph, row+offsetRow, col+offsetCol, all, allo, clem);
						
					}
					tmp = i;
					linkingEdges(graph, clem, existingEdge);
					clem = new ArrayList<Vertex>();
				}
				
				int col = MappingBoardless.cellsRowsColsAdded().get(i)[1];
				int row = MappingBoardless.cellsRowsColsAdded().get(i)[0];

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopHaut12");
				loopBas(graph, row+offsetRow, col+offsetCol, all, allo, clem);
				
				
			}
			// haut

			clem = new ArrayList<Vertex>();
			for (int j=tmp; j<MappingBoardless.cellsRowsColsAdded().size(); j++)
			{
				int col = MappingBoardless.cellsRowsColsAdded().get(j)[1];
				int row = MappingBoardless.cellsRowsColsAdded().get(j)[0];

				System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopHaut13");
				loopHaut(graph, row+offsetRow, col+offsetCol, all, allo, clem);
			}

			System.out.println("GrowingBoardHexagonal.java recalculetoutHexagonal() loopHaut14");
			linkingEdges(graph, clem, existingEdge);
		}
		
		
		// cells added to the top TODO
			
		

		

		System.out.println("GrowingBoard.java recalculetoutHexagonal() linkingEdges5");
		// vertical edges
		lastIndex = 0;
		for (Cell c : context.topology().cells())
		{
			if (lastIndex < MappingBoardless.cellsRowsColsAdded().size())
				System.out.println("GrowingBoard.java recalculetoutHexagonal() cellsRowsColsAdded : "+Arrays.toString(MappingBoardless.cellsRowsColsAdded().get(lastIndex))+" - row : "+c.row()+" - col : "+c.col());
			while (lastIndex < MappingBoardless.cellsRowsColsAdded().size() && ((MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == c.row() && MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1] < c.col()) || (MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] < c.row())))
			{
				int row = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0];
				int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
				final Point2D ptRef = Hex.xy(row+offsetRow, col+offsetCol);
				
				double xtr = ptRef.getX() + Hex.ref[1][0];
				double ytr = ptRef.getY() + Hex.ref[1][1];
				double xdr = ptRef.getX() + Hex.ref[2][0];
				double ydr = ptRef.getY() + Hex.ref[2][1];
				
				double xtl = ptRef.getX() + Hex.ref[5][0];
				double ytl = ptRef.getY() + Hex.ref[5][1];
				double xdl = ptRef.getX() + Hex.ref[4][0];
				double ydl = ptRef.getY() + Hex.ref[4][1];
				
				final Vertex vtr = graph.findVertex(xtr, ytr, 0);
				final Vertex vdr = graph.findVertex(xdr, ydr, 0);
				final Vertex vtl = graph.findVertex(xtl, ytl, 0);
				final Vertex vdl = graph.findVertex(xdl, ydl, 0);

				
				
				String concatenated2 = vtl.id() + "-" + vdl.id();
				String concatenated3 = vdl.id() + "-" + vtl.id();
				if (!existingEdge.contains(concatenated2) && !existingEdge.contains(concatenated3))
				{
					graph.addEdge(vdl, vtl);
					existingEdge.add(concatenated2);
					System.out.println("GrowingBoardHexagonal.java linkingEdges()B edge between v1 : "+vtl.id()+" - v2 : "+vdl.id());
				}
				
				
				String concatenated = vtr.id() + "-" + vdr.id();
				String concatenated1 = vdr.id() + "-" + vtr.id();
				if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
				{
					graph.addEdge(vdr, vtr);
					existingEdge.add(concatenated);
					System.out.println("GrowingBoardHexagonal.java linkingEdges()A edge between v1 : "+vtr.id()+" - v2 : "+vdr.id());
				}
				
				lastIndex ++;
			}
			
			int row = c.row();
			int col = c.col();
			System.out.println("GrowingBoardHexagonal.java linkingEdges() col : "+col+" - row : "+row);
			final Point2D ptRef = Hex.xy(row+offsetRow, col+offsetCol);
			
			double xtr = ptRef.getX() + Hex.ref[1][0];
			double ytr = ptRef.getY() + Hex.ref[1][1];
			double xdr = ptRef.getX() + Hex.ref[2][0];
			double ydr = ptRef.getY() + Hex.ref[2][1];
			
			double xtl = ptRef.getX() + Hex.ref[5][0];
			double ytl = ptRef.getY() + Hex.ref[5][1];
			double xdl = ptRef.getX() + Hex.ref[4][0];
			double ydl = ptRef.getY() + Hex.ref[4][1];
			
			final Vertex vtr = graph.findVertex(xtr, ytr, 0);
			final Vertex vdr = graph.findVertex(xdr, ydr, 0);
			final Vertex vtl = graph.findVertex(xtl, ytl, 0);
			final Vertex vdl = graph.findVertex(xdl, ydl, 0);
			

			System.out.println("GrowingBoardHexagonal.java linkingEdges()D edge between v1 xtl : "+xtl+" -  ytl: "+ytl+" vtr : "+vtl);
			System.out.println("GrowingBoardHexagonal.java linkingEdges()D edge between v1 xdl : "+xdl+" -  ytl: "+ydl+" vdl : "+vdl);
			
			

			
			String concatenated2 = vtl.id() + "-" + vdl.id();
			String concatenated3 = vdl.id() + "-" + vtl.id();
			if (!existingEdge.contains(concatenated2) && !existingEdge.contains(concatenated3))
			{
				graph.addEdge(vdl, vtl);
				existingEdge.add(concatenated2);
				System.out.println("GrowingBoardHexagonal.java linkingEdges()D edge between v1 : "+vtl.id()+" - v2 : "+vdl.id());
			}
			String concatenated = vtr.id() + "-" + vdr.id();
			String concatenated1 = vdr.id() + "-" + vtr.id();
			if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
			{
				graph.addEdge(vdr, vtr);
				existingEdge.add(concatenated);
				System.out.println("GrowingBoardHexagonal.java linkingEdges()C edge between v1 : "+vtr.id()+" - v2 : "+vdr.id());
			}
		}
		
		// new cell with id bigger
		while (lastIndex < MappingBoardless.cellsRowsColsAdded().size())
		{
			int row = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0];
			int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
			final Point2D ptRef = Hex.xy(row+offsetRow, col+offsetCol);
			
			double xtr = ptRef.getX() + Hex.ref[1][0];
			double ytr = ptRef.getY() + Hex.ref[1][1];
			double xdr = ptRef.getX() + Hex.ref[2][0];
			double ydr = ptRef.getY() + Hex.ref[2][1];
			
			double xtl = ptRef.getX() + Hex.ref[5][0];
			double ytl = ptRef.getY() + Hex.ref[5][1];
			double xdl = ptRef.getX() + Hex.ref[4][0];
			double ydl = ptRef.getY() + Hex.ref[4][1];
			
			final Vertex vtr = graph.findVertex(xtr, ytr, 0);
			final Vertex vdr = graph.findVertex(xdr, ydr, 0);
			final Vertex vtl = graph.findVertex(xtl, ytl, 0);
			final Vertex vdl = graph.findVertex(xdl, ydl, 0);

			
			
			String concatenated2 = vtl.id() + "-" + vdl.id();
			String concatenated3 = vdl.id() + "-" + vtl.id();
			if (!existingEdge.contains(concatenated2) && !existingEdge.contains(concatenated3))
			{
				graph.addEdge(vdl, vtl);
				existingEdge.add(concatenated2);
				System.out.println("GrowingBoardHexagonal.java linkingEdges()B edge between v1 : "+vtl.id()+" - v2 : "+vdl.id());
			}
			
			
			String concatenated = vtr.id() + "-" + vdr.id();
			String concatenated1 = vdr.id() + "-" + vtr.id();
			if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
			{
				graph.addEdge(vdr, vtr);
				existingEdge.add(concatenated);
				System.out.println("GrowingBoardHexagonal.java linkingEdges()A edge between v1 : "+vtr.id()+" - v2 : "+vdr.id());
			}
			
			lastIndex ++;
		}

		
		System.out.println("GrowingBoard.java recalculetoutHexagonal() before graph.vertices() : "+graph.vertices());
		System.out.println("GrowingBoard.java recalculetoutHexagonal() before graph.edges() : "+graph.edges());
		System.out.println("GrowingBoard.java recalculetoutHexagonal() before graph.faces() : "+graph.faces());
		System.out.println("GrowingBoard.java recalculetoutHexagonal() before graph : "+graph);
		
		graph.makeFaces(false);
		graph.reorder();

		System.out.println("GrowingBoard.java recalculetoutHexagonal() after graph.vertices() : "+graph.vertices());
		System.out.println("GrowingBoard.java recalculetoutHexagonal() after graph.edges() : "+graph.edges());
		System.out.println("GrowingBoard.java recalculetoutHexagonal() after graph.faces() : "+graph.faces());
		System.out.println("GrowingBoard.java recalculetoutHexagonal() after graph : "+graph);

		
		return graph; 
	}
	
	/*protected static Graph recalculetoutHexagonal2(final Context context)
	{
		System.out.println("GrowingBoard.java recalculetoutHexagonal()");
		final Graph graph = new Graph();	

		int offsetRow = MappingBoardless.lastNbRowAddedCells();
		int lastIndex = 0;
		
		
		int offsetCol = MappingBoardless.lastNbColAddedCells();
		System.out.println("GrowingBoard.java recalculetoutHexagonal() offsetCol : "+offsetCol);
		for (int i=0; i<MappingBoardless.cellsRowsColsAdded().size(); i++)
			System.out.println("GrowingBoard.java recalculetoutHexagonal() MappingBoardless.cellsRowsColsAdded().get(i) : "+Arrays.toString(MappingBoardless.cellsRowsColsAdded().get(i)));
		HashMap<String, Vertex> all = new HashMap<String, Vertex>();
		final DecimalFormat df = new DecimalFormat("#.###");
		
		HashSet<String> existingEdge = new HashSet<String>();
		
		if (offsetRow > 0)
		{
			while (lastIndex < MappingBoardless.cellsRowsColsAdded().size())
			{
				if (MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == -1)
				{
					int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
					final Point2D ptRef = Hex.xy(0, col+offsetCol);
					System.out.println("GrowingBoard.java recalculetoutHexagonal()1 row : "+(0)+" - col : " +(col+offsetCol));
					
					int[] exist = new int[Hex.ref.length];
					Vertex[] vertices = new Vertex[Hex.ref.length];
					for (int n = 0; n < Hex.ref.length; n++)
					{
						
						double x = ptRef.getX() + Hex.ref[n][0];
						double y = ptRef.getY() + Hex.ref[n][1];
						
						String concatenated = df.format(x) + "-" + df.format(y);
						

						if (!all.containsKey(concatenated))
						{
							Vertex a = graph.addVertex(x, y);
							all.put(concatenated, a);
							vertices[n] = a;
						}
						else
						{
							exist[n] = 1;
							vertices[n] = all.get(concatenated);
						}
					}

					//edges
					int l = exist.length;
					for (int i=0; i<l; i++)
					{	
						int verticesA = vertices[i%l].id();
						int verticesB = vertices[(i+1)%l].id();
						String concatenated = verticesA + "-" + verticesB;
						String concatenated1 = verticesB + "-" + verticesA;
						if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
						{
							graph.addEdge(vertices[i%l], vertices[(i+1)%l]);
							existingEdge.add(concatenated);
						}
					}
					
					
					lastIndex ++;
				}
				else
				{
					break;
				}
			}
		}

		System.out.println("GrowingBoard.java recalculetoutHexagonal() context.topology().columnsCc() : "+context.topology().columnsCc());
		System.out.println("GrowingBoard.java recalculetoutHexagonal() context.topology().rowsCc() : "+context.topology().rowsCc());
		for (int row=0; row<context.topology().rowsCc().get(SiteType.Cell).size();row++)
		{
			HashSet<Integer> cols = context.topology().rowsCc().get(SiteType.Cell).get(row);
			
			for (Integer col : cols) {
				final Point2D ptRef = Hex.xy(row+offsetRow, col+offsetCol); //ajout offset
				
				int[] exist = new int[Hex.ref.length];
				Vertex[] vertices = new Vertex[Hex.ref.length];
				for (int n = 0; n < Hex.ref.length; n++)
				{

					System.out.println("GrowingBoard.java recalculetoutHexagonal()2 row : "+(row+offsetRow)+" - col : " +(col+offsetCol));
					double x = ptRef.getX() + Hex.ref[n][0];
					double y = ptRef.getY() + Hex.ref[n][1];
					
					String concatenated = df.format(x) + "-" + df.format(y);
					

					if (!all.containsKey(concatenated))
					{
						Vertex a = graph.addVertex(x, y);
						all.put(concatenated, a);
						vertices[n] = a;
					}
					else
					{
						exist[n] = 1;
						vertices[n] = all.get(concatenated);
					}
				}

				//edges
				int l = exist.length;
				for (int i=0; i<l; i++)
				{					
					int verticesA = vertices[i%l].id();
					int verticesB = vertices[(i+1)%l].id();
					String concatenated = verticesA + "-" + verticesB;
					String concatenated1 = verticesB + "-" + verticesA;
					if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
					{
						graph.addEdge(vertices[i%l], vertices[(i+1)%l]);
						existingEdge.add(concatenated);
					}
				}
				
			}
			
			// check for new cols
			while (lastIndex < MappingBoardless.cellsRowsColsAdded().size())
			{
				if (MappingBoardless.cellsRowsColsAdded().get(lastIndex)[0] == row)
				{
					int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
					final Point2D ptRef = Hex.xy(row+offsetRow, col+offsetCol);
					System.out.println("GrowingBoard.java recalculetoutHexagonal()3 row : "+(row+offsetRow)+" - col : " +(col+offsetCol));
					
					int[] exist = new int[Hex.ref.length];
					Vertex[] vertices = new Vertex[Hex.ref.length];
					for (int n = 0; n < Hex.ref.length; n++)
					{
						
						double x = ptRef.getX() + Hex.ref[n][0];
						double y = ptRef.getY() + Hex.ref[n][1];
						
						String concatenated = df.format(x) + "-" + df.format(y);
						

						if (!all.containsKey(concatenated))
						{
							Vertex a = graph.addVertex(x, y);
							all.put(concatenated, a);
							vertices[n] = a;
						}
						else
						{
							exist[n] = 1;
							vertices[n] = all.get(concatenated);
						}
					}
					
					//edges
					int l = exist.length;
					for (int i=0; i<l; i++)
					{					
						int verticesA = vertices[i%l].id();
						int verticesB = vertices[(i+1)%l].id();
						String concatenated = verticesA + "-" + verticesB;
						String concatenated1 = verticesB + "-" + verticesA;
						if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
						{
							graph.addEdge(vertices[i%l], vertices[(i+1)%l]);
							existingEdge.add(concatenated);
						}
					}
					
					
					lastIndex ++;
				}
				else
				{
					break;
				}
			}
		}
		
		// still some row to add up
		if (lastIndex < MappingBoardless.cellsRowsColsAdded().size()-1)
		{
			int lastRow = context.topology().rowsCc().get(SiteType.Cell).size();
			while (lastIndex < MappingBoardless.cellsRowsColsAdded().size())
			{
				
				int col = MappingBoardless.cellsRowsColsAdded().get(lastIndex)[1];
				final Point2D ptRef = Hex.xy(lastRow+offsetRow, col+offsetCol);
				System.out.println("GrowingBoard.java recalculetoutHexagonal()4 row : "+(lastRow+offsetRow)+" - col : " +(col+offsetCol));
				
				int[] exist = new int[Hex.ref.length];
				Vertex[] vertices = new Vertex[Hex.ref.length];
				for (int n = 0; n < Hex.ref.length; n++)
				{
					
					double x = ptRef.getX() + Hex.ref[n][0];
					double y = ptRef.getY() + Hex.ref[n][1];
					
					String concatenated = df.format(x) + "-" + df.format(y);
					

					if (!all.containsKey(concatenated))
					{
						Vertex a = graph.addVertex(x, y);
						all.put(concatenated, a);
						vertices[n] = a;
					}
					else
					{
						exist[n] = 1;
						vertices[n] = all.get(concatenated);
					}
				}

				//edges
				int l = exist.length;
				for (int i=0; i<l; i++)
				{					
					int verticesA = vertices[i%l].id();
					int verticesB = vertices[(i+1)%l].id();
					String concatenated = verticesA + "-" + verticesB;
					String concatenated1 = verticesB + "-" + verticesA;
					if (!existingEdge.contains(concatenated) && !existingEdge.contains(concatenated1))
					{
						graph.addEdge(vertices[i%l], vertices[(i+1)%l]);
						existingEdge.add(concatenated);
					}
				}
				
				
				lastIndex ++;
			}
		}

		graph.makeFaces(false);
		graph.reorder();
		
		System.out.println("GrowingBoard.java recalculetoutHexagonal() after graph : "+graph);

		
		return graph; 
	}*/
	
}
