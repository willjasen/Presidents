package PresidentsData;

/**
 * 
 * @author willjasen
 *
 */
public class UserCommandData extends UserData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7946901052665535988L;
	
	private String command;
	private String nextRoom;
	
	public UserCommandData() {
		
	}
	
	public UserCommandData(String command) {
		this.command = command;
	}
	
	public String getNextRoom() {
		return nextRoom;
	}
	
	public void setNextRoom(String nextRoom) {
		this.nextRoom = nextRoom;
	}

	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
}
