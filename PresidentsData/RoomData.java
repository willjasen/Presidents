package PresidentsData;

import java.util.ArrayList;

import PresidentsPlayer.Card;
import PresidentsPlayer.Hand;

/**
 * 
 * @author willjasen
 *
 */
public class RoomData extends Data {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6796377914815346078L;
	private Hand hand;
	private ArrayList<String> playerNames;
	
	public RoomData() {
		hand = new Hand();
		playerNames = new ArrayList<String>(0);
		createRandomHand();
	}
	
	public RoomData(Hand hand) {
		this.hand = hand;
	}
	
	public Hand getHand() {
		return hand;
	}
	
	public void setHand(Hand hand) {
		this.hand = hand;
	}
	
	public void setPlayerNames(ArrayList<String> names) {
		this.playerNames = names;
	}
	
	public ArrayList<String> getPlayerNames() {
		return playerNames;
	}
	
	public void createRandomHand() {
		for (int i = 1; i <= 13; i++) {
			for (int j = 0; j < 1; j++) {
				hand.addCard(new Card(i, j));
			}
		}
	}
	
}
