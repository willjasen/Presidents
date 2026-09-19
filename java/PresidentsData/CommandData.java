package PresidentsData;

import java.util.ArrayList;

/**
 * This class should no longer be used. Functionality of this class was
 * transferred to the UserCommandData class.
 * 
 * @author willjasen
 * 
 */
public class CommandData extends Data {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2555730582506409425L;

	private ArrayList<String> info;
	private ArrayList<String> players;

	public CommandData() {

	}

	public CommandData(ArrayList<String> info) {
		this.info = info;
	}

	public CommandData(String command) {
		info = new ArrayList<String>();
		info.add(null);
		info.add(command);
	}

	public CommandData(String command, String subCommand) {
		info = new ArrayList<String>();
		info.add(null);
		info.add(command);
		info.add(subCommand);
	}
	
	public ArrayList<String> getLobbyPlayers() {
		return players;
	}
	
	public ArrayList<String> getPlayers() {
		return players;
	}
	
	public void setPlayers(ArrayList<String> players) {
		this.players = players;
	}

	public String get(int i) {
		return info.get(i);
	}

	public String getLoginToken() {
		return info.get(0);
	}

	public String getCommand() {
		return info.get(1);
	}

	public String getSubCommand() {
		return info.get(2);
	}

	public ArrayList<String> getInfo() {
		return info;
	}

	public void setRoomInfo(ArrayList<String> info) {
		this.info.addAll(info);
	}
}
