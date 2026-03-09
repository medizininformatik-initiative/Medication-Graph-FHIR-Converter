package de.medizininformatikinitiative.medgraph.rxnorm_matching.model;

import de.medizininformatikinitiative.medgraph.rxnorm_matching.UnitNormalizer;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Amount;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Unit;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents an RxNorm Semantic Clinical Drug (SCD) with structured information.
 *
 * @author Markus Budeus
 */
public class DetailedRxNormSCD extends RxNormConcept {

	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+([.]\\d+)?");
	/**
	 * The dose form assigned to this SCD.
	 */
	private final RxNormDoseForm doseForm;
	/**
	 * The Semantic Clinical Drug Concepts assigned to this SCD via consists_of.
	 */
	private final List<RxNormSCDCWithIngredients> components;

	/**
	 * In some cases, an RxNorm SCD contains an amount (e.g. "100 ML mannitol 200 MG/ML injection"). In that case, "100
	 * ML" is the amount which is identified here. Otherwise this is null.
	 */
	@Nullable
	private final Amount drugAmount;

	public DetailedRxNormSCD(String rxcui, String name, RxNormDoseForm doseForm,
	                         List<RxNormSCDCWithIngredients> components) {
		super(rxcui, name, RxNormTermType.SCD);
		this.doseForm = doseForm;
		this.components = components;
		drugAmount = determineDrugAmountFromName(name);
	}

	/**
	 * If the first token is a number and the second token is a non-relative unit (i.e. a string without a forward dash)
	 * and at least one more word exists, then the first two are considered an amount and returned as such. Otherwise,
	 * this returns null.
	 */
	@Nullable
	private Amount determineDrugAmountFromName(String name) {
		String[] tokens = name.split(" ", 3);
		if (tokens.length != 3) return null;

		if (!NUMBER_PATTERN.matcher(tokens[0]).matches()) {
			return null;
		}

		String unitToken = tokens[1];
		String amountToken = tokens[0];

		if (tokens[1].equals("MG,")) {
			// Stupid edge case: "0.25 MG, 0.5 MG Dose 1.5 ML semaglutide 1.34 MG/ML Pen Injector"
			tokens = name.split(" ", 8);
			if (tokens.length != 8) return null;
			if (!tokens[4].equals("Dose")) return null;
			if (!NUMBER_PATTERN.matcher(tokens[5]).matches()) return null;
			amountToken = tokens[5];
			unitToken = tokens[6];
		}

		if (unitToken.equals("HR")) return null; // HR (hour) is exluded in this case

		Unit unit = UnitNormalizer.normalizeFromRawRxNorm(unitToken);
		if (unit.isRelative()) return null;

		return new Amount(new BigDecimal(amountToken), unit);
	}

	public RxNormDoseForm getDoseForm() {
		return doseForm;
	}

	public List<RxNormSCDCWithIngredients> getComponents() {
		return components;
	}

	public @Nullable Amount getDrugAmount() {
		return drugAmount;
	}

	@Override
	public String toString() {
		return "[" + getRxcui() + "] " + super.toString();
	}
}
