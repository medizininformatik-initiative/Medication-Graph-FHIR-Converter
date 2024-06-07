package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.searchengine.model.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class assigns scores based on how well given {@link Dosage}s match {@link Drug}-data from the database.
 *
 * @author Markus Budeus
 */
public class DosageMatchJudge {
	/**
	 * The score to assign if a search query simply does not specify any dosage information which could be used for
	 * judging.
	 */
	public static final double DOSAGELESS_SCORE = 0.2;
	/**
	 * The score to assign if an ingredient amount relative to a drug amount is given and matches perfectly with both
	 * the ingredient amount and the drug amount. For example, this would be assigned to the value "200μg/10ml" if we
	 * have a drug containing 200μg Fentanyl in a 10 ml ampoule.
	 */
	public static final double PERFECT_RELATIVE_MATCH_SCORE = 1.5;
	/**
	 * The score to assign if an ingredient amount relative to a drug amount is given and matches, but does not contain
	 * the exact values. For example, this would be assigned to the value "20μg/ml" if we have a drug containing 200μg
	 * Fentanyl in a 10 ml ampoule.
	 */
	public static final double NORMALIZED_RELATIVE_MATCH_SCORE = 0.8;
	/**
	 * The score to assign if an absolute ingredient amount is given and matches at least one active ingredient's
	 * amount. For example, this would be assigned to the value "200ug" if we have a drug containing 200μg Fentanyl in a
	 * 10 ml ampoule.
	 */
	public static final double ABSOLUTE_MATCH_SCORE = 1;

	private static final BigDecimal EPSILON = new BigDecimal("0.01");

	/**
	 * Assigns a score based on how well the given target dosages match the given drugs. Each given target dosage is
	 * judged separately and the sum of the achieved scores is returned.
	 *
	 * @param drugs         the drugs against which to judge the target dosage information
	 * @param targetDosages the target dosages to judge against the drug dosage information
	 * @return a score based on how well the active ingredients match
	 */
	public double judge(@NotNull List<Drug> drugs, @NotNull List<Dosage> targetDosages) {
		if (targetDosages.isEmpty()) return DOSAGELESS_SCORE;
		List<Dosage> unmatchedDosages = new ArrayList<>(targetDosages);
		double score = 0;
		for (Dosage d : unmatchedDosages) {
			score += judgeDosageMatch(drugs, d);
		}
		return score;
	}

	/**
	 * Judges the given target dosage against a list of drugs. The score of whichever judgement between one of the
	 * drugs' dosages and the target dosage was highest is returned.
	 *
	 * @param drugs        the list of drugs against which to judge the target dosage
	 * @param targetDosage the target dosage for which to find and judge the best-matching drug dosage information
	 * @return the highest achieved score of the judgements of the target dosage against each drug dosage
	 * @see #judgeDosageMatch(Drug, Dosage)
	 */
	private double judgeDosageMatch(List<Drug> drugs, Dosage targetDosage) {
		double bestScore = 0;
		for (Drug drug : drugs) {
			bestScore = Math.max(bestScore, judgeDosageMatch(drug, targetDosage));
		}
		return bestScore;
	}

	/**
	 * Judges how well the given target dosage matches the given drug's dosage information. If the target dosage
	 * contains a denominator, a relative matching is performed, see {@link #judgeRelative(Drug, Dosage)}. Otherwise, an
	 * absolute matching is performed, see {@link #judgeAbsolute(Drug, Dosage)}.
	 *
	 * @param drug         the drug information against which to match the target dosage
	 * @param targetDosage the target dosage which to match against the drug dosage information
	 * @return the judgement score
	 */
	private static double judgeDosageMatch(Drug drug, Dosage targetDosage) {
		if (targetDosage.amountDenominator != null) {
			return judgeRelative(drug, targetDosage);
		} else {
			return judgeAbsolute(drug, targetDosage);
		}
	}

	/**
	 * Compares each active ingredient dosage from the given drug dosage relative to the drug amount against the target
	 * dosage and returns the score of the highest-scoring match. This function requires the target dosage to have a
	 * denominator, as it is compared against the drug active ingredient dosages relative to the amount.
	 *
	 * @param drug         the drug whose dosage information in which to search the best match to the target dosage
	 * @param targetDosage the target dosage for which to search the best match
	 * @return the score of the best match found
	 * @see #judgeRelative(Amount, Amount, Dosage)
	 */
	private static double judgeRelative(Drug drug, Dosage targetDosage) {
		Amount drugAmount = drug.getAmount();
		if (drugAmount == null) return 0;
		double bestScore = 0;
		for (Amount activeIngredientAmount : getActiveIngredientDosages(drug)) {
			bestScore = Math.max(bestScore, judgeRelative(activeIngredientAmount, drugAmount, targetDosage));
		}
		return bestScore;
	}

	/**
	 * Compares each active ingredient dosage from the given drug dosage against the target dosage and returns the score
	 * of the highest-scoring match. However, this function only uses the nominator of the target dosage to compare it
	 * against the active ingredient dosages of the drug. Hence the name "absolute".
	 *
	 * @param drug         the drug whose dosage information to search the best match to the target dosage
	 * @param targetDosage the target dosage for which to search the best match
	 * @return the score of the best match found
	 */
	private static double judgeAbsolute(Drug drug, Dosage targetDosage) {
		// Check if drug dosage matches target dosage. If the drug features multiple ingredients each with their own
		// dosage, take the best match.
		double bestAmountMatch = 0;
		for (Amount amount : getActiveIngredientDosages(drug)) {
			if (matchesAbsolute(amount, targetDosage)) {
				bestAmountMatch = ABSOLUTE_MATCH_SCORE;
				break;
			}
		}
		return bestAmountMatch;
	}

