package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.common.mvc.NamedProgressableImpl;

import java.io.IOException;

/**
 * Base class for different FHIR export sinks. E.g. one sink might write resources to files, another one might write to
 * a FHIR server.
 *
 * @author Markus Budeus
 */
public abstract class FhirExportSink extends NamedProgressableImpl {

	/**
	 * Exports FHIR resources using the given sources and writes them to the sink defined by the underlying
	 * implementation.
	 *
	 * @param sources The sources carrier from which to acquire the FHIR resources providers.
	 */
	public abstract void doExport(FhirExportSources sources) throws IOException;

}
