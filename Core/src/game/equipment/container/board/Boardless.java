package game.equipment.container.board;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.tile.Tile;
import game.equipment.container.other.Hand;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.graph.generators.basis.hex.HexagonOnHex;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.functions.graph.generators.basis.tri.TriangleOnTri;
import game.types.board.SiteType;
import game.types.board.TilingBoardlessType;
import game.types.state.GameType;
import main.Constants;
import metadata.graphics.util.ContainerStyleType;
import other.concept.Concept;

/**
 * Defines a boardless container growing in function of the pieces played.
 *
 * @author Eric.Piette
 * 
 * @remarks The playable sites of the board will be all the sites adjacent to
 *          the places already played/placed. No pregeneration is computed on
 *          the graph except the centre.
 */
public class Boardless extends Board
{
	private static final long serialVersionUID = 1L;
	private int dimension;
	private TilingBoardlessType tiling;

	//-------------------------------------------------------------------------

	/**
	 * @param tiling    The tiling of the boardless container.
	 * @param dimension The "fake" size of the board used for boardless [41].
	 * @param largeStack  The game can involves stack(s) higher than 32.
	 * 
	 * @example (boardless Hexagonal)
	 */
	public Boardless
	(
		            final TilingBoardlessType tiling,
		@Opt        final DimFunction         dimension,       
		@Opt @Name 	final Boolean             largeStack
	)
	{
		super
		(
			tiling == TilingBoardlessType.Square
				? new RectangleOnSquare(dimension == null ? new DimConstant(Constants.SIZE_BOARDLESS) : dimension, null, null, null)
				: tiling == TilingBoardlessType.Hexagonal 
					? new HexagonOnHex(dimension == null ? new DimConstant(Constants.SIZE_HEX_BOARDLESS) : dimension)
						: new TriangleOnTri(dimension == null ? new DimConstant(Constants.SIZE_BOARDLESS) : dimension),
				null, 
				null,
				null, 
				null, 
				SiteType.Cell,
				largeStack
		);

		this.style = ContainerStyleType.Boardless;
		this.dimension = dimension.eval();
		this.tiling = tiling;
	}
	
	/**
	 * Copy constructor.
	 *
	 * Protected because we do not want the compiler to detect it, this is called
	 * only in Clone method.
	 * 
	 * @param other
	 */
	protected Boardless(final Boardless other)
	{
		super(other);
		this.dimension = other.dimension;
		this.tiling = other.tiling;
	}

	@Override
	public boolean isBoardless()
	{
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return super.gameFlags(game) | GameType.Boardless;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Boardless.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		return readEvalContext;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "table" ;
	}
	
	/**
	 * Gets the dimension of the board.
	 * 
	 * @return dimension of the board.
	 */
	public int dimension()
	{
		return this.dimension;
	}
	
	/**
	 * Set a new dimension to the board.
	 * 
	 * @param newDimension the new dimension of the board.
	 */
	public void setDimension(int newDimension) {
		this.dimension = newDimension;
	}
	
	@Override
	public Boardless clone()
	{
		return new Boardless(this);
	}
	
	/**
	 * @return tiling of the board.
	 */
	public TilingBoardlessType tiling()
	{
		return this.tiling;
	}
	
}