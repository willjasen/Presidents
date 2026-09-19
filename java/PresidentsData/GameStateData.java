package PresidentsData;

import java.util.ArrayList;
import java.util.List;

import PresidentsPlayer.Card;
import PresidentsPlayer.Hand;

/** Immutable-on-the-wire view of a room, tailored to one player. */
public final class GameStateData extends Data {
	private static final long serialVersionUID = 7728066059595303929L;

	private final String roomName;
	private final String status;
	private final String currentPlayer;
	private final int cardsInPlay;
	private final int valueInPlay;
	private final Hand hand;
	private final ArrayList<String> playerNames;
	private final ArrayList<Integer> cardCounts;
	private final ArrayList<String> finishOrder;
	private final String message;

	public GameStateData(String roomName, String status, String currentPlayer,
			int cardsInPlay, int valueInPlay, Hand hand, List<String> playerNames,
			List<Integer> cardCounts, List<String> finishOrder, String message) {
		this.roomName = roomName;
		this.status = status;
		this.currentPlayer = currentPlayer;
		this.cardsInPlay = cardsInPlay;
		this.valueInPlay = valueInPlay;
		this.hand = hand == null ? new Hand() : hand;
		this.playerNames = new ArrayList<String>(playerNames);
		this.cardCounts = new ArrayList<Integer>(cardCounts);
		this.finishOrder = new ArrayList<String>(finishOrder);
		this.message = message;
	}

	public String getRoomName() { return roomName; }
	public String getStatus() { return status; }
	public String getCurrentPlayer() { return currentPlayer; }
	public int getCardsInPlay() { return cardsInPlay; }
	public int getValueInPlay() { return valueInPlay; }
	public Hand getHand() { return hand; }
	public ArrayList<String> getPlayerNames() { return new ArrayList<String>(playerNames); }
	public ArrayList<Integer> getCardCounts() { return new ArrayList<Integer>(cardCounts); }
	public ArrayList<String> getFinishOrder() { return new ArrayList<String>(finishOrder); }
	public String getMessage() { return message; }
}
