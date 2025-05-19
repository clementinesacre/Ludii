package games;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import app.PlayerApp;
import app.move.GrowingBoardVisual;
import app.utils.GameUtil;
import app.utils.UpdateTabMessages;
import compiler.Compiler;
import game.Game;
import game.boardless.GrowingBoard;
import game.equipment.Equipment;
import game.equipment.container.board.Boardless;
import game.rules.phase.Phase;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import main.collections.ChunkSet;
import main.collections.FastArrayList;
import main.collections.FastTIntArrayList;
import main.grammar.Report;
import manager.Referee;
import manager.ai.AIUtil;
import other.GameLoader;
import other.trial.Trial;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerFlatState;
import other.state.container.ContainerState;
import other.state.owned.FlatCellOnlyOwned;
import other.state.zhash.HashedBitSet;
import other.state.zhash.HashedChunkSet;
import other.topology.TopologyElement;

/**
 * Test that the board size is changing properly, through all aspects.
 * We suppose the game is boardless - this test does not concern normal board games.
 * These tests focus on go back in the move - making use of the history.
 *
 * @author Clémentine Sacré
 */
//@SuppressWarnings("static-method")
public class GrowingBoardRollBackTest {
	
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
	 * @param mover id of the mover. Starts at 1.
	 * @return the create Move.
	 */
	public Move getMoveMove(int from, int to, int mover)
	{
		String moveStr = "[Move:mover="+mover+",from="+from+",to="+to+",actions=[Move:typeFrom=Cell,from="+from+",typeTo=Cell,to="+to+",decision=true]]";
		return new Move(moveStr);
	}
	

	public Move getMoveAdd(int to, int mover)
	{
		String moveStr = "[Move:mover="+mover+",to="+to+",actions=[Add:type=Cell,to="+to+",what=1,decision=true]]";
		return new Move(moveStr);
	}
	
	/**
	 * Apply a move.
	 * 
	 * @param context context on which apply the move.
	 * @param from index of the from position.
	 * @param to index of the to position.
	 * @param move mover id of the mover. Starts at 1.
	 * @param moveType move type (Move, Add, ...).
	 */
	public void applyMove(Context context, int from, int to, int mover, int moveType)
	{
		Move move = null;
		if(moveType == 0)
			move = getMoveMove(from, to, mover);
		else if(moveType == 1)
			move = getMoveAdd(to, mover);
		
		if (move != null)
		{
			context.game().apply(context, move);
			context.trial().setNumSubmovesPlayed(context.trial().numSubmovesPlayed() + 1); //TODO ??
		}
		else
		{
			fail("moveType does not exist");
		}
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
		GrowingBoard.updateBoard(context, ((Boardless)context.board()).dimension(), ((Boardless) context.board()).dimension() + GrowingBoardVisual.growingStep(context), true);
		
		return prevContainerStates;
	}
	
	/**
	 * Generates the legal moves.
	 * 
	 * @param context
	 * @param player player index.
	 * @return legal moves.
	 */
	public HashMap<Integer, HashSet<Integer>> generateLegalMoves(Context context, int player)
	{
		final int indexPhase = context.state().currentPhase(player);
		final Phase phase = context.game().rules().phases()[indexPhase];
		final FastArrayList<Move> phaseMoves = phase.play().moves().eval(context).moves();
		HashMap<Integer, HashSet<Integer>> legalMoves = new HashMap<Integer, HashSet<Integer>>();

		for(int i=0; i<phaseMoves.size(); i++)
		{
			int from = phaseMoves.get(i).from();
			int to = phaseMoves.get(i).to();
			HashSet<Integer> tos;
			if (legalMoves.containsKey(from))
			{
				tos = legalMoves.get(from);
			}
			else
			{
				tos = new HashSet<Integer>();
			}
			tos.add(to);
			legalMoves.put(from, tos);
		}
		return legalMoves;
	}
	
