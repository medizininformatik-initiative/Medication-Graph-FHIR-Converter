package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Provides utility functions for interpreting the graph database export.
 *
 * @author Markus Budeus
 */
public class GraphUtil {

	private static final DateTimeFormatter FHIR_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	/**
	 * Generates a {@link BigDecimal} from the given string, but replaces commas with dots first.
	 *
	 * @param germanValue the german decimal, which uses commas as decimal separator
	 * @return the generated {@link BigDecimal} or null if null was passed
	 */
	@Contract("null -> null; !null -> !null")
	public static BigDecimal toBigDecimal(String germanValue) {
		if (germanValue == null) return null;
		return new BigDecimal(germanValue.replace(',', '.'));
	}

	/**
	 * Returns a Cypher statement which groups codes into a single value object where the code node is referred to by
	 * the given codeVariableName and the assigned codingSystem node is referred to by the given
	 * codingSystemVariableName. The collection happens in a way that the {@link GraphCode} can read the resulting
	 * value.
	 */
	public static String groupCodingSystem(String codeVariableName, String codingSystemVariableName) {
		return groupCodingSystem(codeVariableName, codingSystemVariableName, null);
	}

	/**
	 * Returns a Cypher statement which groups codes into a single value object where the code node is referred to by
	 * the given codeVariableName and the assigned codingSystem node is referred to by the given
	 * codingSystemVariableName. The collection happens in a way that the {@link GraphCode} can read the resulting
	 * value. Additionally, you can add more properties to the resulting object using the given "extra" parameter.
	 */
	public static String groupCodingSystem(String codeVariableName, String codingSystemVariableName, String extra) {
		return "CASE WHEN NOT " + codeVariableName + " IS NULL THEN {" + (extra != null ? extra + "," : "") +
				GraphCode.CODE + ":" + codeVariableName + ".code," +
				GraphCode.SYSTEM_URI + ":" + codingSystemVariableName + ".uri," +
				GraphCode.SYSTEM_DATE + ":" + codingSystemVariableName + ".date," +
				GraphCode.SYSTEM_VERSION + ":" + codingSystemVariableName + ".version" +
				"} ELSE NULL END";
	}

	/**
	 * Returns the given date as string in FHIR conformant date style. If you pass null, this function returns null.
	 */
	public static String toFhirDate(LocalDate date) {
		if (date == null) return null;
		return FHIR_DATE_FORMATTER.format(date);
	}

	/**
	 * Converts the given input masses and unit to a FHIR {@link Ratio}, always using 1 as denominator. If you pass only
	 * nulls, the returned ratio is null. Otherwise, massFrom must be non-null.
	 *
	 * @param massFrom the mass to use as value for the returned ratio
	 * @param massTo   if only a range for the quantity is known, this is the upper limit of the range
	 * @param unit     the unit of the quantity, may be null
	 * @return a {@link Ratio} representing the given inputs or null if all inputs are null
	 * @throws IllegalArgumentException if massFrom is null but any of the other arguments is non-null or if massTo is
	 *                                  less than massFrom
	 */
	@Nullable
	@Contract("null, null, null -> null; !null, _, _, -> !null; null, !null, _ -> fail; null, null, !null -> fail")
	public static Ratio toFhirRatio(
			@Nullable BigDecimal massFrom, @Nullable BigDecimal massTo,
			@Nullable GraphUnit unit) {
		Quantity quantity = toFhirQuantity(massFrom, massTo, unit);
		if (quantity == null) return null;
		Ratio ratio = new Ratio();
		ratio.setNumerator(quantity);
		ratio.setDenominator(new Quantity(1));
		return ratio;
	}

	/**
	 * Converts the given input masses and unit to a FHIR {@link Quantity}. If you pass only nulls, the returned
	 * quantity is null. Otherwise, massFrom must be non-null.
	 *
	 * @param massFrom the mass to use as value for the returned quantity
	 * @param massTo   if only a range for the quantity is known, this is the upper limit of the range
	 * @param unit     the unit of the quantity, may be null
	 * @return a {@link Quantity} representing the given inputs or null if all inputs are null
	 * @throws IllegalArgumentException if massFrom is null but any of the other arguments is non-null or if massTo is
	 *                                  less than massFrom
	 */
	@Nullable
	@Contract("null, null, null -> null; !null, _, _, -> !null; null, !null, _ -> fail; null, null, !null -> fail")
	public static Quantity toFhirQuantity(@Nullable BigDecimal massFrom, @Nullable BigDecimal massTo,
	                                            @Nullable GraphUnit unit) {
		if (massFrom == null) {
			if (massTo != null || unit != null)
				throw new IllegalArgumentException("If massTo or unit is not null, massFrom must also be non-null!");
			return null;
		}

		Quantity quantity = new Quantity();
		quantity.setValue(massFrom);
		if (massTo != null) {
			int relative = massFrom.compareTo(massTo);
			if (relative < 0) {
				quantity.setComparator(Quantity.QuantityComparator.GREATER_OR_EQUAL);
			} else if (relative > 0) {
				throw new IllegalArgumentException(
						"MassFrom (" + massFrom + ") is greater than massTo (" + massTo + ")!");
			}
		}

		if (unit != null) {
			quantity.setUnit(unit.print());
			if (unit.ucumCs() != null) {
				quantity.setCode(unit.ucumCs());
				quantity.setSystem("http://unitsofmeasure.org");
			}
		}
		return quantity;
	}

	/**
	 * Returns a new {@link CodeableConcept} which captures all the given codes. If the list of codes is null, the
	 * returned object is null. If the list contains exactly one element, that element's corresponding
	 * {@link Coding#display} is used as text.
	 */
	public static CodeableConcept toCodeableConcept(List<? extends GraphCode> codes) {
		if (codes == null) return null;
		CodeableConcept concept = new CodeableConcept();

		for (GraphCode code : codes) {
			concept.addCoding(code.toCoding());
		}

		if (concept.getCoding().size() == 1) {
			concept.setText(concept.getCoding().getFirst().getDisplay());
		}

		return concept;
	}

}
