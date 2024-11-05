package games;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import app.move.GrowingBoard;
import game.Game;
import game.equipment.Equipment;
import game.equipment.container.board.Boardless;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.collections.ChunkSet;
import other.GameLoader;
import other.trial.Trial;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerFlatState;
import other.state.container.ContainerState;
import other.state.zhash.HashedBitSet;
import other.state.zhash.HashedChunkSet;
import other.topology.Cell;
import other.topology.TopologyElement;

/**
 * Test that the board size is changing properly, through all aspects.
 * We suppose the game is boardless - this test does not concern normal board games.
 *
 * @author Clémentine Sacré
 */
//@SuppressWarnings("static-method")
public class GrowingBoardTest {
	
	/**
	 * Initializes the game and creates the context.
	 * 
	 * @return the context
	 */
	public Context initGame()
	{
		final Game game = GameLoader.loadGameFromName("TestClementine.lud");
		final Context context = new Context(game, new Trial(game));
		game.start(context, true);
		return context;	
	}
	
	/**
	 * Creates a move Move that needs a from and a to.
	 * 
	 * @param from index of the from position.
	 * @param to index of the to position.
	 * @return the create Move.
	 */
	public Move getMoveMove(int from, int to)
	{
		String moveStr = "[Move:mover=1,from="+from+",to="+to+",actions=[Move:typeFrom=Cell,from="+from+",typeTo=Cell,to="+to+",decision=true]]";
		return new Move(moveStr);
	}
	
	/**
	 * Creates a move Move that needs a from and a to.
	 * 
	 * @param from index of the from position.
	 * @param to index of the to position.
	 * @param mover id of the mover. Starts at 1.
	 * @return the create Move.
	 */
	public Move getMoveMove(int from, int to, int mover)
	{
		String moveStr = "[Move:mover="+mover+",from="+from+",to="+to+",actions=[Move:typeFrom=Cell,from="+from+",typeTo=Cell,to="+to+",decision=true]]";
		return new Move(moveStr);
	}
	
	/**
	 * Apply a move.
	 * @param context context on which apply the move.
	 * @param move move to apply.
	 */
	public void applyMove(Context context, int from, int to, int mover)
	{
		Move move = getMoveMove(from, to, mover);
		context.game().apply(context, move);
		context.trial().setNumSubmovesPlayed(context.trial().numSubmovesPlayed() + 1);
	}
	
	/**
	 * Call all needed methods to update the board.
	 * 
	 * @param context context on which to update the board.
	 * @return list of the containers (like board and hand's players) before updating the board.
	 */
	public ContainerState[] updateBoard(Context context)
	{
		Game game = context.game();
		Trial trial = context.trial();
		Boardless board = (Boardless) game.board();
		
		ContainerState[] prevContainerStates = context.state().containerStates();
		GrowingBoard.updateBoard(context);
		
		return prevContainerStates;
	}
	
	/**
	 * Tests the method that check if a move made is done on an edge (= the perimeter 
	 * of the board).
	 */
	@Test
	public void testTouchingEdge()
	{
		// init
		Context context = initGame();
		List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());
		
