package PresidentsClient;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*; // Swing GUI classes are defined here.
import PresidentsData.ChatData;
import PresidentsData.CommandData;
import PresidentsData.Data;
import PresidentsData.GameActionData;
import PresidentsData.GameStateData;
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
public class Client extends JFrame {

	private static final long serialVersionUID = -5085586743177866914L;

	private final int WINDOW_WIDTH = 1024;
	private final int WINDOW_HEIGHT = 768;

	private Human clientPlayer;
	private ClientNetwork network;

	private JList<String> list;

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
	private javax.swing.JComboBox<String> cmbRegisterDay;
	private javax.swing.JComboBox<String> cmbRegisterMonth;
	private javax.swing.JComboBox<String> cmbRegisterYear;
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
	private javax.swing.JButton btnPass;
	private javax.swing.JButton btnStartGame;
	private javax.swing.JButton btnRoomChat;
	private javax.swing.JLabel lblGameStatus;
	private javax.swing.JLabel lblTable;
	private javax.swing.JList<String> lstRoomPlayers;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLayeredPane pnlHand;
	private javax.swing.JLayeredPane pnlLeftPlayer;
	private javax.swing.JLayeredPane pnlRightPlayer;
	private javax.swing.JPanel pnlRoomChat;
	private javax.swing.JPanel pnlRoomScreen;
	private javax.swing.JLayeredPane pnlTopPlayer;
	private GameStateData currentGameState;

	// End of room screen variables declaration

