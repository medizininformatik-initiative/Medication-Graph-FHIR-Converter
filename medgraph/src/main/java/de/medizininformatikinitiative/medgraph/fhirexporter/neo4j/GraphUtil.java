package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.Contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

}
