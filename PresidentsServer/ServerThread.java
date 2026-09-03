package PresidentsServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

import PresidentsData.Data;
import PresidentsData.SafeObjectInput;

/**
 * Thread started by the server to handle the input and output of a network
 * socket associated with a given client.
 * 
 * The goal of this class is to separate data passing from data processing. This
 * class receives data from a client and sends it to ServerProtocol for data
 * processing. ServerProtocol returns data, and this class sends the output data
 * back to the client.
 * 
 * @author willjasen
 * 
 */
public class ServerThread implements Runnable {

	/**
	 * Constants that represent the type of log
	 */
	private static final int ERROR_LOG = 0;
	private static final int SYSTEM_LOG = 1;
	private static final int LOGIN_LOG = 2;

	private ServerProtocol psp;
	private Server serverInstance;

	/** Game socket associated with a connected client. */
	private Socket clientGameSock;
	private ObjectInputStream input;
	private ObjectOutputStream output;

	private ServerThread() {

	}

	public ServerThread(Socket clientGameSock, Server serverInstance) {
		this.clientGameSock = clientGameSock;
		this.serverInstance = serverInstance;
		psp = new ServerProtocol(this);
	}

	public Server getServerInstance() {
		return serverInstance;
	}

	public Socket getGameSocket() {
		return clientGameSock;
	}

	/**
	 * This method is executed when the thread begins.
	 * 
	 * This method is used as a bridge between the server's chat service and the
	 * client. Data flow from this perspective is bidirectional, with a separate
	 * method to handle the processing of data.
	 * 
	 * It checks to see if the client has disconnected by testing if the socket
	 * is closed. It takes input data and hands it to the server protocol for
	 * processing if that data is not null. The server protocol returns
	 * processed data and this method sends that output data back to its
	 * associated client.
	 */
	@Override
	public void run() {

		// hold data to be processed and that has been processed
		Data dataInput, dataOutput;

		try {
			output = new ObjectOutputStream(clientGameSock.getOutputStream());
			output.flush();
			input = SafeObjectInput.open(clientGameSock.getInputStream());
			while (!clientGameSock.isClosed() && (dataInput = getData()) != null) {
				dataOutput = psp.processInput(dataInput);
				if (dataOutput != null) sendData(dataOutput);
			}
		} catch (IOException e) {
			serverInstance.logOutput("Client connection could not be initialized.", ERROR_LOG);
		} finally {
			psp.close();
			serverInstance.unregisterGameClient(clientGameSock);
			serverInstance.removePlayer(clientGameSock);
			try { clientGameSock.close(); } catch (IOException ignored) { }
		}
	}

	private Data getData() {
		Data data = null;
		if (!clientGameSock.isClosed()) {
			try {
				data = (Data) input.readObject();
			} catch (IOException | ClassNotFoundException e) {
				return null;
			}
		} else {
			// remove the player
			serverInstance.logOutput("Client has disconnected 2.", ERROR_LOG);
			serverInstance.removePlayer(clientGameSock);
		}

		return data;
	}

	/**
	 * Sends data to client associated with this thread.
	 * 
	 * @param data
	 *            - data to send to client
	 * @return if data was sent successfully
	 */
	public synchronized boolean sendData(Data data) {
		if (!clientGameSock.isClosed()) {
			try {
				output.writeObject(data);
				output.flush();
				output.reset();
			} catch (IOException ioe) {
				serverInstance.logOutput("Error writing to a socket.",
						ERROR_LOG);
				serverInstance.removePlayer(clientGameSock);
				return false;
			}
			return true;
		}
		return false;
	}

}
