package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

/**
 * @author Markus Budeus
 */
public class GraphEdqmPharmaceuticalDoseForm extends GraphCode {

	public static final String NAME = "name";

	private final String name;

	/**
	 * Like the constructor, but can return null if the given value represents the null value. (I.e. {@link Value#isNull()} is true.)
	 * @param value the value from which to construct this instance
	 * @return a new {@link GraphEdqmPharmaceuticalDoseForm} or null
	 */
	@Nullable
	public static GraphEdqmPharmaceuticalDoseForm from(Value value) {
		if (value.isNull()) return null;
		return new GraphEdqmPharmaceuticalDoseForm(value);
	}

	public GraphEdqmPharmaceuticalDoseForm(MapAccessorWithDefaultValue mapAccessor) {
		super(mapAccessor);
		name = mapAccessor.get(NAME, (String) null);
	}

	public String getName() {
		return name;
	}
}
