package PresidentsPlayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

class PresidentsGameTest {
	@Test
	void dealsTheWholeDeckAndThreeOfClubsStarts() {
		PresidentsGame game = new PresidentsGame(List.of("Ada", "Grace", "Linus", "James"));
		game.start(new Random(7));
		int cards = 0;
		for (String player : List.of("Ada", "Grace", "Linus", "James")) {
			cards += game.getHand(player).getSize();
		}
		assertEquals(52, cards);
		assertEquals(true, game.getHand(game.getCurrentPlayer()).contains(new Card(1, 0)));
	}

	@Test
	void supportsUpToSevenPlayers() {
		List<String> players = List.of("Ada", "Grace", "Linus", "James", "Margaret", "Alan", "Katherine");
		PresidentsGame game = new PresidentsGame(players);
		game.start(new Random(7));
		int cards = players.stream().mapToInt(player -> game.getHand(player).getSize()).sum();
		assertEquals(52, cards);
		assertEquals(true, game.getHand(game.getCurrentPlayer()).contains(new Card(1, 0)));
	}

	@Test
	void rejectsMoreThanSevenPlayers() {
		assertThrows(IllegalArgumentException.class, () -> new PresidentsGame(
				List.of("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight")));
	}

	@Test
	void cannotPassWhenLeading() {
		PresidentsGame game = startedGame();
		assertThrows(IllegalStateException.class, () -> game.pass(game.getCurrentPlayer()));
	}

	@Test
	void playRemovesCardsAndAdvancesTurn() {
		PresidentsGame game = startedGame();
		String leader = game.getCurrentPlayer();
		Card threeOfClubs = new Card(1, 0);
		int oldSize = game.getHand(leader).getSize();
		game.play(leader, List.of(threeOfClubs));
		assertEquals(oldSize - 1, game.getHand(leader).getSize());
		assertEquals(1, game.getCardsInPlay());
	}

	@Test
	void winnerOfTheTrickLeadsAgainAfterEveryoneElsePasses() {
		PresidentsGame game = startedGame();
		String leader = game.getCurrentPlayer();
		game.play(leader, List.of(new Card(1, 0)));
		while (game.getCardsInPlay() != 0) game.pass(game.getCurrentPlayer());

		assertEquals(leader, game.getCurrentPlayer());
		assertEquals(0, game.getValueInPlay());
	}

	@Test
	void responseMayIncreaseTheNumberOfCards() {
		for (int seed = 0; seed < 100; seed++) {
			PresidentsGame game = new PresidentsGame(List.of("Ada", "Grace", "Linus", "James"));
			game.start(new Random(seed));
			game.play(game.getCurrentPlayer(), List.of(new Card(1, 0)));
			String responder = game.getCurrentPlayer();
			Hand hand = game.getHand(responder);
			for (int i = 0; i < hand.getSize(); i++) {
				Card first = hand.getCard(i);
				for (int j = i + 1; j < hand.getSize(); j++) {
					Card second = hand.getCard(j);
					if (first.getValue() == second.getValue() && first.getValue() >= 1) {
						int oldSize = hand.getSize();
						game.play(responder, List.of(first, second));
						assertEquals(oldSize - 2, game.getHand(responder).getSize());
						assertEquals(2, game.getCardsInPlay());
						return;
					}
				}
			}
		}
		throw new AssertionError("Expected a deterministic deal with a playable pair");
	}

	@Test
	void aCompleteGameRanksEveryPlayerInTheOrderTheyGoOut() {
		List<String> players = List.of("Ada", "Grace", "Linus", "James");
		PresidentsGame game = new PresidentsGame(players);
		game.start(new Random(19));
		boolean openingPlay = true;

		for (int turns = 0; turns < 2000
				&& game.getStatus() == PresidentsGame.Status.PLAYING; turns++) {
			String player = game.getCurrentPlayer();
			Hand hand = game.getHand(player);
			if (openingPlay) {
				game.play(player, List.of(new Card(1, 0)));
				openingPlay = false;
			} else if (game.getCardsInPlay() == 0) {
				game.play(player, List.of(hand.getCard(0)));
			} else {
				ArrayList<Card> legal = cardsAtOrAbove(hand, game.getValueInPlay(),
						game.getCardsInPlay());
				if (legal.isEmpty()) game.pass(player);
				else game.play(player, legal);
			}
		}

		assertEquals(PresidentsGame.Status.FINISHED, game.getStatus());
		assertEquals(players.size(), game.getFinishOrder().size());
		assertEquals(players.size(), new HashSet<String>(game.getFinishOrder()).size());
		for (int i = 0; i < game.getFinishOrder().size() - 1; i++) {
			assertEquals(0, game.getHand(game.getFinishOrder().get(i)).getSize());
		}
		assertTrue(game.getHand(game.getFinishOrder().getLast()).getSize() > 0);
	}

	private ArrayList<Card> cardsAtOrAbove(Hand hand, int minimumValue, int count) {
		for (int value = minimumValue; value <= 13; value++) {
			ArrayList<Card> cards = new ArrayList<Card>();
			for (Card card : hand) {
				if (card.getValue() == value && cards.size() < count) cards.add(card);
			}
			if (cards.size() == count) return cards;
		}
		return new ArrayList<Card>();
	}

	private PresidentsGame startedGame() {
		PresidentsGame game = new PresidentsGame(List.of("Ada", "Grace", "Linus", "James"));
		game.start(new Random(7));
		return game;
	}
}
