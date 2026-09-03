package PresidentsClient;

import java.io.IOException;
//import java.io.ObjectOutputStream;
import java.net.Socket;

import PresidentsData.ChatData;
import javax.swing.SwingUtilities;


public class ClientChatThread implements Runnable {

	// constants that are used to determine which log to write to
	private static final int ERROR_LOG = 0;
	private static final int SYSTEM_LOG = 1;
	private static final int LOGIN_LOG = 2;
	
	private Client clientInstance;
	private Socket clientChatSock;
	private ClientNetwork network;
	
	public ClientChatThread() {
		
	}
	
	public ClientChatThread(Socket clientChatSock, Client clientInstance) {
		this(clientChatSock, clientInstance, null);
	}

	public ClientChatThread(Socket clientChatSock, Client clientInstance,
			ClientNetwork network) {
		this.clientChatSock = clientChatSock;
		this.clientInstance = clientInstance;
		this.network = network;
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
			ChatData received = dataInput;
			SwingUtilities.invokeLater(() -> clientInstance.handleReceivedChatData(received));
		}
		
		try {
			// Close the socket
			clientChatSock.close();
		} catch (IOException ioe) {
			
		}
	}
	
	public ChatData getData() {
		return network == null ? null : network.getChatData();
	}
}
