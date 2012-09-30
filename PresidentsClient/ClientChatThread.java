package PresidentsClient;

import java.io.IOException;
import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
import java.net.Socket;

import PresidentsData.ChatData;


public class ClientChatThread implements Runnable {

	// constants that are used to determine which log to write to
	private static final int ERROR_LOG = 0;
	private static final int SYSTEM_LOG = 1;
	private static final int LOGIN_LOG = 2;
	
	private Client clientInstance;
	private Socket clientChatSock;
	
	public ClientChatThread() {
		
	}
	
	public ClientChatThread(Socket clientChatSock, Client clientInstance) {
		this.clientChatSock = clientChatSock;
		this.clientInstance = clientInstance;
	}
	
	public Client getClientInstance() {
		return clientInstance;
	}
	
	public void run() {
		
		//hold data to be processed and that has been processed
		ChatData dataInput;
		
		// while the connection isn't closed and there is data, process
		// it and send info to client
		while (!clientChatSock.isClosed() && (dataInput = getData()) != null) {
			clientInstance.handleReceivedChatData(dataInput);
		}
		
		try {
			// Close the socket
			clientChatSock.close();
		} catch (IOException ioe) {
			
		}
	}
	
	public ChatData getData() {
		ChatData data = null;
		ObjectInputStream inFromClient = null;

		if (!clientChatSock.isClosed()) {
			try {
				// Get reader for the socket, and then read a line from
				// the socket
				inFromClient = new ObjectInputStream(clientChatSock.getInputStream());
				try {
					data = (ChatData) inFromClient.readObject();
				} catch (Exception e) {
					System.out.println("Error reading from a chat socket.");
					//serverInstance.writeOutput("Error reading from a chat socket.",ERROR_LOG);
					//serverInstance.removePlayer(clientChatSock);
				}
			} catch (Exception e) {
				//serverInstance.writeOutput("Error reading from a chat socket 2.",ERROR_LOG);
				//serverInstance.removePlayer(clientChatSock);
			}
		} else {
			// remove the player
			//serverInstance.removePlayer(clientChatSock);
		}

		return data;
	}
}