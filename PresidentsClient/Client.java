package PresidentsClient;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*; // Swing GUI classes are defined here.
import PresidentsData.ChatData;
import PresidentsData.CommandData;
import PresidentsData.Data;
import PresidentsData.MD5;
import PresidentsData.RegisterUserData;
import PresidentsData.RoomData;
import PresidentsData.UserCommandData;
import PresidentsData.UserData;
import PresidentsPlayer.Card;
import PresidentsPlayer.Hand;
import PresidentsPlayer.Human;
import PresidentsPlayer.Player;

/**
 * The client applet.
 * 
 * @author willjasen
 * 
 */
public class Client extends JApplet {

	private static final long serialVersionUID = -5085586743177866914L;

	private final int WINDOW_WIDTH = 1024;
	private final int WINDOW_HEIGHT = 768;

	private Human clientPlayer;
	private ClientNetwork network;

	private JList list;

	// Variables declaration for login screen - do not modify
	private javax.swing.JButton btnLogin;
	private javax.swing.JButton btnRegister;
	private javax.swing.JLabel lblPassword;
	private javax.swing.JLabel lblUsername;
	private javax.swing.JPanel pnlLogin;
	private javax.swing.JPanel pnlLoginScreen;
	private javax.swing.JPasswordField txtPassword;
	private javax.swing.JTextField txtUsername;
	// End of login screen variables declaration

	// Variables declaration for register screen - do not modify
	private javax.swing.JButton btnRegisterUser;
	private javax.swing.JComboBox cmbRegisterDay;
	private javax.swing.JComboBox cmbRegisterMonth;
	private javax.swing.JComboBox cmbRegisterYear;
	private javax.swing.JLabel lblBirthday;
	private javax.swing.JLabel lblEmail;
	private javax.swing.JLabel lblFirstName;
	private javax.swing.JLabel lblLastName;
	private javax.swing.JLabel lblRegisterPassword;
	private javax.swing.JLabel lblRegisterPasswordAgain;
	private javax.swing.JLabel lblRegisterUsername;
	private javax.swing.JLabel lblRequiredFields;
	private javax.swing.JPanel pnlRegister;
	private javax.swing.JPanel pnlRegisterScreen;
	private javax.swing.JTextField txtEmail;
	private javax.swing.JTextField txtFirstName;
	private javax.swing.JTextField txtLastName;
	private javax.swing.JPasswordField txtRegisterPassword;
	private javax.swing.JPasswordField txtRegisterPasswordAgain;
	private javax.swing.JTextField txtRegisterUsername;
	// End of register screen variables declaration

	// Variables declaration for lobby screen - do not modify
	private javax.swing.JButton btnLobbyChat;
    private javax.swing.JButton btnLobbyCreateRoom;
    private javax.swing.JButton btnLobbyEnterRoom;
    private javax.swing.JPanel pnlLobbyChat;
    private javax.swing.JPanel pnlLobbyScreen;
    private javax.swing.JScrollPane pnlPlayerList;
    private javax.swing.JPanel pnlRooms;
    private javax.swing.JScrollPane spChatRoom;
    private javax.swing.JScrollPane spLobbyRooms;
    private javax.swing.JTextField txtChatMessage;
    private javax.swing.JTextArea txtChatRoom;
    private javax.swing.JTextArea txtLobbyPlayers;
    private javax.swing.JTextArea txtLobbyRooms;
	// End of lobby screen variables declaration

	// Variables declaration for room screen - do not modify
	private javax.swing.JButton btnPlayCards;
	private javax.swing.JButton btnRoomChat;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLayeredPane pnlHand;
	private javax.swing.JLayeredPane pnlLeftPlayer;
	private javax.swing.JLayeredPane pnlRightPlayer;
	private javax.swing.JPanel pnlRoomChat;
	private javax.swing.JPanel pnlRoomScreen;
	private javax.swing.JLayeredPane pnlTopPlayer;

	// End of room screen variables declaration

	public Client() {
		// start the connection to the server when program is started
		network = new ClientNetwork();
		network.createGameThread(this);
		network.createChatThread(this);
	}

