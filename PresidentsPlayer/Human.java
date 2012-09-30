package PresidentsPlayer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 
 * @author willjasen
 * 
 */
public class Human extends Player {
	
	protected Socket socket; // socket for the game
	protected Socket chatSocket; // socket for chat
	protected String roomName; // current room name
	protected String loginToken;

	public Human() {
		super();
	}

	public Human(String username) {
		super(username);
	}
	
	public Human(Socket socket) {
		this();
		this.socket=socket;
	}
	
	public Human(String username, String loginToken) {
		this.username = username;
		this.loginToken = loginToken;
	}
	
	public void setHand(Hand hand) {
		super.hand = hand;
	}
	
	public String getRoomName() {
		return roomName;
	}
	
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	
	public Socket getChatSocket() {
		return chatSocket;
	}

	public Socket getSocket() {
		return socket;
	}
	
	public void addChatSocket(Socket chatSocket) {
		this.chatSocket = chatSocket;
	}
	
	public void setSocket(Socket sock) {
		socket = sock;
	}
	
	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}
	
	public String getLoginToken() {
		return loginToken;
	}

	/*private int getCardValueInput() {
		String message = "";

		try {
			message = console.readLine().toUpperCase();
		} catch (StringIndexOutOfBoundsException nfe) {
			System.out.println("You cannot enter a blank value.");
		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			return Card.charToValue(message.charAt(0));
		} catch (StringIndexOutOfBoundsException nfe) {
			System.out.println("You cannot enter a blank value.");
		} catch (Exception e) {
			System.out.println(e);
		}
		return -1;
	}*/

	/*private int getNumOfCardsInput() {
		String message = "";

		try {
			message = console.readLine();
		} catch (NumberFormatException nfe) {
			System.out.println("You cannot enter a blank value.");
		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			return Integer.parseInt(message);
		} catch (NumberFormatException nfe) {
			System.out.println("You cannot enter a blank value.");
		} catch (Exception e) {
			System.out.println(e);
		}
		return 0;
	}*/

	/*public void playExistingRound(int valueOfCardInPlay, int numOfCardsInPlay) {
		int intCardValueInput = 0;
		int intNumOfCardsInput = numOfCardsInPlay;
		boolean haveCards;

		if (!getPass()) {
			System.out.println();
			System.out.println("Card in play: "
					+ Card.valueToChar(valueOfCardInPlay));
			System.out.println("Number of cards in play is: "
					+ numOfCardsInPlay);
			System.out.println();
			System.out.println(getHand());
			System.out.println();

			do {
				System.out
						.print("Which card value will you play? (or type P to pass.):  ");
				intCardValueInput = getCardValueInput();

				if (intCardValueInput != -1) {
					if (intCardValueInput == 20) // if a player passes
					{
						setPass(true);
						System.out.println("You passed.");
						return;
					}

					if (searchHand(getHand(), intCardValueInput,
							numOfCardsInPlay)) {
						haveCards = false;
						System.out
								.print("You don't have those cards in your hand.\n");
					} else {
						haveCards = true;
					}

					if (intNumOfCardsInput != numOfCardsInPlay) {
						System.out
								.println("You must play the correct number of cards.\n");
					}

					if (intCardValueInput < valueOfCardInPlay) {
						System.out
								.println("You must play a equal or higher card.\n");
					}
				} else {
					haveCards = false;
				}
			} while ((intCardValueInput < valueOfCardInPlay)
					|| (intNumOfCardsInput != numOfCardsInPlay) || !haveCards);

			cardValuePlayed = intCardValueInput;
			numOfCardsPlayed = intNumOfCardsInput;
			removeCardsFromHand(cardValuePlayed, numOfCardsPlayed);
		}
	}

	public void playNewRound() {
		int intCardValueInput = 0;
		int intNumOfCardsInput = 0;

		int numOfCardsInPlay = 0;

		boolean haveCards = false;

		clearScreen();
		System.out.println("New round!");
		System.out.println(getHand());

		do {
			System.out
					.print("Which card value will you play? (or type P to pass):  ");
			intCardValueInput = getCardValueInput();

			// code so that passing on first turn isn't allowed

			if (intCardValueInput != -1 && intCardValueInput != 20) {
				do {
					System.out
							.print("How many cards of this value will you play? ");
					intNumOfCardsInput = getNumOfCardsInput();
				} while (intNumOfCardsInput < 1 || intNumOfCardsInput > 4);

				if (searchHand(getHand(), intNumOfCardsInput, numOfCardsInPlay)) {
					haveCards = false;
					System.out
							.println("You don't have those cards in your hand");
				} else {
					haveCards = true;
				}
			}

		} while (!haveCards);

		cardValuePlayed = intCardValueInput;
		numOfCardsPlayed = intNumOfCardsInput;

		removeCardsFromHand(cardValuePlayed, numOfCardsPlayed);

		System.out.println();
	}

	public ArrayList<Card> chooseCardsToSwap() {
		ArrayList<Card> chosenCards = new ArrayList<Card>(0);

		if (getPosition() == 1) {
			int card1value;
			int card2value;
			Card card1;
			Card card2;

			System.out.println(getHand());
			System.out.println();
			System.out
					.println("What is the first card you would like to switch?");
			card1value = getCardValueInput();
			System.out
					.println("What is the second card you would like to switch?");
			card2value = getCardValueInput();

			card1 = new Card(card1value);
			card2 = new Card(card2value);

			chosenCards.add(card1);
			chosenCards.add(card2);

			getHand().removeCardFromHand(card1);
			getHand().removeCardFromHand(card2);

			return chosenCards;
		} else if (getPosition() == 2) {
			int card1value;
			Card card1;

			System.out.println(getHand());
			System.out.println();
			System.out.println("What is the card you would like to switch?");
			card1value = getCardValueInput();

			card1 = new Card(card1value);

			chosenCards.add(card1);

			getHand().removeCardFromHand(card1);

			return chosenCards;
		} else if (getPosition() == 3) {
			System.out
					.println("Because you are Secretary, your best card was automatically chosen.");
			chosenCards.add(getHand().getCardInHand(12));
			getHand().removeCardFromHand(12);
		} else {
			System.out
					.println("Because you are Intern, your two best card were automatically chosen.");
			chosenCards.add(getHand().getCardInHand(12));
			getHand().removeCardFromHand(12);
			chosenCards.add(getHand().getCardInHand(11));
			getHand().removeCardFromHand(11);
		}

		return chosenCards;
	}

	private void clearScreen() {
		for (int i = 0; i < 25; i++) {
			System.out.println();
		}
	}*/

}