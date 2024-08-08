package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.neo4j.driver.types.MapAccessorWithDefaultValue;

/**
 * @author Markus Budeus
 */
public class GraphAtc extends GraphCode {

	public static final String DESCRIPTION = "description";

	private final String description;

	public GraphAtc(MapAccessorWithDefaultValue mapAccessor) {
		super(mapAccessor);
		this.description = mapAccessor.get(DESCRIPTION, (String) null);
	}

	public String getDescription() {
		return description;
	}
}
