package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;

/**
 * Service class which holds the application-wide database connection configuration.
 *
 * @author Markus Budeus
 */
public class ApplicationDatabaseConnectionManager implements ConnectionConfigurationService, DatabaseConnectionService {

	// TODO Test

	@NotNull
	private final ConnectionPreferencesWriter preferencesWriter;
	@NotNull
	private ConnectionConfiguration connectionConfiguration;

	public ApplicationDatabaseConnectionManager(@NotNull ConnectionPreferences preferences) {
		this(preferences, new ConnectionConfiguration(preferences));
	}

	public ApplicationDatabaseConnectionManager(@NotNull ConnectionPreferencesWriter preferencesWriter,
	                                            @NotNull ConnectionConfiguration configuration) {
		this.preferencesWriter = preferencesWriter;
		this.connectionConfiguration = configuration;
	}

	@Override
	public void setConnectionConfiguration(@NotNull ConnectionConfiguration connectionConfiguration,
	                                       SaveOption saveOption) {
		this.connectionConfiguration = connectionConfiguration;
		if (saveOption != SaveOption.DONT_SAVE) {
			connectionConfiguration.save(preferencesWriter, saveOption == SaveOption.SAVE_ALL);
		}
	}

	@Override
	@NotNull
	public ConnectionConfiguration getConnectionConfiguration() {
		return connectionConfiguration;
	}

	@Override
	public @NotNull DatabaseConnection createConnection(boolean test) throws DatabaseConnectionException {
		return createConnection(connectionConfiguration, test);
	}

	@Override
	public @NotNull DatabaseConnection createConnection(ConnectionConfiguration connection, boolean test)
	throws DatabaseConnectionException {
		return DatabaseConnectionUtil.createConnection(connectionConfiguration, test);
	}
}
