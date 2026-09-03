package PresidentsServer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;

/**
 * 
 * @author willjasen
 *
 */
public class ServerGUI extends JFrame implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1718424793605246638L;
	
	private static final int WINDOW_HEIGHT = 800;
	private static final int WINDOW_WIDTH = 600;
	
	private Server serverInstance;
	
	private JButton btnStartServer;
	private JButton btnStopServer;
	
	private JTextArea txtArea;
	

	public ServerGUI() {
		serverInstance = new Server(this);
		setupMainScreen();
		startServerInBackground();
	}
	
	public ServerGUI(String title) {
		this();
		this.setTitle(title);
	}
	
	private void createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu serverMenu = new JMenu("Server");
		serverMenu.setMnemonic(KeyEvent.VK_S);

		JMenuItem startItem = new JMenuItem("Start");
		startItem.addActionListener(event -> startServerInBackground());
		serverMenu.add(startItem);

		JMenuItem stopItem = new JMenuItem("Stop");
		stopItem.addActionListener(event -> serverInstance.stopServer());
		serverMenu.add(stopItem);
		serverMenu.addSeparator();

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(event -> {
			serverInstance.stopServer();
			dispose();
		});
		serverMenu.add(exitItem);
		menuBar.add(serverMenu);

		JMenu helpMenu = new JMenu("Help");
		JMenuItem aboutItem = new JMenuItem("About Presidents");
		aboutItem.addActionListener(event -> JOptionPane.showMessageDialog(this,
				"Presidents — also commonly known as Asshole",
				"About Presidents", JOptionPane.INFORMATION_MESSAGE));
		helpMenu.add(aboutItem);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
	}
	
	private void setupMainScreen() {
		
		Container pane = getContentPane();
		
		// top portion of screen
		JPanel pnlTop = new JPanel();
		btnStartServer = new JButton("Start Server");
		btnStopServer = new JButton("Stop Server");
		btnStartServer.addActionListener(this);
		btnStopServer.addActionListener(this);
		pnlTop.add(btnStartServer);
		pnlTop.add(btnStopServer);
		
		// bottom portion of screen
		//JPanel pnlBottom = new JPanel();
		txtArea = new JTextArea();
		txtArea.setLineWrap(true);
	    txtArea.setWrapStyleWord(true);
	    txtArea.setEditable(false);
	    JScrollPane scrollPane = new JScrollPane(txtArea);
		
	    // entire screen
		JPanel pnlScreen = new JPanel(new GridLayout(2,0));
		pnlScreen.add(pnlTop);
		pnlScreen.add(scrollPane);
		
		// set frame properties
		setSize(new Dimension(WINDOW_HEIGHT, WINDOW_WIDTH));
		setTitle("Presidents Server");
		this.setResizable(false);
		
		//2. Optional: What happens when the frame closes?
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create components and put them in the frame
		pane.add(pnlScreen);
		createMenuBar();
		
		//5. Show it.
		setVisible(true);
		
	}
	
	public void writeToTextArea(String text) {
		SwingUtilities.invokeLater(() -> txtArea.append(text));
	}

	private void startServerInBackground() {
		Thread serverThread = new Thread(serverInstance::startServer, "presidents-server");
		serverThread.setDaemon(true);
		serverThread.start();
	}
	
	public void actionPerformed(ActionEvent evt) {

		Object obj = evt.getSource();

		if (obj.equals(btnStartServer)) {
			startServerInBackground();
			repaint();
		} else if (obj.equals(btnStopServer)) {
			serverInstance.stopServer();
			repaint();
		}

	}
}
