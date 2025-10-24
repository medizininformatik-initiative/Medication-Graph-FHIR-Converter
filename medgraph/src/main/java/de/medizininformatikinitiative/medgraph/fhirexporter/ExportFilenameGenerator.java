package de.medizininformatikinitiative.medgraph.fhirexporter;

import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Substance;

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
		if (organization.hasAlias()) {
			name = organization.getAlias().getFirst().toString();
		} else name = organization.getName();
		return combineToFilename(getMmiId(organization.getId()), name);
	}

	public String constructFilename(Substance substance) {
		return combineToFilename(getMmiId(substance.getId()), substance.getCode().getText());
	}

	public String constructFilename(Medication medication) {
		String text = "unnamed";
		if (medication.hasCode()) text = medication.getCode().getText();
		return combineToFilename(getMmiId(medication.getId()), text);
	}

	public String getMmiId(String id) {
		int index = id.lastIndexOf('-');
		if (index == -1 || index == id.length() - 1) return id;
		return id.substring(index + 1);
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
		for (char c : name.toCharArray()) {
			newFilename.append(replaceFilenameCharIfRequired(c));
			if (newFilename.length() >= MAX_FILENAME_LENGTH) break;
		}
		return newFilename.toString();
	}

	/**
	 * If the given character is not supposed to be in a file name, a replacement string is returned. Otherwise, the
	 * character itself is returned as string.
	 */
	private static String replaceFilenameCharIfRequired(char c) {
		switch (c) {
			case 'ä':
				return "ae";
			case 'ö':
				return "oe";
			case 'ü':
				return "ue";
			case 'Ä':
				return "Ae";
			case 'Ö':
				return "Oe";
			case 'Ü':
				return "Ue";
			default:
				if (isAllowedFilenameChar(c)) return "" + c;
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
