package PresidentsServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
		if (gameRoom == null || gameRoom.trim().isEmpty()) {
			throw new IllegalArgumentException("Room name cannot be blank");
		}
		if (gameRooms.containsKey(gameRoom)) {
			throw new IllegalArgumentException("A room with that name already exists");
		}
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
		ArrayList<String> roomNames = new ArrayList<String>(gameRooms.keySet());
		roomNames.remove("Lobby");
		roomNames.sort(String.CASE_INSENSITIVE_ORDER);
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
	public synchronized Iterator<GameRoom> iterator() {
		return new ArrayList<GameRoom>(gameRooms.values()).iterator();
	}

	public boolean containsRoom(String roomName) {
		return gameRooms.containsKey(roomName);
	}
}
