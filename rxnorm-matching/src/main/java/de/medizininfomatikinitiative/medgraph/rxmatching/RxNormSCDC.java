package de.medizininfomatikinitiative.medgraph.rxmatching;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a semantic clinical drug in RxNorm.
 *
 * @param rxcui           The RxCUI of this SCDC.
 * @param ingredient      The referenced ingredient. Note the ingredient can be a IN, but it
 *                        can also be a PIN. It depends on what the dosage references.
 * @param amount          The amount of the ingredient in perfect precision.
 * @param unit            The unit of the ingredient as given in RxNorm.
 * @author Markus Budeus
 */
public record RxNormSCDC(String rxcui, RxNormIngredient ingredient, BigDecimal amount, String unit) {

	private static final Pattern AMOUNT_PATTERN = Pattern.compile(" \\d+([.]\\d+)? ");

	/**
	 * Parses raw RxNorm information into an RxNormSCDC object.
	 * @param rxcui The rxcui of the SCDC to represent.
	 * @param linkedIngredients All ingredients (IN and PIN) that are linked to this SCDC. The "actual" referenced
	 *                          ingredient is determined dynamically.
	 * @param content The text content of this SCDC.
	 * @return A parsed {@link RxNormSCDC}
	 * @throws IllegalArgumentException If the given data does not fit together to build a valid RxNormSCDC object.
	 */
	public static RxNormSCDC parse(String rxcui, List<RxNormIngredient> linkedIngredients, String content) {
		// We look for the first number in the text. The number is used as amount. Everything before is the
		// ingredient name. Everything after is the unit.
		Matcher matcher = AMOUNT_PATTERN.matcher(content);
		if (!matcher.lookingAt()) {
			throw new IllegalArgumentException("Could not parse SCDC content: "+content);
		}

		BigDecimal amount = new BigDecimal(content.substring(matcher.start() + 1, matcher.end() - 1));
		String ingredientName = content.substring(0, matcher.start()).trim();
		String unit = content.substring(matcher.end()).trim();

		RxNormIngredient ingredient = null;

		for (RxNormIngredient ingredientCandidate: linkedIngredients) {
			if (ingredientCandidate.name().equals(ingredientName)) {
				ingredient = ingredientCandidate;
				break;
			}
		}
		if (ingredient == null) {
			throw new IllegalArgumentException("Could not find ingredient \""+ingredientName+"\", got "+linkedIngredients);
		}

		return new RxNormSCDC(rxcui, ingredient, amount, unit)
	}

}
