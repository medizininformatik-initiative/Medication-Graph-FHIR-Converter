package de.medizininformatikinitiative.medgraph.rxnorm_matching;

import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Unit;

/**
 * @author Markus Budeus
 */
public class UnitNormalizer {

	private static final Logger logger = LogManager.getLogger(UnitNormalizer.class);

	public static Unit normalizeFromRawRxNorm(String rxNormUnit) {
		String[] parts = rxNormUnit.split("/");

		String numerator = parts[0].trim();
		String denominator = null;
		if (parts.length == 2) {
			denominator = parts[1].trim();
		}
		return new Unit(normalizeAbsoluteUnitFromRxNorm(numerator), normalizeAbsoluteUnitFromRxNorm(denominator));
	}

	public static String normalizeAbsoluteUnitFromRxNorm(String rxNormAbsolute) {
		if (rxNormAbsolute == null) return null;
		switch (rxNormAbsolute) {
			case "MG" -> {
				return "mg";
			}
			case "G" -> {
				return "g";
			}
			case "MCG" -> {
				return "ug";
			}
			case "KG" -> {
				return "kg";
			}
			case "ML" -> {
				return "ml";
			}
			case "mL" -> {
				return "ml";
			}
			case "L" -> {
				return "l";
			}
			case "UNT" -> {
				return "UNT";
			}
			case "MEQ" -> {
				return "meq";
			}
			case "DAY" -> {
				return "d";
			}
			case "ACTUAT" -> {
				// E.g. one use of a spray pump. I'll map it to unitless.
				return null;
			}
			default -> {
				logger.log(Level.WARN, "Unknown RxNorm unit: " + rxNormAbsolute);
				return rxNormAbsolute;
			}
		}
	}

}
