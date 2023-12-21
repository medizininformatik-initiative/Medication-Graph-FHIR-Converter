package de.tum.med.aiim.markusbudeus.matcher.tools;

import de.tum.med.aiim.markusbudeus.matcher.Amount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DosageDetector {

	// No, the two "µ" signs in there are not the same. They just look the same.
	private static final Set<String> KNOWN_UNITS = Set.of("mg", "g", "ml", "μg", "µg", "dl", "mikrogramm");
	private static final Set<Character> TERMINATOR_SIGNS = Set.of(',', ' ', '.', '/');
	private static final Set<Character> DECIMAL_SEPARATOR_SIGNS = Set.of(',', '.');
	private static final BigDecimal MIN_NUMBER_FOR_UNITLESS_DOSAGE = BigDecimal.TEN;

	public static List<Dosage> detectDosages(String value) {
		return new DosageDetector(value).detectDosages();
	}

	private final String value;
	private final int length;

	private final StringBuffer buffer = new StringBuffer(10);

	private DosageDetector(String value) {
		this.value = value;
		length = value.length();
	}

	public List<Dosage> detectDosages() {
		List<Dosage> result = new ArrayList<>();
		Dosage next;
		int index = 0;
		while ((next = detectNextDosage(index)) != null) {
			result.add(next);
			index = next.startIndex + next.length;
		}
		return result;
	}

	private Dosage detectNextDosage(int startIndex) {
		int maxStartIndex = value.length() - 2; // A dosage should be at least two characters
		int currentIndex = startIndex;
		Dosage dosage = null;
		while (currentIndex <= maxStartIndex && (dosage = detectDosageAt(currentIndex)) == null) {
			currentIndex++;
		}
		return dosage;
	}

	private Dosage detectDosageAt(int startIndex) {
		// The dosage must be preceded by the begin of the string or any terminator character
		if (startIndex != 0 && !TERMINATOR_SIGNS.contains(value.charAt(startIndex - 1)))
			return null;

		DetectedAmount detNominator = detectAmount(startIndex);
		if (detNominator == null) return null;

		Dosage result = new Dosage();
		result.startIndex = startIndex;
		result.amountNominator = detNominator.amount;
		int currentIndex = detNominator.endIndex;
		result.length = currentIndex - startIndex; // result.length will act as our current reading index

		if (result.amountNominator.unit == null) {
			if (result.amountNominator.number.compareTo(MIN_NUMBER_FOR_UNITLESS_DOSAGE) >= 0)
				return result;
			else return null;
		}
		// Only if the nominator actually has a unit, we search for a denominator.
		int separatorIndex = value.indexOf('/', currentIndex);
		if (separatorIndex == -1) {
			return result;
		}

		// If the separator is not the character directly after the nominator and the character directly after
		// the nominator is not a space, we assume the separator does not belong to this dosage.
		if (separatorIndex > currentIndex && value.charAt(currentIndex) != ' ') {
			return result;
		}
		currentIndex++;
		String qualifier = null;
		if (separatorIndex > currentIndex) {
			// Separator is far away! Search for a qualifier.
			for (int index = currentIndex; index < separatorIndex; index++) {
				char current = value.charAt(index);
				buffer.append(current);
				if (TERMINATOR_SIGNS.contains(value.charAt(index))) {
					// There is another terminator between us and the separator. We assume the separator does not
					// belong to this dosage.
					buffer.setLength(0);
					return result;
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
		DetectedAmount detDenominator = detectAmount(currentIndex);
		if (detDenominator != null) {
			result.nominatorQualifier = qualifier;
			result.amountDemoninator = detDenominator.amount;
			result.length = detDenominator.endIndex - startIndex;
		}
		return result;
	}

	/**
	 * Attempts to read a value with a unit at a given start index. If no unit is present, the value will still be
	 * considered to be an amount.
	 */
	private DetectedAmount detectAmount(int startIndex) {
		readNumberIntoBuffer(startIndex);
		if (buffer.isEmpty()) {
			return null;
		}
		BigDecimal value = new BigDecimal(buffer.toString());
		int currentIndex = startIndex + buffer.length();
		buffer.setLength(0);

		String unit = detectUnit(currentIndex);
		if (unit == null &&
				currentIndex < length &&
				this.value.charAt(currentIndex) == ' ') {
			// The unit may be separated by a space, in that case retry
			unit = detectUnit(currentIndex + 1);
			if (unit != null) currentIndex++;
		}
		if (unit != null) {
			currentIndex += unit.length();
		}

		return new DetectedAmount(new Amount(value, unit), currentIndex);
	}

	/**
	 * Starts reading digits starting at the specified index and writing them into the buffer until no more digits
	 * appear. If it encounters one decimal separator, it also reads in a dot. (No matter which kind of separator was
	 * identified.) However, this happens at most once and only if the decimal separator is not at the start or end of
	 * the sequence. Negative numbers are never read.
	 */
	private void readNumberIntoBuffer(int startIndex) {
		int currentIndex = startIndex;
		boolean includesSeparator = false;
		char currentChar = '0';
		while (currentIndex < length) {
			currentChar = value.charAt(currentIndex);
			if (Character.isDigit(currentChar)) {
				buffer.append(currentChar);
				currentIndex++;
			} else if (!includesSeparator
					&& currentIndex > startIndex
					&& (DECIMAL_SEPARATOR_SIGNS.contains(currentChar))) {
				buffer.append('.');
				includesSeparator = true;
				currentIndex++;
			} else break;
		}
		// Remove separator if it was the last character to be read
		if (includesSeparator && DECIMAL_SEPARATOR_SIGNS.contains(currentChar)) {
			buffer.deleteCharAt(currentIndex - 1);
		}
	}

	/**
	 * Attempts to detect a unit at the given index in the string and returns it. This method assumes the buffer is
	 * empty and overwrites it. It always leaves the buffer empty when returning. If no unit is detected, this method
	 * returns null.
	 */
	private String detectUnit(int startIndex) {
		int currentIndex = startIndex;
		char currentChar;
		while (currentIndex < length) {
			currentChar = value.charAt(currentIndex);
			if (TERMINATOR_SIGNS.contains(currentChar))
				break;
			buffer.append(currentChar);
			currentIndex++;
		}
		String unit = buffer.toString();
		buffer.setLength(0);
		if (KNOWN_UNITS.contains(unit.toLowerCase())) {
			return unit;
		}
		return null;
	}

	public static class Dosage {
		int startIndex;
		int length;

		Amount amountNominator;
		/**
		 * An additional qualifier for the nominator's amount. For example, if the dosage were "1mg Iron/1ml", the
		 * qualifier would be "Iron"
		 */
		String nominatorQualifier;
		Amount amountDemoninator;

		private Dosage() {

		}

		public int getStartIndex() {
			return startIndex;
		}

		public int getLength() {
			return length;
		}

		public Amount getAmountNominator() {
			return amountNominator;
		}

		public Amount getAmountDemoninator() {
			return amountDemoninator;
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
