package de.medizininformatikinitiative.medgraph.common.db;

/**
 * @author Markus Budeus
 */
public interface ConnectionConfigurationService {

	enum SaveOption {
		/**
		 * Saves the whole connection config, including the password.
		 */
		SAVE_ALL,
		/**
		 * Saves the connection config without the password. Please note this setting clears any previously saved
		 * password.
		 */
		EXCLUDE_PASSWORD,
		/**
		 * Don't save the configuration at all. This doesn't overwrite any previously stored configuration.
		 */
		DONT_SAVE
	}

	/**
	 * Sets the application's connection configuration. If saving is enabled, the configuration is saved to persistent
	 * storage and will be applied when the application starts the next time. Otherwise, the configuration is only
	 * applied for the lifetime of this process (or until the next overwrite).
	 *
	 * @param connectionConfiguration the connection configuration to apply
	 * @param saveOption              whether to save the given configuration to persistent storage
	 */
	void setConnectionConfiguration(ConnectionConfiguration connectionConfiguration, SaveOption saveOption);

	/**
	 * Returns the currently configured application connection config.
	 */
	ConnectionConfiguration getConnectionConfiguration();

}
