package games;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import org.junit.Test;

import app.move.GrowingBoard;
import game.Game;
import game.equipment.container.board.Boardless;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.util.equipment.Region;
import main.collections.ChunkSet;
import other.GameLoader;
import other.trial.Trial;
import other.context.Context;
import other.model.Model;
import other.move.Move;
import other.move.MoveSequence;
import other.state.container.ContainerFlatState;
import other.state.container.ContainerState;
import other.state.zhash.HashedBitSet;
import other.state.zhash.HashedChunkSet;
import other.topology.TopologyElement;

/**
 * Test that the board size is changing properly, through all aspects.
 * We suppose the game is boardless - this test does not concern normal board games.
 *
 * @author Clémentine Sacré
 */
//@SuppressWarnings("static-method")
public class GrowingBoardTest {
	
	public Context initGame()
	{
		
		final Game game = GameLoader.loadGameFromName("TestClementine.lud");
		final Context context = new Context(game, new Trial(game));
		game.start(context);
		return context;	
	}
	
	public Move getMoveMove(int from, int to)
	{
		String moveStr = "[Move:mover=1,from="+from+",to="+to+",actions=[Move:typeFrom=Cell,from="+from+",typeTo=Cell,to="+to+",decision=true]]";
		return new Move(moveStr);
	}
	
	public void applyMove(Context context, Move move)
	{
		final Move appliedMove = context.game().apply(context, move);
		context.trial().setNumSubmovesPlayed(context.trial().numSubmovesPlayed() + 1);
	}
	
	@Test
	public void testTouchingEdgeTrue()
	{
		// init
		Context context = initGame();
		
		Move move = getMoveMove(25, 10);
		List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());
		
