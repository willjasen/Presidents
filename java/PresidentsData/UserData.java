package PresidentsData;

/**
 * 
 * @author willjasen
 * 
 */
public class UserData extends Data {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1087068224977767773L;

	protected String username;
	protected String password;
	protected String loginToken;

	public UserData() {

	}

	/**
	 * 
	 * @param username
	 *            - the user name of the user
	 * @param password
	 *            - the hashed password of the user
	 */
	public UserData(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getLoginToken() {
		return loginToken;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String name) {
		this.username = name;
	}

}
