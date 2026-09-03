package PresidentsServer;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Stores account credentials as salted PBKDF2 hashes. */
final class PasswordHash {
	private static final int ITERATIONS = 210_000;
	private static final int KEY_BITS = 256;
	private static final int SALT_BYTES = 16;
	private static final SecureRandom RANDOM = new SecureRandom();

	private PasswordHash() { }

	static String create(String credential) {
		if (credential == null || credential.isEmpty()) {
			throw new IllegalArgumentException("Credential cannot be blank");
		}
		byte[] salt = new byte[SALT_BYTES];
		RANDOM.nextBytes(salt);
		byte[] hash = derive(credential, salt, ITERATIONS);
		return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
				+ "$" + Base64.getEncoder().encodeToString(hash);
	}

	static boolean verify(String credential, String stored) {
		if (credential == null || stored == null) return false;
		try {
			String[] parts = stored.split("\\$");
			if (parts.length != 4 || !"pbkdf2".equals(parts[0])) return false;
			int iterations = Integer.parseInt(parts[1]);
			byte[] salt = Base64.getDecoder().decode(parts[2]);
			byte[] expected = Base64.getDecoder().decode(parts[3]);
			byte[] actual = derive(credential, salt, iterations);
			return MessageDigest.isEqual(expected, actual);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static byte[] derive(String credential, byte[] salt, int iterations) {
		PBEKeySpec spec = new PBEKeySpec(credential.toCharArray(), salt, iterations, KEY_BITS);
		try {
			return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
					.generateSecret(spec).getEncoded();
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("PBKDF2 is unavailable", e);
		} finally {
			spec.clearPassword();
		}
	}
}
