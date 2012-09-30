package PresidentsServer;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import PresidentsData.CommandData;
import PresidentsData.Data;
import PresidentsData.MD5;
import PresidentsData.RegisterUserData;
import PresidentsData.RoomData;
import PresidentsData.UserCommandData;
import PresidentsData.UserData;
import PresidentsPlayer.Card;
import PresidentsPlayer.Hand;
import PresidentsPlayer.Player;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

/**
 * 
 * @author willjasen
 * 
 */
public class ServerProtocol {

	/**
	 * State constants
	 */
	private final int REGISTERING = 0;
	private final int LOGGING_IN = 1;
	private final int IN_LOBBY = 2;
	private final int INROOM = 3;

	/**
	 * Log constants
	 */
	private static final int ERROR_LOG = 0;
	private static final int SYSTEM_LOG = 1;
	private static final int LOGIN_LOG = 2;

	private int state = LOGGING_IN;
	private MySQLConnection sqlconn;
	private Server serverInstance;
	private ServerThread serverThread;
	private ServerGameProtocol sgp;
	private String username;
	private String loginToken;

	public ServerProtocol() {

	}

	public ServerProtocol(ServerThread threadInstance) {
		sqlconn = new MySQLConnection();
		this.serverThread = threadInstance;
		this.serverInstance = threadInstance.getServerInstance();
	}

	private String getHashedPassword(UserData input) {
		String password = null;

		if (input != null)
			password = input.getPassword();

		if (password != null)
			return password;

		return null;
	}

	/**
	 * 
	 * @param input
	 *            data of a user
	 * @return the username
	 */
	private String getUsername(UserData input) {
		String username = null;

		if (input != null) {
			username = ((UserData) input).getUsername();
			if (username != null)
				return username;
		}

		return null;
	}

	private CommandData login(UserData dataInput) {
		CommandData dataOutput;

		dataOutput = processLoginData((UserData) dataInput);
		username = getUsername((UserData) dataInput);

		if (((CommandData) dataOutput).getSubCommand().equals("OKLOGIN")) {
			serverInstance.logOutput("Login successful for " + username,
					LOGIN_LOG);
			serverInstance.addPlayerToLobby(username);
			ArrayList<String> players = getLobbyPlayers();
			dataOutput.setPlayers(players);
			state++;
		} else
			logLoginFailureReason(username,
					getHashedPassword((UserData) dataInput));

		return dataOutput;
	}
	
	public ArrayList<String> getLobbyPlayers() {
		return serverInstance.getLobbyPlayers();
	}

	private Data enterRoom(UserCommandData dataInput) {

		CommandData dataOutput = null;
		String username = dataInput.getUsername();
		String nextRoom = dataInput.getNextRoom();

		if (verifyLoginToken(dataInput)) {
			serverInstance.movePlayer(username, "Lobby", nextRoom);
			sgp = serverInstance.getGameProtocol(nextRoom);
			dataOutput = new CommandData("ENTERROOM_OK",dataInput.getNextRoom());
			state++;
		}
		return dataOutput;
	}

	private Data createRoom(UserCommandData dataInput) {

		CommandData dataOutput = null;
		String username = dataInput.getUsername();
		String nextRoom = dataInput.getNextRoom();

		if (verifyLoginToken(dataInput)) {
			serverInstance.addRoom(nextRoom);
			serverInstance.logOutput("Room '" + nextRoom
					+ "' has been created.", 1);

			serverInstance.movePlayer(username, "Lobby", nextRoom);
			sgp = serverInstance.getGameProtocol(nextRoom);
			dataOutput = new CommandData("ENTERROOM_OK",dataInput.getNextRoom());
			state++;
		}
		return dataOutput;
	}

