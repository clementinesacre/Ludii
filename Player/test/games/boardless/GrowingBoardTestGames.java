package games.boardless;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.equipment.container.board.Boardless;
import other.GameLoader;
import other.trial.Trial;
import other.context.Context;

/**
 * Test that the board size is changing properly, through all aspects.
 * We suppose the game is boardless - this test does not concern normal board games.
 * These tests focus on just playing moves, straight forward.
 *
 * @author Clémentine Sacré
 */
//@SuppressWarnings("static-method")
public class GrowingBoardTestGames {
	
	/**
	 * Initializes the game and creates the context.
	 * 
	 * @return the context
	 */
	public Context initGame(String gameName)
	{
		final Game game = GameLoader.loadGameFromName(gameName);
		final Context context = new Context(game, new Trial(game));
		game.start(context, true);
		return context;	
	}
	
	/**
	 * Tests the initial dimension of the board for the game Andantino.
	 */
	@Test
	public void testAndantino()
	{
		// hexagonal
		List<String> options = new ArrayList<String>();
		options.add("Tiling/Hexagonal");
		Game game1 = GameLoader.loadGameFromName("Andantino.lud", options);
		Context context1 = new Context(game1, new Trial(game1));
		Boardless board1 = (Boardless) context1.game().board();
		assertEquals(3, board1.dimension());
		
		// square
		options = new ArrayList<String>();
		options.add("Tiling/Square");
		final Game game2 = GameLoader.loadGameFromName("Andantino.lud", options);
		Context context2 = new Context(game2, new Trial(game2));
		Boardless board2 = (Boardless) context2.game().board();
		assertEquals(4, board2.dimension());
		
		// triangular
		options = new ArrayList<String>();
		options.add("Tiling/Triangle");
		final Game game3 = GameLoader.loadGameFromName("Andantino.lud", options);
		Context context3 = new Context(game3, new Trial(game3));
		Boardless board3 = (Boardless) context3.game().board();
		assertEquals(5, board3.dimension());
	}
	
	
	/**
	 * Tests the initial dimension of the board for the game Bravalath.
	 */
	@Test
	public void testBravalath()
	{
		// hexagonal
		List<String> options = new ArrayList<String>();
		options.add("Tiling/Hexagonal");
		Game game1 = GameLoader.loadGameFromName("Bravalath.lud", options);
		Context context1 = new Context(game1, new Trial(game1));
		Boardless board1 = (Boardless) context1.game().board();
		assertEquals(3, board1.dimension());
		
		// square
		options = new ArrayList<String>();
		options.add("Tiling/Square");
		final Game game2 = GameLoader.loadGameFromName("Bravalath.lud", options);
		Context context2 = new Context(game2, new Trial(game2));
		Boardless board2 = (Boardless) context2.game().board();
		assertEquals(4, board2.dimension());
		
		// triangular
		options = new ArrayList<String>();
		options.add("Tiling/Triangle");
		final Game game3 = GameLoader.loadGameFromName("Bravalath.lud", options);
		Context context3 = new Context(game3, new Trial(game3));
		Boardless board3 = (Boardless) context3.game().board();
		assertEquals(5, board3.dimension());
	}
	
	
	/**
	 * Tests the initial dimension of the board for the game Chex.
	 */
	@Test
	public void testChex()
	{
		Context context = initGame("Chex.lud");
		Boardless board = (Boardless) context.game().board();

		assertEquals(3, board.dimension());
	}
	
	
	/**
	 * Tests the initial dimension of the board for the game Plotto.
	 */
	@Test
	public void testPlotto()
	{
		Context context = initGame("Plotto.lud");
		Boardless board = (Boardless) context.game().board();

		assertEquals(3, board.dimension());
	}
	
	
	/**
	 * Tests the initial dimension of the board for the game Ringo.
	 */
	@Test
	public void testRingo()
	{
		Context context = initGame("Ringo.lud");
		Boardless board = (Boardless) context.game().board();
		
		assertEquals(10, board.dimension());
	}
}