package PresidentsPlayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

class PresidentsGameRulesTest {
	private static final List<String> NAMES = List.of(
			"Ada", "Grace", "Linus", "James", "Margaret", "Alan", "Katherine");

	@Test
	void rejectsInvalidPlayerLists() {
		assertThrows(IllegalArgumentException.class, () -> new PresidentsGame(null));
		assertThrows(IllegalArgumentException.class, () -> new PresidentsGame(List.of("Solo")));
		assertThrows(IllegalArgumentException.class, () -> new PresidentsGame(List.of(
				"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight")));
		assertThrows(IllegalArgumentException.class, () -> new PresidentsGame(List.of("Ada", "Ada")));
		assertThrows(IllegalArgumentException.class, () -> new PresidentsGame(List.of("Ada", " ")));
	}

	@Test
	void dealsEveryCardExactlyOnceAndBalancesHandsForTwoThroughSevenPlayers() {
		for (int playerCount = 2; playerCount <= 7; playerCount++) {
			List<String> names = NAMES.subList(0, playerCount);
			PresidentsGame game = new PresidentsGame(names);
			game.start(new Random(100 + playerCount));

			Set<Card> dealt = new HashSet<Card>();
			int smallestHand = 52;
			int largestHand = 0;
			for (String name : names) {
				Hand hand = game.getHand(name);
				smallestHand = Math.min(smallestHand, hand.getSize());
				largestHand = Math.max(largestHand, hand.getSize());
				for (Card card : hand) assertTrue(dealt.add(card), "Duplicate card: " + card);
			}

			assertEquals(52, dealt.size());
			assertTrue(largestHand - smallestHand <= 1);
			assertTrue(game.getHand(game.getCurrentPlayer()).contains(new Card(1, 0)));
		}
	}

	@Test
	void aGameCannotBeStartedTwice() {
		PresidentsGame game = startedGame();
		assertThrows(IllegalStateException.class, () -> game.start(new Random(8)));
	}

	@Test
	void onlyTheCurrentPlayerMayAct() {
		PresidentsGame game = startedGame();
		String otherPlayer = NAMES.stream()
				.filter(name -> !name.equals(game.getCurrentPlayer()))
				.findFirst().orElseThrow();
		assertThrows(IllegalStateException.class,
				() -> game.play(otherPlayer, List.of(new Card(1, 0))));
		assertThrows(IllegalStateException.class, () -> game.pass(otherPlayer));
	}

	@Test
	void openingPlayMustContainTheThreeOfClubs() {
		PresidentsGame game = startedGame();
		String opener = game.getCurrentPlayer();
		Card otherCard = game.getHand(opener).asList().stream()
				.filter(card -> !card.equals(new Card(1, 0)))
				.findFirst().orElseThrow();
		assertThrows(IllegalArgumentException.class,
				() -> game.play(opener, List.of(otherCard)));
	}

	@Test
	void everyCardInOnePlayMustHaveTheSameRank() {
		PresidentsGame game = startedGame();
		String opener = game.getCurrentPlayer();
		Card differentRank = game.getHand(opener).asList().stream()
				.filter(card -> card.getValue() != 1)
				.findFirst().orElseThrow();
		assertThrows(IllegalArgumentException.class,
				() -> game.play(opener, List.of(new Card(1, 0), differentRank)));
	}

	@Test
	void aPlayerCannotPlayCardsThatAreNotInTheirHand() {
		PresidentsGame game = startedGame();
		String opener = game.getCurrentPlayer();
		Card missingThree = List.of(new Card(1, 1), new Card(1, 2), new Card(1, 3)).stream()
				.filter(card -> !game.getHand(opener).contains(card))
				.findFirst().orElseThrow();
		assertThrows(IllegalArgumentException.class,
				() -> game.play(opener, List.of(new Card(1, 0), missingThree)));
	}

	@Test
	void aResponseCannotUseFewerCardsThanTheCurrentPlay() {
		for (int seed = 0; seed < 500; seed++) {
			PresidentsGame game = new PresidentsGame(NAMES.subList(0, 4));
			game.start(new Random(seed));
			String opener = game.getCurrentPlayer();
			List<Card> openingThrees = cardsOfValue(game.getHand(opener), 1);
			if (openingThrees.size() < 2) continue;
			game.play(opener, openingThrees.subList(0, 2));
			String responder = game.getCurrentPlayer();
			Card single = game.getHand(responder).getCard(0);
			assertThrows(IllegalArgumentException.class,
					() -> game.play(responder, List.of(single)));
			return;
		}
		throw new AssertionError("Expected a deterministic opening pair");
	}

