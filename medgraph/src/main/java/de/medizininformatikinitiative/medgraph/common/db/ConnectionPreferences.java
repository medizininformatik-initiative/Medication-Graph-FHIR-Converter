package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.prefs.Preferences;

/**
 * Stores preferences regarding the Neo4j database connection.
 *
 * @author Markus Budeus
 */
public class ConnectionPreferences implements ConnectionPreferencesWriter {

	public static final String DEFAULT_URI = "neo4j://localhost:7687";
	public static final String DEFAULT_USER = "neo4j";
	public static final String URI_KEY = "uri";
	public static final String USER_KEY = "user";
	public static final String PASSWORD_KEY = "password";

	private final Preferences node;

	public ConnectionPreferences(Preferences node) {
		this.node = node;
	}

	/**
	 * Sets the connection uri which is to be stored in the preferences.
	 *
	 * @param connectionUri the connection uri to store
	 */
	@Override
	public void setConnectionUri(@NotNull String connectionUri) {
		node.put(URI_KEY, connectionUri);
	}

	/**
	 * Returns the stored connection uri or the {@link #DEFAULT_URI} if none is stored.
	 */
	@NotNull
	public String getConnectionUri() {
		return node.get(URI_KEY, DEFAULT_URI);
	}

	@Override
	public void setUser(@NotNull String user) {
		node.put(USER_KEY, user);
	}

	/**
	 * Returns the stored username or {@link #DEFAULT_USER} if none is stored.
	 */
	@NotNull
	public String getUser() {
		return node.get(USER_KEY, DEFAULT_USER);
	}

	@Override
	public void setPassword(char @NotNull [] password) {
		node.putByteArray(PASSWORD_KEY, toByteArray(password));
	}

	@Override
	public void clearPassword() {
		node.remove(PASSWORD_KEY);
	}

	/**
	 * Returns the stored password or null if none is stored.
	 */
	char @Nullable [] getPassword() {
		byte[] password = node.getByteArray(PASSWORD_KEY, null);
		if (password == null) return null;
		return fromByteArray(password);
	}

	/**
	 * Returns whether a password is saved within these connection preferences.
	 * @return true, if these preferences specify a password, false otherwise.
	 */
	public boolean hasConfiguredPassword() {
		return node.getByteArray(PASSWORD_KEY, null) != null;
	}

	private byte[] toByteArray(char[] array) {
		// Not my code. Taken from https://stackoverflow.com/a/9670279 on 2024-05-14, although I slightly changed it
		CharBuffer charBuffer = CharBuffer.wrap(array);
		ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
		byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
				byteBuffer.position(), byteBuffer.limit());
		Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
		return bytes;
	}

	private char[] fromByteArray(byte[] bytes) {
		// I did write this function myself though.
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
		// This seems excessive. As per the documentation, the buffer's position must be zero at this point,
		// and one would assume the backing array has exactly the size of the buffer's limit.
		// But apparently, the backing array includes an additional zero byte at the end, necessitating this approach.
		char[] characters = Arrays.copyOfRange(charBuffer.array(),
				charBuffer.position(), charBuffer.limit());
		Arrays.fill(charBuffer.array(), (char) 0);
		return characters;
	}

}