		// test
		for (int i=0; i<6; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(25, i).to()));
		for (int i=6; i<8; i++)
			assertFalse(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(25, i).to()));
		for (int i=9; i<11; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(25, i).to()));
		for (int i=11; i<14; i++)
			assertFalse(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(25, i).to()));
		for (int i=14; i<16; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(25, i).to()));
		for (int i=16; i<19; i++)
			assertFalse(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(25, i).to()));
		for (int i=19; i<=24; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(25, i).to()));
	}
	
	/**
	 * Tests the method that initializes main constants and data structures, when the 
	 * board size has been changed 1 time (one edge has been touched).
	 */
	@Test
	public void testInitConstants1TouchedEdge()
	{
		// init
		Context context = initGame();

		updateBoard(context);
		
		// test main constants
		assertEquals(GrowingBoard.prevDimensionBoard(), 5);
		assertEquals(GrowingBoard.prevAreaBoard(), 25);
		assertEquals(GrowingBoard.prevTotalIndexes(), 27);
		assertEquals(GrowingBoard.newDimensionBoard(), 7);
		assertEquals(GrowingBoard.newAreaBoard(), 49);
		assertEquals(GrowingBoard.newTotalIndexes(), 51);
		assertEquals(GrowingBoard.diff(), 24);
		
		// test main data structures
		HashMap<Integer, Integer> mappedPrevToNewIndexes = GrowingBoard.mappedPrevToNewIndexes();
		HashMap<Integer, Integer> mappedNewToPrevIndexes = GrowingBoard.mappedNewToPrevIndexes();
		HashSet<Integer> newAddedIndexes = GrowingBoard.newAddedIndexes();
		
		for (int i=0; i<=26; i++)
			assertTrue(mappedPrevToNewIndexes.containsKey(i));
		for (int i=27; i<=50; i++)
			assertFalse(mappedPrevToNewIndexes.containsKey(i));

		for (int i=0; i<=7; i++)
			assertFalse(mappedNewToPrevIndexes.containsKey(i));
		for (int i=8; i<=12; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		assertFalse(mappedNewToPrevIndexes.containsKey(13));
		assertFalse(mappedNewToPrevIndexes.containsKey(14));
		for (int i=15; i<=19; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		assertFalse(mappedNewToPrevIndexes.containsKey(20));
		assertFalse(mappedNewToPrevIndexes.containsKey(21));
		for (int i=22; i<=26; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		assertFalse(mappedNewToPrevIndexes.containsKey(27));
		assertFalse(mappedNewToPrevIndexes.containsKey(28));
		for (int i=29; i<=33; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		assertFalse(mappedNewToPrevIndexes.containsKey(34));
		assertFalse(mappedNewToPrevIndexes.containsKey(35));
		for (int i=36; i<=40; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		for (int i=41; i<=48; i++)
			assertFalse(mappedNewToPrevIndexes.containsKey(i));
		assertTrue(mappedNewToPrevIndexes.containsKey(49));
		assertTrue(mappedNewToPrevIndexes.containsKey(50));

		for (int i=0; i<=7; i++)
			assertTrue(newAddedIndexes.contains(i));
		for (int i=8; i<=12; i++)
			assertFalse(newAddedIndexes.contains(i));
		assertTrue(newAddedIndexes.contains(13));
		assertTrue(newAddedIndexes.contains(14));
		for (int i=15; i<=19; i++)
			assertFalse(newAddedIndexes.contains(i));
		assertTrue(newAddedIndexes.contains(20));
		assertTrue(newAddedIndexes.contains(21));
		for (int i=22; i<=26; i++)
			assertFalse(newAddedIndexes.contains(i));
		assertTrue(newAddedIndexes.contains(27));
		assertTrue(newAddedIndexes.contains(28));
		for (int i=29; i<=33; i++)
			assertFalse(newAddedIndexes.contains(i));
		assertTrue(newAddedIndexes.contains(34));
		assertTrue(newAddedIndexes.contains(35));
		for (int i=36; i<=40; i++)
			assertFalse(newAddedIndexes.contains(i));
		for (int i=41; i<=48; i++)
			assertTrue(newAddedIndexes.contains(i));
		assertFalse(newAddedIndexes.contains(49));
		assertFalse(newAddedIndexes.contains(50));
	}

	/**
	 * Tests the method that initializes main constants and data structures, when the 
	 * board size has been changed 2 times (two edges have been touched one after the 
	 * other).
	 */
	@Test
	public void testInitConstants2TouchedEdge()
	{
		// init
		Context context = initGame();

		updateBoard(context);
		updateBoard(context);
		
		// test main constants
		assertEquals(GrowingBoard.prevDimensionBoard(), 7);
		assertEquals(GrowingBoard.prevAreaBoard(), 49);
		assertEquals(GrowingBoard.prevTotalIndexes(), 51);
		assertEquals(GrowingBoard.newDimensionBoard(), 9);
		assertEquals(GrowingBoard.newAreaBoard(), 81);
		assertEquals(GrowingBoard.newTotalIndexes(), 83);
		assertEquals(GrowingBoard.diff(), 32);
		
		// test main data structures
		HashMap<Integer, Integer> mappedPrevToNewIndexes = GrowingBoard.mappedPrevToNewIndexes();
		HashMap<Integer, Integer> mappedNewToPrevIndexes = GrowingBoard.mappedNewToPrevIndexes();
		HashSet<Integer> newAddedIndexes = GrowingBoard.newAddedIndexes();

		for (int i=0; i<=50; i++)
			assertTrue(mappedPrevToNewIndexes.containsKey(i));
		for (int i=51; i<=82; i++)
			assertFalse(mappedPrevToNewIndexes.containsKey(i));
		
		for (int i=0; i<=9; i++)
			assertFalse(mappedNewToPrevIndexes.containsKey(i));
		for (int i=10; i<=16; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		assertFalse(mappedNewToPrevIndexes.containsKey(17));
		assertFalse(mappedNewToPrevIndexes.containsKey(18));
		for (int i=19; i<=25; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		assertFalse(mappedNewToPrevIndexes.containsKey(26));
		assertFalse(mappedNewToPrevIndexes.containsKey(27));
		for (int i=28; i<=34; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		assertFalse(mappedNewToPrevIndexes.containsKey(35));
		assertFalse(mappedNewToPrevIndexes.containsKey(36));
		for (int i=37; i<=43; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		assertFalse(mappedNewToPrevIndexes.containsKey(44));
		assertFalse(mappedNewToPrevIndexes.containsKey(45));
		for (int i=46; i<=52; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		assertFalse(mappedNewToPrevIndexes.containsKey(53));
		assertFalse(mappedNewToPrevIndexes.containsKey(54));
		for (int i=55; i<=61; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		assertFalse(mappedNewToPrevIndexes.containsKey(62));
		assertFalse(mappedNewToPrevIndexes.containsKey(63));
		for (int i=64; i<=70; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		for (int i=71; i<=80; i++)
			assertFalse(mappedNewToPrevIndexes.containsKey(i));
		assertTrue(mappedNewToPrevIndexes.containsKey(81));
		assertTrue(mappedNewToPrevIndexes.containsKey(82));

		for (int i=0; i<=9; i++)
			assertTrue(newAddedIndexes.contains(i));
		for (int i=10; i<=16; i++)
			assertFalse(newAddedIndexes.contains(i));
		assertTrue(newAddedIndexes.contains(17));
		assertTrue(newAddedIndexes.contains(18));
		for (int i=19; i<=25; i++)
			assertFalse(newAddedIndexes.contains(i));
		assertTrue(newAddedIndexes.contains(26));
		assertTrue(newAddedIndexes.contains(27));
		for (int i=28; i<=34; i++)
			assertFalse(newAddedIndexes.contains(i));
		assertTrue(newAddedIndexes.contains(35));
		assertTrue(newAddedIndexes.contains(36));
		for (int i=37; i<=43; i++)
			assertFalse(newAddedIndexes.contains(i));
		assertTrue(newAddedIndexes.contains(44));
		assertTrue(newAddedIndexes.contains(45));
		for (int i=46; i<=52; i++)
			assertFalse(newAddedIndexes.contains(i));
		assertTrue(newAddedIndexes.contains(53));
		assertTrue(newAddedIndexes.contains(54));
		for (int i=55; i<=61; i++)
			assertFalse(newAddedIndexes.contains(i));
		assertTrue(newAddedIndexes.contains(62));
		assertTrue(newAddedIndexes.contains(63));
		for (int i=64; i<=70; i++)
			assertFalse(newAddedIndexes.contains(i));
		for (int i=71; i<=80; i++)
			assertTrue(newAddedIndexes.contains(i));
		assertFalse(newAddedIndexes.contains(81));
		assertFalse(newAddedIndexes.contains(82));
	}
	
	/**
	 * Tests the indexes used inside the equipment.
	 */
	@Test
	public void testUpdateIndexesInsideEquipment()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Equipment equipment = game.equipment();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		
		// test
		int[] newOffset = equipment.offset();
		for (int i=0; i<=48; i++)
			assertEquals(newOffset[i], i);
		assertEquals(newOffset[49], 0);
		assertEquals(newOffset[50], 0);
		
		int[] newContainerId = equipment.containerId();
		for (int i=0; i<=48; i++)
			assertEquals(newContainerId[i], 0);
		assertEquals(newContainerId[49], 1);
		assertEquals(newContainerId[50], 2);
				
		int[] newSitesFrom = equipment.sitesFrom();
		assertEquals(newSitesFrom[0], 0);
		assertEquals(newSitesFrom[1], 49);
		assertEquals(newSitesFrom[2], 50);
		
		TopologyElement topologyElement0 = game.equipment().containers()[0].topology().getGraphElements(SiteType.Cell).get(0);
		assertEquals(topologyElement0.index(), 0);
		TopologyElement topologyElement1 = game.equipment().containers()[1].topology().getGraphElements(SiteType.Cell).get(0);
		assertEquals(topologyElement1.index(), 49);
		TopologyElement topologyElement2 = game.equipment().containers()[2].topology().getGraphElements(SiteType.Cell).get(0);
		assertEquals(topologyElement2.index(), 50);
	}
	
	/**
	 * Tests the method that re-applys the moves previously done on the new board.
	 */
	@Test
	public void testReplayMoves()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);

		// test
		List<Move> newMovesDone = context.trial().generateCompleteMovesList();
		assertEquals(newMovesDone.get(0).actions().get(0).to(), 49);
		assertEquals(newMovesDone.get(0).actions().get(0).what(), 2);
		assertEquals(newMovesDone.get(0).actions().get(0).count(), 3);
		
		assertEquals(newMovesDone.get(1).actions().get(0).to(), 50);
		assertEquals(newMovesDone.get(1).actions().get(0).what(), 3);
		assertEquals(newMovesDone.get(1).actions().get(0).count(), 3);
		
		assertEquals(newMovesDone.get(2).actions().get(0).to(), 24);
		assertEquals(newMovesDone.get(2).actions().get(0).what(), 4);
		
		assertEquals(newMovesDone.get(3).actions().get(0).to(), 25);
		assertEquals(newMovesDone.get(3).actions().get(0).what(), 4);
		
		assertEquals(newMovesDone.get(4).actions().get(0).to(), 23);
		assertEquals(newMovesDone.get(4).actions().get(0).what(), 4);
		
		assertEquals(newMovesDone.get(5).actions().get(0).from(), 49);
		assertEquals(newMovesDone.get(5).actions().get(0).to(), 22);
	}
	
	/**
	 * Tests the method that re-generate the legal moves after applying the moves on the new board.
	 */
	@Test
	public void testGenerateLegalMoves()
	{
		// TODO BUG : I DONT KNOW HOW TO APPLY A MOVE PROPERLY  - HERE THERE ARE NO LEGAL MOVES AFTER 
		// I APPLIED THE MOVE - UNLIKE WHEN DOING IT WITH THE VISUAL INTERFACE
		
		// init
		Context context = initGame();
		
		applyMove(context, 25, 10, 1);		
		updateBoard(context);
		
		// test
		Moves newLegalMoves = context.trial().cachedLegalMoves();

		assertEquals(newLegalMoves.get(0).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(0).actions().get(0).to(), 15);

		assertEquals(newLegalMoves.get(1).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(1).actions().get(0).to(), 16);

		assertEquals(newLegalMoves.get(2).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(2).actions().get(0).to(), 17);

		assertEquals(newLegalMoves.get(3).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(3).actions().get(0).to(), 18);

		assertEquals(newLegalMoves.get(4).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(4).actions().get(0).to(), 19);

		assertEquals(newLegalMoves.get(5).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(5).actions().get(0).to(), 26);

		assertEquals(newLegalMoves.get(6).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(6).actions().get(0).to(), 29);

		assertEquals(newLegalMoves.get(7).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(7).actions().get(0).to(), 30);

		assertEquals(newLegalMoves.get(8).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(8).actions().get(0).to(), 31);

		assertEquals(newLegalMoves.get(9).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(9).actions().get(0).to(), 32);

		assertEquals(newLegalMoves.get(10).actions().get(0).from(), 50);
		assertEquals(newLegalMoves.get(10).actions().get(0).to(), 33);
	}
	
	/**
	 * Tests the intern state of the Container, which are the chunks, after 
	 * doing a move on an edge.
	 */
	@Test
	public void testUpdateChunksAfterOneMove()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 10, 1);
		ContainerState[] prevContainerStates = updateBoard(context);
		
		// test
		ContainerState[] newContainerStates = context.state().containerStates();
		// Board's state
		ContainerState newContainerState0 = newContainerStates[0];
		if (newContainerState0 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState0;
			ContainerFlatState prevContainerFlatState = (ContainerFlatState) prevContainerStates[0];
						
			assertEquals(newContainerFlatState.who().internalState().numNonZeroChunks(), prevContainerFlatState.who().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.what().internalState().numNonZeroChunks(), prevContainerFlatState.what().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.count().internalState().numNonZeroChunks(), prevContainerFlatState.count().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.state().internalState().numNonZeroChunks(), prevContainerFlatState.state().internalState().numNonZeroChunks());
			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());

			HashedChunkSet who = newContainerFlatState.who();
			for (int i=0; i<22; i++)
				assertEquals(who.getChunk(i), 0);
			assertEquals(who.getChunk(22), 1);
			for (int i=23; i<=50; i++)
				assertEquals(who.getChunk(i), 0);

			HashedChunkSet what = newContainerFlatState.what();
			for (int i=0; i<22; i++)
				assertEquals(who.getChunk(i), 0);
			assertEquals(what.getChunk(22), 2);
			assertEquals(what.getChunk(23), 4);
			assertEquals(what.getChunk(24), 4);
			assertEquals(what.getChunk(25), 4);
			for (int i=26; i<=50; i++)
				assertEquals(what.getChunk(i), 0);

			HashedChunkSet count = newContainerFlatState.count();
			for (int i=0; i<22; i++)
				assertEquals(count.getChunk(i), 0);
			for (int i=22; i<=25; i++)
				assertEquals(count.getChunk(i), 1);
			for (int i=26; i<=50; i++)
				assertEquals(count.getChunk(i), 0);

			HashedChunkSet state = newContainerFlatState.state();
			for (int i=0; i<=50; i++)
				assertEquals(state.getChunk(i), 0);

			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			for (int i=0; i<22; i++)
				assertTrue(empty.get(i));
			for (int i=22; i<=25; i++)
				assertFalse(empty.get(i));
			for (int i=26; i<=48; i++)
				assertTrue(empty.get(i));
			
			HashedBitSet playable = newContainerFlatState.playable();
			for (int i=0; i<14; i++)
				assertFalse(playable.get(i));
			for (int i=14; i<=19; i++)
				assertTrue(playable.get(i));
			assertFalse(playable.get(20));
			assertTrue(playable.get(21));
			for (int i=22; i<26; i++)
				assertFalse(playable.get(i));
			assertTrue(playable.get(26));
			assertFalse(playable.get(27));
			for (int i=28; i<=33; i++)
				assertTrue(playable.get(i));
			for (int i=34; i<=48; i++)
				assertFalse(playable.get(i));
		}
		else 
			fail();
		
		// first player's state
		ContainerState newContainerState1 = newContainerStates[1];
		if (newContainerState1 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState1;
			ContainerFlatState prevContainerFlatState = (ContainerFlatState) prevContainerStates[1];
			
			assertEquals(newContainerFlatState.who().internalState().numNonZeroChunks(), prevContainerFlatState.who().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.what().internalState().numNonZeroChunks(), prevContainerFlatState.what().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.count().internalState().numNonZeroChunks(), prevContainerFlatState.count().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.state().internalState().numNonZeroChunks(), prevContainerFlatState.state().internalState().numNonZeroChunks());
			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());

			HashedChunkSet who = newContainerFlatState.who();
			assertEquals(who.getChunk(0), 1);

			HashedChunkSet what = newContainerFlatState.what();
			assertEquals(what.getChunk(0), 2);

			HashedChunkSet count = newContainerFlatState.count();
			assertEquals(count.getChunk(0), 2);

			HashedChunkSet state = newContainerFlatState.state();
			assertEquals(state.internalState().numNonZeroChunks(), 0);
			
			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			assertTrue(empty.isEmpty());
			
			HashedBitSet playable = newContainerFlatState.playable();
			assertTrue(playable.internalState().isEmpty());
		}
		else 
			fail();

		// second player's state
		ContainerState newContainerState2 = newContainerStates[2];
		if (newContainerState2 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState2;
			ContainerFlatState prevContainerFlatState = (ContainerFlatState) prevContainerStates[2];
			
			assertEquals(newContainerFlatState.who().internalState().numNonZeroChunks(), prevContainerFlatState.who().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.what().internalState().numNonZeroChunks(), prevContainerFlatState.what().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.count().internalState().numNonZeroChunks(), prevContainerFlatState.count().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.state().internalState().numNonZeroChunks(), prevContainerFlatState.state().internalState().numNonZeroChunks());
			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());

			HashedChunkSet who = newContainerFlatState.who();
			assertEquals(who.getChunk(0), 2);

			HashedChunkSet what = newContainerFlatState.what();
			assertEquals(what.getChunk(0), 3);

			HashedChunkSet count = newContainerFlatState.count();
			assertEquals(count.getChunk(0), 3);

			HashedChunkSet state = newContainerFlatState.state();
			assertEquals(state.internalState().numNonZeroChunks(), 0);

			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			assertTrue(empty.isEmpty());
			
			HashedBitSet playable = newContainerFlatState.playable();
			assertTrue(playable.internalState().isEmpty());
			
		}
		else 
			fail();
	}
	
	/**
	 * Tests the intern state of the containers (board and hand's players), 
	 * which are the chunks, after multiple moves, none on any edge.
	 */
	@Test
	public void testUpdateChunksAfterMultipleMovesNoEdge()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 6, 1);
		applyMove(context, 26, 18, 2);
		applyMove(context, 25, 17, 1);
		applyMove(context, 26, 7, 2);
			
		// test
		ContainerState[] newContainerStates = context.state().containerStates();
		// Board's state
		ContainerState newContainerState0 = newContainerStates[0];
		if (newContainerState0 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState0;

			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());

			HashedChunkSet who = newContainerFlatState.who();
			for (int i=0; i<6; i++)
				assertEquals(who.getChunk(i), 0);
			assertEquals(who.getChunk(6), 1);
			assertEquals(who.getChunk(7), 2);
			for (int i=8; i<17; i++)
				assertEquals(who.getChunk(i), 0);
			assertEquals(who.getChunk(17), 1);
			assertEquals(who.getChunk(18), 2);
			for (int i=19; i<=24; i++)
				assertEquals(who.getChunk(i), 0);

			HashedChunkSet what = newContainerFlatState.what();
			for (int i=0; i<6; i++)
				assertEquals(what.getChunk(i), 0);
			assertEquals(what.getChunk(6), 2);
			assertEquals(what.getChunk(7), 3);
			for (int i=8; i<11; i++)
				assertEquals(what.getChunk(i), 0);
			for (int i=11; i<14; i++)
				assertEquals(what.getChunk(i), 4);
			for (int i=14; i<17; i++)
				assertEquals(what.getChunk(i), 0);
			assertEquals(what.getChunk(17), 2);
			assertEquals(what.getChunk(18), 3);
			for (int i=19; i<=24; i++)
				assertEquals(what.getChunk(i), 0);

			HashedChunkSet count = newContainerFlatState.count();
			for (int i=0; i<6; i++)
				assertEquals(count.getChunk(i), 0);
			assertEquals(count.getChunk(6), 1);
			assertEquals(count.getChunk(7), 1);
			for (int i=8; i<11; i++)
				assertEquals(count.getChunk(i), 0);
			assertEquals(count.getChunk(11), 1);
			assertEquals(count.getChunk(12), 1);
			assertEquals(count.getChunk(13), 1);
			for (int i=14; i<17; i++)
				assertEquals(count.getChunk(i), 0);
			assertEquals(count.getChunk(17), 1);
			assertEquals(count.getChunk(18), 1);
			for (int i=19; i<=24; i++)
				assertEquals(count.getChunk(i), 0);

			HashedChunkSet state = newContainerFlatState.state();
			for (int i=0; i<=50; i++)
				assertEquals(state.getChunk(i), 0);

			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			for (int i=0; i<6; i++)
				assertTrue(empty.get(i));
			assertFalse(empty.get(6));
			assertFalse(empty.get(7));
			for (int i=8; i<11; i++)
				assertTrue(empty.get(i));
			assertFalse(empty.get(11));
			assertFalse(empty.get(12));
			assertFalse(empty.get(13));
			for (int i=14; i<17; i++)
				assertTrue(empty.get(i));
			assertFalse(empty.get(17));
			assertFalse(empty.get(18));
			for (int i=19; i<=24; i++)
				assertTrue(empty.get(i));
			
			HashedBitSet playable = newContainerFlatState.playable();
			for (int i=0; i<4; i++)
				assertTrue(playable.get(i));
			assertFalse(playable.get(4));
			assertTrue(playable.get(5));
			for (int i=6; i<=7; i++)
				assertFalse(playable.get(i));
			for (int i=8; i<11; i++)
				assertTrue(playable.get(i));
			for (int i=11; i<=13; i++)
				assertFalse(playable.get(i));
			for (int i=14; i<17; i++)
				assertTrue(playable.get(i));
			for (int i=17; i<=18; i++)
				assertFalse(playable.get(i));
			assertTrue(playable.get(19));
			assertFalse(playable.get(20));
			for (int i=21; i<=24; i++)
				assertTrue(playable.get(i));
		}
		else 
			fail();
		
		// first player's state
		ContainerState newContainerState1 = newContainerStates[1];
		if (newContainerState1 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState1;

			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());

			HashedChunkSet who = newContainerFlatState.who();
			assertEquals(who.getChunk(0), 1);

			HashedChunkSet what = newContainerFlatState.what();
			assertEquals(what.getChunk(0), 2);

			HashedChunkSet count = newContainerFlatState.count();
			assertEquals(count.getChunk(0), 1);

			HashedChunkSet state = newContainerFlatState.state();
			assertEquals(state.internalState().numNonZeroChunks(), 0);
			
			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			assertTrue(empty.isEmpty());
			
			HashedBitSet playable = newContainerFlatState.playable();
			assertTrue(playable.internalState().isEmpty());
		}
		else 
			fail();
		
		// second player's state
		ContainerState newContainerState2 = newContainerStates[2];
		if (newContainerState2 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState2;

			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());

			HashedChunkSet who = newContainerFlatState.who();
			assertEquals(who.getChunk(0), 2);

			HashedChunkSet what = newContainerFlatState.what();
			assertEquals(what.getChunk(0), 3);

			HashedChunkSet count = newContainerFlatState.count();
			assertEquals(count.getChunk(0), 1);

			HashedChunkSet state = newContainerFlatState.state();
			assertEquals(state.internalState().numNonZeroChunks(), 0);

			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			assertTrue(empty.isEmpty());
			
			HashedBitSet playable = newContainerFlatState.playable();
			assertTrue(playable.internalState().isEmpty());
			
		}
		else 
			fail();
	}
	
	/**
	 * Tests the intern state of the containers (board and hand's players), 
	 * which are the chunks, after multiple moves, last one on an edge.
	 */
	@Test
	public void testUpdateChunksAfterMultipleMovesWithEdge()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 6, 1);
		applyMove(context, 26, 18, 2);
		applyMove(context, 25, 17, 1);
		applyMove(context, 26, 5, 2);

		ContainerState[] prevContainerStates = updateBoard(context);
		
		// test
		ContainerState[] newContainerStates = context.state().containerStates();
		// Board's state
		ContainerState newContainerState0 = newContainerStates[0];
		if (newContainerState0 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState0;
			ContainerFlatState prevContainerFlatState = (ContainerFlatState) prevContainerStates[0];			

			List<Cell> adj1 = context.topology().cells().get(15).adjacent();
			List<Cell> adj2 = context.state().containerStates()[0].container().topology().cells().get(15).adjacent();
			assertEquals(adj1, adj2);
			
			assertEquals(newContainerFlatState.who().internalState().numNonZeroChunks(), prevContainerFlatState.who().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.what().internalState().numNonZeroChunks(), prevContainerFlatState.what().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.count().internalState().numNonZeroChunks(), prevContainerFlatState.count().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.state().internalState().numNonZeroChunks(), prevContainerFlatState.state().internalState().numNonZeroChunks());
			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());
			
			HashedChunkSet who = newContainerFlatState.who();
			for (int i=0; i<15; i++)
				assertEquals(who.getChunk(i), 0);
			assertEquals(who.getChunk(15), 2);
			assertEquals(who.getChunk(16), 1);
			for (int i=17; i<31; i++)
				assertEquals(who.getChunk(i), 0);
			assertEquals(who.getChunk(31), 1);
			assertEquals(who.getChunk(32), 2);
			for (int i=33; i<=48; i++)
				assertEquals(who.getChunk(i), 0);

			HashedChunkSet what = newContainerFlatState.what();
			for (int i=0; i<15; i++)
				assertEquals(what.getChunk(i), 0);
			assertEquals(what.getChunk(15), 3);
			assertEquals(what.getChunk(16), 2);
			for (int i=17; i<23; i++)
				assertEquals(what.getChunk(i), 0);
			for (int i=23; i<26; i++)
				assertEquals(what.getChunk(i), 4);
			for (int i=26; i<31; i++)
				assertEquals(what.getChunk(i), 0);
			assertEquals(what.getChunk(31), 2);
			assertEquals(what.getChunk(32), 3);
			for (int i=33; i<=48; i++)
				assertEquals(what.getChunk(i), 0);

			HashedChunkSet count = newContainerFlatState.count();
			for (int i=0; i<15; i++)
				assertEquals(count.getChunk(i), 0);
			assertEquals(count.getChunk(15), 1);
			assertEquals(count.getChunk(16), 1);
			for (int i=17; i<23; i++)
				assertEquals(count.getChunk(i), 0);
			for (int i=23; i<26; i++)
				assertEquals(count.getChunk(i), 1);
			for (int i=26; i<31; i++)
				assertEquals(count.getChunk(i), 0);
			assertEquals(count.getChunk(31), 1);
			assertEquals(count.getChunk(32), 1);
			for (int i=33; i<=48; i++)
				assertEquals(count.getChunk(i), 0);

			HashedChunkSet state = newContainerFlatState.state();
			for (int i=0; i<=50; i++)
				assertEquals(state.getChunk(i), 0);

			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			for (int i=0; i<15; i++)
				assertTrue(empty.get(i));
			assertFalse(empty.get(15));
			assertFalse(empty.get(16));
			for (int i=17; i<23; i++)
				assertTrue(empty.get(i));
			assertFalse(empty.get(23));
			assertFalse(empty.get(24));
			assertFalse(empty.get(25));
			for (int i=26; i<31; i++)
				assertTrue(empty.get(i));
			assertFalse(empty.get(31));
			assertFalse(empty.get(32));
			for (int i=33; i<=48; i++)
				assertTrue(empty.get(i));
			
			HashedBitSet playable = newContainerFlatState.playable();
			for (int i=0; i<7; i++)
				assertFalse(playable.get(i));
			for (int i=7; i<11; i++)
				assertTrue(playable.get(i));
			for (int i=11; i<14; i++)
				assertFalse(playable.get(i));
			assertTrue(playable.get(14));
			for (int i=15; i<17; i++)
				assertFalse(playable.get(i));
			for (int i=17; i<=19; i++)
				assertTrue(playable.get(i));
			assertFalse(playable.get(20));
			for (int i=21; i<=22; i++)
				assertTrue(playable.get(i));
			for (int i=23; i<26; i++)
				assertFalse(playable.get(i));
			assertTrue(playable.get(26));
			for (int i=27; i<29; i++)
				assertFalse(playable.get(i));
			for (int i=29; i<=30; i++)
				assertTrue(playable.get(i));
			for (int i=31; i<33; i++)
				assertFalse(playable.get(i));
			assertTrue(playable.get(33));
			for (int i=34; i<=36; i++)
				assertFalse(playable.get(i));
			for (int i=37; i<=40; i++)
				assertTrue(playable.get(i));
			for (int i=41; i<50; i++)
				assertFalse(playable.get(i));
		}
		else 
			fail();
		
		// first player's state
		ContainerState newContainerState1 = newContainerStates[1];
		if (newContainerState1 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState1;
			ContainerFlatState prevContainerFlatState = (ContainerFlatState) prevContainerStates[1];
			
			assertEquals(newContainerFlatState.who().internalState().numNonZeroChunks(), prevContainerFlatState.who().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.what().internalState().numNonZeroChunks(), prevContainerFlatState.what().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.count().internalState().numNonZeroChunks(), prevContainerFlatState.count().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.state().internalState().numNonZeroChunks(), prevContainerFlatState.state().internalState().numNonZeroChunks());
			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());

			HashedChunkSet who = newContainerFlatState.who();
			assertEquals(who.getChunk(0), 1);

			HashedChunkSet what = newContainerFlatState.what();
			assertEquals(what.getChunk(0), 2);

			HashedChunkSet count = newContainerFlatState.count();
			assertEquals(count.getChunk(0), 1);

			HashedChunkSet state = newContainerFlatState.state();
			assertEquals(state.internalState().numNonZeroChunks(), 0);
			
			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			assertTrue(empty.isEmpty());
			
			HashedBitSet playable = newContainerFlatState.playable();
			assertTrue(playable.internalState().isEmpty());
		}
		else 
			fail();
		
		// second player's state
		ContainerState newContainerState2 = newContainerStates[2];
		if (newContainerState2 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState2;
			ContainerFlatState prevContainerFlatState = (ContainerFlatState) prevContainerStates[2];
			
			assertEquals(newContainerFlatState.who().internalState().numNonZeroChunks(), prevContainerFlatState.who().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.what().internalState().numNonZeroChunks(), prevContainerFlatState.what().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.count().internalState().numNonZeroChunks(), prevContainerFlatState.count().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.state().internalState().numNonZeroChunks(), prevContainerFlatState.state().internalState().numNonZeroChunks());
			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());

			HashedChunkSet who = newContainerFlatState.who();
			assertEquals(who.getChunk(0), 2);

			HashedChunkSet what = newContainerFlatState.what();
			assertEquals(what.getChunk(0), 3);

			HashedChunkSet count = newContainerFlatState.count();
			assertEquals(count.getChunk(0), 1);

			HashedChunkSet state = newContainerFlatState.state();
			assertEquals(state.internalState().numNonZeroChunks(), 0);

			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			assertTrue(empty.isEmpty());
			
			HashedBitSet playable = newContainerFlatState.playable();
			assertTrue(playable.internalState().isEmpty());
		}
		else 
			fail();
	}
	
	/**
	 * Tests the intern state of the containers (board and hand's players), 
	 * which are the chunks, after two moves, both on edges.
	 */
	@Test
	public void testUpdateChunksAfterMultipleMovesWithEdges()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Equipment equipment = game.equipment();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		applyMove(context, 50, 21, 2);
		ContainerState[] prevContainerStates = updateBoard(context);
		
		// test
		ContainerState[] newContainerStates = context.state().containerStates();
		// Board's state
		ContainerState newContainerState0 = newContainerStates[0];
		if (newContainerState0 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState0;
			ContainerFlatState prevContainerFlatState = (ContainerFlatState) prevContainerStates[0];			

			List<Cell> adj1 = context.topology().cells().get(15).adjacent();
			List<Cell> adj2 = context.state().containerStates()[0].container().topology().cells().get(15).adjacent();
			assertEquals(adj1, adj2);
			
			assertEquals(newContainerFlatState.who().internalState().numNonZeroChunks(), prevContainerFlatState.who().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.what().internalState().numNonZeroChunks(), prevContainerFlatState.what().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.count().internalState().numNonZeroChunks(), prevContainerFlatState.count().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.state().internalState().numNonZeroChunks(), prevContainerFlatState.state().internalState().numNonZeroChunks());
			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());
						
			HashedChunkSet who = newContainerFlatState.who();
			for (int i=0; i<37; i++)
				assertEquals(who.getChunk(i), 0);
			assertEquals(who.getChunk(37), 2);
			assertEquals(who.getChunk(38), 1);
			for (int i=39; i<=80; i++)
				assertEquals(who.getChunk(i), 0);

			HashedChunkSet what = newContainerFlatState.what();
			for (int i=0; i<37; i++)
				assertEquals(what.getChunk(i), 0);
			assertEquals(what.getChunk(37), 3);
			assertEquals(what.getChunk(38), 2);
			for (int i=39; i<=41; i++)
				assertEquals(what.getChunk(i), 4);
			for (int i=42; i<=80; i++)
				assertEquals(what.getChunk(i), 0);

			HashedChunkSet count = newContainerFlatState.count();
			for (int i=0; i<37; i++)
				assertEquals(count.getChunk(i), 0);
			for (int i=37; i<=41; i++)
				assertEquals(count.getChunk(i), 1);
			for (int i=42; i<=80; i++)
				assertEquals(count.getChunk(i), 0);

			HashedChunkSet state = newContainerFlatState.state();
			for (int i=0; i<=82; i++)
				assertEquals(state.getChunk(i), 0);

			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			for (int i=0; i<37; i++)
				assertTrue(empty.get(i));
			for (int i=37; i<42; i++)
				assertFalse(empty.get(i));
			for (int i=42; i<=80; i++)
				assertTrue(empty.get(i));
			
			HashedBitSet playable = newContainerFlatState.playable();
			for (int i=0; i<27; i++)
				assertFalse(playable.get(i));
			for (int i=27; i<34; i++)
				assertTrue(playable.get(i));
			for (int i=34; i<36; i++)
				assertFalse(playable.get(i));
			assertTrue(playable.get(36));
			for (int i=37; i<42; i++)
				assertFalse(playable.get(i));
			assertTrue(playable.get(42));
			for (int i=43; i<45; i++)
				assertFalse(playable.get(i));
			for (int i=45; i<52; i++)
				assertTrue(playable.get(i));
			for (int i=52; i<=80; i++)
				assertFalse(playable.get(i));
		}
		else 
			fail();
		
		int[] newOffset = equipment.offset();
		for (int i=0; i<=80; i++)
			assertEquals(newOffset[i], i);
		assertEquals(newOffset[81], 0);
		assertEquals(newOffset[82], 0);
		
		int[] newContainerId = equipment.containerId();
		for (int i=0; i<=80; i++)
			assertEquals(newContainerId[i], 0);
		assertEquals(newContainerId[81], 1);
		assertEquals(newContainerId[82], 2);
				
		int[] newSitesFrom = equipment.sitesFrom();
		assertEquals(newSitesFrom[0], 0);
		assertEquals(newSitesFrom[1], 81);
		assertEquals(newSitesFrom[2], 82);
		
		TopologyElement topologyElement0 = game.equipment().containers()[0].topology().getGraphElements(SiteType.Cell).get(0);
		assertEquals(topologyElement0.index(), 0);
		TopologyElement topologyElement1 = game.equipment().containers()[1].topology().getGraphElements(SiteType.Cell).get(0);
		assertEquals(topologyElement1.index(), 81);
		TopologyElement topologyElement2 = game.equipment().containers()[2].topology().getGraphElements(SiteType.Cell).get(0);
		assertEquals(topologyElement2.index(), 82);
	}
	
	@Test
	public void aa()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		
		// test
		List<Move> movesDone = context.trial().generateCompleteMovesList();
		assertEquals(movesDone.get(0).getFromLocation().site(), 49);
		assertEquals(movesDone.get(0).getToLocation().site(), 49);

		assertEquals(movesDone.get(1).getFromLocation().site(), 50);
		assertEquals(movesDone.get(1).getToLocation().site(), 50);

		assertEquals(movesDone.get(2).getFromLocation().site(), 24);
		assertEquals(movesDone.get(2).getToLocation().site(), 24);

		assertEquals(movesDone.get(3).getFromLocation().site(), 25);
		assertEquals(movesDone.get(3).getToLocation().site(), 25);

		assertEquals(movesDone.get(4).getFromLocation().site(), 23);
		assertEquals(movesDone.get(4).getToLocation().site(), 23);

		assertEquals(movesDone.get(5).getFromLocation().site(), 49);
		assertEquals(movesDone.get(5).getToLocation().site(), 22);
	}
	
	/**
	 * Tests the content of the emptySites of the board after two moves on edges.
	 */
	@Test
	public void testEmptySitesContainer0()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		applyMove(context, 50, 21, 1);
		updateBoard(context);
		
		// test
		int [] emptySites = context.state().containerStates()[0].emptySites().sites();
		List<Integer> emptySitesIndexes = new ArrayList<Integer>();
		for (int i=0; i<emptySites.length; i++) 
			emptySitesIndexes.add(emptySites[i]);

		for (int i=0; i<37; i++)
			assertTrue(emptySitesIndexes.contains(i));
		for (int i=37; i<42; i++)
			assertFalse(emptySitesIndexes.contains(i));
		for (int i=42; i<=80; i++)
			assertTrue(emptySitesIndexes.contains(i));
	}
	
	/**
	 * Tests the perimeter of the board after one move on an edge.
	 */
	@Test
	public void testPerimeterAfter1EdgeMove()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);	
		List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());
		List<Integer> perimeterIndexes = new ArrayList<Integer>();
		for (int i=0; i<perimeter.size(); i++)
			perimeterIndexes.add(perimeter.get(i).index());
		
		// test
		for (int i=0; i<8; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=8; i<13; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=13; i<15; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=15; i<20; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=20; i<22; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=22; i<27; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=27; i<29; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=29; i<34; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=34; i<36; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=36; i<41; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=41; i<=48; i++)
			assertTrue(perimeterIndexes.contains(i));
	}
	
	/**
	 * Tests the perimeter of the board after two moves on edges.
	 */
	@Test
	public void testPerimeterAfter2EdgeMoves()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		applyMove(context, 50, 21, 1);
		updateBoard(context);		
		List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());
		List<Integer> perimeterIndexes = new ArrayList<Integer>();
		for (int i=0; i<perimeter.size(); i++)
			perimeterIndexes.add(perimeter.get(i).index());
		
		// test
		for (int i=0; i<10; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=10; i<17; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=17; i<19; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=19; i<26; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=26; i<28; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=28; i<35; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=35; i<37; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=37; i<44; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=44; i<46; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=46; i<53; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=53; i<55; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=55; i<62; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=62; i<64; i++)
			assertTrue(perimeterIndexes.contains(i));
		for (int i=64; i<71; i++)
			assertFalse(perimeterIndexes.contains(i));
		for (int i=71; i<=80; i++)
			assertTrue(perimeterIndexes.contains(i));
	}
	
	/**
	 * Tests the method that check if a move made is done on an edge (= the perimeter 
	 * of the board), after multiple moves, firsts 2 on edges and last one not on an edge.
	 */
	@Test
	public void testIsTouchingEdgeAfterMultipleMoves()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		applyMove(context, 50, 21, 1);
		updateBoard(context);		
		List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());
		
		// test
		for (int i=0; i<10; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=10; i<17; i++)
			assertFalse(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=17; i<19; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=19; i<26; i++)
			assertFalse(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=26; i<28; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=28; i<35; i++)
			assertFalse(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=35; i<37; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=37; i<44; i++)
			assertFalse(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=44; i<46; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=46; i<53; i++)
			assertFalse(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=53; i<55; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=55; i<62; i++)
			assertFalse(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=62; i<64; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=64; i<71; i++)
			assertFalse(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
		for (int i=71; i<=80; i++)
			assertTrue(GrowingBoard.isTouchingEdge(perimeter, getMoveMove(81, i, 1).to()));
	}
}