	@Test
	void aResponseCannotUseALowerRank() {
		for (int seed = 0; seed < 100; seed++) {
			PresidentsGame game = new PresidentsGame(NAMES.subList(0, 4));
			game.start(new Random(seed));
			game.play(game.getCurrentPlayer(), List.of(new Card(1, 0)));
			while (game.getCardsInPlay() != 0) game.pass(game.getCurrentPlayer());

			String leader = game.getCurrentPlayer();
			for (Card lead : game.getHand(leader)) {
				String responder = nextName(NAMES.subList(0, 4), leader);
				Card lower = game.getHand(responder).asList().stream()
						.filter(card -> card.getValue() < lead.getValue())
						.findFirst().orElse(null);
				if (lower == null) continue;
				game.play(leader, List.of(lead));
				assertEquals(responder, game.getCurrentPlayer());
				assertThrows(IllegalArgumentException.class,
						() -> game.play(responder, List.of(lower)));
				return;
			}
		}
		throw new AssertionError("Expected a deterministic lower-ranked response");
	}

	@Test
	void passingSkipsThatPlayerUntilTheTrickClears() {
		List<String> names = NAMES.subList(0, 4);
		PresidentsGame game = new PresidentsGame(names);
		game.start(new Random(7));
		String leader = game.getCurrentPlayer();
		game.play(leader, List.of(new Card(1, 0)));
		String passer = game.getCurrentPlayer();
		game.pass(passer);
		assertEquals(nextName(names, passer), game.getCurrentPlayer());
		assertFalse(passer.equals(game.getCurrentPlayer()));
	}

	@Test
	void lastPlayerToPlayLeadsAfterAllOtherPlayersPass() {
		PresidentsGame game = startedGame();
		String leader = game.getCurrentPlayer();
		game.play(leader, List.of(new Card(1, 0)));
		while (game.getCardsInPlay() != 0) game.pass(game.getCurrentPlayer());
		assertEquals(leader, game.getCurrentPlayer());
		assertEquals(0, game.getCardsInPlay());
		assertEquals(0, game.getValueInPlay());
		assertThrows(IllegalStateException.class, () -> game.pass(leader));
	}

	@Test
	void returnedHandsAreDefensiveCopies() {
		PresidentsGame game = startedGame();
		String player = game.getCurrentPlayer();
		Hand copy = game.getHand(player);
		int originalSize = copy.getSize();
		copy.removeCard(0);
		assertEquals(originalSize, game.getHand(player).getSize());
	}

	@Test
	void completeGamesTerminateAndRankEveryPlayerForTwoThroughSevenPlayers() {
		for (int playerCount = 2; playerCount <= 7; playerCount++) {
			List<String> names = NAMES.subList(0, playerCount);
			PresidentsGame game = new PresidentsGame(names);
			game.start(new Random(300 + playerCount));
			playToCompletion(game);

			assertEquals(PresidentsGame.Status.FINISHED, game.getStatus());
			assertEquals(playerCount, game.getFinishOrder().size());
			assertEquals(playerCount, new HashSet<String>(game.getFinishOrder()).size());
			assertTrue(game.getHand(game.getFinishOrder().getLast()).getSize() > 0);
		}
	}

	private void playToCompletion(PresidentsGame game) {
		for (int turns = 0; turns < 5000 && game.getStatus() == PresidentsGame.Status.PLAYING; turns++) {
			String player = game.getCurrentPlayer();
			Hand hand = game.getHand(player);
			if (hand.contains(new Card(1, 0)) && game.getValueInPlay() == 0
					&& game.getFinishOrder().isEmpty()) {
				game.play(player, cardsOfValue(hand, 1));
			} else if (game.getCardsInPlay() == 0) {
				game.play(player, cardsOfValue(hand, hand.getCard(0).getValue()));
			} else {
				List<Card> legal = firstLegalGroup(hand, game.getValueInPlay(), game.getCardsInPlay());
				if (legal.isEmpty()) game.pass(player);
				else game.play(player, legal);
			}
		}
	}

	private List<Card> firstLegalGroup(Hand hand, int minimumValue, int minimumCount) {
		for (int value = minimumValue; value <= 13; value++) {
			List<Card> cards = cardsOfValue(hand, value);
			if (cards.size() >= minimumCount) return cards;
		}
		return List.of();
	}

	private List<Card> cardsOfValue(Hand hand, int value) {
		ArrayList<Card> cards = new ArrayList<Card>();
		for (Card card : hand) if (card.getValue() == value) cards.add(card);
		return cards;
	}

	private String nextName(List<String> names, String current) {
		return names.get((names.indexOf(current) + 1) % names.size());
	}

	private PresidentsGame startedGame() {
		PresidentsGame game = new PresidentsGame(NAMES.subList(0, 4));
		game.start(new Random(7));
		return game;
	}
}
