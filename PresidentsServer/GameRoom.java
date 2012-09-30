package PresidentsServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import PresidentsPlayer.Card;
import PresidentsPlayer.Hand;
import PresidentsPlayer.Player;
import PresidentsPlayer.Players;

/**
 * Represents a game room.
 * 
 * @author willjasen
 * 
 */
public class GameRoom {

	private String roomName;
	private HashMap<String, Player> players;
	private ServerGameProtocol sgp;

	private GameRoom() {
		sgp = new ServerGameProtocol(this);
		players = new HashMap<String, Player>();
	}

	public GameRoom(String roomName) {
		this();
		this.roomName = roomName;
	}

	/**
	 * 
	 * @param player
	 *            - player to add
	 */
	public synchronized void addPlayer(Player player) {
		players.put(player.getUsername(), player);
	}

	/**
	 * 
	 * @return game protocol
	 */
	public synchronized ServerGameProtocol getGameProtocol() {
		return sgp;
	}

	/**
	 * 
	 * @return name of the room
	 */
	public synchronized String getName() {
		return roomName;
	}

	/**
	 * Gets a player with a given username
	 * 
	 * @param username
	 *            - username of the player
	 * @return player with the given username
	 */
	public synchronized Player getPlayer(String username) {
		return players.get(username);
	}

	/**
	 * 
	 * @return number of players in the room
	 */
	public synchronized int getSize() {
		return players.size();
	}

	/**
	 * Tests if a given name matches the name of this room.
	 * 
	 * @param comparingName
	 *            - name to test
	 * @return true if given name is equal, false if given name is not equal
	 */
	public synchronized boolean hasName(String comparingName) {
		if (roomName.equals(comparingName))
			return true;
		return false;
	}

	/**
	 * Remove a player from this room by its username.
	 * 
	 * @param username
	 *            - username of the player to remove
	 * @return the player that was removed
	 */
	public synchronized Player removePlayer(String username) {
		return players.remove(username);
	}

	/**
	 * Sets this room's name.
	 * 
	 * @param roomName
	 *            - room name to set
	 */
	public synchronized void setName(String roomName) {
		this.roomName = roomName;
	}

	@Override
	public synchronized String toString() {
		return "Room name: " + roomName;
	}

	/**
	 * Tests if a player with a given username is in this room.
	 * 
	 * @param username
	 *            - username to find
	 * @return true if player is found, false if player is not found
	 */
	public synchronized boolean containsPlayer(String username) {
		return players.containsKey(username);
	}

	public synchronized ArrayList<Player> getPlayers() {
		ArrayList<Player> playersList = new ArrayList<Player>();
		Set playersSet = players.entrySet();
		Iterator<Map.Entry> i = playersSet.iterator();

		while (i.hasNext()) {
			Map.Entry me = i.next();
			playersList.add((Player) me.getValue());
		}

		return playersList;
	}
	
	public synchronized ArrayList<String> getPlayerNames() {
		ArrayList<String> playersList = new ArrayList<String>();
		Set playersSet = players.entrySet();
		Iterator<Map.Entry> i = playersSet.iterator();

		while (i.hasNext()) {
			Map.Entry me = i.next();
			Player player = (Player) me.getValue();
			playersList.add(player.getUsername());
		}

		return playersList;
	}

	public void dealHandToPlayer(Hand hand, String username) {
		Player player = getPlayer(username);
		player.setHand(hand);
	}
}
