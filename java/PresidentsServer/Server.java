package PresidentsServer;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

import PresidentsData.ChatData;
import PresidentsData.GameStateData;
import PresidentsPlayer.Human;
import PresidentsPlayer.Player;

/**
 * 
 * @author willjasen
 * 
 */
public class Server {

	/** Port that the game runs on. */
	private static final int DEFAULT_SERVER_PORT = 3000;
	/** Port that the chat service runs on. */
	private static final int DEFAULT_CHATROOM_PORT = 3001;

	// constants that are used to determine which log to write to
	private static final int ERROR_LOG = 0;
	private static final int SYSTEM_LOG = 1;
	private static final int LOGIN_LOG = 2;

	/**
	 * Running state of the server.
	 */
	private volatile boolean serverRunning = false;

	// create logs
	private static final ServerLog errorLog = new ServerLog();
	private static final ServerLog systemLog = new ServerLog();
	private static final ServerLog loginLog = new ServerLog();

	/**
	 * GUI associated with the server.
	 */
	private ServerGUI gui;
	private final int configuredServerPort;
	private final int configuredChatPort;

	/**
	 * A list of players who are connected to the server, but have not logged
	 * in.
	 */
	private ArrayList<Player> noNamePlayers;

	/** List of current game rooms. */
	private GameRooms gameRooms;
	private final Map<Socket, ServerThread> gameClients = new HashMap<Socket, ServerThread>();

	/** Socket for the game service. */
	private ServerSocket servSock;
	/** Socket for the chat service. */
	private ServerSocket chatSock;

	public Server() {
		this(null, Integer.getInteger("presidents.server.port", DEFAULT_SERVER_PORT),
				Integer.getInteger("presidents.chat.port", DEFAULT_CHATROOM_PORT));
	}

	Server(ServerGUI gui, int serverPort, int chatPort) {
		gameRooms = new GameRooms();
		gameRooms.createRoom("Lobby");
		this.gui = gui;
		this.configuredServerPort = serverPort;
		this.configuredChatPort = chatPort;
	}

	public Server(ServerGUI gui) {
		this(gui, Integer.getInteger("presidents.server.port", DEFAULT_SERVER_PORT),
				Integer.getInteger("presidents.chat.port", DEFAULT_CHATROOM_PORT));
	}

	/**
	 * Sends chat messages to every client in a particular room, specified in
	 * dataInput.
	 */
	public void handleReceivedChatData(Human sender, ChatData dataInput) {
		if (sender == null || sender.getUsername() == null || sender.getRoomName() == null) return;
		String roomName = sender.getRoomName();
		GameRoom gameRoom = gameRooms.getRoom(roomName);
		if (gameRoom == null || !gameRoom.containsPlayer(sender.getUsername())) return;
		String message = dataInput == null ? "" : dataInput.getChatString();
		ChatData sanitized = new ChatData(sender.getUsername(), roomName,
				message == null ? "" : message);

		for (Player player : gameRoom.getPlayers()) {
			if (player instanceof Human) sendChatDataToClient((Human) player, sanitized);
		}
	}

	/**
	 * Gets the game protocol of a room
	 * 
	 * @param roomName
	 *            - name of the room
	 * @return game protocol of the given room
	 */
	public synchronized ServerGameProtocol getGameProtocol(String roomName) {
		return gameRooms.getGameProtocol(roomName);
	}

