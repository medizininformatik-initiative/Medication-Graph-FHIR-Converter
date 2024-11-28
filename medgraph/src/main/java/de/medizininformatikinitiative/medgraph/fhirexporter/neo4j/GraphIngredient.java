package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Extension;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.ExtensionWirkstoffRelation;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.ExtensionWirkstoffTyp;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient;
import de.medizininformatikinitiative.medgraph.searchengine.tools.Util;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Markus Budeus
 */
public class GraphIngredient extends SimpleGraphIngredient {

	private final boolean isActive;
	@NotNull
	private final List<SimpleGraphIngredient> correspondingIngredients;

	public static final String IS_ACTIVE = "isActive";
	public static final String CORRESPONDING_INGREDIENTS = "ci";

	public GraphIngredient(MapAccessorWithDefaultValue value) {
		super(value);
		this.isActive = value.get(IS_ACTIVE).asBoolean();

		Value ci = value.get(CORRESPONDING_INGREDIENTS);
		if (ci.isNull()) {
			this.correspondingIngredients = new ArrayList<>();
		} else {
			this.correspondingIngredients = ci.asList(SimpleGraphIngredient::new);
		}
	}

	public GraphIngredient(long substanceMmiId, String substanceName, boolean isActive, BigDecimal massFrom,
	                       BigDecimal massTo, GraphUnit unit) {
		this(substanceMmiId, substanceName, isActive, massFrom, massTo, unit, new ArrayList<>());
	}

	public GraphIngredient(long substanceMmiId, String substanceName, boolean isActive, BigDecimal massFrom,
	                       BigDecimal massTo, GraphUnit unit, @NotNull List<SimpleGraphIngredient> correspondingIngredients) {
		super(substanceMmiId, substanceName, massFrom, massTo, unit);
		this.isActive = isActive;
		this.correspondingIngredients = correspondingIngredients;
	}

	public Ingredient toFhirIngredient() {
		Ingredient ingredient = toBasicFhirIngredient();
		ingredient.isActive = isActive;
		return ingredient;
	}

	/**
	 * Returns a list with one or two ingredients. The first ingredient is always representing this instance directly.
	 * The other ingredients are only present if corresponding ingredients exists for this ingredient. In that case, the
	 * additional ingredient represent the corresponding ingredients. The extensions wirkstofftyp and wirkstoffrelation
	 * are only set when using this function and if corresponding ingredients exist. All ingredients are also assigned
	 * an id as follows: "#ing_id" where "id" is the idNumber, counting up. (E.g. "#ing_1")
	 *
	 * @param idNumber the first number to assign as ingredient number for the id
	 */
	public List<Ingredient> toFhirIngredientsWithCorrespoindingIngredient(int idNumber) {
		Ingredient self = toFhirIngredient();
		self.id = "#ing_" + idNumber;

		if (correspondingIngredients.isEmpty()) {
			return List.of(self);
		}

		self.extension = new Extension[]{ExtensionWirkstoffTyp.preciseIngredient()};

		List<Ingredient> resultList = new ArrayList<>(correspondingIngredients.size() + 1);
		resultList.add(self);

		for (SimpleGraphIngredient generalizedGraphIngredient : correspondingIngredients) {
			idNumber++;
			Ingredient generalizedIngredient = generalizedGraphIngredient.toBasicFhirIngredient();
			generalizedIngredient.isActive = null;
			generalizedIngredient.id = "#ing_" + idNumber;
			generalizedIngredient.extension = new Extension[]{
					ExtensionWirkstoffTyp.ingredient(),
					ExtensionWirkstoffRelation.relatesTo(self.id)
			};
			resultList.add(generalizedIngredient);
		}

		return resultList;
	}

	public boolean isActive() {
		return isActive;
	}

	@NotNull
	public List<SimpleGraphIngredient> getCorrespondingIngredients() {
		return correspondingIngredients;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		GraphIngredient that = (GraphIngredient) object;
		return isActive == that.isActive && Util.equalsIgnoreOrder(correspondingIngredients,
				that.correspondingIngredients);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), isActive, correspondingIngredients);
	}

	@Override
	public String toString() {
		return "GraphIngredient{" +
				"unit=" + unit +
				", massTo=" + massTo +
				", massFrom=" + massFrom +
				", substanceName='" + substanceName + '\'' +
				", substanceMmiId=" + substanceMmiId +
				", correspondingIngredients=" + correspondingIngredients +
				", isActive=" + isActive +
				'}';
	}
}
