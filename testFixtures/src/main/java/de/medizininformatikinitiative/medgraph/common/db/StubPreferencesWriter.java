package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;

/**
 * @author Markus Budeus
 */
public class StubPreferencesWriter implements ConnectionPreferencesWriter {
	@Override
	public void setConnectionUri(@NotNull String connectionUri) {

	}

	@Override
	public void setUser(@NotNull String user) {

	}

	@Override
	public void setPassword(char @NotNull [] password) {

	}

	@Override
	public void clearPassword() {

	}
}
