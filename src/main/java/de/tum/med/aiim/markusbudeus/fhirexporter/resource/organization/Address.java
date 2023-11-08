package de.tum.med.aiim.markusbudeus.fhirexporter.resource.organization;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Code;

public class Address {


	private Code use;
	private Code type;
	private String text = "";

	private String[] line;
	private String postalCode;
	private String city;
	private String country;

	public Code getUse() {
		return use;
	}
	public void setUse(Use use) {
		this.use = new Code(use.toString().toLowerCase());
	}
	public Code getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = new Code(type.toString().toLowerCase());
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
			textBuilder.append(country);
			textBuilder.append("\n");
		}
		if (!textBuilder.isEmpty()) {
			textBuilder.delete(textBuilder.length() - 2, textBuilder.length());
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

}
