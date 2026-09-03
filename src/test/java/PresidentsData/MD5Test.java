package PresidentsData;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MD5Test {
	@Test
	void producesTheStandardPaddedHash() {
		assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", MD5.getHash("password"));
		assertEquals(32, MD5.getHash("a").length());
	}
}
