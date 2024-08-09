package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.util.Objects;

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

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		GraphEdqmPharmaceuticalDoseForm that = (GraphEdqmPharmaceuticalDoseForm) object;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name);
	}
}