	/**
	 * Whenever data is received by the server, the data is passed to this
	 * method for processing.
	 * 
	 * @param dataInput
	 *            - data to process
	 * @return output data
	 */
	public Data processInput(Data dataInput) {

		Data dataOutput = null;

		if (state == REGISTERING) {
			// attempt to register the user
			dataOutput = register((RegisterUserData) dataInput);
		}
		// if logging in...
		else if (state == LOGGING_IN) {
			// attempt to login the user
			if (dataInput instanceof UserData) {
				dataOutput = login((UserData) dataInput);
			}
			// Set the protocol to register a user
			if (dataInput instanceof CommandData
					&& ((CommandData) dataInput).getCommand()
							.equals("REGISTER")) {
				state = REGISTERING;
				System.out.println("A new user is beginning to register...");
			}
		}

		else if (state == IN_LOBBY) {

			if(dataInput instanceof CommandData) {
				dataOutput = new CommandData("ROOMS");
				((CommandData) dataOutput).setRoomInfo(serverInstance
						.getRooms());
			}
			else if(dataInput instanceof UserCommandData) {
				UserCommandData dataInLobby = (UserCommandData) dataInput;

				if (dataInLobby.getCommand().equals("ENTERROOM")) {
					dataOutput = enterRoom(dataInLobby);
				}

				else if (dataInLobby.getCommand().equals("CREATE_ROOM")) {
					dataOutput = createRoom(dataInLobby);
				}
			}
		}

		else if (state == INROOM) {
			if(dataInput instanceof UserCommandData) {
				UserCommandData dataInRoom = (UserCommandData) dataInput;
				dataOutput = sgp.processInput(dataInRoom,username);
			}
		}
		/*
		 * if(state == INROOM) {
		 * if(((CommandData)dataInput).getCommand().equals("GETROOMINFO")) {
		 * Hand randomHand = new Hand(); for(int i=0;i<13;i++) { Random rand =
		 * new Random(); randomHand.addCard(new Card(rand.nextInt(13)+1,'c')); }
		 * dataOutput = new RoomData(randomHand); } }
		 */
		return dataOutput;
	}

	/**
	 * Writes, to a log, the login failure. It includes the username trying to
	 * login, and includes whether that username exists or if the password is
	 * incorrect.
	 * 
	 * @param username
	 *            - username of player trying to login
	 * @param hashedPassword
	 *            - hased password of player trying to login
	 */
	private void logLoginFailureReason(String username, String hashedPassword) {
		serverInstance.logOutput("Login failed for " + username + " - "
				+ sqlconn.reasonLoginFailed(username, hashedPassword),
				LOGIN_LOG);
	}

	private void removePlayer() {
		serverInstance.removePlayer(username);
	}

	/**
	 * Tests to see if the login token sent by the player is the same as the
	 * login token on the server.
	 * 
	 * @param commandData
	 *            - data input
	 * @return whether or not the token matches the token held by the server
	 */
	private boolean verifyLoginToken(UserCommandData commandData) {
		if (commandData.getLoginToken().equals(loginToken))
			return true;
		return false;
	}

	private Data register(RegisterUserData dataInput) {
		Data dataOutput;
		String username = dataInput.getUsername();
		String password = dataInput.getPassword();
		String email = dataInput.getEmail();
		String firstName = dataInput.getFirstName();
		String lastName = dataInput.getLastName();
		String birthday = dataInput.getBirthday();

		int row = -1;
		try {
			row = sqlconn.update("INSERT INTO player VALUES ('" + username
					+ "','" + password + "','" + email + "','" + firstName
					+ "','" + lastName + "','" + birthday
					+ "',0,'user',0,0,0,0)");
		} catch (MySQLIntegrityConstraintViolationException e) {
			state = REGISTERING;
			row = -1;
		} catch (Exception e) {
			state = REGISTERING;
			row = -1;
		} finally {
			if (row >= 0) {
				dataOutput = new CommandData("REGISTER", "REGISTER_OK");
				state++;
				serverInstance.logOutput("New user " + username
						+ " has been created.", LOGIN_LOG);
			} else {
				serverInstance.logOutput("Account " + username
						+ "could not be created.", ERROR_LOG);
				dataOutput = new CommandData("REGISTER", "REGISTER_NO");
			}
		}

		return dataOutput;
	}

	private CommandData addNamedPlayer(String username) {

		Random randGen = new Random();
		String random = new Long(randGen.nextLong()).toString();
		loginToken = MD5.getHash(random);

		Socket threadSocket = serverThread.getGameSocket();
		serverInstance.addNameToPlayer(threadSocket, username, loginToken);

		ArrayList<String> login = new ArrayList<String>();
		login.add(loginToken);
		login.add("LOGIN");
		login.add("OKLOGIN");
		return new CommandData(login);
	}

	private CommandData processLoginData(UserData input) {

		CommandData dataOutput;
		String username = null;
		String password = null;

		if (input != null) {
			username = input.getUsername();
			password = input.getPassword();
		} else
			dataOutput = new CommandData("LOGIN", "NOLOGIN");

		if (username.equals(null) || password.equals(null)) {
			dataOutput = new CommandData("LOGIN", "NOLOGIN");
		} else if (sqlconn.loginAuthorized(username, password)) {
			dataOutput = addNamedPlayer(username);
		} else
			dataOutput = new CommandData("LOGIN", "NOLOGIN");

		return dataOutput;
	}
}
