package de.medizininformatikinitiative.medgraph.rxnorm_matching.model;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Markus Budeus
 */
public class RxNormSCDC extends RxNormConcept {

	private static final Pattern AMOUNT_PATTERN = Pattern.compile(" \\d+([.]\\d+)? ");

	private final BigDecimal amount;
	private final String unit;
	private final String ingredientName;

	protected RxNormSCDC(RxNormSCDC toCopy) {
		super(toCopy.getRxcui(), toCopy.getName(), RxNormTermType.SCDC);
		this.amount = toCopy.amount;
		this.unit = toCopy.unit;
		this.ingredientName = toCopy.ingredientName;
	}

	public RxNormSCDC(String rxcui, String name) {
		super(rxcui, name, RxNormTermType.SCDC);

		// Find the first number! It's our amount.
		Matcher amountMatcher = AMOUNT_PATTERN.matcher(name);
		if (!amountMatcher.find()) {
			throw new IllegalArgumentException("Could not parse SCDC name \"" + name + "\"!");
		}

		// Now keep looking for more numbers in case the first one wasn't it!
		// For beatius like RxCUI 329684: dextran 70 1 MG/ML
		int start;
		int end;
		do {
			start = amountMatcher.start();
			end = amountMatcher.end();
		} while (amountMatcher.find(end - 1));

		ingredientName = name.substring(0, start);
		try {
			amount = new BigDecimal(name.substring(start + 1, end - 1));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Could not parse amount \""
					+ name.substring(start + 1, end - 1) + "\" in name \"" +
					name + "\"!");
		}
		unit = name.substring(end);
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getUnit() {
		return unit;
	}

	public String getIngredientName() {
		return ingredientName;
	}

}