	public void init() {
		// resize the applet
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		try {
			java.awt.EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					createLoginScreen();
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void updateLobbyPlayerList(CommandData dataInput) {
		ArrayList<String> players = dataInput.getPlayers();
		DefaultListModel listModelPlayers = new DefaultListModel();

		for (int i = 0; i < players.size(); i++) {
			listModelPlayers.addElement(players.get(i));
			System.out.println("1" + players.get(i));
		}

		list = new JList(listModelPlayers);
		pnlPlayerList.setViewportView(list);
	}

	private void createLobbyScreen(CommandData dataInput) {

		getContentPane().remove(pnlLoginScreen);
		sendData(new CommandData());

		pnlLobbyScreen = new javax.swing.JPanel();
        pnlLobbyChat = new javax.swing.JPanel();
        spChatRoom = new javax.swing.JScrollPane();
        txtChatRoom = new javax.swing.JTextArea();
        txtChatMessage = new javax.swing.JTextField();
        btnLobbyChat = new javax.swing.JButton();
        pnlRooms = new javax.swing.JPanel();
        btnLobbyEnterRoom = new javax.swing.JButton();
        spLobbyRooms = new javax.swing.JScrollPane();
        txtLobbyRooms = new javax.swing.JTextArea();
        btnLobbyCreateRoom = new javax.swing.JButton();
        pnlPlayerList = new javax.swing.JScrollPane();
        txtLobbyPlayers = new javax.swing.JTextArea();

        txtChatRoom.setColumns(20);
        txtChatRoom.setEditable(false);
        txtChatRoom.setRows(5);
        spChatRoom.setViewportView(txtChatRoom);

        btnLobbyChat.setText("Send");
        btnLobbyChat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLobbyChatMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnlLobbyChatLayout = new javax.swing.GroupLayout(pnlLobbyChat);
        pnlLobbyChat.setLayout(pnlLobbyChatLayout);
        pnlLobbyChatLayout.setHorizontalGroup(
            pnlLobbyChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLobbyChatLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlLobbyChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLobbyChatLayout.createSequentialGroup()
                        .addComponent(txtChatMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLobbyChat))
                    .addComponent(spChatRoom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 546, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        pnlLobbyChatLayout.setVerticalGroup(
            pnlLobbyChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLobbyChatLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(spChatRoom, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlLobbyChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLobbyChat)
                    .addComponent(txtChatMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        btnLobbyEnterRoom.setText("Enter Room");
        btnLobbyEnterRoom.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLobbyEnterRoomMouseClicked(evt);
            }
        });

        txtLobbyRooms.setColumns(20);
        txtLobbyRooms.setEditable(false);
        txtLobbyRooms.setRows(5);
        spLobbyRooms.setViewportView(txtLobbyRooms);

        btnLobbyCreateRoom.setText("Create Room");
        btnLobbyCreateRoom.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLobbyCreateRoomMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnlRoomsLayout = new javax.swing.GroupLayout(pnlRooms);
        pnlRooms.setLayout(pnlRoomsLayout);
        pnlRoomsLayout.setHorizontalGroup(
            pnlRoomsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRoomsLayout.createSequentialGroup()
                .addContainerGap(32, Short.MAX_VALUE)
                .addComponent(spLobbyRooms, javax.swing.GroupLayout.PREFERRED_SIZE, 545, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(pnlRoomsLayout.createSequentialGroup()
                .addGap(156, 156, 156)
                .addComponent(btnLobbyCreateRoom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(85, 85, 85)
                .addComponent(btnLobbyEnterRoom, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                .addGap(156, 156, 156))
        );
        pnlRoomsLayout.setVerticalGroup(
            pnlRoomsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRoomsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spLobbyRooms, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRoomsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLobbyCreateRoom)
                    .addComponent(btnLobbyEnterRoom, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(45, Short.MAX_VALUE))
        );

        pnlPlayerList.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtLobbyPlayers.setColumns(20);
        txtLobbyPlayers.setEditable(false);
        txtLobbyPlayers.setRows(5);
        pnlPlayerList.setViewportView(txtLobbyPlayers);

        javax.swing.GroupLayout pnlLobbyScreenLayout = new javax.swing.GroupLayout(pnlLobbyScreen);
        pnlLobbyScreen.setLayout(pnlLobbyScreenLayout);
        pnlLobbyScreenLayout.setHorizontalGroup(
            pnlLobbyScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLobbyScreenLayout.createSequentialGroup()
                .addGap(204, 204, 204)
                .addGroup(pnlLobbyScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlLobbyScreenLayout.createSequentialGroup()
                        .addComponent(pnlLobbyChat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(pnlPlayerList, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlRooms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(174, Short.MAX_VALUE))
        );
        pnlLobbyScreenLayout.setVerticalGroup(
            pnlLobbyScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLobbyScreenLayout.createSequentialGroup()
                .addGap(90, 90, 90)
                .addComponent(pnlRooms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(pnlLobbyScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlLobbyScreenLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 139, Short.MAX_VALUE)
                        .addComponent(pnlLobbyChat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(pnlLobbyScreenLayout.createSequentialGroup()
                        .addGap(149, 149, 149)
                        .addComponent(pnlPlayerList, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlLobbyScreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlLobbyScreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        updateLobbyPlayerList(dataInput);
	}

	private void createLoginScreen() {

		pnlLoginScreen = new javax.swing.JPanel();
		pnlLogin = new javax.swing.JPanel();
		btnRegister = new javax.swing.JButton();
		lblUsername = new javax.swing.JLabel();
		txtUsername = new javax.swing.JTextField();
		btnLogin = new javax.swing.JButton();
		txtPassword = new javax.swing.JPasswordField();
		lblPassword = new javax.swing.JLabel();

		if (txtRegisterUsername != null)
			txtUsername.setText(txtRegisterUsername.getText());

		txtUsername.setText("willjasen");
		txtPassword.setText("test");

		btnRegister.setText("Register");
		btnRegister.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnRegisterMouseClicked(evt);
			}
		});

		lblUsername.setText("Username:");

		btnLogin.setText("Login");
		btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnLoginMouseClicked(evt);
			}
		});

		lblPassword.setText("Password:");

		javax.swing.GroupLayout pnlLoginLayout = new javax.swing.GroupLayout(
				pnlLogin);
		pnlLogin.setLayout(pnlLoginLayout);
		pnlLoginLayout
				.setHorizontalGroup(pnlLoginLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								pnlLoginLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												pnlLoginLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																false)
														.addGroup(
																pnlLoginLayout
																		.createSequentialGroup()
																		.addGroup(
																				pnlLoginLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING)
																						.addComponent(
																								lblUsername)
																						.addComponent(
																								lblPassword))
																		.addGap(
																				43,
																				43,
																				43)
																		.addGroup(
																				pnlLoginLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addComponent(
																								txtUsername)
																						.addComponent(
																								txtPassword,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								109,
																								javax.swing.GroupLayout.PREFERRED_SIZE)))
														.addGroup(
																pnlLoginLayout
																		.createSequentialGroup()
																		.addComponent(
																				btnRegister,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				btnLogin,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				79,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addContainerGap()));
		pnlLoginLayout
				.setVerticalGroup(pnlLoginLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								pnlLoginLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												pnlLoginLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																lblUsername)
														.addComponent(
																txtUsername,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												pnlLoginLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																lblPassword)
														.addComponent(
																txtPassword,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												pnlLoginLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(btnLogin)
														.addComponent(
																btnRegister))
										.addContainerGap()));

		javax.swing.GroupLayout pnlLoginScreenLayout = new javax.swing.GroupLayout(
				pnlLoginScreen);
		pnlLoginScreen.setLayout(pnlLoginScreenLayout);
		pnlLoginScreenLayout.setHorizontalGroup(pnlLoginScreenLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						pnlLoginScreenLayout.createSequentialGroup().addGap(
								375, 375, 375).addComponent(pnlLogin,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE).addGap(107, 107, 107)));
		pnlLoginScreenLayout.setVerticalGroup(pnlLoginScreenLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						pnlLoginScreenLayout.createSequentialGroup().addGap(
								298, 298, 298).addComponent(pnlLogin,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE).addGap(144, 144, 144)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				pnlLoginScreen, javax.swing.GroupLayout.DEFAULT_SIZE, 706,
				Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				pnlLoginScreen, javax.swing.GroupLayout.DEFAULT_SIZE, 539,
				Short.MAX_VALUE));
	}

