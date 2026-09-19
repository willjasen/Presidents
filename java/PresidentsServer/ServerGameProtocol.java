package PresidentsServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import PresidentsData.GameActionData;
import PresidentsData.GameStateData;
import PresidentsPlayer.Hand;
import PresidentsPlayer.PresidentsGame;

/** Connects a room to the server-authoritative Presidents rules engine. */
public final class ServerGameProtocol {
	private final GameRoom gameRoom;
	private PresidentsGame game;

	public ServerGameProtocol(GameRoom gameRoom) {
		this.gameRoom = gameRoom;
	}

	public synchronized void process(GameActionData action, String username) {
		if (action == null || action.getCommand() == null) {
			throw new IllegalArgumentException("Missing game action");
		}
		switch (action.getCommand()) {
		case GameActionData.START:
			startGame();
			break;
		case GameActionData.PLAY:
			requireGame().play(username, action.getCards());
			break;
		case GameActionData.PASS:
			requireGame().pass(username);
			break;
		case GameActionData.GET_STATE:
			break;
		default:
			throw new IllegalArgumentException("Unknown game action");
		}
	}

	private void startGame() {
		if (game != null && game.getStatus() != PresidentsGame.Status.WAITING) {
			throw new IllegalStateException("The game has already started");
		}
		List<String> players = gameRoom.getPlayerNames();
		if (players.size() < 2) {
			throw new IllegalStateException("At least two players are required to start");
		}
		if (players.size() > 7) {
			throw new IllegalStateException("A game supports at most seven players");
		}
		game = new PresidentsGame(players);
		game.start(new Random());
		for (String player : players) gameRoom.dealHandToPlayer(game.getHand(player), player);
	}

	public synchronized boolean canJoin() {
		return gameRoom.getSize() < 7
				&& (game == null || game.getStatus() == PresidentsGame.Status.WAITING);
	}

	public synchronized GameStateData snapshotFor(String username, String message) {
		ArrayList<String> players = gameRoom.getPlayerNames();
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for (String player : players) {
			counts.add(game == null ? 0 : game.getHand(player).getSize());
		}
		String status = game == null ? PresidentsGame.Status.WAITING.name()
				: game.getStatus().name();
		String current = game == null || game.getStatus() == PresidentsGame.Status.WAITING
				? null : game.getCurrentPlayer();
		Hand hand = game == null ? new Hand() : game.getHand(username);
		int count = game == null ? 0 : game.getCardsInPlay();
		int value = game == null ? 0 : game.getValueInPlay();
		List<String> finishOrder = game == null ? List.of() : game.getFinishOrder();
		return new GameStateData(gameRoom.getName(), status, current, count, value,
				hand, players, counts, finishOrder, message);
	}

	public synchronized PresidentsGame.Status getStatus() {
		return game == null ? PresidentsGame.Status.WAITING : game.getStatus();
	}

	private PresidentsGame requireGame() {
		if (game == null) throw new IllegalStateException("Start the game first");
		return game;
	}
}
