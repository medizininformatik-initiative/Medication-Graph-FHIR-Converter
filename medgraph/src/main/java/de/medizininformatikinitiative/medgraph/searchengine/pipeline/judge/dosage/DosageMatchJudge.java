package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.searchengine.db.DbAmount;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDosage;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDrugDosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class assigns scores based on how well given {@link Dosage}s match {@link DbDrugDosage}-data from the database.
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
	 * Assigns a score based on how well the given target dosages match the given drug dosages. Each given target dosage
	 * is judged separately and the sum of the achieved scores is returned.
	 *
	 * @param drugDosages   the drug dosages against which to judge the target dosage information
	 * @param targetDosages the target dosages to judge against the drug dosage information
	 * @return a score based on how well the active ingredients match
	 */
	public double judge(@NotNull List<DbDrugDosage> drugDosages, @NotNull List<Dosage> targetDosages) {
		if (targetDosages.isEmpty()) return DOSAGELESS_SCORE;
		List<Dosage> unmatchedDosages = new ArrayList<>(targetDosages);
		double score = 0;
		for (Dosage d : unmatchedDosages) {
			score += judgeDosageMatch(drugDosages, d);
		}
		return score;
	}

	/**
	 * Judges the given target dosage against a list of drug dosages. The score of whichever judgement between one of
	 * the drug dosages and the target dosage was highest is returned.
	 *
	 * @param drugDosages  the list of drug dosages against which to judge the target dosage
	 * @param targetDosage the target dosage for which to find and judge the best-matching drug dosage information
	 * @return the highest achieved score of the judgements of the target dosage against each drug dosage
	 * @see #judgeDosageMatch(DbDrugDosage, Dosage)
	 */
	private double judgeDosageMatch(List<DbDrugDosage> drugDosages, Dosage targetDosage) {
		double bestScore = 0;
		for (DbDrugDosage drugDosage : drugDosages) {
			bestScore = Math.max(bestScore, judgeDosageMatch(drugDosage, targetDosage));
		}
		return bestScore;
	}

	/**
	 * Judges how well the given target dosage matches the given drug dosage information. If the target dosage contains
	 * a denominator, a relative matching is performed, see {@link #judgeRelative(DbDrugDosage, Dosage)}. Otherwise, an
	 * absolute matching is performed, see {@link #judgeAbsolute(DbDrugDosage, Dosage)}.
	 *
	 * @param drugDosage   the drug dosage information against which to match the target dosage
	 * @param targetDosage the target dosage which to match against the drug dosage information
	 * @return the judgement score
	 */
	private static double judgeDosageMatch(DbDrugDosage drugDosage, Dosage targetDosage) {
		if (targetDosage.amountDenominator != null) {
			return judgeRelative(drugDosage, targetDosage);
		} else {
			return judgeAbsolute(drugDosage, targetDosage);
		}
	}

	/**
	 * Compares each active ingredient dosage from the given drug dosage relative to the drug amount against the target
	 * dosage and returns the score of the highest-scoring match. This function requires the target dosage to have a
	 * denominator, as it is compared against the drug active ingredient dosages relative to the amount.
	 *
	 * @param drugDosage   the drug dosage information in which to search the best match to the target dosage
	 * @param targetDosage the target dosage for which to search the best match
	 * @return the score of the best match found
	 * @see #judgeRelative(DbDosage, DbAmount, Dosage)
	 */
	private static double judgeRelative(DbDrugDosage drugDosage, Dosage targetDosage) {
		if (drugDosage.amount == null) return 0;
		double bestScore = 0;
		for (DbDosage d : drugDosage.dosages) {
			bestScore = Math.max(bestScore, judgeRelative(d, drugDosage.amount, targetDosage));
		}
		return bestScore;
	}


	/**
	 * Compares each active ingredient dosage from the given drug dosage against the target dosage and returns the score
	 * of the highest-scoring match. However, this function only uses the nominator of the target dosage to compare it
	 * against the active ingredient dosages of the drug. Hence the name "absolute".
	 *
	 * @param drugDosage   the drug dosage information in which to search the best match to the target dosage
	 * @param targetDosage the target dosage for which to search the best match
	 * @return the score of the best match found
	 */
	private static double judgeAbsolute(DbDrugDosage drugDosage, Dosage targetDosage) {

		// Check if drug dosage matches target dosage. If the drug features multiple ingredients each with their own
		// dosage, take the best match.
		double bestAmountMatch = 0;
		for (DbDosage d : drugDosage.dosages) {
			if (matchesAbsolute(d, targetDosage)) {
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
	private static boolean matchesAbsolute(DbDosage dbDosage, Dosage targetDosage) {
		Amount amount = targetDosage.amountNominator;
		if (!matchesAbsolute(dbDosage.amountFrom, dbDosage.amountTo, amount.number, BigDecimal.ZERO)) return false;

		if (amount.unit != null) {
			return amount.unit.equals(dbDosage.unit);
		}
		return true;
	}

	/**
	 * Judges whether the given targetDosage fraction matches the database dosage and the given drug amount. For
	 * example, if the drug amount is 3ml and the db dosage is 300mg, this would translate to 100mg/ml. If that is the
	 * value provided by the targetDosage (or sth else like 300mg/3ml), it is considered a match and the
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
	private static double judgeRelative(DbDosage dbDosage, DbAmount drugAmount, Dosage targetDosage) {
		assert targetDosage.amountDenominator != null;
		if (!Objects.equals(targetDosage.amountDenominator.unit, drugAmount.unit)) return 0;
		if (!Objects.equals(targetDosage.amountNominator.unit, dbDosage.unit)) return 0;

		// Attempt perfect match
		if (matchesAbsolute(dbDosage.amountFrom, dbDosage.amountTo, targetDosage.amountNominator.number,
				BigDecimal.ZERO) && matchesAbsolute(drugAmount.amount, null, targetDosage.amountDenominator.number,
				BigDecimal.ZERO)) {
			return PERFECT_RELATIVE_MATCH_SCORE;
		}

		// Attempt normalized match by dividing the dosage by the drug amount to get the normalized dosage.
		BigDecimal normalizedAmountLocalFrom = dbDosage.amountFrom.setScale(4, RoundingMode.UNNECESSARY)
		                                                          .divide(drugAmount.amount, RoundingMode.HALF_UP);
		BigDecimal normalizedAmountLocalTo = null;
		if (dbDosage.amountTo != null) normalizedAmountLocalTo = dbDosage.amountTo.setScale(4, RoundingMode.UNNECESSARY)
		                                                                          .divide(drugAmount.amount,
				                                                                          RoundingMode.HALF_UP);
		BigDecimal normalizedAmountOther = targetDosage.amountNominator.number.setScale(4, RoundingMode.HALF_UP)
		                                                                      .divide(targetDosage.amountDenominator.number,
				                                                                      RoundingMode.HALF_UP);

		if (matchesAbsolute(normalizedAmountLocalFrom, normalizedAmountLocalTo, normalizedAmountOther, EPSILON)) {
			return NORMALIZED_RELATIVE_MATCH_SCORE;
		}
		return 0;
	}

	/**
	 * Compares the given optional range with the target.
	 * <p>
	 * If rangeTo is null, true is returned if and only if the different between rangeFrom and target is less or equal
	 * to delta.
	 * <p>
	 * If rangeTo is not null, true is returned if and only if target is between rangeFrom and rangeTo. The delta is
	 * ignored in this case.
	 *
	 * @param rangeFrom the start of the range or the exact value to compare to the target
	 * @param rangeTo   the end of the range to compare to the target or null if rangeFrom shall be compared directly
	 * @param target    the target to which to compare the range or value
	 * @param delta     in case rangeTo is null, the maximum allowed difference between rangeFrom and target which is
	 *                  considered a match
	 * @return true if target is sufficiently close to rangeFrom in case rangeTo is null, or if target is between
	 * rangeFrom and rangeTo
	 */
	private static boolean matchesAbsolute(@NotNull BigDecimal rangeFrom, @Nullable BigDecimal rangeTo,
	                                       @NotNull BigDecimal target, @NotNull BigDecimal delta) {
		if (rangeTo == null) {
			return rangeFrom.subtract(target).abs().compareTo(delta) <= 0;
		} else {
			return rangeFrom.compareTo(target) <= 0 && rangeTo.compareTo(target) >= 0;
		}
	}

}
