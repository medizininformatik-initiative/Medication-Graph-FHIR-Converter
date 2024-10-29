package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.FhirAddress;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

/**
 * @author Markus Budeus
 */
public record GraphAddress(String street, String streetNumber, String postalCode, String city, String country,
                           String countryCode) {

	public static final String STREET = "street";
	public static final String STREET_NUMBER = "streetNumber";
	public static final String POSTAL_CODE = "postalCode";
	public static final String CITY = "city";
	public static final String COUNTRY = "country";
	public static final String COUNTRY_CODE = "countryCode";

	public GraphAddress(MapAccessorWithDefaultValue value) {
		this(value.get(STREET, (String) null),
				value.get(STREET_NUMBER, (String) null),
				value.get(POSTAL_CODE, (String) null),
				value.get(CITY, (String) null),
				value.get(COUNTRY, (String) null),
				value.get(COUNTRY_CODE, (String) null));
	}

	public FhirAddress toFhirAddress() {
		FhirAddress fhirAddress = new FhirAddress();
		fhirAddress.setUse(FhirAddress.Use.WORK);

		String line = null;
		if (street != null) {
			if (streetNumber != null) {
				line = street + " " + streetNumber;
			} else {
				line = street;
			}
		}

		String country = this.country;
		if (country == null) country = this.countryCode;

		String[] effectiveLine = line != null ? new String[] { line } : null;
		fhirAddress.setAddress(effectiveLine, postalCode, city, country);
		return fhirAddress;
	}

}
