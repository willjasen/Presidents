package PresidentsData;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;

/** Creates size-limited object streams that accept only game message classes. */
public final class SafeObjectInput {
	private static final ObjectInputFilter FILTER = ObjectInputFilter.Config.createFilter(
			"maxdepth=20;maxrefs=10000;maxbytes=1048576;"
			+ "PresidentsData.*;PresidentsPlayer.Card;PresidentsPlayer.Hand;"
			+ "java.util.ArrayList;java.lang.Object;java.lang.String;java.lang.Integer;java.lang.Number;!*"
	);

	private SafeObjectInput() { }

	public static ObjectInputStream open(InputStream input) throws IOException {
		ObjectInputStream stream = new ObjectInputStream(input);
		stream.setObjectInputFilter(FILTER);
		return stream;
	}
}
