package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Markus Budeus
 */
public record GraphPackage(String name, BigDecimal amount, LocalDate onMarketDate, List<GraphCode> codes) {

	public static final String NAME = "name";
	public static final String AMOUNT = "amount";
	public static final String ON_MARKET_DATE = "onMarketDate";
	public static final String CODES = "codes";

	public GraphPackage(MapAccessorWithDefaultValue value) {
		this(
				value.get(NAME).asString(null),
				GraphUtil.toBigDecimal(value.get(AMOUNT).asString(null)),
				value.get(ON_MARKET_DATE).asLocalDate(null),
				value.get(CODES).asList(GraphCode::new)
		);
	}

}
