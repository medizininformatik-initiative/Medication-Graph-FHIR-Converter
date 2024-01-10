package de.tum.med.aiim.markusbudeus.matcher.tools;

import de.tum.med.aiim.markusbudeus.matcher.model.Amount;
import de.tum.med.aiim.markusbudeus.matcher.model.Dosage;

import java.math.BigDecimal;
import java.util.*;

public class DosageDetector {

	// No, the two "µ" signs in there are not the same. They just look the same.
	// Also, all units in there must be lowercase, as only lowercase comparison is performed!

	/**
	 * The units allowed as nominator units and the normalized units they are translated to.
	 */
	private static final Map<String, String> KNOWN_NOMINATOR_UNITS = new HashMap<>();
	/**
	 * The units allowed as denominator units and the normalized units they are translated to.
	 */
	private static final Map<String, String> KNOWN_DENOMINATOR_UNITS = new HashMap<>();

	static {
		Map<String, String> validUnits = new HashMap<>();
		KNOWN_NOMINATOR_UNITS.put("mg", "mg");
		KNOWN_NOMINATOR_UNITS.put("ng", "ng");
		KNOWN_NOMINATOR_UNITS.put("μg", "μg");
		KNOWN_NOMINATOR_UNITS.put("µg", "μg");
		KNOWN_NOMINATOR_UNITS.put("mol", "mol");
		KNOWN_NOMINATOR_UNITS.put("mmol", "mmol");
		KNOWN_NOMINATOR_UNITS.put("mikrogramm", "μg");
		KNOWN_NOMINATOR_UNITS.put("i.e.", "I.E.");
		KNOWN_NOMINATOR_UNITS.put("i.u.", "I.E.");
		validUnits.put("g", "g");
		validUnits.put("ml", "ml");
		validUnits.put("nl", "nl");
		validUnits.put("dl", "dl");
		validUnits.put("l", "l");
		validUnits.put("μl", "μl");
		validUnits.put("µl", "μl"); // Yes, this is a different mu symbol. Check the bytes if you don't believe me.
		KNOWN_DENOMINATOR_UNITS.put("beutel", "Beutel");
		KNOWN_DENOMINATOR_UNITS.put("spruehstoss", "Sprühstoß");
		KNOWN_DENOMINATOR_UNITS.put("sprühstoss", "Sprühstoß");
		KNOWN_DENOMINATOR_UNITS.put("spruehstoß", "Sprühstoß");
		KNOWN_DENOMINATOR_UNITS.put("sprühstoß", "Sprühstoß");
		KNOWN_DENOMINATOR_UNITS.put("ampulle", "Ampulle");
		KNOWN_DENOMINATOR_UNITS.put("tablette", "Tablette");
		KNOWN_DENOMINATOR_UNITS.put("zäpfchen", "Zäpfchen");
		KNOWN_DENOMINATOR_UNITS.put("zaepfchen", "Zäpfchen");
		KNOWN_DENOMINATOR_UNITS.put("vaginaltablette", "Vaginaltablette");
		KNOWN_DENOMINATOR_UNITS.put("tbl", "Tablette");
		KNOWN_DENOMINATOR_UNITS.put("filmtablette", "Filmtablette");
		KNOWN_DENOMINATOR_UNITS.put("filmtbl", "Filmtablette");

		KNOWN_NOMINATOR_UNITS.putAll(validUnits);
		KNOWN_DENOMINATOR_UNITS.putAll(validUnits);

	}

	private static final Set<Character> TERMINATOR_SIGNS = Set.of(';', ',', ' ', '/', '(', ')');
	private static final Set<Character> DECIMAL_OR_THOUSANDS_SEPARATOR_SIGNS = Set.of(',', '.');
	private static final BigDecimal MIN_NUMBER_FOR_UNITLESS_DOSAGE = BigDecimal.TEN;

	public static List<DetectedDosage> detectDosages(String value) {
		return new DosageDetector(value).detectDosages();
	}

	private final String value;
	private final int length;

	private final StringBuffer buffer = new StringBuffer(10);

	private DosageDetector(String value) {
		this.value = value;
		length = value.length();
	}

	public List<DetectedDosage> detectDosages() {
		List<DetectedDosage> result = new ArrayList<>();
		DetectedDosage next;
		int index = 0;
		while ((next = detectNextDosage(index)) != null) {
			result.add(next);
			index = next.startIndex + next.length;
		}
		return result;
	}

	private DetectedDosage detectNextDosage(int startIndex) {
		int maxStartIndex = value.length() - 2; // A dosage should be at least two characters
		int currentIndex = startIndex;
		DetectedDosage dosage = null;
		while (currentIndex <= maxStartIndex && (dosage = detectDosageAt(currentIndex)) == null) {
			currentIndex++;
		}
		return dosage;
	}

