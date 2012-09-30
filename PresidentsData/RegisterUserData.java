package PresidentsData;

/**
 * 
 * @author willjasen
 *
 */
public class RegisterUserData extends UserData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3670026323721480082L;
	private String passwordAgain;
	private String email;
	private String firstName;
	private String lastName;
	private String birthday;
	
	public String getBirthday() {
		return birthday;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPasswordAgain() {
		return passwordAgain;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setPasswordAgain(String passwordAgain) {
		this.passwordAgain = passwordAgain;
	}
}
