package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.util.List;

/**
 * @author Markus Budeus
 */
public record GraphOrganization(long mmiId, String name, String shortName, List<GraphAddress> addresses) {

	public static final String MMI_ID = "mmiId";
	public static final String NAME = "name";
	public static final String SHORT_NAME = "shortName";
	public static final String ADDRESSES = "addresses";

	public GraphOrganization(MapAccessorWithDefaultValue value) {
		this(
				value.get(MMI_ID).asLong(),
				value.get(NAME, (String) null),
				value.get(SHORT_NAME, (String) null),
				value.get(ADDRESSES).asList(GraphAddress::new)
		);
	}

}