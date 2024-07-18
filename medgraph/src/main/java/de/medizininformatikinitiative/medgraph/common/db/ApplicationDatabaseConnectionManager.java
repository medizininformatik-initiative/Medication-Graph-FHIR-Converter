package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.DI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service class which holds the application-wide database connection configuration.
 *
 * @author Markus Budeus
 */
public class ApplicationDatabaseConnectionManager implements ConnectionConfigurationService, DatabaseConnectionService {

	// TODO Test

	private final ConnectionPreferences preferences = DI.get(ConnectionPreferences.class);
	@NotNull
	private ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(preferences);

	@Override
	public void setConnectionConfiguration(@NotNull ConnectionConfiguration connectionConfiguration,
	                                       SaveOption saveOption) {
		this.connectionConfiguration = connectionConfiguration;
		if (saveOption != SaveOption.DONT_SAVE) {
			connectionConfiguration.save(preferences, saveOption == SaveOption.SAVE_ALL);
		}
	}

	@Override
	@NotNull
	public ConnectionConfiguration getConnectionConfiguration() {
		return connectionConfiguration;
	}

	@Override
	public @NotNull DatabaseConnection createConnection(boolean test) throws DatabaseConnectionException {
		return DatabaseConnectionUtil.createConnection(connectionConfiguration, test);
	}
}
