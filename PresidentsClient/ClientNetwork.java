/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bbuchanan
 */
package PresidentsClient;
import java.net.*;
import java.io.*;
import PresidentsData.ChatData;
import PresidentsData.Data;
import PresidentsData.SafeObjectInput;
public class ClientNetwork {


    private Socket socket;
    private Socket chatSock;
    //private Player clientPlayer;
    private ObjectOutputStream gameOutput;
    private ObjectInputStream gameInput;
    private ObjectOutputStream chatOutput;
    private ObjectInputStream chatInput;
    private final String host;
    private final int serverPort;
    private final int chatPort;

    public ClientNetwork() {
        this(System.getProperty("presidents.server.host", "localhost"),
                Integer.getInteger("presidents.server.port", 3000),
                Integer.getInteger("presidents.chat.port", 3001));
    }

    public ClientNetwork(String host, int serverPort, int chatPort) {
        this.host = host;
        this.serverPort = serverPort;
        this.chatPort = chatPort;
        connect();
    }

	public boolean isConnected() {
		return socket != null && chatSock != null && socket.isConnected()
				&& chatSock.isConnected() && !socket.isClosed() && !chatSock.isClosed();
	}

    public final void connect() {
        close();
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, serverPort), 3000);
            chatSock = new Socket();
            chatSock.connect(new InetSocketAddress(host, chatPort), 3000);

            gameOutput = new ObjectOutputStream(socket.getOutputStream());
            gameOutput.flush();
            gameInput = SafeObjectInput.open(socket.getInputStream());

            chatOutput = new ObjectOutputStream(chatSock.getOutputStream());
            chatOutput.flush();
            chatInput = SafeObjectInput.open(chatSock.getInputStream());
        } catch (IOException e) {
            System.err.println("Unable to connect to the Presidents server: " + e.getMessage());
            close();
        }
    }
    
    public synchronized void sendData(Data data) {

		if (isConnected() && gameOutput != null) {
			try {
				gameOutput.writeObject(data);
				gameOutput.flush();
				gameOutput.reset();
			} catch (IOException ioe) {
				System.err.println("The connection to the server was lost.");
				close();
			}
		}
	}

    public synchronized void sendChatData(Data data) {

			if (isConnected() && chatOutput != null) {
                    try {
                            chatOutput.writeObject(data);
                            chatOutput.flush();
                            chatOutput.reset();
                    } catch (IOException ioe) {
                            System.err.println("The chat connection to the server was lost.");
                            close();
                    }
                    
            }
    }
    
    public Data getData() {
		if (isConnected() && gameInput != null) {
			try {
				return (Data) gameInput.readObject();
			} catch (IOException | ClassNotFoundException e) {
				close();
			}
		}
		return null;
	}

	public ChatData getChatData() {
		if (isConnected() && chatInput != null) {
			try {
				return (ChatData) chatInput.readObject();
			} catch (IOException | ClassNotFoundException e) {
				close();
			}
		}
		return null;
	}
    
    public Socket getGameSocket() {
    	return socket;
    }

    public void createChatThread(Client client) {
        (new Thread(new ClientChatThread(chatSock,client,this))).start();
    }
    
    public void createGameThread(Client client) {
		(new Thread(new ClientGameThread(socket,client,this))).start();
    }

	public synchronized void close() {
		try { if (socket != null) socket.close(); } catch (IOException ignored) { }
		try { if (chatSock != null) chatSock.close(); } catch (IOException ignored) { }
		socket = null;
		chatSock = null;
		gameOutput = null;
		gameInput = null;
		chatOutput = null;
		chatInput = null;
	}

}
