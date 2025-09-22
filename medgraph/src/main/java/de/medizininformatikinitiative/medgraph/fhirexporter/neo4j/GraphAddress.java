package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.FhirAddress;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.StringType;
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

	@Deprecated
	public FhirAddress toLegacyFhirAddress() {
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

		String[] effectiveLine = line != null ? new String[]{line} : null;
		fhirAddress.setAddress(effectiveLine, postalCode, city, country);
		return fhirAddress;
	}

	public Address toFhirAddress() {
		Address address = new Address();
		address.setUse(Address.AddressUse.WORK);

		if (street != null) {
			if (streetNumber != null) {
				address.addLine(street + " " + streetNumber);
			} else {
				address.addLine(street);
			}
		}

		address.setPostalCode(this.postalCode);
		address.setCity(this.city);
		if (country == null) {
			address.setCountry(countryCode);
		} else {
			address.setCountry(country);
		}

		inferText(address);

		return address;
	}

	private void inferText(Address address) {
		StringBuilder textBuilder = new StringBuilder();
		for (StringType cLine : address.getLine()) {
			textBuilder.append(cLine.getValue());
			textBuilder.append("\n");
		}
		String postalCode = address.getPostalCode();
		String city = address.getCity();
		if (postalCode != null) {
			textBuilder.append(postalCode);
			if (city != null) {
				textBuilder.append(" ");
			} else {
				textBuilder.append("\n");
			}
		}
		if (city != null) {
			textBuilder.append(city);
			textBuilder.append("\n");
		}
		String country = address.getCountry();
		if (country != null) {
			textBuilder.append(country);
			textBuilder.append("\n");
		}

		if (!textBuilder.isEmpty()) {
			textBuilder.delete(textBuilder.length() - 1, textBuilder.length());
		}
		String text = textBuilder.toString();
		if (text.isEmpty()) text = null;
		address.setText(text);
	}

}
