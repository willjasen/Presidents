package PresidentsPlayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CardAndHandTest {
	@Test
	void characterSuitRoundTrips() {
		Card card = new Card(5);
		card.setSuit('d');
		assertEquals('d', card.getSuit());
	}

	@Test
	void removingOneCardDoesNotRemoveEveryCardOfThatRank() {
		Hand hand = new Hand();
		hand.addCard(new Card(4, 0));
		hand.addCard(new Card(4, 1));
		hand.removeCard(new Card(4, 0));
		assertEquals(1, hand.getSize());
		assertEquals('h', hand.getCard(0).getSuit());
	}

	@Test
	void rejectsUnknownSuit() {
		Card card = new Card(5);
		assertThrows(IllegalArgumentException.class, () -> card.setSuit('x'));
	}
}
