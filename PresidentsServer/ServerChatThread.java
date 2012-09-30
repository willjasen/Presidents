package PresidentsServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import PresidentsData.ChatData;

public class ServerChatThread implements Runnable {

	/**
	 * Log constants
	 */
	private static final int ERROR_LOG = 0;
	private static final int SYSTEM_LOG = 1;
	private static final int LOGIN_LOG = 2;

	private Server serverInstance;
	private Socket clientChatSock;

	public ServerChatThread() {

	}

	public ServerChatThread(Socket clientChatSock, Server serverInstance) {
		this.clientChatSock = clientChatSock;
		this.serverInstance = serverInstance;
	}

	public Server getServerInstance() {
		return serverInstance;
	}

	/**
	 * This method is executed when the thread begins.
	 * 
	 * This method is used as a bridge between the server's chat service and the
	 * client. Data flow from this perspective is unidirectional in the
	 * direction of the client to the server.
	 * 
	 * It checks to see if the client has disconnected by testing if the socket
	 * is closed. It takes input data and hands it to the server for processing
	 * if that data is not null.
	 */
	@Override
	public void run() {

		ChatData dataInput;

		// while the connection isn't closed and there is data, process
		// it and send info to client
		while (!clientChatSock.isClosed() && (dataInput = getData()) != null) {
			serverInstance.handleReceivedChatData(dataInput);
		}
	}

	public ChatData getData() {
		ChatData data = null;
		ObjectInputStream inFromClient = null;

		if (!clientChatSock.isClosed()) {
			try {
				// Get reader for the socket, and then read a line from
				// the socket
				inFromClient = new ObjectInputStream(clientChatSock
						.getInputStream());
				try {
					data = (ChatData) inFromClient.readObject();
				} catch (Exception e) {
					System.out.println("Error reading from a chat socket.");
					serverInstance.logOutput(
							"Error reading from a chat socket.", ERROR_LOG);
					serverInstance.removePlayer(clientChatSock);
				}
			} catch (Exception e) {
				serverInstance.logOutput(
						"Error reading from a chat socket 2.", ERROR_LOG);
				// serverInstance.removePlayer(clientChatSock);
			}
		} else {
			// remove the player
			// serverInstance.removePlayer(clientChatSock);
		}

		return data;
	}
}
