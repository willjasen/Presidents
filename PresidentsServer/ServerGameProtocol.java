package PresidentsServer;

import java.util.ArrayList;
import java.util.Random;

import PresidentsData.CommandData;
import PresidentsData.Data;
import PresidentsData.RoomData;
import PresidentsData.UserCommandData;
import PresidentsPlayer.Card;
import PresidentsPlayer.Hand;
import PresidentsPlayer.Players;

public class ServerGameProtocol {

	private GameRoom gameRoom;
	private ArrayList<Card> deck;
	private int state;

	private ServerGameProtocol() {
		deck = new ArrayList<Card>(52);
		makeDeck();
		shuffleDeck();
	}

	public ServerGameProtocol(GameRoom gameRoom) {
		this();
		this.gameRoom = gameRoom;
		state = 0;
	}

	/*public Data processInput(Data dataInput) {
		Data dataOutput = null;
		if (((CommandData) dataInput).getCommand().equals("GETROOMINFO")) {
			RoomData roomData = new RoomData();
			
		}
		return dataOutput;
	}*/

	public Data processInput(UserCommandData dataInput, String username) {
		RoomData dataOutput = null;
		if (dataInput.getCommand().equals("GETROOMINFO")) {
			System.out.println("Room size is: " + gameRoom.getSize());
			Hand hand = new Hand();
			for(int i=0;i<13;i++) {
				Card card = deck.remove(0);
				hand.addCard(card);
			}
			gameRoom.dealHandToPlayer(hand,username);
			while(gameRoom.getSize()!=2) {
				// don't do anything
			}
			dataOutput = new RoomData();
			dataOutput.setHand(hand);
			dataOutput.setPlayerNames(gameRoom.getPlayerNames());
		}
		return dataOutput;
	}

	/**
	 * Fill the deck with all possible card values of a standard 52 card deck.
	 */
	private void makeDeck() {
		for (int i = 1; i <= 13; i++) {
			for (int j = 0; j < 4; j++) {
				deck.add(new Card(i, j));
			}
		}
	}

	/**
	 * Shuffles the deck of cards by randomly swapping cards within the deck.
	 */
	private void shuffleDeck() {

		Random generator = new Random();
		int j, k;

		for (int i = 0; i < 52; i++) {
			j = generator.nextInt(52);
			k = generator.nextInt(52);
			Card temp = deck.get(j);
			deck.set(j, deck.get(k));
			deck.set(k, temp);
		}
	}

	/**
	 * @return each card in the deck printed sequentially
	 */
	@Override
	public String toString() {
		String cardDeck = "";
		for (Card card : deck) {
			cardDeck += card + " ";
		}
		return cardDeck;
	}
}
