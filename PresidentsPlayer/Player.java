package PresidentsPlayer;

import java.net.*;

/**
 * 
 * @author willjasen
 * 
 */
public class Player {

	/**
	 * Constants
	 */
	protected final static int PRESIDENT=0;
	protected final static int VICE=1;
	protected final static int SECRETARY=2;
	protected final static int INTERN=3;
	
	// user info
	protected String username; // the name of the player

	// game fields
	protected boolean turn; // whether it is this player's turn
	protected boolean pass; // whether the player has passed

	protected int cardValuePlayed; // the card value played by the player
	protected int numOfCardsPlayed; // the num of cards played by the player

	protected int intPosition;
	
	protected Hand hand; // the player's hand

	public Player() {
		turn = false;
		pass = false;
		hand = new Hand();
		cardValuePlayed = 0;
		numOfCardsPlayed = 0;
	}

	public Player(String username) {
		this();
		this.username = username;
	}

	@Override
	public String toString() {
		return "Player: " + username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public String getStatus() {
		String status;

		if (intPosition == 1)
			status = "President";
		else if (intPosition == 2)
			status = "Vice President";
		else if (intPosition == 3)
			status = "Secretary";
		else if (intPosition == 4)
			status = "Intern";
		else
			status = "of no status.";

		return status;
	}

	public int getPosition() {
		return intPosition;
	}

	public void setStatus(int status) {
		intPosition = status;
	}

	public void setTurn(boolean t) {
		turn = t;
	}

	public void setPass(boolean p) {
		pass = p;
	}

	public void setCardValuePlayed(int cardValue) {
		cardValuePlayed = cardValue;
	}

	public int getCardValuePlayed() {
		return cardValuePlayed;
	}

	public int getNumOfCardsPlayed() {
		return numOfCardsPlayed;
	}

	public boolean getPass() {
		return pass;
	}

	public boolean getTurn() {
		return turn;
	}

	public String getName() {
		return username;
	}

	public Hand getHand() {
		return hand;
	}

	protected void sortHand() {

		/*
		 * for(Card card : hand) { int lowest = 14; int place = 0;
		 * 
		 * }
		 */
		for (int i = 0; i < getHand().getSize(); i++) {
			int lowest = 14;
			int place = 0;
			for (int j = i; j < getHand().getSize(); j++) {
				if (hand.getCard(j).getValue() < lowest) {
					lowest = hand.getCard(j).getValue();
					place = j;
				}
			}

			Card temp = hand.getCard(place);
			hand.setCard(place, hand.getCard(i));
			hand.setCard(i, temp);
		}
	}

	protected boolean searchHand(Hand temp, int value, int num) {
		// returns false if the number of the cards in the hand is less than
		// that in play

		int tempcardcount = 0;
		for (int j = 0; j < getHand().getSize(); j++) {
			if (temp.getCard(j).getValue() == value) {
				tempcardcount++;
			}
		}
		if (tempcardcount < num) {
			return true;
		}
		return false;
	}

	protected void removeCardsFromHand(int valueOfCardInPlay,
			int numOfCardsInPlay) {
		// remove cards from hand
		int numberOfCardsRemoved = 0;
		for (int i = 0; i < getHand().getSize(); i++) {
			if (getHand().getCard(i).getValue() == valueOfCardInPlay) {
				if (numberOfCardsRemoved != numOfCardsInPlay) {
					getHand().removeCard(i);
					numberOfCardsRemoved++;
					i--;
				} else {
					return;
				}
			}
		}
	}

}