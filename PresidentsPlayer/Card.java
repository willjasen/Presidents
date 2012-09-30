package PresidentsPlayer;

import java.io.Serializable;

import javax.swing.JLabel;

/**
 * Represents a playing card.
 * 
 * @author willjasen
 * 
 */
public class Card implements Serializable {

	private static final long serialVersionUID = 4404210030845458568L;

	/**
	 * Constants of card values.
	 * 
	 * Two is the highest value in Presidents. Three is the lowest value in
	 * Presidents.
	 */
	private static final int TWO = 13;
	private static final int ACE = 12;
	private static final int KING = 11;
	private static final int QUEEN = 10;
	private static final int JACK = 9;
	private static final int TEN = 8;
	private static final int NINE = 7;
	private static final int EIGHT = 6;
	private static final int SEVEN = 5;
	private static final int SIX = 4;
	private static final int FIVE = 3;
	private static final int FOUR = 2;
	private static final int THREE = 1;
	private static final int NOTHING = 0;
	private static final int NULL = -1;
	private static final int PASS = 20;

	/**
	 * Constants of card suit.
	 * 
	 * For organizational purpose, notice that the order of suits uses the
	 * CHaSeD sequence.
	 */
	private static final int CLUBS = 0;
	private static final int HEARTS = 1;
	private static final int SPADES = 2;
	private static final int DIAMONDS = 3;

	public static int charToValue(char c) {
		// converts a character to a card value

		int card = 0;

		switch (c) {
		case '3':
			card = THREE;
			break;
		case '4':
			card = FOUR;
			break;
		case '5':
			card = FIVE;
			break;
		case '6':
			card = SIX;
			break;
		case '7':
			card = SEVEN;
			break;
		case '8':
			card = EIGHT;
			break;
		case '9':
			card = NINE;
			break;
		case 'T':
			card = TEN;
			break;
		case 'J':
			card = JACK;
			break;
		case 'Q':
			card = QUEEN;
			break;
		case 'K':
			card = KING;
			break;
		case 'A':
			card = ACE;
			break;
		case '2':
			card = TWO;
			break;
		case 'P':
			card = PASS;
			break;
		default:
			card = NULL;
		}

		return card;
	}

	public static char valueToChar(int value) {
		// converts a card value to a character

		char card = ' ';

		switch (value) {
		case NOTHING:
			card = ' ';
			break;
		case THREE:
			card = '3';
			break;
		case FOUR:
			card = '4';
			break;
		case FIVE:
			card = '5';
			break;
		case SIX:
			card = '6';
			break;
		case SEVEN:
			card = '7';
			break;
		case EIGHT:
			card = '8';
			break;
		case NINE:
			card = '9';
			break;
		case TEN:
			card = 'T';
			break;
		case JACK:
			card = 'J';
			break;
		case QUEEN:
			card = 'Q';
			break;
		case KING:
			card = 'K';
			break;
		case ACE:
			card = 'A';
			break;
		case TWO:
			card = '2';
			break;
		}

		return card;
	}

	private int faceValue;
	private int suit;

	/**
	 * Used for the GUI.
	 */
	private JLabel cardLabel;
	private boolean clicked = false;

	private Card() {
		faceValue = 0;
	}

	public Card(int value) {
		// creates a card with the passed value
		faceValue = value;
	}

	public Card(int faceValue, int suit) {
		this.faceValue = faceValue;
		this.suit = suit;
	}

	public char getChar() {
		return valueToChar(faceValue);
	}

	public JLabel getLabel() {
		return cardLabel;
	}

	/**
	 * 
	 * @return suit of this card, represented by a character
	 */
	public char getSuit() {
		return getSuitFromInt();
	}

	private char getSuitFromInt() {
		char suitChar = ' ';

		switch (suit) {
		case 0:
			suitChar = 'c';
			break;
		case 1:
			suitChar = 'h';
			break;
		case 2:
			suitChar = 's';
			break;
		case 3:
			suitChar = 'd';
			break;
		}

		return suitChar;
	}

	public int getValue() {
		return faceValue;
	}

	/**
	 * Tests if the card has been chosen or unchosen on the GUI.
	 * 
	 * @return whether or not this card has been chosen
	 */
	public boolean hasBeenClicked() {
		return clicked;
	}

	public void setLabel(JLabel label) {
		cardLabel = label;
	}

	/**
	 * Sets the suit of this card. Valid values are 'c', 'h', 's', or 'd'.
	 * Notice that the suit order follows the CHaSeD sequence.
	 * 
	 * @param suit
	 *            character of the represented suit
	 */
	public void setSuit(char suit) {
		if (suit == 'c' || suit == 'h' || suit == 's' || suit == 'd')
			this.suit = suit;
		else
			System.out.println("Cannot set suit - incorrect suit.");
	}

	public void switchClicked() {
		if (clicked == true)
			clicked = false;
		else
			clicked = true;
	}

	@Override
	public String toString() {
		// return the card's value and a space
		return String.valueOf(faceValue) + String.valueOf(getSuitFromInt());
	}
}