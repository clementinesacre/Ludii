package boardless;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import game.equipment.container.board.Boardless;
import game.types.board.SiteType;
import game.types.board.TilingBoardlessType;
import game.util.graph.Graph;
import main.math.Point3D;
import other.context.Context;
import other.move.Move;
import other.topology.Cell;
import other.topology.Vertex;

/**
 * Methods to create the mapping that will tell the board how to grow.
 * 
 * @author Clémentine.Sacré
 */
public abstract class BoardlessAbstract
{
	protected HashMap<Integer, Integer> mappedPrevToNewIndexes;
	protected HashMap<Integer, Integer> mappedNewToPrevIndexes;
	protected HashSet<Integer> surplusIndexes; // cells indexes that are being added from one board to another

	protected HashMap<Integer, Integer> mappedInitToNewIndexes = new HashMap<Integer, Integer>();
	protected HashMap<Integer, Integer> mappedNewToInitIndexes = new HashMap<Integer, Integer>();
	protected HashSet<Integer> surplusInitIndexes = new HashSet<Integer>(); // cells indexes that are being added from init board to new one
	
	protected List<HashMap<Integer, Integer>> listCopieMappedInitToNewIndexes = new ArrayList<HashMap<Integer, Integer>>();
	protected List<HashMap<Integer, Integer>> listCopieMappedNewToInitIndexes = new ArrayList<HashMap<Integer, Integer>>();
	protected List<HashSet<Integer>> listCopieSurplusInitIndexes = new ArrayList<HashSet<Integer>>();
	protected int currentCopy = -1;
	
	//----------------------------Cells-----------------------------------------
	// remember the cells added at each step to be able to undo them easily when going back

	protected int nbAddedCellsFromStart = 0; // total number  of row/col (=cell) added since the beginning
	protected int[][] initialHands; // id initiaux des éléments qui ne sont pas sur le plateau (les mains des joueurs et autres)
	protected int[][] initialCells; // matrice du plateau initial pour suivre combien de cellules ont été rajoutées à chaque edge move pour chaque cellule bien précis
	protected int[][] initialCells2;
	protected int[][] initialVertices; // 0 if no vertex at this position on initial board, 1 else

	protected ArrayList<ArrayList<int[]>> cellsRowsColsAddedFromStart = new ArrayList<ArrayList<int[]>>(); // liste de listes de row,col relatives au plateau courant ajoutées pour un edge move
	protected ArrayList<int[]> lastCellsRowsColsAddedFromStart = new ArrayList<int[]>();
	protected ArrayList<int[]> cellsRowsColsAdded; // liste de row,col relatives au plateau courant ajoutées pour un edge move rowsColsAdded
	protected ArrayList<Integer> cellsAddedLeftCols = new ArrayList<Integer>(); // pour chaque edge move combien de colonne ont été crée en négatif
	protected ArrayList<Integer> cellsAddedDownRows = new ArrayList<Integer>(); // pour chaque edge move combien de lignes ont été crée en négatif
	protected ArrayList<Integer> cellsAddedRightCols = new ArrayList<Integer>(); // pour chaque edge move combien de colonne ont été crée en négatif
	protected ArrayList<Integer> cellsAddedUpRows = new ArrayList<Integer>();
	
	//----------------------------Vertices--------------------------------------
	// remember the vertices added at each step to be able to undo them easily when going back
	
	protected ArrayList<Integer> rowsNeighborsVertices; // liste de row en position relatif qui ont été ajoutés. Sont dans l'ordre croissant
	protected HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToColsVertices; // clé row ajoutées, valeur liste de col ajoutées pour cette row. Sont dans l'ordre croissant
	
	protected HashSet<Integer>[] nbAddedColPerRow; // for each row what column where added. 2 elem en plus, un pour une nouvelle row en bas, et un pour une row en haut // = list de row de liste de col
	
	public boolean vertexAddedLeftCol; // pour le dernier edge move, est ce qu'une colonne en vertex a été créée en négatif ? (pourrait utiliser celui des cells, ça va avec)

	
	public ArrayList<Boolean> vertexAddedLeftColFromStart = new ArrayList<Boolean>();
	
