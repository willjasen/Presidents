package PresidentsServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import javax.swing.JLabel;

import org.junit.jupiter.api.Test;

import PresidentsClient.ClientNetwork;
import PresidentsData.ChatData;
import PresidentsData.CommandData;
import PresidentsData.GameActionData;
import PresidentsData.GameStateData;
import PresidentsData.MD5;
import PresidentsData.RegisterUserData;
import PresidentsData.UserCommandData;
import PresidentsData.UserData;
import PresidentsPlayer.Card;

class NetworkLoginTest {
	@Test
	void twoClientsCanStartARealGameAndPlayTheOpeningCard() throws Exception {
		System.setProperty("presidents.database.url",
				"jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
		Server server = new Server(null, 31310, 31311);
		Thread serverThread = new Thread(server::startServer, "test-server");
		serverThread.start();
		for (int i = 0; i < 100 && !server.isRunning(); i++) Thread.sleep(10);
		assertTrue(server.isRunning());

		ClientNetwork first = new ClientNetwork("localhost", server.getServerPort(),
				server.getChatPort());
		ClientNetwork second = null;
		try {
			Login firstLogin = registerAndLogin(first, "network-player-one");

			first.sendData(new CommandData());
			assertEquals("ROOMS", ((CommandData) first.getData()).getCommand());

			UserCommandData createRoom = roomAction("CREATE_ROOM", firstLogin,
					"Integration Room");
			first.sendData(createRoom);
			assertEquals("ENTERROOM_OK", ((CommandData) first.getData()).getCommand());
			first.sendData(gameAction(GameActionData.GET_STATE, firstLogin,
					"Integration Room", null));
			GameStateData waiting = (GameStateData) first.getData();
			assertEquals("WAITING", waiting.getStatus());
			assertEquals(1, waiting.getPlayerNames().size());

			second = new ClientNetwork("localhost", server.getServerPort(),
					server.getChatPort());
			Login secondLogin = registerAndLogin(second, "network-player-two");
			second.sendData(new CommandData());
			CommandData rooms = (CommandData) second.getData();
			assertTrue(rooms.getInfo().contains("Integration Room"));
			second.sendData(roomAction("ENTERROOM", secondLogin, "Integration Room"));
			assertEquals("ENTERROOM_OK", ((CommandData) second.getData()).getCommand());
			second.sendData(gameAction(GameActionData.GET_STATE, secondLogin,
					"Integration Room", null));
			assertEquals(2, ((GameStateData) second.getData()).getPlayerNames().size());

			first.sendData(gameAction(GameActionData.START, firstLogin,
					"Integration Room", null));
			GameStateData firstStarted = (GameStateData) first.getData();
			GameStateData secondStarted = (GameStateData) second.getData();
			assertEquals("PLAYING", firstStarted.getStatus());
			assertEquals("PLAYING", secondStarted.getStatus());
			assertEquals(52, firstStarted.getCardCounts().stream().mapToInt(Integer::intValue).sum());

			boolean firstStarts = firstStarted.getCurrentPlayer().equals(firstLogin.username());
			GameStateData starterState = firstStarts ? firstStarted : secondStarted;
			ClientNetwork starter = firstStarts ? first : second;
			Login starterLogin = firstStarts ? firstLogin : secondLogin;
			Card threeOfClubs = new Card(1, 0);
			// UI-only card labels must never cross the network.
			threeOfClubs.setLabel(new JLabel("selected"));
			assertTrue(starterState.getHand().contains(threeOfClubs));
			starter.sendData(gameAction(GameActionData.PLAY, starterLogin,
					"Integration Room", List.of(threeOfClubs)));
			GameStateData firstAfterPlay = (GameStateData) first.getData();
			GameStateData secondAfterPlay = (GameStateData) second.getData();
			assertEquals(1, firstAfterPlay.getCardsInPlay());
			assertEquals(1, secondAfterPlay.getCardsInPlay());
			assertEquals(51, firstAfterPlay.getCardCounts().stream().mapToInt(Integer::intValue).sum());

			first.sendChatData(new ChatData("forged-name", "forged-room", "hello"));
			ChatData firstChat = first.getChatData();
			ChatData secondChat = second.getChatData();
			assertEquals(firstLogin.username(), firstChat.getUsername());
			assertEquals("Integration Room", secondChat.getRoomName());
			assertEquals("hello", secondChat.getChatString());
		} finally {
			first.close();
			if (second != null) second.close();
			server.stopServer();
			serverThread.join(2000);
		}
	}

	private Login registerAndLogin(ClientNetwork client, String username) {
		assertTrue(client.isConnected());
		client.sendData(new CommandData("REGISTER"));
		RegisterUserData registration = new RegisterUserData();
		registration.setUsername(username);
		registration.setPassword(MD5.getHash("password"));
		registration.setEmail(username + "@example.com");
		registration.setFirstName("Network");
		registration.setLastName("Player");
		registration.setBirthday("2000-01-02");
		client.sendData(registration);
		assertEquals("REGISTER_OK", ((CommandData) client.getData()).getSubCommand());

		client.sendData(new UserData(username, MD5.getHash("password")));
		CommandData loggedIn = (CommandData) client.getData();
		assertEquals("OKLOGIN", loggedIn.getSubCommand());
		assertNotNull(loggedIn.getLoginToken());
		return new Login(username, loggedIn.getLoginToken());
	}

	private UserCommandData roomAction(String action, Login login, String room) {
		UserCommandData data = new UserCommandData(action);
		data.setUsername(login.username());
		data.setLoginToken(login.token());
		data.setNextRoom(room);
		return data;
	}

	private GameActionData gameAction(String action, Login login, String room,
			List<Card> cards) {
		GameActionData data = new GameActionData(action, cards);
		data.setUsername(login.username());
		data.setLoginToken(login.token());
		data.setNextRoom(room);
		return data;
	}

	private record Login(String username, String token) { }
}
