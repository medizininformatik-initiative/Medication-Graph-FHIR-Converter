package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance;

/**
 * Generates the file names used for the FHIR export.
 */
public class ExportFilenameGenerator {

	private static final char[] ILLEGAL_CHARACTERS = new char[] { '<', '>', ':', '"', '/', '\\', '|', '?' };

	public String constructFilename(Organization organization) {
		String name;
		if (organization.alias != null && organization.alias.length > 0) {
			name = organization.alias[0];
		} else name = organization.name;
		return combineToFilename(organization.identifier[0].value, name);
	}

	public String constructFilename(Substance substance) {
		return combineToFilename(substance.identifier[0].value, substance.code.text);
	}

	public String constructFilename(Medication medication) {
		String text = "unnamed";
		if (medication.code != null) text = medication.code.text;
		return combineToFilename(medication.identifier[0].value, text);
	}

	/**
	 * Combines the given parts and replaces characters not allowed in file names.
	 */
	private static String combineToFilename(String... parts) {
		return replaceIllegalCharacters(combine(parts));
	}

	/**
	 * Concatenates the given parts with spaces in between.
	 */
	private static String combine(String... parts) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : parts) {
			if (s != null) {
				if (first)
					first = false;
				else
					sb.append(" ");
				sb.append(s);
			}
		}
		return sb.toString();
	}

	private static String replaceIllegalCharacters(String name) {
		for (char illegal: ILLEGAL_CHARACTERS) {
			name = name.replace(illegal, '-');
		}
		return name;
	}

}
