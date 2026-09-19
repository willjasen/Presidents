package PresidentsServer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHashTest {
	@Test
	void saltsAndVerifiesStoredCredentials() {
		String first = PasswordHash.create("client-credential");
		String second = PasswordHash.create("client-credential");
		assertNotEquals(first, second);
		assertTrue(PasswordHash.verify("client-credential", first));
		assertFalse(PasswordHash.verify("wrong", first));
		assertFalse(PasswordHash.verify("client-credential", "malformed"));
	}
}
