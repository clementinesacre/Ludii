package games;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import app.move.GrowingBoard;
import game.Game;
import game.equipment.Equipment;
import game.equipment.container.Container;
import game.equipment.container.board.Boardless;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.util.directions.CompassDirection;
import game.util.directions.DirectionFacing;
import game.util.graph.Perimeter;
import game.util.graph.Trajectories;
import gnu.trove.list.array.TIntArrayList;
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
		context.trial().setNumSubmovesPlayed(context.trial().numSubmovesPlayed() + 1); //TODO ??
	}
	
	/**
	 * Call all needed methods to update the board.
	 * 
	 * @param context context on which to update the board.
	 * @return list of the containers (like board and hand's players) before updating the board.
	 */
	public ContainerState[] updateBoard(Context context)
	{		
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
	 * Tests the indexes used inside the equipment after 1 edge move.
	 */
	@Test
	public void testUpdateIndexesInsideEquipmentAfter1Move()
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
	 * Tests the indexes used inside the equipment after 2 edge moves.
	 */
	@Test
	public void testUpdateIndexesInsideEquipmentAfter2Move()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Equipment equipment = game.equipment();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		applyMove(context, 50, 21, 2);
		updateBoard(context);
		
		// test
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
		List<Integer> perimeterIndexes = perimeter.stream().map(TopologyElement::index).collect(Collectors.toList());
		
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
	
	/**
	 * Tests the pre-computed values stored in the Topology object. 
	 * After one move on an edge.
	 */
	@Test
	public void testTopologyAfter1MoveEdge()
	{
		Context context = initGame();

		System.out.println("GrowingBoardTest.java testTypology() slash : "+context.topology().connectivities());
	
		applyMove(context, 25, 10, 1);
		updateBoard(context);

		System.out.println("GrowingBoardTest.java testTypology() slash : "+context.topology().distanceToRegions());
		
		// Corners
		List<TopologyElement> cornerTypeVertex = context.topology().corners().get(SiteType.Vertex);
		List<Integer> cornerTypeVertexIndexes = cornerTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerTypeVertexIndexes.contains(0));
		assertTrue(cornerTypeVertexIndexes.contains(7));
		assertTrue(cornerTypeVertexIndexes.contains(56));
		assertTrue(cornerTypeVertexIndexes.contains(63));
		assertEquals(cornerTypeVertex.size(), 4);
		List<TopologyElement> cornerTypeEdge = context.topology().corners().get(SiteType.Edge);
		List<Integer> cornerTypeEdgeIndexes = cornerTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerTypeEdgeIndexes.contains(0));
		assertTrue(cornerTypeEdgeIndexes.contains(6));
		assertTrue(cornerTypeEdgeIndexes.contains(7));
		assertTrue(cornerTypeEdgeIndexes.contains(14));
		assertTrue(cornerTypeEdgeIndexes.contains(97));
		assertTrue(cornerTypeEdgeIndexes.contains(104));
		assertTrue(cornerTypeEdgeIndexes.contains(105));
		assertTrue(cornerTypeEdgeIndexes.contains(111));
		assertEquals(cornerTypeEdge.size(), 8);
		List<TopologyElement> cornerTypeCell = context.topology().corners().get(SiteType.Cell);
		List<Integer> cornerTypeCellIndexes = cornerTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerTypeCellIndexes.contains(0));
		assertTrue(cornerTypeCellIndexes.contains(6));
		assertTrue(cornerTypeCellIndexes.contains(42));
		assertTrue(cornerTypeCellIndexes.contains(48));
		assertEquals(cornerTypeCell.size(), 4);
		
		// Corners convex
		List<TopologyElement> cornerConvexTypeVertex = context.topology().cornersConvex().get(SiteType.Vertex);
		List<Integer> cornerConvexTypeVertexIndexes = cornerConvexTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerConvexTypeVertexIndexes.contains(0));
		assertTrue(cornerConvexTypeVertexIndexes.contains(7));
		assertTrue(cornerConvexTypeVertexIndexes.contains(56));
		assertTrue(cornerConvexTypeVertexIndexes.contains(63));
		assertEquals(cornerConvexTypeVertex.size(), 4);
		List<TopologyElement> cornerConvexTypeEdge = context.topology().cornersConvex().get(SiteType.Edge);
		List<Integer> cornerConvexTypeEdgeIndexes = cornerConvexTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerConvexTypeEdgeIndexes.contains(0));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(6));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(7));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(14));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(97));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(104));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(105));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(111));
		assertEquals(cornerConvexTypeEdge.size(), 8);
		List<TopologyElement> cornerConvexTypeCell = context.topology().cornersConvex().get(SiteType.Cell);
		List<Integer> cornerConvexTypeCellIndexes = cornerConvexTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerConvexTypeCellIndexes.contains(0));
		assertTrue(cornerConvexTypeCellIndexes.contains(6));
		assertTrue(cornerConvexTypeCellIndexes.contains(42));
		assertTrue(cornerConvexTypeCellIndexes.contains(48));
		assertEquals(cornerConvexTypeCell.size(), 4);
		
		// Corners concave
		List<TopologyElement> cornerConcaveTypeVertex = context.topology().cornersConcave().get(SiteType.Vertex);
		assertEquals(cornerConcaveTypeVertex.size(), 0);
		List<TopologyElement> cornerConcaveTypeEdge = context.topology().cornersConcave().get(SiteType.Edge);
		assertEquals(cornerConcaveTypeEdge.size(), 0);
		List<TopologyElement> cornerConcaveTypeCell = context.topology().cornersConcave().get(SiteType.Cell);
		assertEquals(cornerConcaveTypeCell.size(), 0);
		
		// Major
		List<TopologyElement> majorTypeVertex = context.topology().major().get(SiteType.Vertex);
		assertEquals(majorTypeVertex.size(), 0);
		List<TopologyElement> majorTypeEdge = context.topology().major().get(SiteType.Edge);
		assertEquals(majorTypeEdge.size(), 0);
		List<TopologyElement> majorConcaveTypeCell = context.topology().major().get(SiteType.Cell);
		List<Integer> majorTypeCellIndexes = majorConcaveTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<49; i++)
			assertTrue(majorTypeCellIndexes.contains(i));
		assertEquals(majorConcaveTypeCell.size(), 49);
		
		// Corners minor
		List<TopologyElement> minorTypeVertex = context.topology().minor().get(SiteType.Vertex);
		assertEquals(minorTypeVertex.size(), 0);
		List<TopologyElement> minorTypeEdge = context.topology().minor().get(SiteType.Edge);
		assertEquals(minorTypeEdge.size(), 0);
		List<TopologyElement> minorTypeCell = context.topology().minor().get(SiteType.Cell);
		assertEquals(minorTypeCell.size(), 0);
		
		// Outer
		List<TopologyElement> outerTypeVertex = context.topology().outer().get(SiteType.Vertex);
		List<Integer> outerTypeVertexIndexes = outerTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(outerTypeVertexIndexes.contains(i));
		assertTrue(outerTypeVertexIndexes.contains(15));
		assertTrue(outerTypeVertexIndexes.contains(16));
		assertTrue(outerTypeVertexIndexes.contains(23));
		assertTrue(outerTypeVertexIndexes.contains(24));
		assertTrue(outerTypeVertexIndexes.contains(31));
		assertTrue(outerTypeVertexIndexes.contains(32));
		assertTrue(outerTypeVertexIndexes.contains(39));
		assertTrue(outerTypeVertexIndexes.contains(40));
		assertTrue(outerTypeVertexIndexes.contains(47));
		assertTrue(outerTypeVertexIndexes.contains(48));
		for (int i=55; i<=63; i++)
			assertTrue(outerTypeVertexIndexes.contains(i));
		assertEquals(outerTypeVertex.size(), 28);
		/*List<TopologyElement> outerTypeEdge = context.topology().outer().get(SiteType.Edge);
		List<Integer> outerTypeEdgeIndexes = outerTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(outerTypeEdgeIndexes.contains(i));
		assertTrue(outerTypeEdgeIndexes.contains(14));
		assertTrue(outerTypeEdgeIndexes.contains(22));
		assertTrue(outerTypeEdgeIndexes.contains(29));
		assertTrue(outerTypeEdgeIndexes.contains(37));
		assertTrue(outerTypeEdgeIndexes.contains(44));
		assertTrue(outerTypeEdgeIndexes.contains(52));
		assertTrue(outerTypeEdgeIndexes.contains(59));
		assertEquals(outerTypeEdge.size(), 8);*/
		List<TopologyElement> outerTypeCell = context.topology().outer().get(SiteType.Cell);
		List<Integer> outerTypeCellIndexes = outerTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=7; i++)
			assertTrue(outerTypeCellIndexes.contains(i));
		assertTrue(outerTypeCellIndexes.contains(13));
		assertTrue(outerTypeCellIndexes.contains(14));
		assertTrue(outerTypeCellIndexes.contains(20));
		assertTrue(outerTypeCellIndexes.contains(21));
		assertTrue(outerTypeCellIndexes.contains(27));
		assertTrue(outerTypeCellIndexes.contains(28));
		assertTrue(outerTypeCellIndexes.contains(34));
		assertTrue(outerTypeCellIndexes.contains(35));
		for (int i=41; i<=48; i++)
			assertTrue(outerTypeCellIndexes.contains(i));
		assertEquals(outerTypeCell.size(), 24);
		
		// Perimeter
		List<TopologyElement> perimeterTypeVertex = context.topology().perimeter().get(SiteType.Vertex);
		List<Integer> perimeterTypeVertexIndexes = perimeterTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(perimeterTypeVertexIndexes.contains(i));
		assertTrue(perimeterTypeVertexIndexes.contains(15));
		assertTrue(perimeterTypeVertexIndexes.contains(16));
		assertTrue(perimeterTypeVertexIndexes.contains(23));
		assertTrue(perimeterTypeVertexIndexes.contains(24));
		assertTrue(perimeterTypeVertexIndexes.contains(31));
		assertTrue(perimeterTypeVertexIndexes.contains(32));
		assertTrue(perimeterTypeVertexIndexes.contains(39));
		assertTrue(perimeterTypeVertexIndexes.contains(40));
		assertTrue(perimeterTypeVertexIndexes.contains(47));
		assertTrue(perimeterTypeVertexIndexes.contains(48));
		for (int i=55; i<=63; i++)
			assertTrue(perimeterTypeVertexIndexes.contains(i));
		assertEquals(perimeterTypeVertex.size(), 28);
		/*List<TopologyElement> perimeterTypeEdge = context.topology().perimeter.get(SiteType.Edge);
		List<Integer> perimeterTypeEdgeIndexes = perimeterTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(perimeterTypeEdgeIndexes.contains(i));
		assertTrue(perimeterTypeEdgeIndexes.contains(14));
		assertTrue(perimeterTypeEdgeIndexes.contains(22));
		assertTrue(perimeterTypeEdgeIndexes.contains(29));
		assertTrue(perimeterTypeEdgeIndexes.contains(37));
		assertTrue(perimeterTypeEdgeIndexes.contains(44));
		assertTrue(perimeterTypeEdgeIndexes.contains(52));
		assertTrue(perimeterTypeEdgeIndexes.contains(59));
		assertEquals(perimeterTypeEdge.size(), 8);*/
		List<TopologyElement> perimeterTypeCell = context.topology().perimeter().get(SiteType.Cell);
		List<Integer> perimeterTypeCellIndexes = perimeterTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=7; i++)
			assertTrue(perimeterTypeCellIndexes.contains(i));
		assertTrue(perimeterTypeCellIndexes.contains(13));
		assertTrue(perimeterTypeCellIndexes.contains(14));
		assertTrue(perimeterTypeCellIndexes.contains(20));
		assertTrue(perimeterTypeCellIndexes.contains(21));
		assertTrue(perimeterTypeCellIndexes.contains(27));
		assertTrue(perimeterTypeCellIndexes.contains(28));
		assertTrue(perimeterTypeCellIndexes.contains(34));
		assertTrue(perimeterTypeCellIndexes.contains(35));
		for (int i=41; i<=48; i++)
			assertTrue(perimeterTypeCellIndexes.contains(i));
		assertEquals(perimeterTypeCell.size(), 24);
		
		// Inner
		List<TopologyElement> innerTypeVertex = context.topology().inner().get(SiteType.Vertex);
		List<Integer> innerTypeVertexIndexes = innerTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=9; i<15; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=17; i<23; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=25; i<31; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=33; i<39; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=41; i<47; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=49; i<55; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		assertEquals(innerTypeVertex.size(), 36);
		/*List<TopologyElement> innerTypeEdge = context.topology().inner.get(SiteType.Edge);
		List<Integer> innerTypeEdgeIndexes = innerTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(innerTypeEdgeIndexes.contains(i));
		assertTrue(innerTypeEdgeIndexes.contains(14));
		assertTrue(innerTypeEdgeIndexes.contains(22));
		assertTrue(innerTypeEdgeIndexes.contains(29));
		assertTrue(innerTypeEdgeIndexes.contains(37));
		assertTrue(innerTypeEdgeIndexes.contains(44));
		assertTrue(innerTypeEdgeIndexes.contains(52));
		assertTrue(innerTypeEdgeIndexes.contains(59));
		assertEquals(innerTypeEdge.size(), 8);*/
		List<TopologyElement> innerTypeCell = context.topology().inner().get(SiteType.Cell);
		List<Integer> innerTypeCellIndexes = innerTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=8; i<12; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		for (int i=15; i<19; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		for (int i=22; i<26; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		for (int i=29; i<33; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		for (int i=36; i<40; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		assertEquals(innerTypeCell.size(), 25);
		
		// Interlayer
		List<TopologyElement> interlayerConcaveTypeVertex = context.topology().interlayer().get(SiteType.Vertex);
		assertEquals(interlayerConcaveTypeVertex.size(), 0);
		List<TopologyElement> interlayerConcaveTypeEdge = context.topology().interlayer().get(SiteType.Edge);
		assertEquals(interlayerConcaveTypeEdge.size(), 0);
		List<TopologyElement> interlayerConcaveTypeCell = context.topology().interlayer().get(SiteType.Cell);
		assertEquals(interlayerConcaveTypeCell.size(), 0);
		
		// Top
		List<TopologyElement> topTypeVertex = context.topology().top().get(SiteType.Vertex);
		List<Integer> topTypeVertexIndexes = topTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=56; i<=63; i++)
			assertTrue(topTypeVertexIndexes.contains(i));
		assertEquals(topTypeVertex.size(), 8);
		List<TopologyElement> topTypeEdge = context.topology().top().get(SiteType.Edge);
		List<Integer> topTypeEdgeIndexes = topTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=105; i<=111; i++)
			assertTrue(topTypeEdgeIndexes.contains(i));
		assertEquals(topTypeEdge.size(), 7);
		List<TopologyElement> topTypeCell = context.topology().top().get(SiteType.Cell);
		List<Integer> topTypeCellIndexes = topTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=42; i<=48; i++)
			assertTrue(topTypeCellIndexes.contains(i));
		assertEquals(topTypeCell.size(), 7);

		// Left
		List<TopologyElement> leftTypeVertex = context.topology().left().get(SiteType.Vertex);
		List<Integer> leftTypeVertexIndexes = leftTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=56; i+=8)
			assertTrue(leftTypeVertexIndexes.contains(i));
		assertEquals(leftTypeVertex.size(), 8);
		List<TopologyElement> leftTypeEdge = context.topology().left().get(SiteType.Edge);
		List<Integer> leftTypeEdgeIndexes = leftTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=7; i<=97; i+=15)
			assertTrue(leftTypeEdgeIndexes.contains(i));
		assertEquals(leftTypeEdge.size(), 7);
		List<TopologyElement> leftTypeCell = context.topology().left().get(SiteType.Cell);
		List<Integer> leftTypeCellIndexes = leftTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=42; i+=7)
			assertTrue(leftTypeCellIndexes.contains(i));
		assertEquals(leftTypeCell.size(), 7);
		
		// Right
		List<TopologyElement> rightTypeVertex = context.topology().right().get(SiteType.Vertex);
		List<Integer> rightTypeVertexIndexes = rightTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=7; i<=63; i+=8)
			assertTrue(rightTypeVertexIndexes.contains(i));
		assertEquals(rightTypeVertex.size(), 8);
		List<TopologyElement> rightTypeEdge = context.topology().right().get(SiteType.Edge);
		List<Integer> rightTypeEdgeIndexes = rightTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=14; i<=104; i+=15)
			assertTrue(rightTypeEdgeIndexes.contains(i));
		assertEquals(rightTypeEdge.size(), 7);
		List<TopologyElement> rightTypeCell = context.topology().right().get(SiteType.Cell);
		List<Integer> rightTypeCellIndexes = rightTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=6; i<=48; i+=7)
			assertTrue(rightTypeCellIndexes.contains(i));
		assertEquals(rightTypeCell.size(), 7);
		
		// Bottom
		List<TopologyElement> bottomTypeVertex = context.topology().bottom().get(SiteType.Vertex);
		List<Integer> bottomTypeVertexIndexes = bottomTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=7; i++)
			assertTrue(bottomTypeVertexIndexes.contains(i));
		assertEquals(bottomTypeVertex.size(), 8);
		List<TopologyElement> bottomTypeEdge = context.topology().bottom().get(SiteType.Edge);
		List<Integer> bottomTypeEdgeIndexes = bottomTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=6; i++)
			assertTrue(bottomTypeEdgeIndexes.contains(i));
		assertEquals(bottomTypeEdge.size(), 7);
		List<TopologyElement> bottomTypeCell = context.topology().bottom().get(SiteType.Cell);
		List<Integer> bottomTypeCellIndexes = bottomTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=6; i++)
			assertTrue(bottomTypeCellIndexes.contains(i));
		assertEquals(bottomTypeCell.size(), 7);
		
		// Centre
		List<TopologyElement> centreTypeVertex = context.topology().centre().get(SiteType.Vertex);
		List<Integer> centreTypeVertexIndexes = centreTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(centreTypeVertexIndexes.contains(27));
		assertTrue(centreTypeVertexIndexes.contains(28));
		assertTrue(centreTypeVertexIndexes.contains(35));
		assertTrue(centreTypeVertexIndexes.contains(36));
		assertEquals(centreTypeVertex.size(), 4);
		List<TopologyElement> centreTypeEdge = context.topology().centre().get(SiteType.Edge);
		List<Integer> centreTypeEdgeIndexes = centreTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(centreTypeEdgeIndexes.contains(48));
		assertTrue(centreTypeEdgeIndexes.contains(55));
		assertTrue(centreTypeEdgeIndexes.contains(56));
		assertTrue(centreTypeEdgeIndexes.contains(63));
		assertEquals(centreTypeEdge.size(), 4);
		List<TopologyElement> centreTypeCell = context.topology().centre().get(SiteType.Cell);
		List<Integer> centreTypeCellIndexes = centreTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(centreTypeCellIndexes.contains(24));
		assertEquals(centreTypeCell.size(), 1);
		
		// Columns
		List<List<TopologyElement>> columnsTypeVertex = context.topology().columns().get(SiteType.Vertex);
		for (int i=0; i<8; i++) 
		{
			List<Integer> columnsTypeVertexIndexes = columnsTypeVertex.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=i; j<64; j+=8)
				assertTrue(columnsTypeVertexIndexes.contains(i));
			assertEquals(columnsTypeVertexIndexes.size(), 8);
		}
		assertEquals(columnsTypeVertex.size(), 8);
		List<List<TopologyElement>> columnsTypeEdge = context.topology().columns().get(SiteType.Edge);
		for (int i=0; i<15; i+=2) 
		{
			List<Integer> columnsTypeEdgeIndexes1 = columnsTypeEdge.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=(i/2)+7; j<106; j+=15)
				assertTrue(columnsTypeEdgeIndexes1.contains(j));
			assertEquals(columnsTypeEdgeIndexes1.size(), 7);
			if (i+1 < 15) 
			{
				List<Integer> columnsTypeEdgeIndexes2 = columnsTypeEdge.get(i+1).stream().map(TopologyElement::index).collect(Collectors.toList());
				for (int j=i/2; j<106; j+=15)
					assertTrue(columnsTypeEdgeIndexes2.contains(j));
				assertEquals(columnsTypeEdgeIndexes2.size(), 8);
			}
		}
		assertEquals(columnsTypeEdge.size(), 15);
		List<List<TopologyElement>> columnsTypeCell = context.topology().columns().get(SiteType.Cell);
		for (int i=0; i<7; i++) 
		{
			List<Integer> columnsTypeCellIndexes = columnsTypeCell.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=i; j<49; j+=7)
				assertTrue(columnsTypeCellIndexes.contains(j));
			assertEquals(columnsTypeCellIndexes.size(), 7);
		}
		assertEquals(columnsTypeCell.size(), 7);
		
		// Rows
		List<List<TopologyElement>> rowsTypeVertex = context.topology().rows().get(SiteType.Vertex);
		for (int i=0; i<8; i++) 
		{
			List<Integer> rowsTypeVertexIndexes = rowsTypeVertex.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=i*8; j<8+(i*8); j++)
				assertTrue(rowsTypeVertexIndexes.contains(j));
			assertEquals(rowsTypeVertexIndexes.size(), 8);
		}
		assertEquals(rowsTypeVertex.size(), 8);
		List<List<TopologyElement>> rowsTypeEdge = context.topology().rows().get(SiteType.Edge);
		int index = 0;
		for (int i=0; i<15; i+=2) 
		{
			List<Integer> rowsTypeEdgeIndexes1 = rowsTypeEdge.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=i*15; j<7; j++) 
			{
				assertTrue(rowsTypeEdgeIndexes1.contains(index));
				index ++;
			}
			assertEquals(rowsTypeEdgeIndexes1.size(), 7);
			if (i+1 < 15) {
				List<Integer> rowsTypeEdgeIndexes2 = rowsTypeEdge.get(i+1).stream().map(TopologyElement::index).collect(Collectors.toList());
				for (int j=(i*15)+7; j<8; j++) 
				{
					assertTrue(rowsTypeEdgeIndexes2.contains(index));
					index ++;
				}
				assertEquals(rowsTypeEdgeIndexes2.size(), 8);
			}
		}
		assertEquals(rowsTypeEdge.size(), 15);
		List<List<TopologyElement>> rowsTypeCell = context.topology().rows().get(SiteType.Cell);
		for (int i=0; i<7; i++) 
		{
			List<Integer> rowsTypeCellIndexes = rowsTypeCell.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=i*7; j<7+(i*7); j++)
				assertTrue(rowsTypeCellIndexes.contains(j));
			assertEquals(rowsTypeCellIndexes.size(), 7);
		}
		assertEquals(rowsTypeCell.size(), 7);

		// Sides
		Map<DirectionFacing, List<TopologyElement>> sidesTypeVertex = context.topology().sides().get(SiteType.Vertex);
		assertTrue(sidesTypeVertex.get(CompassDirection.WNW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.SSW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.NE).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.NW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.SE).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.SW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.ENE).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.SSE).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.ESE).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.WSW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.NNW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.NNE).isEmpty());
		List<TopologyElement> sideNTypeVertex = sidesTypeVertex.get(CompassDirection.N);
		List<Integer> sideNTypeVertexIndexes = sideNTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=56; i<=63; i++)
			assertTrue(sideNTypeVertexIndexes.contains(i));
		assertEquals(sideNTypeVertexIndexes.size(), 8);
		List<TopologyElement> sideETypeVertex = sidesTypeVertex.get(CompassDirection.E);
		List<Integer> sideETypeVertexIndexes = sideETypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=7; i<=63; i+=8)
			assertTrue(sideETypeVertexIndexes.contains(i));
		assertEquals(sideETypeVertexIndexes.size(), 8);
		List<TopologyElement> sideSTypeVertex = sidesTypeVertex.get(CompassDirection.S);
		List<Integer> sideSTypeVertexIndexes = sideSTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=7; i++)
			assertTrue(sideSTypeVertexIndexes.contains(i));
		assertEquals(sideSTypeVertexIndexes.size(), 8);
		List<TopologyElement> sideWTypeVertex = sidesTypeVertex.get(CompassDirection.W);
		List<Integer> sideWTypeVertexIndexes = sideWTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=56; i+=8)
			assertTrue(sideWTypeVertexIndexes.contains(i));
		assertEquals(sideWTypeVertexIndexes.size(), 8);
		assertEquals(sidesTypeVertex.size(), 16);
		Map<DirectionFacing, List<TopologyElement>> sidesTypeEdge = context.topology().sides().get(SiteType.Edge);
		assertTrue(sidesTypeEdge.get(CompassDirection.WNW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.SSW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.NE).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.NW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.SE).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.SW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.ENE).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.SSE).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.ESE).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.WSW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.NNW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.NNE).isEmpty());
		List<TopologyElement> sideNTypeEdge = sidesTypeEdge.get(CompassDirection.N);
		List<Integer> sideNTypeEdgeIndexes = sideNTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=97; i<=111; i++)
			assertTrue(sideNTypeEdgeIndexes.contains(i));
		assertEquals(sideNTypeEdgeIndexes.size(), 15);
		List<TopologyElement> sideETypeEdge = sidesTypeEdge.get(CompassDirection.E);
		List<Integer> sideETypeEdgeIndexes = sideETypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=14; i<=104; i+=15)
			assertTrue(sideETypeEdgeIndexes.contains(i));
		for (int i=6; i<=111; i+=15)
			assertTrue(sideETypeEdgeIndexes.contains(i));
		assertEquals(sideETypeEdgeIndexes.size(), 15);
		List<TopologyElement> sideSTypeEdge = sidesTypeEdge.get(CompassDirection.S);
		List<Integer> sideSTypeEdgeIndexes = sideSTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=14; i++)
			assertTrue(sideSTypeEdgeIndexes.contains(i));
		assertEquals(sideSTypeEdgeIndexes.size(), 15);
		List<TopologyElement> sideWTypeEdge = sidesTypeEdge.get(CompassDirection.W);
		List<Integer> sideWTypeEdgeIndexes = sideWTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=7; i<=97; i+=15)
			assertTrue(sideWTypeEdgeIndexes.contains(i));
		for (int i=0; i<=105; i+=15)
			assertTrue(sideWTypeEdgeIndexes.contains(i));
		assertEquals(sideWTypeEdgeIndexes.size(), 15);
		assertEquals(sidesTypeEdge.size(), 16);
		Map<DirectionFacing, List<TopologyElement>> sidesTypeCell = context.topology().sides().get(SiteType.Cell);
		assertTrue(sidesTypeCell.get(CompassDirection.WNW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.SSW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.NE).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.NW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.SE).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.SW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.ENE).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.SSE).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.ESE).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.WSW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.NNW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.NNE).isEmpty());
		List<TopologyElement> sideNTypeCell = sidesTypeCell.get(CompassDirection.N);
		List<Integer> sideNTypeCellIndexes = sideNTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=42; i<=48; i++)
			assertTrue(sideNTypeCellIndexes.contains(i));
		assertEquals(sideNTypeCellIndexes.size(), 7);
		List<TopologyElement> sideETypeCell = sidesTypeCell.get(CompassDirection.E);
		List<Integer> sideETypeCellIndexes = sideETypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=6; i<=48; i+=7)
			assertTrue(sideETypeCellIndexes.contains(i));
		assertEquals(sideETypeCellIndexes.size(), 7);
		List<TopologyElement> sideSTypeCell = sidesTypeCell.get(CompassDirection.S);
		List<Integer> sideSTypeCellIndexes = sideSTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=6; i++)
			assertTrue(sideSTypeCellIndexes.contains(i));
		assertEquals(sideSTypeCellIndexes.size(), 7);
		List<TopologyElement> sideWTypeCell = sidesTypeCell.get(CompassDirection.W);
		List<Integer> sideWTypeCellIndexes = sideWTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=42; i+=7)
			assertTrue(sideWTypeCellIndexes.contains(i));
		assertEquals(sideWTypeCellIndexes.size(), 7);
		assertEquals(sidesTypeCell.size(), 16);
		
		// Axials
		assertEquals(context.topology().axials().get(SiteType.Vertex).size(), 0);
		List<TopologyElement> axialsTypeEdge = context.topology().axials().get(SiteType.Edge);
		List<Integer> axialsTypeEdgeIndexes = axialsTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<112; i++)
			assertTrue(axialsTypeEdgeIndexes.contains(i));
		assertEquals(axialsTypeEdge.size(), 112);
		assertEquals(context.topology().axials().get(SiteType.Cell).size(), 0);
		
		// Horizontal
		assertEquals(context.topology().horizontal().get(SiteType.Vertex).size(), 0);
		List<TopologyElement> horizontalTypeEdge = context.topology().horizontal().get(SiteType.Edge);
		List<Integer> horizontalTypeEdgeIndexes = horizontalTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<8; i++)
			for (int j=0+(i*15); j<7+(i*15); j++)
				assertTrue(horizontalTypeEdgeIndexes.contains(j));
		assertEquals(horizontalTypeEdge.size(), 56);
		assertEquals(context.topology().horizontal().get(SiteType.Cell).size(), 0);
		
		// Vertical
		assertEquals(context.topology().vertical().get(SiteType.Vertex).size(), 0);
		List<TopologyElement> verticalTypeEdge = context.topology().vertical().get(SiteType.Edge);
		List<Integer> verticalTypeEdgeIndexes = verticalTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<7; i++)
			for (int j=7+(i*15); j<14+(i*15); j++)
				assertTrue(verticalTypeEdgeIndexes.contains(j));
		assertEquals(verticalTypeEdge.size(), 56);
		assertEquals(context.topology().vertical().get(SiteType.Cell).size(), 0);
	}
	
	/**
	 * Tests the pre-computed values stored in the Topology object. 
	 * After two moves, each on an edge.
	 */
	@Test
	public void testTopologyAfter2MovesEdge()
	{
		Context context = initGame();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		applyMove(context, 50, 21, 2);
		updateBoard(context);
		
		// Corners
		List<TopologyElement> cornerTypeVertex = context.topology().corners().get(SiteType.Vertex);
		List<Integer> cornerTypeVertexIndexes = cornerTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerTypeVertexIndexes.contains(0));
		assertTrue(cornerTypeVertexIndexes.contains(9));
		assertTrue(cornerTypeVertexIndexes.contains(90));
		assertTrue(cornerTypeVertexIndexes.contains(99));
		assertEquals(cornerTypeVertex.size(), 4);
		List<TopologyElement> cornerTypeEdge = context.topology().corners().get(SiteType.Edge);
		List<Integer> cornerTypeEdgeIndexes = cornerTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerTypeEdgeIndexes.contains(0));
		assertTrue(cornerTypeEdgeIndexes.contains(8));
		assertTrue(cornerTypeEdgeIndexes.contains(9));
		assertTrue(cornerTypeEdgeIndexes.contains(18));
		assertTrue(cornerTypeEdgeIndexes.contains(161));
		assertTrue(cornerTypeEdgeIndexes.contains(170));
		assertTrue(cornerTypeEdgeIndexes.contains(171));
		assertTrue(cornerTypeEdgeIndexes.contains(179));
		assertEquals(cornerTypeEdge.size(), 8);
		List<TopologyElement> cornerTypeCell = context.topology().corners().get(SiteType.Cell);
		List<Integer> cornerTypeCellIndexes = cornerTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerTypeCellIndexes.contains(0));
		assertTrue(cornerTypeCellIndexes.contains(8));
		assertTrue(cornerTypeCellIndexes.contains(72));
		assertTrue(cornerTypeCellIndexes.contains(80));
		assertEquals(cornerTypeCell.size(), 4);
		
		// Corners convex
		List<TopologyElement> cornerConvexTypeVertex = context.topology().corners().get(SiteType.Vertex);
		List<Integer> cornerConvexTypeVertexIndexes = cornerConvexTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerConvexTypeVertexIndexes.contains(0));
		assertTrue(cornerConvexTypeVertexIndexes.contains(9));
		assertTrue(cornerConvexTypeVertexIndexes.contains(90));
		assertTrue(cornerConvexTypeVertexIndexes.contains(99));
		assertEquals(cornerConvexTypeVertex.size(), 4);
		List<TopologyElement> cornerConvexTypeEdge = context.topology().corners().get(SiteType.Edge);
		List<Integer> cornerConvexTypeEdgeIndexes = cornerConvexTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerConvexTypeEdgeIndexes.contains(0));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(8));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(9));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(18));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(161));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(170));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(171));
		assertTrue(cornerConvexTypeEdgeIndexes.contains(179));
		assertEquals(cornerConvexTypeEdge.size(), 8);
		List<TopologyElement> cornerTConvexypeCell = context.topology().corners().get(SiteType.Cell);
		List<Integer> cornerConvexTypeCellIndexes = cornerTConvexypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(cornerConvexTypeCellIndexes.contains(0));
		assertTrue(cornerConvexTypeCellIndexes.contains(8));
		assertTrue(cornerConvexTypeCellIndexes.contains(72));
		assertTrue(cornerConvexTypeCellIndexes.contains(80));
		assertEquals(cornerTConvexypeCell.size(), 4);
		
		// Corners concave
		List<TopologyElement> cornerConcaveTypeVertex = context.topology().cornersConcave().get(SiteType.Vertex);
		assertEquals(cornerConcaveTypeVertex.size(), 0);
		List<TopologyElement> cornerConcaveTypeEdge = context.topology().cornersConcave().get(SiteType.Edge);
		assertEquals(cornerConcaveTypeEdge.size(), 0);
		List<TopologyElement> cornerConcaveTypeCell = context.topology().cornersConcave().get(SiteType.Cell);
		assertEquals(cornerConcaveTypeCell.size(), 0);
		
		// Major
		List<TopologyElement> majorTypeVertex = context.topology().major().get(SiteType.Vertex);
		assertEquals(majorTypeVertex.size(), 0);
		List<TopologyElement> majorTypeEdge = context.topology().major().get(SiteType.Edge);
		assertEquals(majorTypeEdge.size(), 0);
		List<TopologyElement> majorConcaveTypeCell = context.topology().major().get(SiteType.Cell);
		List<Integer> majorTypeCellIndexes = majorConcaveTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<81; i++)
			assertTrue(majorTypeCellIndexes.contains(i));
		assertEquals(majorConcaveTypeCell.size(), 81);
		
		// Corners minor
		List<TopologyElement> minorTypeVertex = context.topology().minor().get(SiteType.Vertex);
		assertEquals(minorTypeVertex.size(), 0);
		List<TopologyElement> minorTypeEdge = context.topology().minor().get(SiteType.Edge);
		assertEquals(minorTypeEdge.size(), 0);
		List<TopologyElement> minorTypeCell = context.topology().minor().get(SiteType.Cell);
		assertEquals(minorTypeCell.size(), 0);
		
		// Outer
		List<TopologyElement> outerTypeVertex = context.topology().outer().get(SiteType.Vertex);
		List<Integer> outerTypeVertexIndexes = outerTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=10; i++)
			assertTrue(outerTypeVertexIndexes.contains(i));
		assertTrue(outerTypeVertexIndexes.contains(19));
		assertTrue(outerTypeVertexIndexes.contains(20));
		assertTrue(outerTypeVertexIndexes.contains(29));
		assertTrue(outerTypeVertexIndexes.contains(30));
		assertTrue(outerTypeVertexIndexes.contains(39));
		assertTrue(outerTypeVertexIndexes.contains(40));
		assertTrue(outerTypeVertexIndexes.contains(49));
		assertTrue(outerTypeVertexIndexes.contains(50));
		assertTrue(outerTypeVertexIndexes.contains(59));
		assertTrue(outerTypeVertexIndexes.contains(60));
		assertTrue(outerTypeVertexIndexes.contains(69));
		assertTrue(outerTypeVertexIndexes.contains(70));
		assertTrue(outerTypeVertexIndexes.contains(79));
		assertTrue(outerTypeVertexIndexes.contains(80));
		for (int i=89; i<=99; i++)
			assertTrue(outerTypeVertexIndexes.contains(i));
		assertEquals(outerTypeVertex.size(), 36);
		/*List<TopologyElement> outerTypeEdge = context.topology().outer.get(SiteType.Edge);
		List<Integer> outerTypeEdgeIndexes = outerTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(outerTypeEdgeIndexes.contains(i));
		assertTrue(outerTypeEdgeIndexes.contains(14));
		assertTrue(outerTypeEdgeIndexes.contains(22));
		assertTrue(outerTypeEdgeIndexes.contains(29));
		assertTrue(outerTypeEdgeIndexes.contains(37));
		assertTrue(outerTypeEdgeIndexes.contains(44));
		assertTrue(outerTypeEdgeIndexes.contains(52));
		assertTrue(outerTypeEdgeIndexes.contains(59));
		assertEquals(outerTypeEdge.size(), 8);*/
		List<TopologyElement> outerTypeCell = context.topology().outer().get(SiteType.Cell);
		List<Integer> outerTypeCellIndexes = outerTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=9; i++)
			assertTrue(outerTypeCellIndexes.contains(i));
		assertTrue(outerTypeCellIndexes.contains(17));
		assertTrue(outerTypeCellIndexes.contains(18));
		assertTrue(outerTypeCellIndexes.contains(26));
		assertTrue(outerTypeCellIndexes.contains(27));
		assertTrue(outerTypeCellIndexes.contains(35));
		assertTrue(outerTypeCellIndexes.contains(36));
		assertTrue(outerTypeCellIndexes.contains(44));
		assertTrue(outerTypeCellIndexes.contains(45));
		assertTrue(outerTypeCellIndexes.contains(53));
		assertTrue(outerTypeCellIndexes.contains(54));
		assertTrue(outerTypeCellIndexes.contains(62));
		assertTrue(outerTypeCellIndexes.contains(63));
		for (int i=71; i<=80; i++)
			assertTrue(outerTypeCellIndexes.contains(i));
		assertEquals(outerTypeCell.size(), 32);
		
		// Perimeter
		List<TopologyElement> perimeterTypeVertex = context.topology().perimeter().get(SiteType.Vertex);
		List<Integer> perimeterTypeVertexIndexes = perimeterTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=10; i++)
			assertTrue(perimeterTypeVertexIndexes.contains(i));
		assertTrue(perimeterTypeVertexIndexes.contains(19));
		assertTrue(perimeterTypeVertexIndexes.contains(20));
		assertTrue(perimeterTypeVertexIndexes.contains(29));
		assertTrue(perimeterTypeVertexIndexes.contains(30));
		assertTrue(perimeterTypeVertexIndexes.contains(39));
		assertTrue(perimeterTypeVertexIndexes.contains(40));
		assertTrue(perimeterTypeVertexIndexes.contains(49));
		assertTrue(perimeterTypeVertexIndexes.contains(50));
		assertTrue(perimeterTypeVertexIndexes.contains(59));
		assertTrue(perimeterTypeVertexIndexes.contains(60));
		assertTrue(perimeterTypeVertexIndexes.contains(69));
		assertTrue(perimeterTypeVertexIndexes.contains(70));
		assertTrue(perimeterTypeVertexIndexes.contains(79));
		assertTrue(perimeterTypeVertexIndexes.contains(80));
		for (int i=89; i<=99; i++)
			assertTrue(perimeterTypeVertexIndexes.contains(i));
		assertEquals(perimeterTypeVertex.size(), 36);
		/*List<TopologyElement> perimeterTypeEdge = context.topology().perimeter.get(SiteType.Edge);
		List<Integer> perimeterTypeEdgeIndexes = perimeterTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(perimeterTypeEdgeIndexes.contains(i));
		assertTrue(perimeterTypeEdgeIndexes.contains(14));
		assertTrue(perimeterTypeEdgeIndexes.contains(22));
		assertTrue(perimeterTypeEdgeIndexes.contains(29));
		assertTrue(perimeterTypeEdgeIndexes.contains(37));
		assertTrue(perimeterTypeEdgeIndexes.contains(44));
		assertTrue(perimeterTypeEdgeIndexes.contains(52));
		assertTrue(perimeterTypeEdgeIndexes.contains(59));
		assertEquals(perimeterTypeEdge.size(), 8);*/
		List<TopologyElement> perimeterTypeCell = context.topology().perimeter().get(SiteType.Cell);
		List<Integer> perimeterTypeCellIndexes = perimeterTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=9; i++)
			assertTrue(perimeterTypeCellIndexes.contains(i));
		assertTrue(perimeterTypeCellIndexes.contains(17));
		assertTrue(perimeterTypeCellIndexes.contains(18));
		assertTrue(perimeterTypeCellIndexes.contains(26));
		assertTrue(perimeterTypeCellIndexes.contains(27));
		assertTrue(perimeterTypeCellIndexes.contains(35));
		assertTrue(perimeterTypeCellIndexes.contains(36));
		assertTrue(perimeterTypeCellIndexes.contains(44));
		assertTrue(perimeterTypeCellIndexes.contains(45));
		assertTrue(perimeterTypeCellIndexes.contains(53));
		assertTrue(perimeterTypeCellIndexes.contains(54));
		assertTrue(perimeterTypeCellIndexes.contains(62));
		assertTrue(perimeterTypeCellIndexes.contains(63));
		for (int i=71; i<=80; i++)
			assertTrue(perimeterTypeCellIndexes.contains(i));
		assertEquals(perimeterTypeCell.size(), 32);
		
		// Inner
		List<TopologyElement> innerTypeVertex = context.topology().inner().get(SiteType.Vertex);
		List<Integer> innerTypeVertexIndexes = innerTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=11; i<19; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=21; i<29; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=31; i<39; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=41; i<49; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=51; i<59; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=61; i<69; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=71; i<79; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		for (int i=81; i<89; i++)
			assertTrue(innerTypeVertexIndexes.contains(i));
		assertEquals(innerTypeVertex.size(), 64);
		/*List<TopologyElement> innerTypeEdge = context.topology().inner.get(SiteType.Edge);
		List<Integer> innerTypeEdgeIndexes = innerTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(innerTypeEdgeIndexes.contains(i));
		assertTrue(innerTypeEdgeIndexes.contains(14));
		assertTrue(innerTypeEdgeIndexes.contains(22));
		assertTrue(innerTypeEdgeIndexes.contains(29));
		assertTrue(innerTypeEdgeIndexes.contains(37));
		assertTrue(innerTypeEdgeIndexes.contains(44));
		assertTrue(innerTypeEdgeIndexes.contains(52));
		assertTrue(innerTypeEdgeIndexes.contains(59));
		assertEquals(innerTypeEdge.size(), 8);*/
		List<TopologyElement> innerTypeCell = context.topology().inner().get(SiteType.Cell);
		List<Integer> innerTypeCellIndexes = innerTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=10; i<17; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		for (int i=19; i<26; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		for (int i=28; i<35; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		for (int i=37; i<44; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		for (int i=46; i<53; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		for (int i=55; i<62; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		for (int i=64; i<71; i++)
			assertTrue(innerTypeCellIndexes.contains(i));
		assertEquals(innerTypeCell.size(), 49);
		
		// Interlayer
		List<TopologyElement> interlayerConcaveTypeVertex = context.topology().interlayer().get(SiteType.Vertex);
		assertEquals(interlayerConcaveTypeVertex.size(), 0);
		List<TopologyElement> interlayerConcaveTypeEdge = context.topology().interlayer().get(SiteType.Edge);
		assertEquals(interlayerConcaveTypeEdge.size(), 0);
		List<TopologyElement> interlayerConcaveTypeCell = context.topology().interlayer().get(SiteType.Cell);
		assertEquals(interlayerConcaveTypeCell.size(), 0);
		
		// Top
		List<TopologyElement> topTypeVertex = context.topology().top().get(SiteType.Vertex);
		List<Integer> topTypeVertexIndexes = topTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=90; i<=99; i++)
			assertTrue(topTypeVertexIndexes.contains(i));
		assertEquals(topTypeVertex.size(), 10);
		List<TopologyElement> topTypeEdge = context.topology().top().get(SiteType.Edge);
		List<Integer> topTypeEdgeIndexes = topTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=171; i<=179; i++)
			assertTrue(topTypeEdgeIndexes.contains(i));
		assertEquals(topTypeEdge.size(), 9);
		List<TopologyElement> topTypeCell = context.topology().top().get(SiteType.Cell);
		List<Integer> topTypeCellIndexes = topTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=72; i<=80; i++)
			assertTrue(topTypeCellIndexes.contains(i));
		assertEquals(topTypeCell.size(), 9);

		// Left
		List<TopologyElement> leftTypeVertex = context.topology().left().get(SiteType.Vertex);
		List<Integer> leftTypeVertexIndexes = leftTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=90; i+=10)
			assertTrue(leftTypeVertexIndexes.contains(i));
		assertEquals(leftTypeVertex.size(), 10);
		List<TopologyElement> leftTypeEdge = context.topology().left().get(SiteType.Edge);
		List<Integer> leftTypeEdgeIndexes = leftTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=9; i<=151; i+=19)
			assertTrue(leftTypeEdgeIndexes.contains(i));
		assertEquals(leftTypeEdge.size(), 9);
		List<TopologyElement> leftTypeCell = context.topology().left().get(SiteType.Cell);
		List<Integer> leftTypeCellIndexes = leftTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=72; i+=9)
			assertTrue(leftTypeCellIndexes.contains(i));
		assertEquals(leftTypeCell.size(), 9);
		
		// Right
		List<TopologyElement> rightTypeVertex = context.topology().right().get(SiteType.Vertex);
		List<Integer> rightTypeVertexIndexes = rightTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=9; i<=99; i+=10)
			assertTrue(rightTypeVertexIndexes.contains(i));
		assertEquals(rightTypeVertex.size(), 10);
		List<TopologyElement> rightTypeEdge = context.topology().right().get(SiteType.Edge);
		List<Integer> rightTypeEdgeIndexes = rightTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=18; i<=160; i+=19)
			assertTrue(rightTypeEdgeIndexes.contains(i));
		assertEquals(rightTypeEdge.size(), 9);
		List<TopologyElement> rightTypeCell = context.topology().right().get(SiteType.Cell);
		List<Integer> rightTypeCellIndexes = rightTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=8; i<=80; i+=9)
			assertTrue(rightTypeCellIndexes.contains(i));
		assertEquals(rightTypeCell.size(), 9);
		
		// Bottom
		List<TopologyElement> bottomTypeVertex = context.topology().bottom().get(SiteType.Vertex);
		List<Integer> bottomTypeVertexIndexes = bottomTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=9; i++)
			assertTrue(bottomTypeVertexIndexes.contains(i));
		assertEquals(bottomTypeVertex.size(), 10);
		List<TopologyElement> bottomTypeEdge = context.topology().bottom().get(SiteType.Edge);
		List<Integer> bottomTypeEdgeIndexes = bottomTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(bottomTypeEdgeIndexes.contains(i));
		assertEquals(bottomTypeEdge.size(), 9);
		List<TopologyElement> bottomTypeCell = context.topology().bottom().get(SiteType.Cell);
		List<Integer> bottomTypeCellIndexes = bottomTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(bottomTypeCellIndexes.contains(i));
		assertEquals(bottomTypeCell.size(), 9);
		
		// Centre
		List<TopologyElement> centreTypeVertex = context.topology().centre().get(SiteType.Vertex);
		List<Integer> centreTypeVertexIndexes = centreTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(centreTypeVertexIndexes.contains(44));
		assertTrue(centreTypeVertexIndexes.contains(45));
		assertTrue(centreTypeVertexIndexes.contains(54));
		assertTrue(centreTypeVertexIndexes.contains(55));
		assertEquals(centreTypeVertex.size(), 4);
		List<TopologyElement> centreTypeEdge = context.topology().centre().get(SiteType.Edge);
		List<Integer> centreTypeEdgeIndexes = centreTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(centreTypeEdgeIndexes.contains(80));
		assertTrue(centreTypeEdgeIndexes.contains(89));
		assertTrue(centreTypeEdgeIndexes.contains(90));
		assertTrue(centreTypeEdgeIndexes.contains(99));
		assertEquals(centreTypeEdge.size(), 4);
		List<TopologyElement> centreTypeCell = context.topology().centre().get(SiteType.Cell);
		List<Integer> centreTypeCellIndexes = centreTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		assertTrue(centreTypeCellIndexes.contains(40));
		assertEquals(centreTypeCell.size(), 1);
		
		// Columns
		List<List<TopologyElement>> columnsTypeVertex = context.topology().columns().get(SiteType.Vertex);
		for (int i=0; i<10; i++) 
		{
			List<Integer> columnsTypeVertexIndexes = columnsTypeVertex.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=i; j<100; j+=10)
				assertTrue(columnsTypeVertexIndexes.contains(i));
			assertEquals(columnsTypeVertexIndexes.size(), 10);
		}
		assertEquals(columnsTypeVertex.size(), 10);
		List<List<TopologyElement>> columnsTypeEdge = context.topology().columns().get(SiteType.Edge);
		for (int i=0; i<19; i+=2) 
		{
			List<Integer> columnsTypeEdgeIndexes1 = columnsTypeEdge.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=(i/2)+9; j<180; j+=19) {
				assertTrue(columnsTypeEdgeIndexes1.contains(j));
			}
			assertEquals(columnsTypeEdgeIndexes1.size(), 9);
			if (i+1 < 19) 
			{
				List<Integer> columnsTypeEdgeIndexes2 = columnsTypeEdge.get(i+1).stream().map(TopologyElement::index).collect(Collectors.toList());
				for (int j=i/2; j<180; j+=19) {
					assertTrue(columnsTypeEdgeIndexes2.contains(j));
				}
				assertEquals(columnsTypeEdgeIndexes2.size(), 10);
			}
		}
		assertEquals(columnsTypeEdge.size(), 19);
		List<List<TopologyElement>> columnsTypeCell = context.topology().columns().get(SiteType.Cell);
		for (int i=0; i<9; i++) 
		{
			List<Integer> columnsTypeCellIndexes = columnsTypeCell.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=i; j<81; j+=9)
				assertTrue(columnsTypeCellIndexes.contains(j));
			assertEquals(columnsTypeCellIndexes.size(), 9);
		}
		assertEquals(columnsTypeCell.size(), 9);
		
		// Rows
		List<List<TopologyElement>> rowsTypeVertex = context.topology().rows().get(SiteType.Vertex);
		for (int i=0; i<10; i++) 
		{
			List<Integer> rowsTypeVertexIndexes = rowsTypeVertex.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=i*10; j<10+(i*10); j++)
				assertTrue(rowsTypeVertexIndexes.contains(j));
			assertEquals(rowsTypeVertexIndexes.size(), 10);
		}
		assertEquals(rowsTypeVertex.size(), 10);
		List<List<TopologyElement>> rowsTypeEdge = context.topology().rows().get(SiteType.Edge);
		int index = 0;
		for (int i=0; i<19; i+=2) 
		{
			List<Integer> rowsTypeEdgeIndexes1 = rowsTypeEdge.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=i*19; j<9; j++) 
			{
				assertTrue(rowsTypeEdgeIndexes1.contains(index));
				index ++;
			}
			assertEquals(rowsTypeEdgeIndexes1.size(), 9);
			if (i+1 < 19) {
				List<Integer> rowsTypeEdgeIndexes2 = rowsTypeEdge.get(i+1).stream().map(TopologyElement::index).collect(Collectors.toList());
				for (int j=(i*19)+9; j<10; j++) 
				{
					assertTrue(rowsTypeEdgeIndexes2.contains(index));
					index ++;
				}
				assertEquals(rowsTypeEdgeIndexes2.size(), 10);
			}
		}
		assertEquals(rowsTypeEdge.size(), 19);
		List<List<TopologyElement>> rowsTypeCell = context.topology().rows().get(SiteType.Cell);
		for (int i=0; i<9; i++) 
		{
			List<Integer> rowsTypeCellIndexes = rowsTypeCell.get(i).stream().map(TopologyElement::index).collect(Collectors.toList());
			for (int j=i*9; j<9+(i*9); j++)
				assertTrue(rowsTypeCellIndexes.contains(j));
			assertEquals(rowsTypeCellIndexes.size(), 9);
		}
		assertEquals(rowsTypeCell.size(), 9);
		
		// Sides
		Map<DirectionFacing, List<TopologyElement>> sidesTypeVertex = context.topology().sides().get(SiteType.Vertex);
		assertTrue(sidesTypeVertex.get(CompassDirection.WNW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.SSW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.NE).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.NW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.SE).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.SW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.ENE).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.SSE).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.ESE).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.WSW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.NNW).isEmpty());
		assertTrue(sidesTypeVertex.get(CompassDirection.NNE).isEmpty());
		List<TopologyElement> sideNTypeVertex = sidesTypeVertex.get(CompassDirection.N);
		List<Integer> sideNTypeVertexIndexes = sideNTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=90; i<=99; i++)
			assertTrue(sideNTypeVertexIndexes.contains(i));
		assertEquals(sideNTypeVertexIndexes.size(), 10);
		List<TopologyElement> sideETypeVertex = sidesTypeVertex.get(CompassDirection.E);
		List<Integer> sideETypeVertexIndexes = sideETypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=9; i<=99; i+=10)
			assertTrue(sideETypeVertexIndexes.contains(i));
		assertEquals(sideETypeVertexIndexes.size(), 10);
		List<TopologyElement> sideSTypeVertex = sidesTypeVertex.get(CompassDirection.S);
		List<Integer> sideSTypeVertexIndexes = sideSTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=9; i++)
			assertTrue(sideSTypeVertexIndexes.contains(i));
		assertEquals(sideSTypeVertexIndexes.size(), 10);
		List<TopologyElement> sideWTypeVertex = sidesTypeVertex.get(CompassDirection.W);
		List<Integer> sideWTypeVertexIndexes = sideWTypeVertex.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=90; i+=10)
			assertTrue(sideWTypeVertexIndexes.contains(i));
		assertEquals(sideWTypeVertexIndexes.size(), 10);
		assertEquals(sidesTypeVertex.size(), 16);
		Map<DirectionFacing, List<TopologyElement>> sidesTypeEdge = context.topology().sides().get(SiteType.Edge);
		assertTrue(sidesTypeEdge.get(CompassDirection.WNW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.SSW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.NE).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.NW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.SE).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.SW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.ENE).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.SSE).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.ESE).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.WSW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.NNW).isEmpty());
		assertTrue(sidesTypeEdge.get(CompassDirection.NNE).isEmpty());
		List<TopologyElement> sideNTypeEdge = sidesTypeEdge.get(CompassDirection.N);
		List<Integer> sideNTypeEdgeIndexes = sideNTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=163; i<=179; i++)
			assertTrue(sideNTypeEdgeIndexes.contains(i));
		assertEquals(sideNTypeEdgeIndexes.size(), 19);
		List<TopologyElement> sideETypeEdge = sidesTypeEdge.get(CompassDirection.E);
		List<Integer> sideETypeEdgeIndexes = sideETypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=18; i<=160; i+=19)
			assertTrue(sideETypeEdgeIndexes.contains(i));
		for (int i=8; i<=179; i+=19)
			assertTrue(sideETypeEdgeIndexes.contains(i));
		assertEquals(sideETypeEdgeIndexes.size(), 19);
		List<TopologyElement> sideSTypeEdge = sidesTypeEdge.get(CompassDirection.S);
		List<Integer> sideSTypeEdgeIndexes = sideSTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=16; i++)
			assertTrue(sideSTypeEdgeIndexes.contains(i));
		assertEquals(sideSTypeEdgeIndexes.size(), 19);
		List<TopologyElement> sideWTypeEdge = sidesTypeEdge.get(CompassDirection.W);
		List<Integer> sideWTypeEdgeIndexes = sideWTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=9; i<=151; i+=19)
			assertTrue(sideWTypeEdgeIndexes.contains(i));
		for (int i=0; i<=171; i+=19)
			assertTrue(sideWTypeEdgeIndexes.contains(i));
		assertEquals(sideWTypeEdgeIndexes.size(), 19);
		assertEquals(sidesTypeEdge.size(), 16);
		Map<DirectionFacing, List<TopologyElement>> sidesTypeCell = context.topology().sides().get(SiteType.Cell);
		assertTrue(sidesTypeCell.get(CompassDirection.WNW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.SSW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.NE).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.NW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.SE).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.SW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.ENE).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.SSE).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.ESE).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.WSW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.NNW).isEmpty());
		assertTrue(sidesTypeCell.get(CompassDirection.NNE).isEmpty());
		List<TopologyElement> sideNTypeCell = sidesTypeCell.get(CompassDirection.N);
		List<Integer> sideNTypeCellIndexes = sideNTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=72; i<=80; i++)
			assertTrue(sideNTypeCellIndexes.contains(i));
		assertEquals(sideNTypeCellIndexes.size(), 9);
		List<TopologyElement> sideETypeCell = sidesTypeCell.get(CompassDirection.E);
		List<Integer> sideETypeCellIndexes = sideETypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=8; i<=8; i+=9)
			assertTrue(sideETypeCellIndexes.contains(i));
		assertEquals(sideETypeCellIndexes.size(), 9);
		List<TopologyElement> sideSTypeCell = sidesTypeCell.get(CompassDirection.S);
		List<Integer> sideSTypeCellIndexes = sideSTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=8; i++)
			assertTrue(sideSTypeCellIndexes.contains(i));
		assertEquals(sideSTypeCellIndexes.size(), 9);
		List<TopologyElement> sideWTypeCell = sidesTypeCell.get(CompassDirection.W);
		List<Integer> sideWTypeCellIndexes = sideWTypeCell.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<=72; i+=9)
			assertTrue(sideWTypeCellIndexes.contains(i));
		assertEquals(sideWTypeCellIndexes.size(), 9);
		assertEquals(sidesTypeCell.size(), 16);
		
		// Axials
		assertEquals(context.topology().axials().get(SiteType.Vertex).size(), 0);
		List<TopologyElement> axialsTypeEdge = context.topology().axials().get(SiteType.Edge);
		List<Integer> axialsTypeEdgeIndexes = axialsTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<180; i++)
			assertTrue(axialsTypeEdgeIndexes.contains(i));
		assertEquals(axialsTypeEdge.size(), 180);
		assertEquals(context.topology().axials().get(SiteType.Cell).size(), 0);
		
		// Horizontal
		assertEquals(context.topology().horizontal().get(SiteType.Vertex).size(), 0);
		List<TopologyElement> horizontalTypeEdge = context.topology().horizontal().get(SiteType.Edge);
		List<Integer> horizontalTypeEdgeIndexes = horizontalTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<10; i++)
			for (int j=0+(i*19); j<7+(i*19); j++)
				assertTrue(horizontalTypeEdgeIndexes.contains(j));
		assertEquals(horizontalTypeEdge.size(), 90);
		assertEquals(context.topology().horizontal().get(SiteType.Cell).size(), 0);
		
		// Vertical
		assertEquals(context.topology().vertical().get(SiteType.Vertex).size(), 0);
		List<TopologyElement> verticalTypeEdge = context.topology().vertical().get(SiteType.Edge);
		List<Integer> verticalTypeEdgeIndexes = verticalTypeEdge.stream().map(TopologyElement::index).collect(Collectors.toList());
		for (int i=0; i<9; i++)
			for (int j=9+(i*19); j<18+(i*19); j++)
				assertTrue(verticalTypeEdgeIndexes.contains(j));
		assertEquals(verticalTypeEdge.size(), 90);
		assertEquals(context.topology().vertical().get(SiteType.Cell).size(), 0);
	}
	
	/**
	 * Tests the value of the mover each time after applying a move 
	 * on an edge. Tests it on four edge moves.
	 */
	@Test
	public void testMoverAfterMultipleMovesOnEdge()
	{
		// init
		Context context = initGame();

		// test
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		assertEquals(context.state().mover(), 2);

		applyMove(context, 50, 21, 2);
		updateBoard(context);
		assertEquals(context.state().mover(), 1);

		applyMove(context, 81, 36, 1);
		updateBoard(context);
		assertEquals(context.state().mover(), 2);

		applyMove(context, 122, 55, 2);
		updateBoard(context);
		assertEquals(context.state().mover(), 1);
	}

	/**
	 * Tests the value of the number of initial placement after applying
	 * moves on edges.
	 */
	@Test
	public void testnumInitialPlacementMovesAfterMultipleMovesOnEdge()
	{
		// init
		Context context = initGame();

		applyMove(context, 25, 10, 1);
		updateBoard(context);
		applyMove(context, 50, 21, 2);
		updateBoard(context);
		applyMove(context, 81, 36, 1);
		updateBoard(context);
		applyMove(context, 122, 55, 2);
		updateBoard(context);
		
		// test
		assertEquals(context.trial().numInitialPlacementMoves(), 5);
	}
	
	/**
	 * Tests the indexes used inside the equipment after 1 edge move.
	 */
	@Test
	public void testUpdateIndexesInsideContainerIdAfter1Move()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		
		// test
		int[] containerId = context.containerId();
		for (int i=0; i<=48; i++)
			assertEquals(containerId[i], 0);
		assertEquals(containerId[49], 1);
		assertEquals(containerId[50], 2);
	}
	
	/**
	 * Tests the indexes used inside the equipment after 2 edge moves.
	 */
	@Test
	public void testUpdateIndexesInsideContainerIdAfter2Moves()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 10, 1);
		updateBoard(context);
		applyMove(context, 50, 21, 2);
		updateBoard(context);
		
		// test
		int[] containerId = context.containerId();
		for (int i=0; i<=80; i++)
			assertEquals(containerId[i], 0);
		assertEquals(containerId[81], 1);
		assertEquals(containerId[82], 2);
	}
	
	/**
	 * Tests the content of the cells of the topology of each container 
	 * (board component, but also hand's players components).
	 */
	@Test
	public void testTopologyCellsOfAllContainer()
	{
		// init
		Context context = initGame();

		// test
		applyMove(context, 25, 14, 1);
		updateBoard(context);
		assertEquals(context.state().containerStates()[0].container().topology().cells().size(), 49);
		assertEquals(context.state().containerStates()[1].container().topology().cells().get(0).index(), 49);
		assertEquals(context.state().containerStates()[2].container().topology().cells().get(0).index(), 50);

		applyMove(context, 50, 27, 2);
		updateBoard(context);
		assertEquals(context.state().containerStates()[0].container().topology().cells().size(), 81);
		assertEquals(context.state().containerStates()[1].container().topology().cells().get(0).index(), 81);
		assertEquals(context.state().containerStates()[2].container().topology().cells().get(0).index(), 82);
		
		applyMove(context, 81, 44, 1);
		updateBoard(context);
		assertEquals(context.state().containerStates()[0].container().topology().cells().size(), 121);
		assertEquals(context.state().containerStates()[1].container().topology().cells().get(0).index(), 121);
		assertEquals(context.state().containerStates()[2].container().topology().cells().get(0).index(), 122);
		
		applyMove(context, 122, 65, 2);
		updateBoard(context);
		assertEquals(context.state().containerStates()[0].container().topology().cells().size(), 169);
		assertEquals(context.state().containerStates()[1].container().topology().cells().get(0).index(), 169);
		assertEquals(context.state().containerStates()[2].container().topology().cells().get(0).index(), 170);
		
		applyMove(context, 169, 90, 1);
		updateBoard(context);
		assertEquals(context.state().containerStates()[0].container().topology().cells().size(), 225);
		assertEquals(context.state().containerStates()[1].container().topology().cells().get(0).index(), 225);
		assertEquals(context.state().containerStates()[2].container().topology().cells().get(0).index(), 226);
	}
}
