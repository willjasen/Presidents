package PresidentsPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Server-authoritative rules for one game of Presidents.
 *
 * The restored rules follow the intent in the original Player class: threes
 * are low, twos are high, a response contains the same number or more equal or
 * higher cards, and a player may not pass when leading a new trick.
 */
public final class PresidentsGame {

	public enum Status { WAITING, PLAYING, FINISHED }

	private final List<String> playerOrder;
	private final Map<String, Hand> hands = new LinkedHashMap<String, Hand>();
	private final Set<String> passed = new LinkedHashSet<String>();
	private final List<String> finishOrder = new ArrayList<String>();

	private Status status = Status.WAITING;
	private int currentPlayerIndex;
	private int cardsInPlay;
	private int valueInPlay;
	private String trickLeader;
	private boolean firstPlay = true;

	public PresidentsGame(List<String> playerNames) {
		if (playerNames == null || playerNames.size() < 2 || playerNames.size() > 7) {
			throw new IllegalArgumentException("A game requires two to seven players");
		}
		LinkedHashSet<String> uniqueNames = new LinkedHashSet<String>();
		for (String name : playerNames) {
			if (name == null || name.trim().isEmpty()) {
				throw new IllegalArgumentException("Player names cannot be blank");
			}
			uniqueNames.add(name);
		}
		if (uniqueNames.size() != playerNames.size()) {
			throw new IllegalArgumentException("Player names must be unique");
		}
		playerOrder = List.copyOf(uniqueNames);
	}

	public synchronized void start(Random random) {
		if (status != Status.WAITING) {
			throw new IllegalStateException("The game has already started");
		}
		List<Card> deck = createDeck();
		Collections.shuffle(deck, random);
		for (String player : playerOrder) hands.put(player, new Hand());
		for (int i = 0; i < deck.size(); i++) {
			hands.get(playerOrder.get(i % playerOrder.size())).addCard(deck.get(i));
		}
		currentPlayerIndex = findThreeOfClubsHolder();
		status = Status.PLAYING;
	}

	public synchronized void play(String player, List<Card> cards) {
		requireTurn(player);
		if (cards == null || cards.isEmpty()) {
			throw new IllegalArgumentException("Choose at least one card");
		}
		int value = cards.get(0).getValue();
		for (Card card : cards) {
			if (card.getValue() != value) {
				throw new IllegalArgumentException("Every card in a play must have the same rank");
			}
		}
		if (firstPlay && !cards.contains(new Card(1, 0))) {
			throw new IllegalArgumentException("The opening play must include the three of clubs");
		}
		if (cardsInPlay != 0 && cards.size() < cardsInPlay) {
			throw new IllegalArgumentException("Play at least as many cards as the current trick");
		}
		if (cardsInPlay != 0 && value < valueInPlay) {
			throw new IllegalArgumentException("Play cards equal to or higher than the current rank");
		}

		Hand hand = hands.get(player);
		List<Card> remaining = hand.asList();
		for (Card card : cards) {
			if (!remaining.remove(card)) {
				throw new IllegalArgumentException("The selected card is not in the player's hand");
			}
		}
		for (Card card : cards) hand.removeCard(card);

		cardsInPlay = cards.size();
		valueInPlay = value;
		trickLeader = player;
		firstPlay = false;
		passed.remove(player);
		if (hand.getSize() == 0) finishOrder.add(player);
		completeGameIfNeeded();
		if (status == Status.PLAYING) advanceTurn();
	}

	public synchronized void pass(String player) {
		requireTurn(player);
		if (cardsInPlay == 0) {
			throw new IllegalStateException("The leader of a new trick cannot pass");
		}
		passed.add(player);
		advanceTurn();
	}

	private void advanceTurn() {
		if (shouldClearTrick()) {
			clearTrick();
			int leaderIndex = indexOf(trickLeader);
			currentPlayerIndex = hasFinished(trickLeader)
					? nextActiveIndex(leaderIndex) : leaderIndex;
			return;
		}
		currentPlayerIndex = nextEligibleIndex(currentPlayerIndex);
	}

	private boolean shouldClearTrick() {
		int eligibleOpponents = 0;
		for (String player : playerOrder) {
			if (!player.equals(trickLeader) && !hasFinished(player) && !passed.contains(player)) {
				eligibleOpponents++;
			}
		}
		return eligibleOpponents == 0;
	}

	private void clearTrick() {
		cardsInPlay = 0;
		valueInPlay = 0;
		passed.clear();
	}

	private void completeGameIfNeeded() {
		if (finishOrder.size() == playerOrder.size() - 1) {
			for (String player : playerOrder) {
				if (!finishOrder.contains(player)) finishOrder.add(player);
			}
			status = Status.FINISHED;
		}
	}

	private void requireTurn(String player) {
		if (status != Status.PLAYING) throw new IllegalStateException("The game is not in progress");
		if (!getCurrentPlayer().equals(player)) throw new IllegalStateException("It is not " + player + "'s turn");
	}

	private int findThreeOfClubsHolder() {
		Card threeOfClubs = new Card(1, 0);
		for (int i = 0; i < playerOrder.size(); i++) {
			if (hands.get(playerOrder.get(i)).contains(threeOfClubs)) return i;
		}
		throw new IllegalStateException("The deck does not contain the three of clubs");
	}

	private int nextEligibleIndex(int fromIndex) {
		int index = fromIndex;
		for (int i = 0; i < playerOrder.size(); i++) {
			index = (index + 1) % playerOrder.size();
			String player = playerOrder.get(index);
			if (!hasFinished(player) && !passed.contains(player)) return index;
		}
		throw new IllegalStateException("No player is eligible to take a turn");
	}

	private int nextActiveIndex(int fromIndex) {
		int index = fromIndex;
		for (int i = 0; i < playerOrder.size(); i++) {
			index = (index + 1) % playerOrder.size();
			if (!hasFinished(playerOrder.get(index))) return index;
		}
		throw new IllegalStateException("No active player remains");
	}

	private int indexOf(String player) {
		return playerOrder.indexOf(player);
	}

	private boolean hasFinished(String player) {
		return finishOrder.contains(player);
	}

	private static List<Card> createDeck() {
		List<Card> deck = new ArrayList<Card>(52);
		for (int value = 1; value <= 13; value++) {
			for (int suit = 0; suit < 4; suit++) deck.add(new Card(value, suit));
		}
		return deck;
	}

	public synchronized Status getStatus() { return status; }
	public synchronized String getCurrentPlayer() { return playerOrder.get(currentPlayerIndex); }
	public synchronized int getCardsInPlay() { return cardsInPlay; }
	public synchronized int getValueInPlay() { return valueInPlay; }
	public synchronized List<String> getFinishOrder() { return List.copyOf(finishOrder); }
	public synchronized Hand getHand(String player) {
		Hand source = hands.get(player);
		if (source == null) throw new IllegalArgumentException("Unknown player: " + player);
		Hand copy = new Hand();
		copy.addCards(source.asList());
		return copy;
	}
}
