package PresidentsData;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import PresidentsPlayer.Card;
import PresidentsPlayer.Hand;

class SafeObjectInputTest {
	@Test
	void acceptsRoomSnapshotsAndTheirCardGraph() throws Exception {
		Hand hand = new Hand();
		hand.addCard(new Card(1, 0));
		RoomData room = new RoomData(hand);
		room.getPlayerNames().add("Player");

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
			output.writeObject(room);
		}

		try (var input = SafeObjectInput.open(new ByteArrayInputStream(bytes.toByteArray()))) {
			RoomData restored = (RoomData) input.readObject();
			assertEquals(new Card(1, 0), restored.getHand().getCard(0));
			assertEquals("Player", restored.getPlayerNames().get(0));
		}
	}
}