	private DetectedDosage detectDosageAt(int startIndex) {
		// The dosage must be preceded by the begin of the string or any terminator character
		if (startIndex != 0 && !TERMINATOR_SIGNS.contains(value.charAt(startIndex - 1)))
			return null;

		DetectedAmount detNominator = detectAmount(startIndex, KNOWN_NOMINATOR_UNITS);
		if (detNominator == null) return null;

		DetectingDosage result = new DetectingDosage();
		result.startIndex = startIndex;
		result.amountNominator = detNominator.amount;
		int currentIndex = detNominator.endIndex;
		result.length = currentIndex - startIndex; // result.length will act as our current reading index

		if (result.amountNominator.unit == null) {
			// Ignore numbers without unit preceded by "LM "
			if (startIndex >= 3 && value.startsWith("LM ", startIndex - 3))
				return null;
			if (result.amountNominator.number.compareTo(MIN_NUMBER_FOR_UNITLESS_DOSAGE) >= 0)
				return result.complete();
			else return null;
		}
		// Only if the nominator actually has a unit, we search for a denominator.
		int separatorIndex = value.indexOf('/', currentIndex);
		if (separatorIndex == -1) {
			return result.complete();
		}

		// If the separator is not the character directly after the nominator and the character directly after
		// the nominator is not a space, we assume the separator does not belong to this dosage.
		if (separatorIndex > currentIndex && value.charAt(currentIndex) != ' ') {
			return result.complete();
		}
		currentIndex++;
		String qualifier = null;
		if (separatorIndex > currentIndex) {
			// Separator is far away! Search for a qualifier.
			for (int index = currentIndex; index < separatorIndex; index++) {
				char current = value.charAt(index);
				if (TERMINATOR_SIGNS.contains(current)) {
					if (index == separatorIndex - 1 && current == ' ') {
						// A space between the separator and the qualifier is allowed
						break;
					}
					// There is another terminator between us and the separator. We assume the separator does not
					// belong to this dosage.
					buffer.setLength(0);
					return result.complete();
				} else {
					buffer.append(current);
				}
			}
			// We have a qualifier candidate, but only apply it if an actual denominator exists!
			qualifier = buffer.toString();
			buffer.setLength(0);
		}

		currentIndex = separatorIndex + 1;
		if (currentIndex < length && value.charAt(currentIndex) == ' ') {
			currentIndex++;
		}
		DetectedAmount detDenominator = detectAmount(currentIndex, KNOWN_DENOMINATOR_UNITS);
		if (detDenominator != null) {
			if (detDenominator.amount.unit != null) { // Denominator must always have a unit to be recognized as such
				result.nominatorQualifier = qualifier;
				result.amountDenominator = detDenominator.amount;
				result.length = detDenominator.endIndex - startIndex;
			}
		} else {
			// This is relevant if we have something like 40 mg/ml, where the denominator is implicitly 1
			String unitOnlyDenominator = detectUnit(currentIndex, KNOWN_DENOMINATOR_UNITS);
			if (unitOnlyDenominator != null) {
				result.nominatorQualifier = qualifier;
				result.amountDenominator = new Amount(BigDecimal.ONE, unitOnlyDenominator);
				result.length = (currentIndex + unitOnlyDenominator.length()) - startIndex;
			}
		}
		return result.complete();
	}

	/**
	 * Attempts to read a value with a unit at a given start index. If no unit is present, the value will still be
	 * considered to be an amount.
	 */
	private DetectedAmount detectAmount(int startIndex, Map<String, String> knownUnits) {
		BigDecimal value = readNumberIntoBufferAndParse(startIndex);
		if (value == null) {
			return null;
		}
		int currentIndex = startIndex + buffer.length();
		buffer.setLength(0);

		String unit = detectUnit(currentIndex, knownUnits);
		if (unit == null &&
				currentIndex < length &&
				this.value.charAt(currentIndex) == ' ') {
			// The unit may be separated by a space, in that case retry
			unit = detectUnit(currentIndex + 1, knownUnits);
			if (unit != null) currentIndex++;
		}
		if (unit != null) {
			currentIndex += unit.length();
		}

		return new DetectedAmount(new Amount(value, unit), currentIndex);
	}

