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
import PresidentsData.CommandData;
import PresidentsData.Data;
import PresidentsData.MD5;
//import PresidentsData.RegisterUserData;
import PresidentsData.UserData;
import PresidentsPlayer.Player;
public class ClientNetwork {


    private Socket socket;
    private Socket chatSock;
    //private Player clientPlayer;
    private ObjectOutputStream outToClient;
    private static String ipaddr = "localhost";

    public ClientNetwork() {
        connect();
    }

    public void connect() {
        try {
            // game connection successful
            socket = new Socket(ipaddr, 3000);
            // JOptionPane.showMessageDialog(null, "Connected.", "Congrats!",
            // JOptionPane.INFORMATION_MESSAGE);
        } catch (UnknownHostException e) {
            System.out.println("unknown host");
            //JOptionPane.showMessageDialog(null, "Unknown host.", "Sorry..",
                               // JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            System.out.println("io error");
            // JOptionPane.showMessageDialog(null,
            // "Unable to connect to server.", "Sorry..",
            // JOptionPane.INFORMATION_MESSAGE);
        }

        try {
            // chat connection successful
            chatSock = new Socket(ipaddr, 3001);
            // JOptionPane.showMessageDialog(null, "Connected.", "Congrats!",
            // JOptionPane.INFORMATION_MESSAGE);
        } catch (UnknownHostException e) {
            //JOptionPane.showMessageDialog(null, "Unknown host.", "Sorry..",
                                //JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            // JOptionPane.showMessageDialog(null,
            //"Unable to connect to server.", "Sorry..",
            //JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public void sendData(Data data) {

		if (!socket.isClosed()) {
			try {
				outToClient = new ObjectOutputStream(socket
						.getOutputStream());
				outToClient.writeObject(data);
			} catch (IOException ioe) {
				System.out
						.println("Error writing to a socket.  Cannot send data.");
			}
		}
	}

    public void sendChatData(Data data) {

            if (!chatSock.isClosed()) {
                    try {
                            outToClient = new ObjectOutputStream(chatSock
                                            .getOutputStream());
                            outToClient.writeObject(data);
                    } catch (IOException ioe) {
                            System.out
                                            .println("Error writing to a chat socket.  Cannot send data.");
                    }
                    
            }
    }
    
    public Data getData() {
		Data data = null;
		ObjectInputStream inFromClient = null;

		if (!socket.isClosed()) {
			try {
				// Get reader for the socket, and then read a line from
				// the socket
				inFromClient = new ObjectInputStream(socket
						.getInputStream());
				try {
					data = (Data) inFromClient.readObject();
				} catch (Exception e) {

				}
			} catch (Exception e) {

			}
		}

		return data;
	}
    
    public Socket getGameSocket() {
    	return socket;
    }

    public void createChatThread(Client client) {
        (new Thread(new ClientChatThread(chatSock,client))).start();
    }
    
    public void createGameThread(Client client) {
    	(new Thread(new ClientGameThread(socket,client,this))).start();
    }

}
