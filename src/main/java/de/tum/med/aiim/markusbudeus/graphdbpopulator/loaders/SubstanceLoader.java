package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

public class SubstanceLoader extends Loader {

	public SubstanceLoader(Path directory, Session session)
	throws IOException {
		super(directory, "MOLECULE.CSV", session);
	}

	@Override
	protected void executeLoad() {

	}

}
