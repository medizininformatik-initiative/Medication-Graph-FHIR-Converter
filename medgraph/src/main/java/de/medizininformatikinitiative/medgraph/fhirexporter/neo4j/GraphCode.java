package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.hl7.fhir.r4.model.Coding;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.time.LocalDate;
import java.util.Objects;

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

	public GraphCode(String code, String systemUri, LocalDate systemDate, String systemVersion) {
		this.code = code;
		this.systemUri = systemUri;
		this.systemDate = systemDate;
		this.systemVersion = systemVersion;
	}

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

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		GraphCode graphCode = (GraphCode) object;
		return Objects.equals(code, graphCode.code) && Objects.equals(systemUri,
				graphCode.systemUri) && Objects.equals(systemDate,
				graphCode.systemDate) && Objects.equals(systemVersion, graphCode.systemVersion);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, systemUri, systemDate, systemVersion);
	}

	@Override
	public String toString() {
		return "GraphCode{" +
				"code='" + code + '\'' +
				", systemUri='" + systemUri + '\'' +
				", systemDate=" + systemDate +
				", systemVersion='" + systemVersion + '\'' +
				'}';
	}

	public Coding toCoding() {
		Coding coding = new Coding();
		coding.setSystem(systemUri);
		if (systemVersion == null) {
			coding.setVersion(GraphUtil.toFhirDate(systemDate));
		} else {
			coding.setVersion(systemVersion);
		}
		coding.setCode(code);
		return coding;
	}
}
