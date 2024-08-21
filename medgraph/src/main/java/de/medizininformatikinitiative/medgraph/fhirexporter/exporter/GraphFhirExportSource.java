package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportSource;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.FhirResource;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Meta;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Takes objects from a {@link Neo4jExporter}, converts them to fhir resources and sets the {@link Meta#source} value.
 *
 * @author Markus Budeus
 */
public class GraphFhirExportSource<S, T extends FhirResource> implements FhirExportSource<T> {

	public static final String META_SOURCE = "https://www.mmi.de/mmi-pharmindex/mmi-pharmindex-daten";
	private final Neo4jExporter<S> exporter;
	private final Function<Stream<S>, Stream<T>> streamConversionFunction;

	public GraphFhirExportSource(Neo4jExporter<S> exporter, Function<Stream<S>, Stream<T>> streamConversionFunction) {
		this.exporter = exporter;
		this.streamConversionFunction = streamConversionFunction;
	}

	@Override
	public Stream<T> export() {
		return streamConversionFunction.apply(exporter.exportObjects())
		                               .peek(this::addSource);
	}

	private void addSource(FhirResource resource) {
		if (resource.meta == null)
			resource.meta = new Meta();
		resource.meta.source = META_SOURCE;
	}
}
