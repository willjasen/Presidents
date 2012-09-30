package PresidentsClient;

import java.io.IOException;
import java.net.Socket;
import PresidentsData.Data;

public class ClientGameThread implements Runnable {

	private static final int ERROR_LOG = 0;
	private static final int SYSTEM_LOG = 1;
	private static final int LOGIN_LOG = 2;
	
	private Client clientInstance;
	private Socket clientGameSock;
	private ClientNetwork network;
	
	public ClientGameThread() {
		
	}
	
	public ClientGameThread(Socket clientGameSock, Client clientInstance, ClientNetwork network) {
		this.clientGameSock = clientGameSock;
		this.clientInstance = clientInstance;
		this.network = network;
	}
	
	public Client getClientInstance() {
		return clientInstance;
	}
	
	public void run() {
		
		//hold data to be processed and that has been processed
		Data dataInput=null;
		
		// while the connection isn't closed and there is data, process
		// it and send info to client
		while (!clientGameSock.isClosed() && (dataInput = getData()) != null) {
			clientInstance.handleReceivedGameData(dataInput);
		}
	}
		
	public Data getData() {
		return network.getData();
	}	
	
	
}
