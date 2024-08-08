package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.neo4j.driver.Record;

import java.util.List;

/**
 * @author Markus Budeus
 */
public record GraphSubstance(long mmiId, String name, List<GraphCode> codes) {

	public GraphSubstance(Record record) {
		this(
				record.get(0).asLong(),
				record.get(1).asString(null),
				record.get(2).asList(GraphCode::new)
		);
	}

}
