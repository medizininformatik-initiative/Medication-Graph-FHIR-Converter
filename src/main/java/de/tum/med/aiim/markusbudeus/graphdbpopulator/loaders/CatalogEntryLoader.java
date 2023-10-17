package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

abstract class CatalogEntryLoader extends Loader {

	public CatalogEntryLoader(Session session) throws IOException {
		super("CATALOGENTRY.CSV", session);
	}
}
