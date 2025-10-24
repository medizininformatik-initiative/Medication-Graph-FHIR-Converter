package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportSource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Meta;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Takes objects from a {@link Neo4jExporter}, converts them to fhir resources and sets the {@link Meta#source} value.
 *
 * @author Markus Budeus
 */
public class GraphFhirExportSource<S, T extends DomainResource> implements FhirExportSource<T> {

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

	private void addSource(DomainResource resource) {
		resource.getMeta().setSource(META_SOURCE);
	}
}