	/**
	 * Jump to a specific move. Usually used to go back.
	 * If boardless game and last move was an edge move, need to reduce board size.
	 * 
	 * @param context
	 * @param moveToJumpTo move to jump to.
	 */
	public static void jumpToMove(final Context context, final int moveToJumpTo)
	{
		final List<Move> allMoves = context.trial().generateCompleteMovesList();
		
		Moves legalMoves = context.trial().cachedLegalMoves();
		FastTIntArrayList[][] locations = ((FlatCellOnlyOwned) context.state().owned()).locations();

		//GameUtil.resetGame(app, true); //TODO how reset game ? tests wont work properly else
		System.out.println("GrowingBoardRollBackTest.java jumpToMove() allMoves : "+allMoves);
		final int moveToJumpToWithSetup;
		if (moveToJumpTo == 0)
			moveToJumpToWithSetup = context.currentInstanceContext().trial().numInitialPlacementMoves();
		else
			moveToJumpToWithSetup = moveToJumpTo;
		
		// -------------
		Move currMove = allMoves.get(moveToJumpToWithSetup);
		if (currMove.isOnEdge())
		{
			context.trial().setLegalMoves(legalMoves, context);
			
			((FlatCellOnlyOwned) context.state().owned()).setLocations(locations);
			
			int currDim = ((Boardless) context.board()).dimension();
			GrowingBoard.updateBoard(context, currDim, currDim - GrowingBoardVisual.growingStep(context), false);

			for (int i=0; i<allMoves.size(); i++) 
			{
				Move newMove = GrowingBoardVisual.generateNewMove(allMoves.get(i), false);
				allMoves.set(i, newMove);
			}
		}
		// -------------
		
		final List<Move> newDoneMoves = allMoves.subList(0, moveToJumpToWithSetup);
		final List<Move> newUndoneMoves = allMoves.subList(moveToJumpToWithSetup, allMoves.size());
		
		Move move = null;
		
		for (int i = context.trial().numMoves(); i < newDoneMoves.size(); i++)
		{
			move = newDoneMoves.get(i);
			context.game().apply(context, move);
		}
	
	}
	
	/**
	 * Go back one move before.
	 * 
	 * @param context
	 */
	public static void goBack1Move(final Context context)
	{
		jumpToMove(context, context.trial().numMoves() - 1);
	}
	
	/**
	 * Go back to start.
	 * 
	 * @param context
	 */
	public static void goBackToStart(final Context context)
	{
		final int numInitialPlacementMoves = context.currentInstanceContext().trial().numInitialPlacementMoves();
		final int startIndex = context.trial().numMoves() - context.currentInstanceContext().trial().numMoves() + numInitialPlacementMoves;
		jumpToMove(context, startIndex);
	}
	
