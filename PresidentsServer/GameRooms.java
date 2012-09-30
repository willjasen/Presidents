package PresidentsServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import PresidentsPlayer.Player;

public class GameRooms implements Iterable<GameRoom> {

	private HashMap<String, GameRoom> gameRooms;

	public GameRooms() {
		gameRooms = new HashMap<String, GameRoom>();
	}

	/**
	 * 
	 * @param player
	 *            - player to be added
	 * @param roomName
	 *            - name of the room to add a player to
	 */
	public synchronized void addPlayerToRoom(Player player, String roomName) {
		GameRoom gameRoom = gameRooms.get(roomName);
		gameRoom.addPlayer(player);
	}

	/**
	 * 
	 * @param gameRoom
	 *            - create a new game room
	 */
	public synchronized void createRoom(String gameRoom) {
		gameRooms.put(gameRoom, new GameRoom(gameRoom));
	}

	public synchronized ServerGameProtocol getGameProtocol(String roomName) {
		return getRoom(roomName).getGameProtocol();
	}

	/**
	 * Gets a room with the given name.
	 * 
	 * @param roomName
	 *            name of room to get
	 * @return game room with given name
	 */
	public synchronized GameRoom getRoom(String roomName) {
		return gameRooms.get(roomName);
	}

	/**
	 * 
	 * @return list of room names
	 */
	public synchronized ArrayList<String> getRoomNames() {
		ArrayList<String> roomNames = new ArrayList<String>();
		Set rooms = gameRooms.entrySet();
		Iterator<Map.Entry> i = rooms.iterator();

		while (i.hasNext()) {
			Map.Entry me = i.next();
			roomNames.add((String) me.getKey());
		}

		roomNames.remove("Lobby");
		return roomNames;
	}

	/**
	 * 
	 * @return number of rooms
	 */
	public synchronized int getSize() {
		return gameRooms.size();
	}

	@Override
	public Iterator<GameRoom> iterator() {
		Set rooms = gameRooms.entrySet();
		return rooms.iterator();
	}

	public boolean containsRoom(String roomName) {
		return gameRooms.containsKey(roomName);
	}
}