	/**
	 * Attempts to recognize a number at the given index. The parsed number is returned or null if no number was
	 * identified. If a number was identified, the parsed sequence is written in the buffer, otherwise the buffer will
	 * be empty.
	 */
	private BigDecimal readNumberIntoBufferAndParse(int startIndex) {
		int currentIndex = startIndex;
		List<Character> separatorsRead = new ArrayList<>();
		char currentChar = '0';
		while (currentIndex < length) {
			currentChar = value.charAt(currentIndex);
			if (Character.isDigit(currentChar)) {
				buffer.append(currentChar);
				currentIndex++;
			} else if (currentIndex > startIndex
					&& (DECIMAL_OR_THOUSANDS_SEPARATOR_SIGNS.contains(currentChar))) {
				buffer.append(currentChar);
				separatorsRead.add(currentChar);
				currentIndex++;
			} else break;
		}
		// Remove separator if it was the last character to be read
		if (buffer.isEmpty())
			return null;
		int lastIndex = buffer.length() - 1;
		if (DECIMAL_OR_THOUSANDS_SEPARATOR_SIGNS.contains(buffer.charAt(lastIndex))) {
			buffer.deleteCharAt(lastIndex);
		}
		BigDecimal decimal = parseBufferToBigDecimal(separatorsRead);
		if (decimal == null) {
			buffer.setLength(0);
		}
		return decimal;
	}

	private BigDecimal parseBufferToBigDecimal(List<Character> separatorsRead) {
		if (buffer.isEmpty()) return null;
		if (separatorsRead.isEmpty()) {
			return new BigDecimal(buffer.toString());
		} else {
			// We need to find out which separator was used as comma and which as thousands separator.
			String number = buffer.toString();
			if (new HashSet<>(separatorsRead).size() > 1) {
				// Two different separators appear, so we have both a decimal and thousands separator
				// The last thing must be the decimal separator
				char decimalSep = separatorsRead.get(separatorsRead.size() - 1);
				for (int i = 0; i < separatorsRead.size() - 2; i++) {
					if (separatorsRead.get(i) == decimalSep) {
						// The decimal separator appears multiple times! This is not a number any more IMO.
						return null;
					}
				}
				char thousandsSep = separatorsRead.get(0);
				return new BigDecimal(number.replace("" + thousandsSep, "").replace(decimalSep, '.'));
			} else if (separatorsRead.size() > 1) {
				// It occurs multiple times, so it's a thousands separator
				return new BigDecimal(number.replace("" + separatorsRead.get(0), ""));
			} else {
				// Thing occurs only once, it could be both a decimal or thousands separator
				char sep = separatorsRead.get(0);
				if (number.length() >= 5
						&& number.length() <= 7
						&& number.charAt(number.length() - 4) == sep
						&& number.charAt(number.length() - 1) == '0') {
					// Number is 4 to 6 digits (5-7 characters including separator), has exactly 3 digits after
					// the thousands separator and the last character is a zero, which makes little sense for a decimal
					// Well, I guess it's a thousands separator
					return new BigDecimal(number.replace("" + sep, ""));
				} else {
					// I guess it's a decimal separator
					return new BigDecimal(number.replace(sep, '.'));
				}
			}
		}
	}

	/**
	 * Attempts to detect a unit at the given index in the string and returns it. This method assumes the buffer is
	 * empty and overwrites it. It always leaves the buffer empty when returning. If no unit is detected, this method
	 * returns null.
	 */
	private String detectUnit(int startIndex, Map<String, String> knownUnits) {
		int currentIndex = startIndex;
		char currentChar = '0';
		while (currentIndex < length) {
			currentChar = value.charAt(currentIndex);
			if (TERMINATOR_SIGNS.contains(currentChar))
				break;
			buffer.append(currentChar);
			currentIndex++;
		}
		String unit = buffer.toString();
		buffer.setLength(0);
		return knownUnits.get(unit.toLowerCase());
	}

	private static class DetectingDosage {
		int startIndex;
		int length;

		Amount amountNominator;
		String nominatorQualifier;
		Amount amountDenominator;

		DetectedDosage complete() {
			return new DetectedDosage(startIndex, length, new Dosage(amountNominator, nominatorQualifier,
					amountDenominator));
		}
	}

	public static class DetectedDosage {
		final int startIndex;
		final int length;

		final Dosage dosage;

		private DetectedDosage(int startIndex, int length, Dosage dosage) {
			this.startIndex = startIndex;
			this.length = length;
			this.dosage = dosage;
		}

		public int getStartIndex() {
			return startIndex;
		}

		public int getLength() {
			return length;
		}

		public Dosage getDosage() {
			return dosage;
		}
	}

	private static class DetectedAmount {
		/**
		 * The detected amount.
		 */
		public final Amount amount;
		/**
		 * The (exclusive) end index of the amount found in the string.
		 */
		public final int endIndex;

		private DetectedAmount(Amount amount, int endIndex) {
			this.amount = amount;
			this.endIndex = endIndex;
		}
	}

}
