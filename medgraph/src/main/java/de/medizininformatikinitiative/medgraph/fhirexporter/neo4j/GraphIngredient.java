package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Extension;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.ExtensionWirkstoffRelation;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.ExtensionWirkstoffTyp;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Ingredient;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author Markus Budeus
 */
public class GraphIngredient extends SimpleGraphIngredient {

	private final boolean isActive;
	private final SimpleGraphIngredient correspondingIngredient;

	public static final String IS_ACTIVE = "isActive";
	public static final String CORRESPONDING_INGREDIENT = "ci";

	public GraphIngredient(MapAccessorWithDefaultValue value) {
		super(value);
		this.isActive = value.get(IS_ACTIVE).asBoolean();

		Value ci = value.get(CORRESPONDING_INGREDIENT);
		if (ci.isNull()) {
			this.correspondingIngredient = null;
		} else {
			this.correspondingIngredient = new SimpleGraphIngredient(ci);
		}
	}

	public GraphIngredient(long substanceMmiId, String substanceName, boolean isActive, BigDecimal massFrom,
	                       BigDecimal massTo, GraphUnit unit, SimpleGraphIngredient correspondingIngredient) {
		super(substanceMmiId, substanceName, massFrom, massTo, unit);
		this.isActive = isActive;
		this.correspondingIngredient = correspondingIngredient;
	}

	public Ingredient toFhirIngredient() {
		Ingredient ingredient = toBasicFhirIngredient();
		ingredient.isActive = isActive;
		return ingredient;
	}

	/**
	 * Returns a list with one or two ingredients. The first ingredient is always representing this instance directly.
	 * The second ingredient is only present if a corresponding ingredient exists for this ingredient. In that case, the
	 * second ingredient represents the corresponding ingredient. The extensions wirkstofftyp and wirkstoffrelation are
	 * only set when using this function and if a second ingredient exists. All ingredients are also assigned an id as
	 * follows: "#ing_id" where "id" is the idNumber, counting up. (E.g. "#ing_1")
	 *
	 * @param idNumber the first number to assign as ingredient number for the id
	 */
	public List<Ingredient> toFhirIngredientsWithCorrespoindingIngredient(int idNumber) {
		Ingredient self = toFhirIngredient();
		self.id = "#ing_" + idNumber;

		if (correspondingIngredient == null) {
			return List.of(self);
		}

		self.extension = new Extension[]{ExtensionWirkstoffTyp.preciseIngredient()};

		Ingredient generalizedIngredient = correspondingIngredient.toBasicFhirIngredient();
		generalizedIngredient.isActive = isActive;
		generalizedIngredient.id = "#ing_" + (idNumber + 1);
		generalizedIngredient.extension = new Extension[]{
				ExtensionWirkstoffTyp.ingredient(),
				ExtensionWirkstoffRelation.relatesTo(self.id)
		};

		return List.of(self, generalizedIngredient);
	}

	public boolean isActive() {
		return isActive;
	}

	public SimpleGraphIngredient getCorrespondingIngredient() {
		return correspondingIngredient;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		GraphIngredient that = (GraphIngredient) object;
		return isActive == that.isActive && Objects.equals(correspondingIngredient, that.correspondingIngredient);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), isActive, correspondingIngredient);
	}
}
