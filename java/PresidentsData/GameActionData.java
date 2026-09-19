package PresidentsData;

import java.util.ArrayList;
import java.util.List;

import PresidentsPlayer.Card;

/** A player's requested action. The server remains authoritative. */
public final class GameActionData extends UserCommandData {
	private static final long serialVersionUID = 6504683814824688286L;

	public static final String START = "START_GAME";
	public static final String PLAY = "PLAY_CARDS";
	public static final String PASS = "PASS";
	public static final String GET_STATE = "GET_GAME_STATE";

	private final ArrayList<Card> cards = new ArrayList<Card>();

	public GameActionData(String action) {
		super(action);
	}

	public GameActionData(String action, List<Card> selectedCards) {
		this(action);
		if (selectedCards != null) cards.addAll(selectedCards);
	}

	public ArrayList<Card> getCards() {
		return new ArrayList<Card>(cards);
	}
}
