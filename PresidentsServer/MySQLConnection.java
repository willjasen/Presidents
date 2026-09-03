package PresidentsServer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;

import PresidentsData.RegisterUserData;

/**
 * 
 * @author willjasen
 *
 */
public class MySQLConnection implements AutoCloseable {
	private static final String DEFAULT_DATABASE_URL = "jdbc:h2:./data/presidents";
	private final Connection con;

	public MySQLConnection() {
		try {
			String databaseUrl = System.getProperty("presidents.database.url", DEFAULT_DATABASE_URL);
			con = DriverManager.getConnection(databaseUrl, "sa", "");
			createSchema();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to open the Presidents account database", e);
		}
	}

	private void createSchema() throws SQLException {
		try (java.sql.Statement statement = con.createStatement()) {
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS player ("
					+ "username VARCHAR(40) PRIMARY KEY, "
					+ "password_hash VARCHAR(128) NOT NULL, "
					+ "email VARCHAR(254) NOT NULL, "
					+ "first_name VARCHAR(80) NOT NULL, "
					+ "last_name VARCHAR(80), "
					+ "birthday DATE)");
		}
	}

	public boolean loginAuthorized(String username, String password) {
		String sql = "SELECT password_hash FROM player WHERE username = ?";
		try (PreparedStatement statement = con.prepareStatement(sql)) {
			statement.setString(1, username);
			try (ResultSet result = statement.executeQuery()) {
				return result.next() && PasswordHash.verify(password, result.getString(1));
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to check login", e);
		}
	}

	public boolean register(RegisterUserData user) {
		String sql = "INSERT INTO player "
				+ "(username, password_hash, email, first_name, last_name, birthday) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement statement = con.prepareStatement(sql)) {
			statement.setString(1, user.getUsername());
			statement.setString(2, PasswordHash.create(user.getPassword()));
			statement.setString(3, user.getEmail());
			statement.setString(4, user.getFirstName());
			statement.setString(5, user.getLastName());
			statement.setObject(6, parseBirthday(user.getBirthday()));
			return statement.executeUpdate() == 1;
		} catch (SQLException | RuntimeException e) {
			return false;
		}
	}

	/** Accepts both the old client's yyyy-M-d dates and ISO yyyy-MM-dd dates. */
	static LocalDate parseBirthday(String birthday) {
		if (birthday == null) throw new DateTimeException("Birthday is missing");
		String[] parts = birthday.trim().split("-", -1);
		if (parts.length != 3) throw new DateTimeException("Birthday must be yyyy-MM-dd");
		try {
			return LocalDate.of(Integer.parseInt(parts[0]),
					Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
		} catch (NumberFormatException e) {
			throw new DateTimeException("Birthday must contain numbers", e);
		}
	}

	public String reasonLoginFailed(String username, String password) {
		try (PreparedStatement statement = con.prepareStatement(
				"SELECT 1 FROM player WHERE username = ?")) {
			statement.setString(1, username);
			try (ResultSet result = statement.executeQuery()) {
				return result.next() ? "Incorrect password" : "User does not exist.";
			}
		} catch (SQLException e) {
			return "Account database error.";
		}
	}

	@Override
	public void close() {
		try { con.close(); } catch (SQLException ignored) { }
	}
}

