package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.searchengine.db.DbAmount;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDrugDosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class assigns scores based on how well given {@link Amount}s match the drug amount information from the
 * {@link DbDrugDosage}-data from the database.
 *
 * @author Markus Budeus
 */
public class DrugAmountMatchJudge {

	/**
	 * The score which is assigned if no amount information is given.
	 */
	public static final double NO_AMOUNTS_SCORE = 0.2;

	/**
	 * The score to be assigned if a drug amount matches the target amount perfectly, including unit. This includes if
	 * the unit is absent for both.
	 */
	public static final double MATCH_SCORE = 1.0;
	/**
	 * The score to be assigned if a drug amount matches the target amount, but the target amount did not specify a unit
	 * while a unit is present for the drug amount.
	 */
	public static final double UNITLESS_MATCH_SCORE = 0.5;

	/**
	 * Assigns a score based on how well the given target amounts match the given drug amounts. Each given target amount
	 * is judged separately and the sum of the achieved scores is returned.
	 *
	 * @param drugAmounts   the drug dosage info against which to judge the target dosage information
	 * @param targetAmounts the target amounts to judge against the drug amount information
	 * @return a score based on how well the drug amounts match
	 */
	public double judge(@NotNull List<DbAmount> drugAmounts, @NotNull List<Amount> targetAmounts) {
		if (targetAmounts.isEmpty()) return NO_AMOUNTS_SCORE;
		List<Amount> unmatchedAmounts = new ArrayList<>(targetAmounts);
		double score = 0;
		for (Amount amount : unmatchedAmounts) {
			score += judgeAmountMatch(drugAmounts, amount);
		}
		return score;
	}

	/**
	 * Judges the given target amount against each of the provided drug amounts. Returns the score of the
	 * highest-scoring match.
	 */
	private static double judgeAmountMatch(List<DbAmount> drugAmounts, Amount targetAmount) {
		double maxScore = 0;
		for (DbAmount drugAmount : drugAmounts) {
			double localScore = judgeAmountMatch(drugAmount, targetAmount);
			if (localScore > maxScore) maxScore = localScore;
		}
		return maxScore;
	}

	/**
	 * Judges how well the given target amount matches the drug amount. If they perfectly match, {@link #MATCH_SCORE}
	 * is returned. If their numbers match, but the targetAmount has no specified unit while the drug amount has,
	 * {@link #UNITLESS_MATCH_SCORE} is returned. Otherwise, 0 is returned.
	 */
	private static double judgeAmountMatch(DbAmount drugAmount, Amount targetAmount) {
		if (drugAmount.amount.compareTo(targetAmount.getNumber()) != 0)
			return 0;
		if (Objects.equals(drugAmount.unit, targetAmount.getUnit()))
			return MATCH_SCORE;
		if (targetAmount.getUnit() == null)
			return UNITLESS_MATCH_SCORE;
		return 0;
	}


}
