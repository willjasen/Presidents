package PresidentsServer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

/**
 * 
 * @author willjasen
 *
 */
public class MySQLConnection {
	String url; // holds URL pointing to the MySQL database
	Connection con; // connection to the database
	java.sql.Statement stmt;

	public MySQLConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			url = "jdbc:mysql://localhost:3306/test";
			con = DriverManager.getConnection(url, "root", "test");
			stmt = con.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean loginAuthorized(String username, String password) {
		String sqlString = "";
		//stmt = con.prepareStatement(sqlString);
		ResultSet rs = query("SELECT * FROM player WHERE username='" + username
				+ "' AND md5password='" + password + "'");
		try {
			return rs.next();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public ResultSet query(String query) {
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;
	}
	
	public int update(String query) throws MySQLIntegrityConstraintViolationException {
		int result=-1;
		try {
			result = stmt.executeUpdate(query);
		} catch (Exception e) {
			//e.printStackTrace();
			result=-1;
		}

		return result;
	}
	
	public String reasonLoginFailed(String username, String password) {
		ResultSet rs = query("SELECT username FROM player WHERE username='" + username + "'");
		boolean userExists=false;
		
		try {
			userExists = rs.next();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(userExists)
			return "Incorrect password";
		else 
			return "User does not exist.";
	}
}

	