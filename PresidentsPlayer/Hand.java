package PresidentsPlayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author willjasen
 * 
 */
public class Hand implements Serializable, Iterable<Card> {

	private static final long serialVersionUID = 516505469152006126L;
	private ArrayList<Card> cards; // ArrayList of Cards

	public Hand() {
		cards = new ArrayList<Card>(13);
	}

	/**
	 * Add a card to this hand.
	 * 
	 * @param card
	 *            card to add
	 */
	public void addCard(Card card) {
		cards.add(card);
	}

	public void addCards(ArrayList<Card> givenCards) {
		cards.addAll(givenCards);
	}

	public void setCard(int index, Card card) {
		cards.set(index, card);
	}

	/**
	 * Remove a card from this hand.
	 * 
	 * @param card
	 *            card to remove
	 */
	public void removeCard(Card card) {
		int i;
		for (i = 0; i < cards.size(); i++) {
			if (getCard(i).getValue() == card.getValue()) {
				cards.remove(i);
			}
		}
	}

	public void removeCard(int index) {
		cards.remove(index);
	}

	public Card getCard(int index) {
		return cards.get(index);
	}

	public int getSize() {
		return cards.size();
	}

	public int indexOf(Card card) {
		return cards.indexOf(card);
	}

	@Override
	public String toString() {
		String str = "";
		for (Card card : cards) {
			str += card + " ";
		}
		return str;
	}

	@Override
	public Iterator<Card> iterator() {
		// TODO Auto-generated method stub
		return cards.iterator();
	}
}