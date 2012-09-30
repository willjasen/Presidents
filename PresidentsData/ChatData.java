package PresidentsData;

public class ChatData extends Data {

	private static final long serialVersionUID = 2365690445854228656L;

	private String username;
	private String chatRoomName;
	private String chatString;

	public ChatData() {

	}

	public ChatData(String username, String chatRoomName, String chatString) {
		this.username = username;
		this.chatRoomName = chatRoomName;
		this.chatString = chatString;
	}

	public String getRoomName() {
		return chatRoomName;
	}

	public void setName(String chatRoomName) {
		this.chatRoomName = chatRoomName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getChatString() {
		return chatString;
	}

	public void setChatString(String chatString) {
		this.chatString = chatString;
	}

	public String toString() {
		return getUsername() + ": " + getChatString();
	}

}