	protected ArrayList<HashSet<Integer>[]> nbAddedColPerRowFromStart = new ArrayList<HashSet<Integer>[]>();
	
	
	protected HashSet<Integer>[] lastNbAddedColPerRow;
	protected boolean lastVertexAddedLeftCol;
	
	protected int newTotalIndexesCells;
	
	
	// ----------------------------New technique--------------------------------------
	protected List<List<Integer>> addedFacesSinceBeginning;
	
	
	//--------------------------------Getters----------------------------------
	
	public HashMap<Integer, Integer> mappedPrevToNewIndexes()
	{
		return mappedPrevToNewIndexes;
	}
	
	public HashMap<Integer, Integer> mappedNewToPrevIndexes()
	{
		return mappedNewToPrevIndexes;
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
	
	protected ArrayList<Integer> rowsNeighborsVertices()
	{
		return rowsNeighborsVertices;
	}
	
	public HashMap<Integer, ArrayList<Integer>> rowsNeighborsMapToColsVertices()
	{
		return rowsNeighborsMapToColsVertices;
	}
	
	public HashSet<Integer>[] nbAddedColPerRow()
	{
		return nbAddedColPerRow;
	}
	
	public int[][] initialHands()
	{
		return initialHands;
	}
	
	public ArrayList<Integer> cellsAddedLeftCols()
	{
		return cellsAddedLeftCols;
	}
	
	public ArrayList<Integer> cellsAddedDownRows()
	{
		return cellsAddedDownRows;
	}
	
	public ArrayList<Integer> cellsAddedRightCols()
	{
		return cellsAddedRightCols;
	}
	
	public ArrayList<Integer> cellsAddedUpRows()
	{
		return cellsAddedUpRows;
	}
	
	public ArrayList<int[]> cellsRowsColsAdded()
	{
		return cellsRowsColsAdded;
	}
	
	public ArrayList<ArrayList<int[]>> cellsRowsColsAddedFromStart()
	{
		return cellsRowsColsAddedFromStart;
	}
	
	public ArrayList<int[]> lastCellsRowsColsAddedFromStart()
	{
		return lastCellsRowsColsAddedFromStart;
	}
	
	public int[][] initialCells()
	{
		return initialCells;
	}
	
	public int[][] initialCells2()
	{
		return initialCells2;
	}
	
	public int nbAddedCellsFromStart()
	{
		return nbAddedCellsFromStart;
	}
	
	public boolean vertexAddedLeftCol()
	{
		return vertexAddedLeftCol;
	}
	
	public ArrayList<Boolean> vertexAddedLeftColFromStart()
	{
		return vertexAddedLeftColFromStart;
	}
	
	public ArrayList<HashSet<Integer>[]> nbAddedColPerRowFromStart()
	{
		return nbAddedColPerRowFromStart;
	}
	
	public boolean lastVertexAddedLeftCol()
	{
		return lastVertexAddedLeftCol;
	}
	
	public HashSet<Integer>[] lastNbAddedColPerRow()
	{
		return lastNbAddedColPerRow;
	}
	
	public int newTotalIndexesCells()
	{
		return newTotalIndexesCells;
	}
	
	public int[][] initialVertices()
	{
		return initialVertices;
	}
	
	public List<List<Integer>> addedFacesSinceBeginning()
	{
		return addedFacesSinceBeginning;
	}

	
	//-------------------------------------------------------------------------
	
	public void init(Context context)
	{
		initialHands = new int[context.containers().length-1][];
		for (int i=1; i<context.containers().length; i++)
		{
			int[] initialHand = new int[context.containers()[i].topology().cells().size()];
			for (int j=0; j<context.containers()[i].topology().cells().size(); j++)
			{
				Cell c = context.containers()[i].topology().cells().get(j);
				int prevIndex = c.index();
				initialHand[j] = prevIndex;
				
			}
			initialHands()[i-1] = initialHand;
		}
		
		initialCells = new int[context.topology().rows().get(SiteType.Cell).size()][];
		for (int i=0; i<context.topology().rows().get(SiteType.Cell).size(); i++)
		{
			int nbCol = context.topology().rows().get(SiteType.Cell).get(i).size();
			initialCells[i] = new int[nbCol];
		}
		
		initialCells = new int[context.topology().rows().get(SiteType.Cell).size()][context.topology().columns().get(SiteType.Cell).size()];
		for (Cell c : context.topology().cells())
		{
			
			initialCells[c.row()][c.col()] = 1;
		}
		for (int i=0; i<initialCells.length; i++)
		{
			for (int j=0; j<initialCells[i].length; j++)
			{
				if (initialCells[i][j] == 1)
					initialCells[i][j] = 0;
				else
					initialCells[i][j] = -1;
			}
		}
		
		initialVertices = new int[context.topology().rows().get(SiteType.Vertex).size()][context.topology().columns().get(SiteType.Vertex).size()];
		for (Vertex v : context.topology().vertices())
		{
			initialVertices[v.row()][v.col()] = 1;
		}
		
		initialCells2 = new int[context.topology().rows().get(SiteType.Cell).size()][context.topology().columns().get(SiteType.Cell).size()];
		for (Cell c : context.topology().cells())
		{
			initialCells2[c.row()][c.col()] = 1;
		}
		
		
		addedFacesSinceBeginning = new ArrayList<List<Integer>>();
		currentCopy = -1;
		
		// initialize TODO
		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		int nbFaces = context.board().graph().faces().size();
		for (int i=0; i<nbFaces; i++)
		{
			mappedInitToNewIndexes().put(i, i);
			mappedNewToInitIndexes().put(i, i);
		}
		// adding ids for the other containers than the board
		for (int i=nbFaces; i<nbFaces+context.containers().length-1; i++)
		{
			mappedInitToNewIndexes().put(i, i);
			mappedNewToInitIndexes().put(i, i);
		}
		
		listCopieMappedInitToNewIndexes = new ArrayList<HashMap<Integer, Integer>>();
		listCopieMappedNewToInitIndexes = new ArrayList<HashMap<Integer, Integer>>();
		listCopieSurplusInitIndexes = new ArrayList<HashSet<Integer>>();
		
	}
	
	public void initForNewBoard(Context context)
	{
		// data structures to map cells between current plate and new plate
		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>();
		
		surplusInitIndexes = new HashSet<Integer>();
        
		// data structures to map vertices between current plate and new plate
		nbAddedColPerRow = new HashSet[context.topology().rows().get(SiteType.Vertex).size()+2];
		
		vertexAddedLeftCol = false;
		
		cellsRowsColsAdded = new ArrayList<int[]>();
		
		if (initialCells == null)	
			init(context);
	}
	
	public void clean()
	{

		mappedPrevToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToPrevIndexes = new HashMap<Integer, Integer>();
		surplusIndexes = new HashSet<Integer>(); 

		mappedInitToNewIndexes = new HashMap<Integer, Integer>();
		mappedNewToInitIndexes = new HashMap<Integer, Integer>();
		surplusInitIndexes = new HashSet<Integer>(); 
		
		initialHands = null;
		initialCells = null;
		initialCells2 = null;
		
		nbAddedCellsFromStart = 0;
		
		cellsRowsColsAddedFromStart = new ArrayList<ArrayList<int[]>>();
		cellsRowsColsAdded = new ArrayList<int[]>();
		cellsAddedLeftCols = new ArrayList<Integer>();
		cellsAddedDownRows = new ArrayList<Integer>();
		cellsAddedRightCols = new ArrayList<Integer>();
		cellsAddedUpRows = new ArrayList<Integer>();

		rowsNeighborsVertices = new ArrayList<Integer>(); 
		rowsNeighborsMapToColsVertices = new HashMap<Integer, ArrayList<Integer>>();		
		nbAddedColPerRow = null;
		
		vertexAddedLeftCol = false; 
		
	}
	
	public void updateNewTotalIndexesCells()
	{
		newTotalIndexesCells = mappedPrevToNewIndexes().size() + surplusIndexes.size();
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
	
	protected abstract Graph forward(final Context context, final Cell cell);
	
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