package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.common.db.ConnectionPreferences;

import java.util.prefs.Preferences;

/**
 * Manages and provides access to different specialized preference managers.
 *
 * @author Markus Budeus
 */
public class ApplicationPreferences {

	public static final String APPLICATION_ROOT_NODE = "de/medizininformatikinitiative";
	private static final String CONNECTION_PREFERENCES_PATH = "db";
	private static volatile ConnectionPreferences connectionPreferences;

	public static Preferences getApplicationRootNode() {
		return Preferences.userRoot().node(APPLICATION_ROOT_NODE);
	}

	static ConnectionPreferences getDatabaseConnectionPreferences() {
		if (connectionPreferences == null) {
			synchronized (ApplicationPreferences.class) {
				if (connectionPreferences == null) {
					connectionPreferences = new ConnectionPreferences(
							getApplicationRootNode().node(CONNECTION_PREFERENCES_PATH));
				}
			}
		}
		return connectionPreferences;
	}

}
