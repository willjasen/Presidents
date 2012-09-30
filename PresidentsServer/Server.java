package PresidentsServer;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

import PresidentsClient.ClientChatThread;
import PresidentsData.ChatData;
import PresidentsPlayer.Human;
import PresidentsPlayer.Player;
import PresidentsPlayer.Players;

/**
 * 
 * @author willjasen
 * 
 */
public class Server {

	/** Port that the game runs on. */
	private static final int SERVER_PORT = 3000;
	/** Port that the chat service runs on. */
	private static final int CHATROOM_PORT = 3001;

	// constants that are used to determine which log to write to
	private static final int ERROR_LOG = 0;
	private static final int SYSTEM_LOG = 1;
	private static final int LOGIN_LOG = 2;

	/**
	 * Running state of the server.
	 */
	private static boolean serverRunning = true;

	// create logs
	private static final ServerLog errorLog = new ServerLog();
	private static final ServerLog systemLog = new ServerLog();
	private static final ServerLog loginLog = new ServerLog();

	/**
	 * GUI associated with the server.
	 */
	private ServerGUI gui;

	/**
	 * A list of players who are connected to the server, but have not logged
	 * in.
	 */
	private ArrayList<Player> noNamePlayers;

	/** List of current game rooms. */
	private GameRooms gameRooms;

	/** Socket for the game service. */
	private ServerSocket servSock;
	/** Socket for the chat service. */
	private ServerSocket chatSock;

	private Server() {
		// add rooms just to test
		gameRooms = new GameRooms();
		gameRooms.createRoom("Lobby");
		gameRooms.createRoom("room 1");
		gameRooms.createRoom("this is room 2.");
		gameRooms.createRoom("ROOM 3!!!");
	}

	public Server(ServerGUI gui) {
		this();
		this.gui = gui;

		// startServer();
	}

	/**
	 * Sends chat messages to every client in a particular room, specified in
	 * dataInput.
	 */
	public void handleReceivedChatData(ChatData dataInput) {
		String roomName = dataInput.getRoomName();
		GameRoom gameRoom = gameRooms.getRoom(roomName);

		for (Player player : gameRoom.getPlayers()) {
			sendChatDataToClient((Human) player, dataInput);
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
	public synchronized void addRoom(String gameRoom) {
		gameRooms.createRoom(gameRoom);
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
	public synchronized void movePlayer(String username, String currentRoom,
			String nextRoom) {

		GameRoom oldRoom, newRoom;

		if (gameRooms.containsRoom(currentRoom)) {
			oldRoom = gameRooms.getRoom(currentRoom);
		} else {
			oldRoom = null;
			System.out.println("currentRoom does not exists.");
		}

		if (gameRooms.containsRoom(nextRoom)) {
			newRoom = gameRooms.getRoom(nextRoom);
		} else {
			newRoom = null;
			System.out.println("nextRoom does not exists.");
		}

		Player player = oldRoom.removePlayer(username);
		if (player == null)
			System.out.println("player does not exist");
		newRoom.addPlayer(player);
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

		ObjectOutputStream outToClient;

		if (!player.getChatSocket().isClosed()) {
			try {
				outToClient = new ObjectOutputStream(player.getChatSocket()
						.getOutputStream());
				outToClient.writeObject(dataInput);
			} catch (IOException ioe) {
				System.out
						.println("Error writing to a chat socket.  Cannot send data.");
			}
		}
	}

	/**
	 * Starts the server.
	 */
	public void startServer() {
		noNamePlayers = new ArrayList<Player>(0);
		serverRunning = true;
		logOutput("Starting server...", SYSTEM_LOG);
		try {
			servSock = new ServerSocket(SERVER_PORT);
			logOutput("Server started.", SYSTEM_LOG);
		} catch (Exception e) {
			logOutput("Cannot create a new server socket.", ERROR_LOG);
		}

		logOutput("Starting chat server...", SYSTEM_LOG);
		try {
			chatSock = new ServerSocket(CHATROOM_PORT);
			logOutput("Chat server started.", SYSTEM_LOG);
		} catch (Exception e) {
			logOutput("Cannot create a new server socket.", ERROR_LOG);
		}

		while (serverRunning) {
			// listen for and accept a connection
			Socket connSock = null;
			Socket chatConnSock = null;

			if (!servSock.isClosed()) {
				try {
					connSock = servSock.accept();
				} catch (IOException e) {
					logOutput("Server socket closed unexpectedly.", ERROR_LOG);
				}
			}

			if (!chatSock.isClosed()) {
				try {
					chatConnSock = chatSock.accept();
				} catch (IOException e) {
					logOutput("Chat socket closed unexpectedly.", ERROR_LOG);
				}
			}

			Human newPlayer = null;
			// log the new connection (change the InetAddress toString to cut
			// off a / at the beginning)
			if (!servSock.isClosed()) {
				logOutput("New connection has been made from "
						+ connSock.getInetAddress().toString().substring(1),
						SYSTEM_LOG);

				// add the new connection to a list of all the connected clients
				newPlayer = new Human(connSock);
				noNamePlayers.add(newPlayer);

				// send the new connection to a thread to allow others to
				// connect
				(new Thread(new ServerThread(connSock, this))).start();
			} else
				System.out.println("Server socket is closed");

			if (!chatSock.isClosed()) {
				// start chat sock
				logOutput(
						"New chat connection has been made from "
								+ chatConnSock.getInetAddress().toString()
										.substring(1), SYSTEM_LOG);
				newPlayer.addChatSocket(chatConnSock);
				noNamePlayers.add(newPlayer);
				(new Thread(new ServerChatThread(chatConnSock, this))).start();
			} else
				System.out.println("Chat socket is closed");
		}
	}

	/**
	 * Stops this server instance.
	 */
	public void stopServer() {
		serverRunning = false;

		try {
			logOutput("Stopping the server...  ", SYSTEM_LOG);
			servSock.close();
			logOutput("Stopped.", SYSTEM_LOG);
		} catch (Exception e) {

		}
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
		for (Player player : noNamePlayers) {
			if (((Human) player).getSocket().equals(socket)) {
				boolean playerRemoved = noNamePlayers.remove(player);
				System.out.println("Player " + player.getUsername()
						+ " was removed.");
				break;
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
				gameRoom.removePlayer(username);
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
		gui
				.writeToTextArea("[" + ServerLog.now() + "]  " + textToOutput
						+ "\n");
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