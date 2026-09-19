package PresidentsPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.net.Socket;

/**
 * This class should no longer be used.
 * 
 * @deprecated Its methods have been moved to the class GameRoom. This decision
 *             makes the code more readable.
 * 
 * @author willjasen
 * 
 */
@Deprecated
public class Players implements Iterable<Player> {

	private static final long serialVersionUID = -3850661933324348531L;

	private ArrayList<Player> players;

	/**
	 * Creates a list of players with size 0
	 */
	public Players() {
		players = new ArrayList<Player>(0);
	}

	/**
	 * Creates a list of players with a specified size
	 * 
	 * @param num
	 *            the inital size of the list
	 */
	public Players(int num) {
		players = new ArrayList<Player>(num);
	}

	/**
	 * Return a player at an index.
	 * 
	 * @param index
	 *            index of player
	 * @return player at given index
	 */
	public synchronized Player getPlayer(int index) {
		return players.get(index);
	}

	/**
	 * Return a player with a specified username.
	 * 
	 * @param username
	 *            username of player
	 * @return player with given username
	 */
	public synchronized Player getPlayer(String username) {
		Player player = null;
		for (int i = 0; i < getSize(); i++) {
			player = players.get(i);
			if (player.getName().equals(username)) {
				break;
			}
		}
		return player;
	}

	/**
	 * Return a player with a specified socket.
	 * 
	 * @param socket
	 *            socket of player
	 * @return player with given socket
	 */
	public synchronized Player getPlayer(Socket socket) {
		Player player = null;
		for (int i = 0; i < players.size(); i++) {
			player = players.get(i);
			if (player instanceof Human)
				if (((Human) player).getSocket().equals(socket)) {
					break;
				}
		}
		return player;
	}

	/**
	 * Adds a username and a login token to a player at a specified index.
	 * 
	 * @param index
	 *            index of the player
	 * @param username
	 *            username to give to the player
	 * @param loginToken
	 *            login token to give to the player
	 */
	public synchronized void addNameToPlayer(int index, String username,
			String loginToken) {
		((Human) getPlayer(index)).setUsername(username);
		((Human) getPlayer(index)).setLoginToken(loginToken);
	}

	/**
	 * Returns the number of current players.
	 * 
	 * @return number of players
	 */
	public synchronized int getSize() {
		return players.size();
	}

	/**
	 * Removes a player at a specified index and returns the player.
	 * 
	 * @param index
	 *            index of player
	 * @return the removed player
	 */
	public synchronized Player removePlayer(int index) {
		return players.remove(index);
	}

	/**
	 * Removes a specified player and returns whether or not the specified
	 * player was removed.
	 * 
	 * @param player
	 *            player to be removed
	 * @return if the player was removed
	 */
	public synchronized boolean removePlayer(Player player) {
		return players.remove(player);
	}

	/**
	 * Removes a specified player and returns the player.
	 * 
	 * @param username
	 *            username of the player to remove
	 * @return the removed player
	 */
	public synchronized Player removePlayer(String username) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getName().equals(username)) {
				return removePlayer(i);
			}
		}

		return null;
	}

	/**
	 * Adds a player to the current list of players.
	 * 
	 * @param player
	 *            player to add
	 */
	public synchronized void addPlayer(Player player) {
		players.add(player);
	}

	/**
	 * Prints the number of players.
	 */
	public String toString() {
		return String.valueOf(getSize());
	}

	@Override
	public Iterator<Player> iterator() {
		// TODO Auto-generated method stub
		return players.iterator();
	}
}