	private void createRegisterScreen() {

		getContentPane().remove(pnlLoginScreen);

		pnlRegisterScreen = new javax.swing.JPanel();
		pnlRegister = new javax.swing.JPanel();
		btnRegisterUser = new javax.swing.JButton();
		txtEmail = new javax.swing.JTextField();
		lblRegisterPasswordAgain = new javax.swing.JLabel();
		txtRegisterUsername = new javax.swing.JTextField();
		txtLastName = new javax.swing.JTextField();
		lblRequiredFields = new javax.swing.JLabel();
		txtFirstName = new javax.swing.JTextField();
		lblFirstName = new javax.swing.JLabel();
		lblEmail = new javax.swing.JLabel();
		lblRegisterUsername = new javax.swing.JLabel();
		lblBirthday = new javax.swing.JLabel();
		txtRegisterPassword = new javax.swing.JPasswordField();
		lblRegisterPassword = new javax.swing.JLabel();
		lblLastName = new javax.swing.JLabel();
		txtRegisterPasswordAgain = new javax.swing.JPasswordField();
		cmbRegisterMonth = new javax.swing.JComboBox();
		cmbRegisterDay = new javax.swing.JComboBox();
		cmbRegisterYear = new javax.swing.JComboBox();

		btnRegisterUser.setText("Register");
		btnRegisterUser.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnRegisterUserMouseClicked(evt);
			}
		});

		lblRegisterPasswordAgain.setText("*Password again:");

		lblRequiredFields.setText("(*fields are required)");

		lblFirstName.setText("*First Name:");

		lblEmail.setText("*Email:");

		lblRegisterUsername.setText("*Username:");

		lblBirthday.setText("*Birthday:");

		lblRegisterPassword.setText("*Password:");

		lblLastName.setText("Last Name:");

		cmbRegisterMonth.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "January", "February", "March", "April", "May",
						"June", "July", "August", "September", "October",
						"November", "December" }));
		cmbRegisterMonth.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				cmbRegisterMonthItemStateChanged(evt);
			}
		});

		cmbRegisterDay.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9",
						"10", "11", "12", "13", "14", "15", "16", "17", "18",
						"19", "20", "21", "22", "23", "24", "25", "26", "27",
						"28", "29", "30", "31" }));

		cmbRegisterYear.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "2008", "2007", "2006", "2005", "2004", "2003",
						"2002", "2001", "2000", "1999", "1998", "1997", "1996",
						"1995", "1994", "1993", "1992", "1991", "1990", "1989",
						"1988", "1987", "1986", "1985", "1984", "1983", "1982",
						"1981", "1980", "1979", "1978", "1977", "1976", "1975",
						"1974", "1973", "1972", "1971", "1970", "1969", "1968",
						"1967", "1966", "1965", "1964", "1963", "1962", "1961",
						"1960", "1959", "1958", "1957", "1956", "1955", "1954",
						"1953", "1952", "1951", "1950", "1949", "1948", "1947",
						"1946", "1945", "1944", "1943", "1942", "1941", "1940",
						"1939", "1938", "1937", "1936", "1935", "1934", "1933",
						"1932", "1931", "1930", "1929", "1928", "1927", "1926",
						"1925", "1924", "1923", "1922", "1921", "1920", "1919",
						"1918", "1917", "1916", "1915", "1914", "1913", "1912",
						"1911", "1910", "1909", "1908", "1907", "1906", "1905",
						"1904", "1903", "1902", "1901", "1900" }));

		javax.swing.GroupLayout pnlRegisterLayout = new javax.swing.GroupLayout(
				pnlRegister);
		pnlRegister.setLayout(pnlRegisterLayout);
		pnlRegisterLayout
				.setHorizontalGroup(pnlRegisterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								pnlRegisterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												pnlRegisterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																lblRegisterPassword)
														.addComponent(
																lblRegisterUsername)
														.addComponent(
																lblFirstName)
														.addComponent(
																lblLastName)
														.addComponent(
																lblBirthday)
														.addComponent(lblEmail)
														.addComponent(
																lblRegisterPasswordAgain)
														.addComponent(
																lblRequiredFields))
										.addGap(29, 29, 29)
										.addGroup(
												pnlRegisterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																txtRegisterPasswordAgain,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																183,
																Short.MAX_VALUE)
														.addComponent(
																txtRegisterUsername,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																183,
																Short.MAX_VALUE)
														.addComponent(
																txtEmail,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																183,
																Short.MAX_VALUE)
														.addComponent(
																txtLastName,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																183,
																Short.MAX_VALUE)
														.addComponent(
																txtRegisterPassword,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																183,
																Short.MAX_VALUE)
														.addComponent(
																txtFirstName,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																183,
																Short.MAX_VALUE)
														.addGroup(
																pnlRegisterLayout
																		.createSequentialGroup()
																		.addComponent(
																				cmbRegisterMonth,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				cmbRegisterDay,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				cmbRegisterYear,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addComponent(
																btnRegisterUser,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																97,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap()));
		pnlRegisterLayout
				.setVerticalGroup(pnlRegisterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								pnlRegisterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												pnlRegisterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																lblFirstName)
														.addComponent(
																txtFirstName,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												pnlRegisterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																lblLastName)
														.addComponent(
																txtLastName,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(8, 8, 8)
										.addGroup(
												pnlRegisterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																lblBirthday)
														.addComponent(
																cmbRegisterMonth,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																cmbRegisterDay,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																cmbRegisterYear,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(14, 14, 14)
										.addGroup(
												pnlRegisterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(lblEmail)
														.addComponent(
																txtEmail,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												pnlRegisterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																lblRegisterUsername)
														.addComponent(
																txtRegisterUsername,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												pnlRegisterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																lblRegisterPassword)
														.addComponent(
																txtRegisterPassword,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												pnlRegisterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																lblRegisterPasswordAgain)
														.addComponent(
																txtRegisterPasswordAgain,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(14, 14, 14)
										.addGroup(
												pnlRegisterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																lblRequiredFields)
														.addComponent(
																btnRegisterUser))
										.addContainerGap()));

		javax.swing.GroupLayout pnlRegisterScreenLayout = new javax.swing.GroupLayout(
				pnlRegisterScreen);
		pnlRegisterScreen.setLayout(pnlRegisterScreenLayout);
		pnlRegisterScreenLayout.setHorizontalGroup(pnlRegisterScreenLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						pnlRegisterScreenLayout.createSequentialGroup().addGap(
								362, 362, 362).addComponent(pnlRegister,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE).addGap(267, 267, 267)));
		pnlRegisterScreenLayout.setVerticalGroup(pnlRegisterScreenLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						pnlRegisterScreenLayout.createSequentialGroup().addGap(
								229, 229, 229).addComponent(pnlRegister,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE).addGap(131, 131, 131)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				pnlRegisterScreen, javax.swing.GroupLayout.DEFAULT_SIZE,
				javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				pnlRegisterScreen, javax.swing.GroupLayout.DEFAULT_SIZE,
				javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
	}

	private void clickedOnCard(java.awt.event.MouseEvent evt) {
		JLabel clickedLabel = (JLabel) evt.getSource();
		for (int i = 0; i < clientPlayer.getHand().getSize(); i++) {
			Card tempCard = clientPlayer.getHand().getCard(i);
			JLabel tempLabel = tempCard.getLabel();
			if (clientPlayer.getHand().getCard(i).getLabel().equals(
					clickedLabel)) {
				if (clientPlayer.getHand().getCard(i).hasBeenClicked() == true) {
					// tempLabel.setBorder(null);
					tempLabel.setBounds(20 * (i + 1), 10, 71, 96);
				} else {
					// tempLabel.setBorder(javax.swing.BorderFactory.
					// createLineBorder(new java.awt.Color(255, 0, 0)));
					tempLabel.setBounds(20 * (i + 1), 00, 71, 96);
				}
				clientPlayer.getHand().getCard(i).switchClicked();
				clientPlayer.getHand().getCard(i).setLabel(tempLabel);
				break;
			}
		}
	}

	private void createRoomScreen(RoomData roomData) {

		getContentPane().remove(pnlLobbyScreen);
		Hand hand = roomData.getHand();
		clientPlayer.setHand(hand);

		pnlRoomScreen = new javax.swing.JPanel();
		pnlRoomChat = new javax.swing.JPanel();
		spChatRoom = new javax.swing.JScrollPane();
		txtChatRoom = new javax.swing.JTextArea();
		txtChatMessage = new javax.swing.JTextField();
		btnRoomChat = new javax.swing.JButton();
		pnlHand = new javax.swing.JLayeredPane();
		jLabel2 = new javax.swing.JLabel();
		pnlLeftPlayer = new javax.swing.JLayeredPane();
		jLabel1 = new javax.swing.JLabel();
		pnlTopPlayer = new javax.swing.JLayeredPane();
		pnlRightPlayer = new javax.swing.JLayeredPane();
		btnPlayCards = new javax.swing.JButton();

		// create and show hands
		// this player
		for (int i = hand.getSize(); i > 0; i--) {
			JLabel temp = new JLabel();
			temp.setBounds(20 * i, 10, 71, 96);
			temp.setIcon(new javax.swing.ImageIcon(getClass().getResource(
					"/PresidentsClient/cards/classic/" + hand.getCard(i - 1)
							+ ".png")));
			pnlHand.add(temp, javax.swing.JLayeredPane.DEFAULT_LAYER);
			hand.getCard(i - 1).setLabel(temp);

			temp.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					clickedOnCard(evt);
				}
			});

		}

		// top player
		for (int i = 13; i > 0; i--) {
			JLabel temp = new JLabel();
			temp.setBounds(20 * i, 10, 71, 96);
			temp.setIcon(new javax.swing.ImageIcon(getClass().getResource(
					"/PresidentsClient/cards/classic/b1fv.png")));
			pnlTopPlayer.add(temp, javax.swing.JLayeredPane.DEFAULT_LAYER);
			hand.getCard(i - 1).setLabel(temp);

			temp.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					clickedOnCard(evt);
				}
			});

		}

		// left player
		for (int i = 13; i > 0; i--) {
			JLabel temp = new JLabel();
			temp.setBounds(20, 10 * i, 71, 96);
			temp.setIcon(new javax.swing.ImageIcon(getClass().getResource(
					"/PresidentsClient/cards/classic/b1fv.png")));
			pnlLeftPlayer.add(temp, javax.swing.JLayeredPane.DEFAULT_LAYER);

			temp.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					clickedOnCard(evt);
				}
			});
		}

		// right player
		for (int i = 13; i > 0; i--) {
			JLabel temp = new JLabel();
			temp.setBounds(20, 10 * i, 71, 96);
			temp.setIcon(new javax.swing.ImageIcon(getClass().getResource(
					"/PresidentsClient/cards/classic/b1fv.png")));
			pnlRightPlayer.add(temp, javax.swing.JLayeredPane.DEFAULT_LAYER);

			temp.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					clickedOnCard(evt);
				}
			});
		}

		txtChatRoom.setColumns(20);
		txtChatRoom.setEditable(false);
		txtChatRoom.setRows(5);
		spChatRoom.setViewportView(txtChatRoom);

		btnRoomChat.setText("Send");
		btnRoomChat.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnRoomChatMouseClicked(evt);
			}
		});

		javax.swing.GroupLayout pnlRoomChatLayout = new javax.swing.GroupLayout(
				pnlRoomChat);
		pnlRoomChat.setLayout(pnlRoomChatLayout);
		pnlRoomChatLayout
				.setHorizontalGroup(pnlRoomChatLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								pnlRoomChatLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												pnlRoomChatLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																spChatRoom,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																985,
																Short.MAX_VALUE)
														.addGroup(
																pnlRoomChatLayout
																		.createSequentialGroup()
																		.addComponent(
																				txtChatMessage,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				922,
																				Short.MAX_VALUE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				btnRoomChat)))
										.addContainerGap()));
		pnlRoomChatLayout
				.setVerticalGroup(pnlRoomChatLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								pnlRoomChatLayout
										.createSequentialGroup()
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(
												spChatRoom,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												117,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												pnlRoomChatLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																btnRoomChat)
														.addComponent(
																txtChatMessage,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap()));

		btnPlayCards.setText("Play Cards");
		btnPlayCards.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				btnPlayCardsMouseClicked(evt);
			}
		});

		javax.swing.GroupLayout pnlRoomScreenLayout = new javax.swing.GroupLayout(
				pnlRoomScreen);
		pnlRoomScreen.setLayout(pnlRoomScreenLayout);
		pnlRoomScreenLayout
				.setHorizontalGroup(pnlRoomScreenLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								pnlRoomScreenLayout
										.createSequentialGroup()
										.addGroup(
												pnlRoomScreenLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																pnlRoomScreenLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addGroup(
																				pnlRoomScreenLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(
																								pnlRoomScreenLayout
																										.createSequentialGroup()
																										.addGap(
																												27,
																												27,
																												27)
																										.addComponent(
																												pnlLeftPlayer,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												100,
																												javax.swing.GroupLayout.PREFERRED_SIZE))
																						.addGroup(
																								pnlRoomScreenLayout
																										.createSequentialGroup()
																										.addGap(
																												373,
																												373,
																												373)
																										.addComponent(
																												pnlTopPlayer,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												350,
																												javax.swing.GroupLayout.PREFERRED_SIZE)))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																				93,
																				Short.MAX_VALUE))
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																pnlRoomScreenLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				pnlHand,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				521,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
										.addComponent(
												pnlRightPlayer,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												100,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(134, 134, 134))
						.addGroup(
								pnlRoomScreenLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												pnlRoomChat,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)).addGroup(
								pnlRoomScreenLayout.createSequentialGroup()
										.addGap(437, 437, 437).addComponent(
												btnPlayCards).addContainerGap(
												504, Short.MAX_VALUE)));
		pnlRoomScreenLayout
				.setVerticalGroup(pnlRoomScreenLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								pnlRoomScreenLayout
										.createSequentialGroup()
										.addGroup(
												pnlRoomScreenLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																pnlRoomScreenLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				pnlTopPlayer,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				200,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				18,
																				18,
																				18)
																		.addComponent(
																				pnlLeftPlayer,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				246,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				20,
																				20,
																				20)
																		.addComponent(
																				pnlHand,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				132,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(
																pnlRoomScreenLayout
																		.createSequentialGroup()
																		.addGap(
																				108,
																				108,
																				108)
																		.addComponent(
																				pnlRightPlayer,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				336,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addGap(18, 18, 18)
										.addComponent(btnPlayCards)
										.addGap(2, 2, 2)
										.addComponent(
												pnlRoomChat,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(106, 106, 106)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addComponent(pnlRoomScreen,
						javax.swing.GroupLayout.PREFERRED_SIZE, 1024,
						javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(880, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addComponent(pnlRoomScreen,
						javax.swing.GroupLayout.PREFERRED_SIZE, 768,
						javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(195, Short.MAX_VALUE)));
	}

	private void sendData(Data data) {
		network.sendData(data);
	}

	private void sendChatData() {
		ChatData data = new ChatData(clientPlayer.getUsername(), clientPlayer
				.getRoomName(), txtChatMessage.getText());
		network.sendChatData(data);
		txtChatMessage.setText("");
	}

	public void handleReceivedChatData(ChatData dataInput) {
		String newChatLine = dataInput.getUsername() + ": "
				+ dataInput.getChatString();
		txtChatRoom.append(newChatLine + "\r\n");
	}

	public void handleReceivedGameData(Data dataInput) {

		if (dataInput instanceof CommandData) {
			CommandData commandDataInput = (CommandData) dataInput;
			if (commandDataInput.getCommand().equals("LOGIN")) {
				if (commandDataInput.getSubCommand().equals("OKLOGIN")) {
					showStatus("Logged in as " + txtUsername.getText());
					clientPlayer = new Human(txtUsername.getText(),
							commandDataInput.getLoginToken());
					clientPlayer.setRoomName("Lobby");
					createLobbyScreen(commandDataInput);

				} else {
					showStatus("Not logged in");
					JOptionPane.showMessageDialog(null,
							"Login username or password incorrect.", "!",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}

			else if (commandDataInput.getCommand().equals("REGISTER")) {
				if (commandDataInput.getSubCommand().equals("REGISTER_OK")) {
					JOptionPane.showMessageDialog(null,
							"Your account has been created.\n\nYour username is: "
									+ txtRegisterUsername.getText(), "Sign up",
							JOptionPane.INFORMATION_MESSAGE);
					getContentPane().remove(pnlRegisterScreen);
					createLoginScreen();
				}

				else if (commandDataInput.getCommand().equals("REGISTER_NO")) {

					String username = txtRegisterUsername.getText();
					String output = "User "
							+ username
							+ " already exists.  Please choose a different username.";
					JOptionPane.showMessageDialog(null, output, "Sign up",
							JOptionPane.INFORMATION_MESSAGE);
				}

				else
					JOptionPane.showMessageDialog(null,
							"REGISTER data is not set to OK or NO", "Sign up",
							JOptionPane.INFORMATION_MESSAGE);
			}

			else if (commandDataInput.getCommand().equals("ROOMS")) {
				ArrayList<String> rooms = commandDataInput.getInfo();
				DefaultListModel listModel = new DefaultListModel();

				for (int i = 2; i < rooms.size(); i++) {
					listModel.addElement(rooms.get(i));
				}

				list = new JList(listModel);
				spLobbyRooms.setViewportView(list);
			}

			else if (commandDataInput.getCommand().equals("ENTERROOM_OK")) {
				
				UserCommandData dataToSend = new UserCommandData();
				String enteredRoom = commandDataInput.getSubCommand();
				
				dataToSend.setCommand("GETROOMINFO");
				dataToSend.setUsername(clientPlayer.getUsername());
				dataToSend.setLoginToken(clientPlayer.getLoginToken());
				dataToSend.setNextRoom(enteredRoom);
				
				sendData(dataToSend);
				// add a waiting screen for players joining?
			}

			else if (commandDataInput.getCommand().equals("ROOMINFO")) {
				// createRoomScreen(null);
			}
		}
		if (dataInput instanceof RoomData) {
			RoomData roomData = (RoomData) dataInput;
			createRoomScreen((roomData));
		}
	}

	private void btnLoginMouseClicked(java.awt.event.MouseEvent evt) {
		login();
	}

	private void btnRegisterMouseClicked(java.awt.event.MouseEvent evt) {
		sendData(new CommandData("REGISTER"));
		createRegisterScreen();
	}

	private void btnRegisterUserMouseClicked(java.awt.event.MouseEvent evt) {
		register();
	}

	private void btnLobbyChatMouseClicked(java.awt.event.MouseEvent evt) {
		sendChatData();
	}

	private void btnLobbyEnterRoomMouseClicked(java.awt.event.MouseEvent evt) {
		enterRoom();
	}

	private void btnLobbyCreateRoomMouseClicked(java.awt.event.MouseEvent evt) {
		createNewRoom();
	}

	private void btnRoomChatMouseClicked(java.awt.event.MouseEvent evt) {
		sendChatData();
	}

	private void btnPlayCardsMouseClicked(java.awt.event.MouseEvent evt) {
		// TODO add your handling code here:
	}

	private void createNewRoom() {

		UserCommandData dataOutput = new UserCommandData();

		String newRoomName = JOptionPane.showInputDialog(null,
				"Enter the room name", "Enter the name of a new room.",
				JOptionPane.QUESTION_MESSAGE);

		clientPlayer.setRoomName(newRoomName);

		dataOutput.setCommand("CREATE_ROOM");
		dataOutput.setUsername(clientPlayer.getUsername());
		dataOutput.setLoginToken(clientPlayer.getLoginToken());
		dataOutput.setNextRoom(newRoomName);

		sendData(dataOutput);
	}

	private void enterRoom() {

		UserCommandData dataOutput = new UserCommandData();

		String selectedRoom = (String) list.getSelectedValue();
		clientPlayer.setRoomName(selectedRoom);

		dataOutput.setCommand("ENTERROOM");
		dataOutput.setUsername(clientPlayer.getUsername());
		dataOutput.setLoginToken(clientPlayer.getLoginToken());
		dataOutput.setNextRoom(selectedRoom);

		sendData(dataOutput);
	}

	/**
	 * Prepare and send data to the server containing login information.
	 */
	private void login() {

		String username = txtUsername.getText();
		String password = new String(txtPassword.getPassword());

		// prepare to send user name and hashed MD5 password to the server
		UserData data = new UserData(username, MD5.getHash(password));

		// send data to server
		sendData(data);
	}

	private boolean requiredRegisterFields() {
		String username = txtRegisterUsername.getText();
		String email = txtEmail.getText();
		String firstName = txtFirstName.getText();
		String birthday = (String) cmbRegisterYear.getSelectedItem()
				+ "-"
				+ new Integer(cmbRegisterMonth.getSelectedIndex() + 1)
						.toString() + "-"
				+ (String) cmbRegisterDay.getSelectedItem();

		if (username.equals("") || email.equals("") || firstName.equals("")
				|| birthday.equals(""))
			return false;
		return true;
	}

	private void register() {

		RegisterUserData dataRegisterUser = new RegisterUserData();

		String password = new String(txtRegisterPassword.getPassword());
		String passwordAgain = new String(txtRegisterPasswordAgain
				.getPassword());

		// check if user entered a password and if both password fields are
		// equal
		if (!password.equals(passwordAgain) || password.equals("")
				|| passwordAgain.equals("")) {

			if (password.equals("") || passwordAgain.equals("")) {
				JOptionPane.showMessageDialog(null,
						"Cannot have a blank password", "",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "Passwords do not match.",
						"", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (!requiredRegisterFields()) {
			JOptionPane.showMessageDialog(null,
					"Fields with an * are required!", "",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			dataRegisterUser.setFirstName(txtFirstName.getText());
			dataRegisterUser.setLastName(txtLastName.getText());
			dataRegisterUser.setBirthday((String) cmbRegisterYear
					.getSelectedItem()
					+ "-"
					+ new Integer(cmbRegisterMonth.getSelectedIndex() + 1)
							.toString()
					+ "-"
					+ (String) cmbRegisterDay.getSelectedItem());
			dataRegisterUser.setEmail(txtEmail.getText());
			dataRegisterUser.setUsername(txtRegisterUsername.getText());
			dataRegisterUser.setPassword(MD5.getHash(new String(
					txtRegisterPassword.getPassword())));
			// dataRegisterUser.setPasswordAgain(MD5.getHash(new
			// String(txtRegisterPasswordAgain.getPassword())));

			/*
			 * if (!registerCommandSent) { // send data to server sendData(new
			 * CommandData("REGISTER")); registerCommandSent = true; }
			 */
			sendData(dataRegisterUser);
		}
	}

	/**
	 * Change the number of days listed in the 'day' select box on the register
	 * screen when the 'month' select box is changed. The number of days listed
	 * is in sync with the month that is selected.
	 * 
	 * @param evt
	 *            mouse click action event
	 */
	private void cmbRegisterMonthItemStateChanged(java.awt.event.ItemEvent evt) {

		final int FEBRUARY = 1;
		final int APRIL = 3;
		final int JUNE = 5;
		final int SEPTEMBER = 8;
		final int NOVEMBER = 10;
		int itemChosen = cmbRegisterMonth.getSelectedIndex();

		if (itemChosen == APRIL || itemChosen == JUNE
				|| itemChosen == SEPTEMBER || itemChosen == NOVEMBER) {
			refreshRegisterScreenDays(30);
		} else if (itemChosen == FEBRUARY) {
			refreshRegisterScreenDays(28);
		} else
			refreshRegisterScreenDays(31);
	}

	/**
	 * Refreshes the 'day' select box on the register screen with the given
	 * number of days.
	 * 
	 * @param days
	 *            number of days to list
	 */
	private void refreshRegisterScreenDays(int days) {
		cmbRegisterDay.removeAllItems();
		for (int i = 1; i <= days; i++) {
			cmbRegisterDay.addItem(new Integer(i).toString());
		}
	}

	public void paint(Graphics g) {
		invalidate();
		validate();
		super.paint(g);
	}

}