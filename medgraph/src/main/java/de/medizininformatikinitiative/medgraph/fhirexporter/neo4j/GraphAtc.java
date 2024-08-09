package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.util.Objects;

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

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		GraphAtc graphAtc = (GraphAtc) object;
		return Objects.equals(description, graphAtc.description);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(description);
	}
}