		// test		
		assertTrue(GrowingBoard.isTouchingEdge(perimeter, move.to()));
	}
	
	@Test
	public void testTouchingEdgeFalse()
	{
		// init
		Context context = initGame();
		
		Move move = getMoveMove(25, 7);
		List<TopologyElement> perimeter = context.topology().perimeter(context.board().defaultSite());
		
		// test		
		assertFalse(GrowingBoard.isTouchingEdge(perimeter, move.to()));
	}
	
	@Test
	public void testInitMainConstants()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Boardless board = (Boardless) game.board();

		GrowingBoard.initMainConstants(context, board.getDimension());
		
		// test
		assertEquals(GrowingBoard.prevDimensionBoard(), 5);
		assertEquals(GrowingBoard.prevAreaBoard(), 25);
		assertEquals(GrowingBoard.prevTotalIndexes(), 27);
		assertEquals(GrowingBoard.newDimensionBoard(), 7);
		assertEquals(GrowingBoard.newAreaBoard(), 49);
		assertEquals(GrowingBoard.newTotalIndexes(), 51);
		assertEquals(GrowingBoard.diff(), 24);
	}
	
	@Test
	public void testInitMappingIndexes()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Boardless board = (Boardless) game.board();

		GrowingBoard.initMainConstants(context, board.getDimension());
		GrowingBoard.initMappingIndexes();
		
		// test
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

	@Test
	public void testUpdateBoardDimensions()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Boardless board = (Boardless) game.board();

		GrowingBoard.updateBoardDimensions(board);
		GrowingBoard.initMainConstants(context, board.getDimension());
		
		// test
		assertEquals(GrowingBoard.prevDimensionBoard(), 7);
		assertEquals(GrowingBoard.prevAreaBoard(), 49);
		assertEquals(GrowingBoard.prevTotalIndexes(), 51);
		assertEquals(GrowingBoard.newDimensionBoard(), 9);
		assertEquals(GrowingBoard.newAreaBoard(), 81);
		assertEquals(GrowingBoard.newTotalIndexes(), 83);
		assertEquals(GrowingBoard.diff(), 32);
	}
	
	@Test
	public void testUpdateIndexesInsideComponents()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Boardless board = (Boardless) game.board();
		
		GrowingBoard.initMainConstants(context, board.getDimension());
		GrowingBoard.updateBoardDimensions(board);
		int[] prevContainerId = context.containerId();
		int[] prevSitesFrom = game.equipment().sitesFrom();
		GrowingBoard.updateIndexesInsideComponents(context, game);
		
		// test
		int[] newContainerId = game.equipment().containerId();
		for (int i=0; i<=48; i++)
			assertEquals(newContainerId[i], 0);
		assertEquals(newContainerId[49], 1);
		assertEquals(newContainerId[50], 2);
				
		int[] newSitesFrom = game.equipment().sitesFrom();
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
	
	@Test
	public void testUpdateChunks()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Trial trial = context.trial();
		Boardless board = (Boardless) game.board();
		
		Move move = getMoveMove(25, 10);
		applyMove(context, move);
		
		GrowingBoard.initMainConstants(context, board.getDimension());
		GrowingBoard.updateBoardDimensions(board);
		GrowingBoard.updateIndexesInsideComponents(context, game);
		
		//resetMoves(app);
		context.trial().setMoves(new MoveSequence(null), 0);
		//resetMoves(app);
		
		GrowingBoard.initMappingIndexes();
		ContainerState[] prevContainerStates = context.state().containerStates();
		System.out.println("prevContainerStates : "+Arrays.toString(prevContainerStates));
		GrowingBoard.updateChunks(context);
		
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
			for (int i=0; i<15; i++)
				assertFalse(playable.get(i));
			for (int i=15; i<=19; i++)
				assertTrue(playable.get(i));
			assertFalse(playable.get(22));
			assertTrue(playable.get(26));
			for (int i=29; i<=33; i++)
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
			//for (int i=0; i<=50; i++)
			//	assertEquals(state.getChunk(i), 0);
			assertEquals(state.internalState().numNonZeroChunks(), 0);
			
			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			//for (int i=0; i<=50; i++)
			//	assertFalse(empty.get(i));
			assertTrue(empty.isEmpty());
			
			HashedBitSet playable = newContainerFlatState.playable();
			//for (int i=0; i<=50; i++)
			//	assertFalse(playable.get(i));
			assertTrue(playable.internalState().isEmpty());
		}
		else 
			fail();
		
		// second player's state
		ContainerState newContainerState2 = newContainerStates[2];
		if (newContainerState2 instanceof other.state.container.ContainerFlatState) 
		{
			ContainerFlatState newContainerFlatState = (ContainerFlatState) newContainerState1;
			ContainerFlatState prevContainerFlatState = (ContainerFlatState) prevContainerStates[2];
			
			assertEquals(newContainerFlatState.who().internalState().numNonZeroChunks(), prevContainerFlatState.who().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.what().internalState().numNonZeroChunks(), prevContainerFlatState.what().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.count().internalState().numNonZeroChunks(), prevContainerFlatState.count().internalState().numNonZeroChunks());
			assertEquals(newContainerFlatState.state().internalState().numNonZeroChunks(), prevContainerFlatState.state().internalState().numNonZeroChunks());
			assertNull(newContainerFlatState.rotation());
			assertNull(newContainerFlatState.value());

			HashedChunkSet who = newContainerFlatState.who();
			assertEquals(who.getChunk(0), 1);

			HashedChunkSet what = newContainerFlatState.what();
			assertEquals(what.getChunk(0), 3);

			HashedChunkSet count = newContainerFlatState.count();
			assertEquals(count.getChunk(0), 3);

			HashedChunkSet state = newContainerFlatState.state();
			//for (int i=0; i<=50; i++)
			//	assertEquals(state.getChunk(i), 0);
			assertEquals(state.internalState().numNonZeroChunks(), 0);

			ChunkSet empty =  newContainerFlatState.emptyChunkSetCell();
			//for (int i=0; i<=50; i++)
			//	assertFalse(empty.get(i));
			assertTrue(empty.isEmpty());
			
			HashedBitSet playable = newContainerFlatState.playable();
			//for (int i=0; i<=50; i++)
			//	assertFalse(playable.get(i));
			assertTrue(playable.internalState().isEmpty());
			
		}
		else 
			fail();
	}
	
	@Test
	public void testReplayMoves()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Trial trial = context.trial();
		Boardless board = (Boardless) game.board();
		
		Move move = getMoveMove(25, 10);
		applyMove(context, move);
		
		List<Move> prevMovesDone = trial.generateCompleteMovesList();
		GrowingBoard.initMainConstants(context, board.getDimension());
		GrowingBoard.updateBoardDimensions(board);
		GrowingBoard.updateIndexesInsideComponents(context, game);
		
		//resetMoves(app);
		context.trial().setMoves(new MoveSequence(null), 0);
		//resetMoves(app);
		
		// GrowingBoard.remakeTrial(context, movesDone, legalMoves); = :
		GrowingBoard.initMappingIndexes();
		GrowingBoard.updateChunks(context);
		GrowingBoard.replayMoves(context, prevMovesDone);
		

		// test
		List<Move> newMovesDone = trial.generateCompleteMovesList();
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
	
	@Test
	public void testGenerateLegalMoves()
	{
		// TODO BUG : I DONT KNOW HOW TO APPLY A MOVE PROPERLY  - HERE THERE ARE NO LEGAL MOVES AFTER 
		// I APPLIED THE MOVE - UNLIKE WHEN DOING IT WITH THE VISUAL INTERFACE
		
		// init
		Context context = initGame();
		Game game = context.game();
		Trial trial = context.trial();
		Boardless board = (Boardless) game.board();
		
		Move move = getMoveMove(25, 10);
		applyMove(context, move);
		
		GrowingBoard.initMainConstants(context, board.getDimension());
		GrowingBoard.updateBoardDimensions(board);
		GrowingBoard.updateIndexesInsideComponents(context, game);
		
		List<Move> movesDone = trial.generateCompleteMovesList();
		Moves prevLegalMoves = trial.cachedLegalMoves();
		
		//resetMoves(app);
		trial.setMoves(new MoveSequence(null), 0);
		//resetMoves(app);
		
		// GrowingBoard.remakeTrial(context, movesDone, legalMoves); = :
		GrowingBoard.initMappingIndexes();
		GrowingBoard.updateChunks(context);
		GrowingBoard.replayMoves(context, movesDone);
		GrowingBoard.generateLegalMoves(context, prevLegalMoves);
		
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
	
}
