package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.FhirResource;

import java.util.stream.Stream;

/**
 * Represents the whole pipeline of an object export from a source system and conversion to FHIR instances.
 *
 * @author Markus Budeus
 */
public interface FhirExportSource<T extends FhirResource> {

	/**
	 * Runs the export.
	 *
	 * @return a {@link Stream} providing the exported objects one after another.
	 */
	Stream<T> export();

}
