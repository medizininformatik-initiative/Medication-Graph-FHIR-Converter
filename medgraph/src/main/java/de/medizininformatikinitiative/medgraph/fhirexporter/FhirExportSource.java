package de.medizininformatikinitiative.medgraph.fhirexporter;

import org.hl7.fhir.r4.model.DomainResource;

import java.util.stream.Stream;

/**
 * Represents the whole pipeline of an object export from a source system and conversion to FHIR instances.
 *
 * @author Markus Budeus
 */
public interface FhirExportSource<T extends DomainResource> {

	/**
	 * Runs the export.
	 *
	 * @return a {@link Stream} providing the exported objects one after another.
	 */
	Stream<T> export();

}
