package PresidentsServer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import PresidentsData.RegisterUserData;

class AccountDatabaseTest {
	@Test
	void registersAndAuthenticatesAnAccount() {
		System.setProperty("presidents.database.url",
				"jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
		MySQLConnection database = new MySQLConnection();
		RegisterUserData user = new RegisterUserData();
		user.setUsername("test-player");
		user.setPassword("hashed-password");
		user.setEmail("player@example.com");
		user.setFirstName("Test");
		user.setLastName("Player");
		user.setBirthday("2000-01-02");

		assertTrue(database.register(user));
		assertTrue(database.loginAuthorized("test-player", "hashed-password"));
		assertFalse(database.loginAuthorized("test-player", "wrong-password"));
		assertFalse(database.register(user));
		database.close();
	}

	@Test
	void acceptsTheUnpaddedBirthdayFormatSentByTheOldClient() {
		System.setProperty("presidents.database.url",
				"jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
		MySQLConnection database = new MySQLConnection();
		RegisterUserData user = new RegisterUserData();
		user.setUsername("legacy-date-player");
		user.setPassword("hashed-password");
		user.setEmail("legacy@example.com");
		user.setFirstName("Legacy");
		user.setLastName("Player");
		user.setBirthday("2026-1-1");

		assertTrue(database.register(user));
		assertTrue(database.loginAuthorized("legacy-date-player", "hashed-password"));
		database.close();
	}
}