	/**
	 * Tests the method that initializes main constants and data structures, when the 
	 * board size has been changed 2 times (two edges have been touched one after the 
	 * other) and then go back 1 time.
	 */
	@Test
	public void testInitConstantAfter2EdgeMoves()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 14, 1, 0);
		updateBoard(context);
		applyMove(context, 50, 27, 2, 0);
		updateBoard(context);
		
		goBack1Move(context);
		
		// test main constants
		assertEquals(GrowingBoard.prevDimensionBoard(), 9);
		assertEquals(GrowingBoard.prevAreaBoard(), 81);
		assertEquals(GrowingBoard.prevTotalIndexes(), 83);
		assertEquals(GrowingBoard.newDimensionBoard(), 7);
		assertEquals(GrowingBoard.newAreaBoard(), 49);
		assertEquals(GrowingBoard.newTotalIndexes(), 51);
		assertEquals(GrowingBoard.diff(), 32);
		
		// test main data structures
		HashMap<Integer, Integer> mappedPrevToNewIndexes = GrowingBoard.mappedPrevToNewIndexes();
		HashMap<Integer, Integer> mappedNewToPrevIndexes = GrowingBoard.mappedNewToPrevIndexes();
		HashSet<Integer> surplusIndexes = GrowingBoard.surplusIndexes();
		
		for (int i=0; i<=50; i++)
			assertTrue(mappedNewToPrevIndexes.containsKey(i));
		for (int i=51; i<=82; i++)
			assertFalse(mappedNewToPrevIndexes.containsKey(i));
		
		for (int i=0; i<=9; i++)
			assertFalse(mappedPrevToNewIndexes.containsKey(i));
		for (int i=10; i<=16; i++)
			assertTrue(mappedPrevToNewIndexes.containsKey(i));
		assertFalse(mappedPrevToNewIndexes.containsKey(17));
		assertFalse(mappedPrevToNewIndexes.containsKey(18));
		for (int i=19; i<=25; i++)
			assertTrue(mappedPrevToNewIndexes.containsKey(i));
		assertFalse(mappedPrevToNewIndexes.containsKey(26));
		assertFalse(mappedPrevToNewIndexes.containsKey(27));
		for (int i=28; i<=34; i++)
			assertTrue(mappedPrevToNewIndexes.containsKey(i));
		assertFalse(mappedPrevToNewIndexes.containsKey(35));
		assertFalse(mappedPrevToNewIndexes.containsKey(36));
		for (int i=37; i<=43; i++)
			assertTrue(mappedPrevToNewIndexes.containsKey(i));
		assertFalse(mappedPrevToNewIndexes.containsKey(44));
		assertFalse(mappedPrevToNewIndexes.containsKey(45));
		for (int i=46; i<=52; i++)
			assertTrue(mappedPrevToNewIndexes.containsKey(i));
		assertFalse(mappedPrevToNewIndexes.containsKey(53));
		assertFalse(mappedPrevToNewIndexes.containsKey(54));
		for (int i=55; i<=61; i++)
			assertTrue(mappedPrevToNewIndexes.containsKey(i));
		assertFalse(mappedPrevToNewIndexes.containsKey(62));
		assertFalse(mappedPrevToNewIndexes.containsKey(63));
		for (int i=64; i<=70; i++)
			assertTrue(mappedPrevToNewIndexes.containsKey(i));
		for (int i=71; i<=80; i++)
			assertFalse(mappedPrevToNewIndexes.containsKey(i));
		assertTrue(mappedPrevToNewIndexes.containsKey(81));
		assertTrue(mappedPrevToNewIndexes.containsKey(82));

		for (int i=0; i<=9; i++)
			assertTrue(surplusIndexes.contains(i));
		for (int i=10; i<=16; i++)
			assertFalse(surplusIndexes.contains(i));
		assertTrue(surplusIndexes.contains(17));
		assertTrue(surplusIndexes.contains(18));
		for (int i=19; i<=25; i++)
			assertFalse(surplusIndexes.contains(i));
		assertTrue(surplusIndexes.contains(26));
		assertTrue(surplusIndexes.contains(27));
		for (int i=28; i<=34; i++)
			assertFalse(surplusIndexes.contains(i));
		assertTrue(surplusIndexes.contains(35));
		assertTrue(surplusIndexes.contains(36));
		for (int i=37; i<=43; i++)
			assertFalse(surplusIndexes.contains(i));
		assertTrue(surplusIndexes.contains(44));
		assertTrue(surplusIndexes.contains(45));
		for (int i=46; i<=52; i++)
			assertFalse(surplusIndexes.contains(i));
		assertTrue(surplusIndexes.contains(53));
		assertTrue(surplusIndexes.contains(54));
		for (int i=55; i<=61; i++)
			assertFalse(surplusIndexes.contains(i));
		assertTrue(surplusIndexes.contains(62));
		assertTrue(surplusIndexes.contains(63));
		for (int i=64; i<=70; i++)
			assertFalse(surplusIndexes.contains(i));
		for (int i=71; i<=80; i++)
			assertTrue(surplusIndexes.contains(i));
		assertFalse(surplusIndexes.contains(81));
		assertFalse(surplusIndexes.contains(82));
	}
	
	/**
	 * Tests the indexes used inside the equipment after 2 edge moves,
	 * and then go back 1 time.
	 */
	@Test
	public void testUpdateIndexesInsideEquipmentAfter2Moves()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Equipment equipment = game.equipment();
		
		applyMove(context, 25, 14, 1, 0);
		updateBoard(context);
		applyMove(context, 50, 27, 2, 0);
		updateBoard(context);
		
		goBack1Move(context);
		
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
	 * Tests the indexes used inside the equipment after 3 edge moves
	 * and then go back 1 time.
	 */
	@Test
	public void testUpdateIndexesInsideEquipmentAfter3Moves()
	{
		// init
		Context context = initGame();
		Game game = context.game();
		Equipment equipment = game.equipment();
		
		applyMove(context, 25, 14, 1, 0);
		updateBoard(context);
		applyMove(context, 50, 21, 2, 0);
		updateBoard(context);
		applyMove(context, 81, 44, 1, 0);
		updateBoard(context);
		
		goBack1Move(context);
		
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
	 * Tests the method that re-applys the moves previously done on 
	 * the new board after 2 edge moves, and then go back 1 time.
	 */
	@Test
	public void testReplayMoves()
	{
		// init
		Context context = initGame();
		
		applyMove(context, 25, 14, 1, 0);
		updateBoard(context);
		applyMove(context, 50, 27, 2, 0);
		updateBoard(context);
		
		goBack1Move(context);

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
		assertEquals(newMovesDone.get(5).actions().get(0).to(), 26);
	}
	
	/**
	 * Tests the method that re-generate the legal moves after applying 
	 * the moves on the new board, after 2 edge moves, and then go back 
	 * 1 time.
	 */
	@Test
	public void testGenerateLegalMoves()
	{
		// TODO BUG : I DONT KNOW HOW TO APPLY A MOVE PROPERLY  - HERE THERE ARE NO LEGAL MOVES AFTER 
		// I APPLIED THE MOVE - UNLIKE WHEN DOING IT WITH THE VISUAL INTERFACE
		
		// init
		Context context = initGame();
		
		applyMove(context, 25, 14, 1, 0);		
		updateBoard(context);
		applyMove(context, 50, 27, 2, 0);
		updateBoard(context);

		context.game().moves(context); // make sure cachedLegalMoves is init 
		
		goBack1Move(context);
		
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
	 * doing 2 edge moves, and then go back 1 time.
	 */
	@Test
	public void testUpdateChunksAfter2EdgeMoves()
	{
		// init
		Context context = initGame();

		System.out.println("GrowingBoardRollBackTest.java testUpdateChunksAfter2EdgeMoves() prevContainerStates[0] 0: "+(ContainerFlatState) context.state().containerStates()[0]);
		applyMove(context, 25, 14, 1, 0);
		updateBoard(context);

		System.out.println("GrowingBoardRollBackTest.java testUpdateChunksAfter2EdgeMoves() prevContainerStates[0] 1: "+(ContainerFlatState) context.state().containerStates()[0]);
		applyMove(context, 50, 27, 2, 0);
		updateBoard(context);
		
		ContainerState[] prevContainerStates = context.state().containerStates();
		System.out.println("GrowingBoardRollBackTest.java testUpdateChunksAfter2EdgeMoves() prevContainerStates[0] 2: "+(ContainerFlatState) context.state().containerStates()[0]);
		goBack1Move(context);
		
		
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
			System.out.println("GrowingBoardRollBackTest.java testUpdateChunksAfter2EdgeMoves() newContainerFlatState 3: "+newContainerFlatState);
			System.out.println("GrowingBoardRollBackTest.java testUpdateChunksAfter2EdgeMoves() who : "+who);
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
	
	
}
