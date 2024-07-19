package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.DI;
import org.jetbrains.annotations.NotNull;

/**
 * Service class which holds the application-wide database connection configuration.
 *
 * @author Markus Budeus
 */
public class ApplicationDatabaseConnectionManager implements ConnectionConfigurationService, DatabaseConnectionService {

	@NotNull
	private final ConnectionTestService connectionTestService = DI.get(ConnectionTestService.class);
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
		return connectionTestService.createConnection(connectionConfiguration, test);
	}

}
