package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * Stores connection configuration for a {@link DatabaseConnection}. This class is immutable.
 *
 * @author Markus Budeus
 */
public class ConnectionConfiguration {

	@NotNull
	private final String uri;
	@NotNull
	private final String user;
	/**
	 * Password may be unconfigured if none was specified by the preferences.
	 */
	private final char @Nullable [] password;

	/**
	 * Creates a connection configuration using data from the given preferences.
	 *
	 * @param preferences the preferences from which to retrieve the configuration
	 */
	public ConnectionConfiguration(@NotNull ConnectionPreferences preferences) {
		this(preferences.getConnectionUri(), preferences.getUser(), preferences.getPassword());
	}

	/**
	 * Creates a connection configuration using the given uri and user, but taking the password from the given
	 * configuration.
	 *
	 * @param uri           the connection URI
	 * @param user          the user to authenticate with
	 * @param configuration the other configuration from which to take the password
	 */
	public ConnectionConfiguration(@NotNull String uri, @NotNull String user,
	                               @NotNull ConnectionConfiguration configuration) {
		this.uri = uri;
		this.user = user;
		this.password = configuration.password;
	}

	/**
	 * Creates a {@link ConnectionConfiguration} using provided values.
	 *
	 * @param uri      the connection URI to use
	 * @param user     the user to authenticate with
	 * @param password the password to authenticate with or null to create an instance without configured password
	 */
	public ConnectionConfiguration(@NotNull String uri, @NotNull String user, char @Nullable [] password) {
		this.uri = uri;
		this.user = user;
		this.password = password;
	}

	/**
	 * Returns the configured connection URI.
	 */
	public @NotNull String getUri() {
		return uri;
	}

	/**
	 * Returns the configured user.
	 */
	public @NotNull String getUser() {
		return user;
	}

	/**
	 * Returns whether a password is specified in this instance.
	 *
	 * @return true if this configuration includes a password, false otherwise
	 */
	public boolean hasConfiguredPassword() {
		return password != null;
	}

	/**
	 * Creates a {@link DatabaseConnection}-object using this configuration. If this configuration lacks a password, the
	 * connection is attempted with the empty string as password.
	 */
	@NotNull
	DatabaseConnection createConnection() {
		return new DatabaseConnection(uri, user, password != null ? password : new char[0]);
	}

	/**
	 * Saves this configuration to the given preferences.
	 *
	 * @param preferencesWriter  the preferences writer to which to save
	 * @param savePassword if true, saves the password as well, otherwise erases the stored password from the
	 *                     preferences if it is present
	 */
	void save(ConnectionPreferencesWriter preferencesWriter, boolean savePassword) {
		preferencesWriter.setConnectionUri(uri);
		preferencesWriter.setUser(user);
		if (savePassword && password != null) {
			preferencesWriter.setPassword(password);
		} else {
			preferencesWriter.clearPassword();
		}
	}

	@Override
	public String toString() {
		return uri + " (" + user + ")";
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		ConnectionConfiguration that = (ConnectionConfiguration) object;
		return Objects.equals(uri, that.uri) && Objects.equals(user,
				that.user) && Objects.deepEquals(password, that.password);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uri, user);
	}
}
