package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance;

/**
 * Generates the file names used for the FHIR export.
 */
public class ExportFilenameGenerator {

	/**
	 * All ASCII characters which are banned from filenames. Nonascii-characters are all disallowed.
	 */
	private static final char[] ILLEGAL_CHARACTERS = new char[]{'<', '>', ':', '"', '/', '\\', '|', '?', '{', '}', '~'};
	private static final String ILLEGAL_CHAR_REPLACEMENT = "-";
	private static final char MAX_FILENAME_LENGTH = 50;

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
		return replaceIllegalCharactersAndLimitFilename(combine(parts));
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

	private static String replaceIllegalCharactersAndLimitFilename(String name) {
		StringBuilder newFilename = new StringBuilder();
		boolean lastReplaced = false;
		for (char c : name.toCharArray()) {
			newFilename.append(replaceFilenameCharIfRequired(c));
			if (newFilename.length() >= MAX_FILENAME_LENGTH) break;
		}
		return newFilename.toString();
	}

	/**
	 * If the given character is not supposed to be in a file name, a replacement string is returned. Otherwise,
	 * the character itself is returned as string.
	 */
	private static String replaceFilenameCharIfRequired(char c) {
		switch (c) {
			case 'ä': return "ae";
			case 'ö': return "oe";
			case 'ü': return "ue";
			case 'Ä': return "Ae";
			case 'Ö': return "Oe";
			case 'Ü': return "Ue";
			default: if (isAllowedFilenameChar(c)) return "" + c;
		}
		return ILLEGAL_CHAR_REPLACEMENT;
	}

	/**
	 * Returns whether the given character is allowed in file names.
	 */
	private static boolean isAllowedFilenameChar(char c) {
		if (c > 126) return false; // Nonascii is not allowed
		if (c < 32) return false; // Invisible characters (except space) and control characters are not allowed
		for (char illegal : ILLEGAL_CHARACTERS) {
			if (c == illegal) return false;
		}
		return true;
	}

}
