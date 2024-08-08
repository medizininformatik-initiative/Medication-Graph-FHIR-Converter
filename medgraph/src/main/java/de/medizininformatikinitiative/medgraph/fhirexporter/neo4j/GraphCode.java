package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.time.LocalDate;

/**
 * @author Markus Budeus
 */
public class GraphCode {

	public static final String CODE = "code";
	public static final String SYSTEM_URI = "uri";
	public static final String SYSTEM_DATE = "date";
	public static final String SYSTEM_VERSION = "version";

	private final String code;
	private final String systemUri;
	private final LocalDate systemDate;
	private final String systemVersion;

	public GraphCode(MapAccessorWithDefaultValue mapAccessor) {
		this.code = mapAccessor.get(CODE).asString();
		this.systemUri = mapAccessor.get(SYSTEM_URI).asString(null);
		this.systemDate = (LocalDate) mapAccessor.get(SYSTEM_DATE, (LocalDate) null);
		this.systemVersion = mapAccessor.get(SYSTEM_VERSION).asString(null);
	}

	public String getCode() {
		return code;
	}

	public String getSystemUri() {
		return systemUri;
	}

	public LocalDate getSystemDate() {
		return systemDate;
	}

	public String getSystemVersion() {
		return systemVersion;
	}
}
