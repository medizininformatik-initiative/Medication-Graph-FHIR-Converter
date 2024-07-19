package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;

/**
 * @author Markus Budeus
 */
public interface ConnectionPreferencesWriter {

	/**
	 * Sets the connection uri which is to be stored in the preferences.
	 *
	 * @param connectionUri the connection uri to store
	 */
	void setConnectionUri(@NotNull String connectionUri);

	/**
	 * Sets the username which is to be stored in the preferences.
	 *
	 * @param user the username to store
	 */
	void setUser(@NotNull String user);

	/**
	 * Sets the password which is to be stored in the preferences.
	 *
	 * @param password the password to stroe
	 */
	void setPassword(char @NotNull [] password);

	/**
	 * Clears a stored password if there is one.
	 */
	void clearPassword();

}
