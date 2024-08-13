package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization;

/**
 * Represents an Address, which is part of a FHIR Organization object.
 *
 * @author Markus Budeus
 */
public class FhirAddress {

	private String use;
	private String type;
	private String text = "";

	private String[] line;
	private String postalCode;
	private String city;
	private String country;

	public String getUse() {
		return use;
	}
	public void setUse(Use use) {
		this.use = use.toString().toLowerCase();
	}
	public String getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type.toString().toLowerCase();
	}
	public String getText() {
		return text;
	}

	public void setAddress(String[] line, String postalCode, String city, String country) {
		this.line = line;
		this.postalCode = postalCode;
		this.city = city;
		this.country = country;
		reloadText();
	}

	private void reloadText() {
		StringBuilder textBuilder = new StringBuilder();
		if (line != null && line.length != 0) {
			for (String cLine : line) {
				textBuilder.append(cLine);
				textBuilder.append("\n");
			}
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
			if (country != null) {
				textBuilder.append(country);
				textBuilder.append("\n");
			}
		}
		if (!textBuilder.isEmpty()) {
			textBuilder.delete(textBuilder.length() - 1, textBuilder.length());
		}
		text = textBuilder.toString();
	}


	public enum Use {
		HOME,
		WORK,
		TEMP,
		OLD,
		BILLING
	}
	public enum Type {
		POSTAL,
		PHYSICAL,
		BOTH
	}

	public String[] getLine() {
		return line;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}
}
