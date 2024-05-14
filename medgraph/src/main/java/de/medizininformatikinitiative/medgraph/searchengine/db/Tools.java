package de.medizininformatikinitiative.medgraph.searchengine.db;

import java.math.BigDecimal;

/**
 * @author Markus Budeus
 */
class Tools {

	static BigDecimal fromString(String value) {
		if (value == null) return null;
		return new BigDecimal(value.replace(',', '.'));
	}

}
