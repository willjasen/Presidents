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
	
	JMenuBar menuBar;
	JMenu menu, submenu;
	JMenuItem menuItem;
	JMenuItem menuItemAbout;
	JRadioButtonMenuItem rbMenuItem;
	JCheckBoxMenuItem cbMenuItem;
	
	public ServerGUI() {
		
		setupMainScreen();
		serverInstance = new Server(this);
		serverInstance.startServer();
	}
	
	public ServerGUI(String title) {
		this();
		this.setTitle(title);
	}
	
	private void createMenuBar() {

		// Create the menu bar.
		menuBar = new JMenuBar();

		// Build the first menu.
		menu = new JMenu("Game");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription(
				"The only menu in this program that has menu items");
		menuBar.add(menu);

		// a group of JMenuItems
		menuItem = new JMenuItem("A text-only menu item", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
				ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"This doesn't really do anything");
		menu.add(menuItem);

		menuItem = new JMenuItem("Both text and icon", new ImageIcon(
				"images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_B);
		menu.add(menuItem);

		menuItem = new JMenuItem(new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_D);
		menu.add(menuItem);

		// a group of radio button menu items
		menu.addSeparator();
		ButtonGroup group = new ButtonGroup();
		rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
		rbMenuItem.setSelected(true);
		rbMenuItem.setMnemonic(KeyEvent.VK_R);
		group.add(rbMenuItem);
		menu.add(rbMenuItem);

		rbMenuItem = new JRadioButtonMenuItem("Another one");
		rbMenuItem.setMnemonic(KeyEvent.VK_O);
		group.add(rbMenuItem);
		menu.add(rbMenuItem);

		// a group of check box menu items
		menu.addSeparator();
		cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
		cbMenuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Another one");
		cbMenuItem.setMnemonic(KeyEvent.VK_H);
		menu.add(cbMenuItem);

		// a submenu
		menu.addSeparator();
		submenu = new JMenu("A submenu");
		submenu.setMnemonic(KeyEvent.VK_S);

		menuItem = new JMenuItem("An item in the submenu");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
				ActionEvent.ALT_MASK));
		submenu.add(menuItem);

		menuItem = new JMenuItem("Another item");
		submenu.add(menuItem);
		menu.add(submenu);

		// Build second menu in the menu bar.
		menu = new JMenu("About");
		menu.setMnemonic(KeyEvent.VK_N);
		menu.getAccessibleContext().setAccessibleDescription(
				"This menu does nothing");
		menuBar.add(menu);

		this.setJMenuBar(menuBar);
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
		txtArea.append(text);
	}
	
	public void actionPerformed(ActionEvent evt) {

		Object obj = evt.getSource();

		if (obj.equals(btnStartServer)) {
			serverInstance.startServer();
			repaint();
		} else if (obj.equals(btnStopServer)) {
			serverInstance.stopServer();
			repaint();
		}

	}
}