	/**
	 * Creates a new room.
	 * 
	 * @param gameRoom
	 *            - name of the room to add
	 */
	public synchronized boolean addRoom(String gameRoom) {
		try {
			gameRooms.createRoom(gameRoom);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Moves a player from one room to another.
	 * 
	 * @param username
	 *            - username of the player to move
	 * @param currentRoom
	 *            - current room of the user
	 * @param nextRoom
	 *            - room that user is entering
	 */
	public synchronized boolean movePlayer(String username, String currentRoom,
			String nextRoom) {
		GameRoom oldRoom = gameRooms.getRoom(currentRoom);
		GameRoom newRoom = gameRooms.getRoom(nextRoom);
		if (oldRoom == null || newRoom == null) return false;
		if (!"Lobby".equals(nextRoom) && !newRoom.getGameProtocol().canJoin()) return false;
		Player player = oldRoom.removePlayer(username);
		if (player == null) return false;
		newRoom.addPlayer(player);
		if (player instanceof Human) ((Human) player).setRoomName(nextRoom);
		return true;
	}

	/**
	 * Gets a list of player names that are currently in the lobby.
	 * 
	 * @return list of player names in the lobby
	 */
	public synchronized ArrayList<String> getLobbyPlayers() {
		GameRoom lobby = gameRooms.getRoom("Lobby");
		ArrayList<Player> players = lobby.getPlayers();
		ArrayList<String> playerNames = new ArrayList<String>();
		for(Player player : players) {
			playerNames.add(player.getUsername());
			System.out.println(player.getUsername());
		}
		return playerNames;
	}

	public synchronized void addPlayerToRoom(String roomName, Player player) {
		gameRooms.addPlayerToRoom(player, roomName);
	}

	public synchronized void addPlayerToRoom(String roomName, String username) {
		gameRooms.addPlayerToRoom(new Player(username), roomName);
	}

	// send the chat data to a specific client
	public synchronized void sendChatDataToClient(Human player,
			ChatData dataInput) {

		if (player.getChatSocket() != null && !player.getChatSocket().isClosed()) {
			try {
				player.sendChat(dataInput);
			} catch (IOException ioe) {
				removePlayer(player.getUsername());
				player.closeConnections();
			}
		}
	}

	/**
	 * Starts the server.
	 */
	public void startServer() {
		synchronized (this) {
			if (serverRunning) return;
			serverRunning = true;
		}
		noNamePlayers = new ArrayList<Player>(0);
		logOutput("Starting server...", SYSTEM_LOG);
		try {
			servSock = new ServerSocket(configuredServerPort);
			chatSock = new ServerSocket(configuredChatPort);
			logOutput("Server started on ports " + getServerPort() + " and "
					+ getChatPort() + ".", SYSTEM_LOG);
		} catch (IOException e) {
			serverRunning = false;
			logOutput("Cannot start the server. Ports " + configuredServerPort + " and "
					+ configuredChatPort + " must be available.", ERROR_LOG);
			closeServerSockets();
			return;
		}

		while (serverRunning) {
			Socket connSock = null;
			Socket chatConnSock = null;
			try {
				connSock = servSock.accept();
				chatConnSock = chatSock.accept();
				Human newPlayer = new Human(connSock);
				newPlayer.addChatSocket(chatConnSock);
				noNamePlayers.add(newPlayer);
				logOutput("New connection from " + connSock.getInetAddress().getHostAddress(), SYSTEM_LOG);
				ServerThread gameThread = new ServerThread(connSock, this);
				registerGameClient(connSock, gameThread);
				new Thread(gameThread, "game-client").start();
				new Thread(new ServerChatThread(chatConnSock, this, newPlayer), "chat-client").start();
			} catch (IOException e) {
				closeSocket(connSock);
				closeSocket(chatConnSock);
				if (serverRunning) logOutput("Unable to accept a client connection.", ERROR_LOG);
			}
		}
	}

	/**
	 * Stops this server instance.
	 */
	public void stopServer() {
		serverRunning = false;

		logOutput("Stopping the server...", SYSTEM_LOG);
		closeServerSockets();
		closeClientConnections();
		logOutput("Stopped.", SYSTEM_LOG);
	}

	public boolean isRunning() {
		return serverRunning;
	}

	public int getServerPort() {
		return servSock == null ? configuredServerPort : servSock.getLocalPort();
	}

	public int getChatPort() {
		return chatSock == null ? configuredChatPort : chatSock.getLocalPort();
	}

	private void closeServerSockets() {
		closeSocket(servSock);
		closeSocket(chatSock);
	}

	private synchronized void closeClientConnections() {
		if (noNamePlayers != null) {
			for (Player player : new ArrayList<Player>(noNamePlayers)) {
				if (player instanceof Human) ((Human) player).closeConnections();
			}
			noNamePlayers.clear();
		}
		for (GameRoom room : gameRooms) {
			for (Player player : room.getPlayers()) {
				room.removePlayer(player.getUsername());
				if (player instanceof Human) ((Human) player).closeConnections();
			}
		}
		gameClients.clear();
	}

	synchronized void registerGameClient(Socket socket, ServerThread thread) {
		gameClients.put(socket, thread);
	}

	synchronized void unregisterGameClient(Socket socket) {
		gameClients.remove(socket);
	}

	/** Sends a state tailored to each player, without exposing other hands. */
	public synchronized void broadcastGameState(String roomName,
			ServerGameProtocol protocol) {
		GameRoom room = gameRooms.getRoom(roomName);
		if (room == null) return;
		for (Player player : room.getPlayers()) {
			if (!(player instanceof Human)) continue;
			Human human = (Human) player;
			ServerThread thread = gameClients.get(human.getSocket());
			if (thread != null) {
				GameStateData state = protocol.snapshotFor(player.getUsername(), null);
				thread.sendData(state);
			}
		}
	}

	private void closeSocket(Closeable socket) {
		try { if (socket != null) socket.close(); } catch (IOException ignored) { }
	}

	/**
	 * Gets a list of rooms.
	 * 
	 * @return a list of rooms
	 */
	public synchronized ArrayList<String> getRooms() {
		ArrayList<String> rooms;
		rooms = gameRooms.getRoomNames();
		return rooms;
	}

	// remove a player by its socket
	public synchronized void removePlayer(Socket socket) {
		for (Player player : new ArrayList<Player>(noNamePlayers)) {
			Human human = (Human) player;
			if (socket.equals(human.getSocket()) || socket.equals(human.getChatSocket())) {
				noNamePlayers.remove(player);
				human.closeConnections();
				return;
			}
		}
		for (GameRoom room : gameRooms) {
			for (Player player : room.getPlayers()) {
				if (player instanceof Human) {
					Human human = (Human) player;
					if (socket.equals(human.getSocket()) || socket.equals(human.getChatSocket())) {
						room.removePlayer(player.getUsername());
						human.closeConnections();
						return;
					}
				}
			}
		}
	}

	/**
	 * Searches through all game rooms for a player by its username and removes
	 * that player.
	 * 
	 * @param username
	 *            - username of the player to remove
	 */
	public synchronized void removePlayer(String username) {
		for (GameRoom gameRoom : gameRooms) {
			if (gameRoom.containsPlayer(username)) {
				Player removed = gameRoom.removePlayer(username);
				if (removed instanceof Human) ((Human) removed).closeConnections();
			}
		}
	}

	/**
	 * Loop through unnamed players and find the newly logged in client, then
	 * move the player to the lobby.
	 * 
	 * @param gameSocket
	 *            - socket used to find the associated player
	 * @param username
	 *            - username of the player
	 * @param loginToken
	 *            - login token to give the player
	 */
	public synchronized void addNameToPlayer(Socket gameSocket,
			String username, String loginToken) {
		// loop through players and find player with gameSocket
		for (Player player : noNamePlayers) {
			if (player instanceof Human) {
				if (((Human) player).getSocket().equals(gameSocket)) {
					player.setUsername(username);
					((Human) player).setLoginToken(loginToken);
					((Human) player).setRoomName("Lobby");
					// use move player
					gameRooms.addPlayerToRoom(player, "Lobby");
					noNamePlayers.remove(player);
					break;
				}
			}
		}
	}

	/**
	 * This method is responsible for writing various categories of input to a
	 * log. The log can be output to the GUI, output to a file, or saved in a
	 * database, determined by this method.
	 * 
	 * @param textToOutput
	 *            - text to output
	 * @param log
	 *            - category of log
	 */
	public void logOutput(String textToOutput, int log) {
		if (gui != null) {
			gui.writeToTextArea("[" + ServerLog.now() + "]  " + textToOutput + "\n");
		}
		switch (log) {
		case ERROR_LOG:
			errorLog.write(textToOutput);
			break;
		case SYSTEM_LOG:
			systemLog.write(textToOutput);
			break;
		case LOGIN_LOG:
			loginLog.write(textToOutput);
			break;
		}
	}

	public void addPlayerToLobby(String username) {
		addPlayerToRoom("Lobby", username);
	}
}