	/**
	 * Returns whether the given targetDosage nominator matches the database dosage. If the given targetDosage does not
	 * provide a unit, then no unit comparison is performed, i.e. the unit is always considered to match. But if a unit
	 * is provided, it must match.
	 */
	private static boolean matchesAbsolute(Amount amount, Dosage targetDosage) {
		Amount targetAmount = targetDosage.amountNominator;
		if (!matchesAbsolute(amount, targetAmount.getNumber(), BigDecimal.ZERO))
			return false;

		if (targetAmount.getUnit() != null) {
			return targetAmount.getUnit().equals(amount.getUnit());
		}
		return true;
	}

	/**
	 * Judges whether the given targetDosage fraction matches the activeIngredientAmount and the given drug amount. For
	 * example, if the drug amount is 3ml and the activeIngredientAmount is 300mg, this would translate to 100mg/ml. If
	 * that is the value provided by the targetDosage (or sth else like 300mg/3ml), it is considered a match and the
	 * {@link #NORMALIZED_RELATIVE_MATCH_SCORE} is returned. The units must match perfectly in this case, unlike in the
	 * absolute matching.
	 * <p>
	 * If both the dosage of the drug and the given targetDosage match perfectly in nominator and denominator (e.g. if
	 * the drug is 300mg ingredient and 3ml ingredient and the given targetDosage has a nominator of exactly 300mg and a
	 * denominator of 3ml), the {@link #PERFECT_RELATIVE_MATCH_SCORE} is returned instead.
	 * <p>
	 * If no such match is achieved, zero is returned.
	 *
	 * @throws NullPointerException if the target dosage has no denominator
	 */
	private static double judgeRelative(Amount activeIngredientAmount, Amount drugAmount, Dosage targetDosage) {
		assert targetDosage.amountDenominator != null;
		if (!Objects.equals(targetDosage.amountDenominator.getUnit(), drugAmount.getUnit())) return 0;
		if (!Objects.equals(targetDosage.amountNominator.getUnit(), activeIngredientAmount.getUnit())) return 0;

		BigDecimal activeIngredientAmountFrom = activeIngredientAmount.getNumber();
		BigDecimal activeIngredientAmountTo = null;
		if (activeIngredientAmount instanceof AmountRange r) {
			activeIngredientAmountFrom = r.getFrom();
			activeIngredientAmountTo = r.getTo();
		}

		// Attempt perfect match
		if (matchesAbsolute(activeIngredientAmount, targetDosage.amountNominator.getNumber(), BigDecimal.ZERO)
				&& matchesAbsolute(drugAmount, targetDosage.amountDenominator.getNumber(), BigDecimal.ZERO)) {
			return PERFECT_RELATIVE_MATCH_SCORE;
		}

		// Attempt normalized match by dividing the dosage by the drug amount to get the normalized dosage.
		BigDecimal normalizedAmountLocalFrom = activeIngredientAmountFrom.setScale(4, RoundingMode.UNNECESSARY)
		                                                                 .divide(drugAmount.getNumber(),
				                                                                 RoundingMode.HALF_UP);
		Amount resultAmount;
		if (activeIngredientAmountTo != null) {
			BigDecimal normalizedAmountLocalTo = activeIngredientAmountTo.setScale(4, RoundingMode.UNNECESSARY)
			                                                  .divide(drugAmount.getNumber(),
					                                                  RoundingMode.HALF_UP);
			resultAmount = new AmountRange(normalizedAmountLocalFrom, normalizedAmountLocalTo, activeIngredientAmount.getUnit());
		} else {
			resultAmount = new Amount(normalizedAmountLocalFrom, activeIngredientAmount.getUnit());
		}
		BigDecimal normalizedAmountOther = targetDosage.amountNominator.getNumber().setScale(4, RoundingMode.HALF_UP)
		                                                               .divide(targetDosage.amountDenominator.getNumber(),
				                                                               RoundingMode.HALF_UP);

		if (matchesAbsolute(resultAmount, normalizedAmountOther, EPSILON)) {
			return NORMALIZED_RELATIVE_MATCH_SCORE;
		}
		return 0;
	}

	/**
	 * Compares the given amount with the target number.
	 *
	 * @param source the range or value to compare to the target
	 * @param target the target to which to compare the range or value
	 * @param delta  if the source is not a range, the maximum allowed difference between the soruce and target which is
	 *               considered a match
	 * @return true if target is sufficiently close to rangeFrom in case rangeTo is null, or if target is between
	 * rangeFrom and rangeTo
	 */
	private static boolean matchesAbsolute(@NotNull Amount source,
	                                       @NotNull BigDecimal target, @NotNull BigDecimal delta) {
		if (source instanceof AmountRange range) {
			return range.getFrom().compareTo(target) <= 0 && range.getTo().compareTo(target) >= 0;
		} else {
			return source.getNumber().subtract(target).abs().compareTo(delta) <= 0;
		}
	}

	/**
	 * Returns the amounts of this drug's active ingredients, including dosages of any corresponding ingredients.
	 */
	private static List<Amount> getActiveIngredientDosages(Drug d) {
		List<Amount> list = new ArrayList<>();
		for (ActiveIngredient ingredient : d.getActiveIngredients()) {
			list.add(ingredient.getAmount());
			if (ingredient instanceof CorrespondingActiveIngredient c) {
				list.add(c.getCorrespondingSubstanceAmount());
			}
		}
		return list;
	}

}
