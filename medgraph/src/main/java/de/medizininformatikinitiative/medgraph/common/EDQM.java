package de.medizininformatikinitiative.medgraph.common;

/**
 * Provides the EDQM concept classes used in this application.
 *
 * @author Markus Budeus
 */
public enum EDQM {

	PHARMACEUTICAL_DOSE_FORM("PDF", "Pharmaceutical dose form", 8),
	BASIC_DOSE_FORM("BDF", "Basic dose form", 4),
	INTENDED_SITE("ISI", "Intended site", 4),
	RELEASE_CHARACTERISTIC("RCA", "Release characteristic", 4);

	/**
	 * The shorthand for this EDQM concept class.
	 */
	private final String shorthand;
	/**
	 * The full name of this EDQM concept class.
	 */
	private final String typeFullName;

	/**
	 * The length of the identifying code of this class type (excluding the shorthand prefix, i.e. "PDF-00010001" is a
	 * code of length 8).
	 */
	private final int codeLength;

	private EDQM(String shorthand, String typeFullName, int codeLength) {
		this.shorthand = shorthand;
		this.typeFullName = typeFullName;
		this.codeLength = codeLength;
	}

	public String getShorthand() {
		return shorthand;
	}

	public String getTypeFullName() {
		return typeFullName;
	}

	/**
	 * Checks the given code valid for this type. This means the code has the structure
	 * <b>[shorthand]-XXXXXXXX</b> with the "X" characters being digits of the length required for this concept class.
	 * The shorthand-part may be missing, in which case it is prepended automatically.
	 *
	 * @param code the code to validate
	 * @return the code, with the shorthand-part prepended if necessary
	 * @throws IllegalArgumentException if the given code is not valid for this concept class
	 */
	public String validateAndCorrectCode(String code) {
		try {
			String codePrefix = getCodePrefix();
			String digitsPart;
			int length = code.length();
			if (length == codeLength) {
				digitsPart = code;
				code =  codePrefix + code;
			} else if (length == codeLength + codePrefix.length()) {
				if (!code.startsWith(codePrefix)) {
					throw new IllegalArgumentException();
				}
				digitsPart = code.substring(codePrefix.length());
			} else {
				throw new IllegalArgumentException();
			}

			for (int i = 0; i < codeLength; i++) {
				if (!Character.isDigit(digitsPart.charAt(i))) {
					throw new IllegalArgumentException();
				}
			}
			return code;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("The code \""+code+"\" is not a valid "+ this +"-class code! ");
		}
	}

	/**
	 * Returns the prefix to be used for codes of this type.
	 */
	public String getCodePrefix() {
		return getShorthand() + "-";
	}

}