	public Client() {
		super("Presidents");
		// start the connection to the server when program is started
		network = new ClientNetwork();
		if (network.isConnected()) {
			network.createGameThread(this);
			network.createChatThread(this);
		}
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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent event) {
				network.close();
			}
		});
		setLocationRelativeTo(null);
		setVisible(true);
		if (!network.isConnected()) {
			JOptionPane.showMessageDialog(this,
					"The Presidents server is not running. Start the server, then reopen this client.",
					"Unable to connect", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) {
		new Client().init();
	}
	
	public void updateLobbyPlayerList(CommandData dataInput) {
		ArrayList<String> players = dataInput.getPlayers();
		DefaultListModel<String> listModelPlayers = new DefaultListModel<String>();

		for (int i = 0; i < players.size(); i++) {
			listModelPlayers.addElement(players.get(i));
			System.out.println("1" + players.get(i));
		}

		list = new JList<String>(listModelPlayers);
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
		btnLobbyChat.addActionListener(evt -> sendChatData());

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
		btnLobbyEnterRoom.addActionListener(evt -> enterRoom());

        txtLobbyRooms.setColumns(20);
        txtLobbyRooms.setEditable(false);
        txtLobbyRooms.setRows(5);
        spLobbyRooms.setViewportView(txtLobbyRooms);

		btnLobbyCreateRoom.setText("Create Room");
		btnLobbyCreateRoom.addActionListener(evt -> createNewRoom());

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

		btnRegister.setText("Create Account");
		btnRegister.addActionListener(evt -> {
			sendData(new CommandData("REGISTER"));
			createRegisterScreen();
		});

		lblUsername.setText("Username:");

		btnLogin.setText("Login");
		btnLogin.addActionListener(evt -> login());

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
		cmbRegisterMonth = new javax.swing.JComboBox<String>();
		cmbRegisterDay = new javax.swing.JComboBox<String>();
		cmbRegisterYear = new javax.swing.JComboBox<String>();

		btnRegisterUser.setText("Register");
		btnRegisterUser.addActionListener(evt -> register());

		lblRegisterPasswordAgain.setText("*Password again:");

		lblRequiredFields.setText("(*fields are required)");

		lblFirstName.setText("*First Name:");

		lblEmail.setText("*Email:");

		lblRegisterUsername.setText("*Username:");

		lblBirthday.setText("*Birthday:");

		lblRegisterPassword.setText("*Password:");

		lblLastName.setText("Last Name:");

		cmbRegisterMonth.setModel(new javax.swing.DefaultComboBoxModel<String>(
				new String[] { "January", "February", "March", "April", "May",
						"June", "July", "August", "September", "October",
						"November", "December" }));
		cmbRegisterMonth.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				cmbRegisterMonthItemStateChanged(evt);
			}
		});

		cmbRegisterDay.setModel(new javax.swing.DefaultComboBoxModel<String>(
				new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9",
						"10", "11", "12", "13", "14", "15", "16", "17", "18",
						"19", "20", "21", "22", "23", "24", "25", "26", "27",
						"28", "29", "30", "31" }));

		DefaultComboBoxModel<String> years = new DefaultComboBoxModel<String>();
		for (int year = java.time.Year.now().getValue(); year >= 1900; year--) {
			years.addElement(Integer.toString(year));
		}
		cmbRegisterYear.setModel(years);
		cmbRegisterYear.setSelectedItem(Integer.toString(
				java.time.Year.now().getValue() - 18));
		cmbRegisterYear.addItemListener(evt -> cmbRegisterMonthItemStateChanged(evt));

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
			if (tempLabel == clickedLabel) {
				tempLabel.setLocation(tempLabel.getX(), tempCard.hasBeenClicked() ? 12 : 2);
				tempCard.switchClicked();
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
		btnRoomChat.addActionListener(evt -> sendChatData());

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
		btnPlayCards.addActionListener(evt -> playSelectedCards());

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

	private void showGameRoomScreen(GameStateData state) {
		if (pnlRoomScreen == null || pnlRoomScreen.getParent() == null) {
			getContentPane().removeAll();
			pnlRoomScreen = new JPanel(new BorderLayout(12, 12));
			pnlRoomScreen.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

			lblGameStatus = new JLabel();
			lblGameStatus.setFont(lblGameStatus.getFont().deriveFont(Font.BOLD, 16f));
			lblTable = new JLabel(" ", SwingConstants.CENTER);
			lblTable.setFont(lblTable.getFont().deriveFont(Font.BOLD, 20f));

			pnlHand = new JLayeredPane();
			pnlHand.setPreferredSize(new Dimension(900, 145));
			JScrollPane handScroll = new JScrollPane(pnlHand,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			handScroll.setBorder(BorderFactory.createTitledBorder("Your hand — click cards to select"));

			lstRoomPlayers = new JList<String>();
			JScrollPane playersScroll = new JScrollPane(lstRoomPlayers);
			playersScroll.setPreferredSize(new Dimension(185, 180));
			playersScroll.setBorder(BorderFactory.createTitledBorder("Players"));

			JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
			tablePanel.add(lblTable, BorderLayout.NORTH);
			tablePanel.add(handScroll, BorderLayout.CENTER);
			tablePanel.add(playersScroll, BorderLayout.EAST);

			btnStartGame = new JButton("Start Game");
			btnStartGame.addActionListener(evt -> sendGameAction(GameActionData.START, null));
			btnPlayCards = new JButton("Play Selected Cards");
			btnPlayCards.addActionListener(evt -> playSelectedCards());
			btnPass = new JButton("Pass");
			btnPass.addActionListener(evt -> sendGameAction(GameActionData.PASS, null));
			JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
			controls.add(btnStartGame);
			controls.add(btnPlayCards);
			controls.add(btnPass);
			tablePanel.add(controls, BorderLayout.SOUTH);

			if (txtChatRoom == null) txtChatRoom = new JTextArea();
			txtChatRoom.setEditable(false);
			txtChatRoom.setRows(7);
			txtChatMessage = new JTextField();
			btnRoomChat = new JButton("Send");
			btnRoomChat.addActionListener(evt -> sendChatData());
			txtChatMessage.addActionListener(evt -> sendChatData());
			JPanel chatInput = new JPanel(new BorderLayout(6, 0));
			chatInput.add(txtChatMessage, BorderLayout.CENTER);
			chatInput.add(btnRoomChat, BorderLayout.EAST);
			pnlRoomChat = new JPanel(new BorderLayout(4, 4));
			pnlRoomChat.setBorder(BorderFactory.createTitledBorder("Room chat"));
			pnlRoomChat.add(new JScrollPane(txtChatRoom), BorderLayout.CENTER);
			pnlRoomChat.add(chatInput, BorderLayout.SOUTH);
			pnlRoomChat.setPreferredSize(new Dimension(900, 190));

			pnlRoomScreen.add(lblGameStatus, BorderLayout.NORTH);
			pnlRoomScreen.add(tablePanel, BorderLayout.CENTER);
			pnlRoomScreen.add(pnlRoomChat, BorderLayout.SOUTH);
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(pnlRoomScreen, BorderLayout.CENTER);
			getContentPane().revalidate();
			getContentPane().repaint();
		}
		updateGameRoomScreen(state);
	}

	private void updateGameRoomScreen(GameStateData state) {
		currentGameState = state;
		setTitle("Presidents — " + clientPlayer.getUsername() + " — " + state.getRoomName());

		ArrayList<Card> cards = state.getHand().asList();
		cards.sort((left, right) -> {
			int rank = Integer.compare(left.getValue(), right.getValue());
			return rank != 0 ? rank : Character.compare(left.getSuit(), right.getSuit());
		});
		Hand sortedHand = new Hand();
		sortedHand.addCards(cards);
		clientPlayer.setHand(sortedHand);
		pnlHand.removeAll();
		int spacing = cards.size() > 20 ? 25 : 32;
		for (int i = 0; i < cards.size(); i++) {
			Card card = cards.get(i);
			JLabel label = new JLabel(new ImageIcon(getClass().getResource(
					"/PresidentsClient/cards/classic/" + card + ".png")));
			label.setBounds(12 + spacing * i, 12, 71, 96);
			label.setToolTipText(cardName(card));
			label.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override public void mouseClicked(java.awt.event.MouseEvent evt) {
					clickedOnCard(evt);
				}
			});
			card.setLabel(label);
			pnlHand.add(label, Integer.valueOf(i));
		}
		pnlHand.setPreferredSize(new Dimension(Math.max(700, 95 + spacing * cards.size()), 125));
		pnlHand.revalidate();
		pnlHand.repaint();

		DefaultListModel<String> players = new DefaultListModel<String>();
		ArrayList<String> names = state.getPlayerNames();
		ArrayList<Integer> counts = state.getCardCounts();
		for (int i = 0; i < names.size(); i++) {
			String suffix = "WAITING".equals(state.getStatus()) ? "ready"
					: counts.get(i) + (counts.get(i) == 1 ? " card" : " cards");
			players.addElement(names.get(i) + " — " + suffix);
		}
		lstRoomPlayers.setModel(players);

		boolean waiting = "WAITING".equals(state.getStatus());
		boolean playing = "PLAYING".equals(state.getStatus());
		boolean myTurn = playing && clientPlayer.getUsername().equals(state.getCurrentPlayer());
		btnStartGame.setVisible(waiting);
		btnStartGame.setEnabled(waiting);
		btnPlayCards.setVisible(!waiting);
		btnPlayCards.setEnabled(myTurn);
		btnPass.setVisible(!waiting);
		btnPass.setEnabled(myTurn && state.getCardsInPlay() > 0);

		if (waiting) {
			lblGameStatus.setText("Room: " + state.getRoomName()
					+ " — waiting for 2–7 players (" + names.size() + " joined)");
			lblTable.setText("Start the game after another player joins");
		} else if (playing) {
			lblGameStatus.setText(myTurn ? "Your turn" : "Waiting for "
					+ state.getCurrentPlayer());
			lblTable.setText(state.getCardsInPlay() == 0
					? "New trick — the leader cannot pass"
					: "Current play: " + state.getCardsInPlay() + " × "
							+ rankName(state.getValueInPlay()));
		} else {
			lblGameStatus.setText("Game over — " + formatFinishOrder(state.getFinishOrder()));
			lblTable.setText("Winner: " + (state.getFinishOrder().isEmpty()
					? "unknown" : state.getFinishOrder().get(0)));
		}
		if (state.getMessage() != null && !state.getMessage().isBlank()) {
			JOptionPane.showMessageDialog(this, state.getMessage(), "Presidents",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private String formatFinishOrder(ArrayList<String> finishOrder) {
		ArrayList<String> places = new ArrayList<String>();
		for (int i = 0; i < finishOrder.size(); i++) {
			places.add((i + 1) + ". " + finishOrder.get(i));
		}
		return String.join("   ", places);
	}

	private String cardName(Card card) {
		return rankName(card.getValue()) + " of " + switch (card.getSuit()) {
		case 'c' -> "clubs";
		case 'h' -> "hearts";
		case 's' -> "spades";
		case 'd' -> "diamonds";
		default -> "unknown suit";
		};
	}

	private String rankName(int value) {
		return switch (value) {
		case 1 -> "3";
		case 2 -> "4";
		case 3 -> "5";
		case 4 -> "6";
		case 5 -> "7";
		case 6 -> "8";
		case 7 -> "9";
		case 8 -> "10";
		case 9 -> "Jack";
		case 10 -> "Queen";
		case 11 -> "King";
		case 12 -> "Ace";
		case 13 -> "2";
		default -> "none";
		};
	}

	private void sendGameAction(String action, java.util.List<Card> cards) {
		GameActionData data = new GameActionData(action, cards);
		data.setUsername(clientPlayer.getUsername());
		data.setLoginToken(clientPlayer.getLoginToken());
		data.setNextRoom(clientPlayer.getRoomName());
		sendData(data);
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
					setTitle("Presidents — " + txtUsername.getText());
					clientPlayer = new Human(txtUsername.getText(),
							commandDataInput.getLoginToken());
					clientPlayer.setRoomName("Lobby");
					createLobbyScreen(commandDataInput);

				} else {
					setTitle("Presidents — Not logged in");
					JOptionPane.showMessageDialog(null,
							"Login failed. If this is your first time running Presidents, "
									+ "choose Create Account first.",
							"Unable to log in",
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

				else if (commandDataInput.getSubCommand().equals("REGISTER_NO")) {

					String username = txtRegisterUsername.getText();
					String output = "The account for " + username
							+ " could not be created. The username may already be in use, "
							+ "or one of the account fields may be invalid.";
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
				DefaultListModel<String> listModel = new DefaultListModel<String>();

				for (int i = 2; i < rooms.size(); i++) {
					listModel.addElement(rooms.get(i));
				}

				list = new JList<String>(listModel);
				spLobbyRooms.setViewportView(list);
			}

			else if (commandDataInput.getCommand().equals("ENTERROOM_OK")) {
				String enteredRoom = commandDataInput.getSubCommand();
				clientPlayer.setRoomName(enteredRoom);
				sendGameAction(GameActionData.GET_STATE, null);
				// add a waiting screen for players joining?
			}

			else if (commandDataInput.getCommand().equals("ENTERROOM_NO")
					|| commandDataInput.getCommand().equals("CREATE_ROOM_NO")) {
				JOptionPane.showMessageDialog(this,
						"That room is unavailable. Refresh the lobby and choose another name.",
						"Room unavailable", JOptionPane.INFORMATION_MESSAGE);
			}

			else if (commandDataInput.getCommand().equals("ROOMINFO")) {
				// createRoomScreen(null);
			}
		}
		if (dataInput instanceof RoomData) {
			RoomData roomData = (RoomData) dataInput;
			createRoomScreen((roomData));
		}
		if (dataInput instanceof GameStateData) {
			showGameRoomScreen((GameStateData) dataInput);
		}
	}

	private void playSelectedCards() {
		ArrayList<Card> selected = new ArrayList<Card>();
		for (Card card : clientPlayer.getHand()) {
			if (card.hasBeenClicked()) selected.add(card);
		}
		if (selected.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Click one or more cards first.",
					"No cards selected", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		sendGameAction(GameActionData.PLAY, selected);
	}

	private void createNewRoom() {

		UserCommandData dataOutput = new UserCommandData();

		String newRoomName = JOptionPane.showInputDialog(this,
				"Room name:", "Create Room",
				JOptionPane.QUESTION_MESSAGE);
		if (newRoomName == null || newRoomName.trim().isEmpty()) return;
		newRoomName = newRoomName.trim();

		dataOutput.setCommand("CREATE_ROOM");
		dataOutput.setUsername(clientPlayer.getUsername());
		dataOutput.setLoginToken(clientPlayer.getLoginToken());
		dataOutput.setNextRoom(newRoomName);

		sendData(dataOutput);
	}

	private void enterRoom() {

		UserCommandData dataOutput = new UserCommandData();

		String selectedRoom = list.getSelectedValue();
		if (selectedRoom == null) {
			JOptionPane.showMessageDialog(this, "Select a room first.",
					"No room selected", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

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

		String username = txtUsername.getText().trim();
		String password = new String(txtPassword.getPassword());
		if (username.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Enter both a username and password.",
					"Missing login", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// prepare to send user name and hashed MD5 password to the server
		UserData data = new UserData(username, MD5.getHash(password));

		// send data to server
		sendData(data);
	}

	private String selectedBirthday() {
		int year = Integer.parseInt((String) cmbRegisterYear.getSelectedItem());
		int month = cmbRegisterMonth.getSelectedIndex() + 1;
		int day = Integer.parseInt((String) cmbRegisterDay.getSelectedItem());
		return String.format("%04d-%02d-%02d", year, month, day);
	}

	private String registrationValidationError() {
		String username = txtRegisterUsername.getText().trim();
		String email = txtEmail.getText().trim();
		String firstName = txtFirstName.getText().trim();

		if (username.isEmpty() || email.isEmpty() || firstName.isEmpty())
			return "Fields with an * are required!";
		if (!email.contains("@") || email.startsWith("@") || email.endsWith("@"))
			return "Please enter a valid email address.";
		try {
			java.time.LocalDate birthday = java.time.LocalDate.parse(selectedBirthday());
			if (birthday.isAfter(java.time.LocalDate.now()))
				return "Birthday cannot be in the future.";
		} catch (RuntimeException e) {
			return "Please enter a valid birthday.";
		}
		return null;
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
		} else {
			String validationError = registrationValidationError();
			if (validationError != null) {
				JOptionPane.showMessageDialog(null, validationError, "Sign up",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			dataRegisterUser.setFirstName(txtFirstName.getText().trim());
			dataRegisterUser.setLastName(txtLastName.getText().trim());
			dataRegisterUser.setBirthday(selectedBirthday());
			dataRegisterUser.setEmail(txtEmail.getText().trim());
			dataRegisterUser.setUsername(txtRegisterUsername.getText().trim());
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
		if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
			refreshRegisterScreenDays();
		}
	}

	/**
	 * Refreshes the 'day' select box on the register screen with the given
	 * number of days.
	 * 
	 * @param days
	 *            number of days to list
	 */
	private void refreshRegisterScreenDays() {
		if (cmbRegisterYear.getSelectedItem() == null || cmbRegisterMonth.getSelectedIndex() < 0) return;
		int year = Integer.parseInt((String) cmbRegisterYear.getSelectedItem());
		int month = cmbRegisterMonth.getSelectedIndex() + 1;
		int days = java.time.YearMonth.of(year, month).lengthOfMonth();
		String selectedDay = (String) cmbRegisterDay.getSelectedItem();
		cmbRegisterDay.removeAllItems();
		for (int i = 1; i <= days; i++) {
			cmbRegisterDay.addItem(Integer.toString(i));
		}
		if (selectedDay != null && Integer.parseInt(selectedDay) <= days) {
			cmbRegisterDay.setSelectedItem(selectedDay);
		}
	}

}
