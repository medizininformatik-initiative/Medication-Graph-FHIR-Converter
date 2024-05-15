package de.medizininformatikinitiative.medgraph.searchengine.db;

import java.math.BigDecimal;

/**
 * @author Markus Budeus
 */
class Tools {

	/**
	 * Converts the given value into a {@link BigDecimal} while using ',' as decimal separator.
	 * If the value is null, null is returned.
	 */
	static BigDecimal fromString(String value) {
		if (value == null) return null;
		return new BigDecimal(value.replace(',', '.'));
	}

}
