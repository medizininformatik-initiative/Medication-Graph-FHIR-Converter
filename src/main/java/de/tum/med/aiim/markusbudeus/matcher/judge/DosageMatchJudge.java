package de.tum.med.aiim.markusbudeus.matcher.judge;

import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.Amount;
import de.tum.med.aiim.markusbudeus.matcher.model.Dosage;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

public class DosageMatchJudge implements MatchJudge {

	/**
	 * The score to assign if the given matching target is not a product.
	 */
	public static final double NO_PRODUCT_SCORE = 0.0;
	/**
	 * The score to assign if a houselist entry simply does not specify any dosage information which could be used for
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
	 * amount. For example, this would be assigned to the value "20ug" if we have a drug containing 200μg Fentanyl in a
	 * 10 ml ampoule.
	 */
	public static final double ABSOLUTE_MATCH_SCORE = 1;
	/**
	 * The score to assign if a given dosage matches the drug's amount. For example, this would be assigned to the value
	 * "10 ml" if we have a drug containing 200μg Fentanyl in a 10 ml ampoule.
	 */
	public static final double DRUG_AMOUNT_MATCH_SCORE = 0.7;

	/**
	 * The minimun score which is applied if there is any kind of match. Without any match, the score is zero.
	 */
	public static final double MIN_SCORE_ON_MATCH = DOSAGELESS_SCORE;

	private final Session session;

	public DosageMatchJudge(Session session) {
		this.session = session;
	}

	@Override
	public List<Double> batchJudge(List<MatchingTarget> targets, HouselistEntry entry) {
		Map<Long, MatchingTarget> targetIds = new HashMap<>(targets.size());
		for (MatchingTarget t : targets) {
			if (t.getType() == MatchingTarget.Type.PRODUCT)
				targetIds.put(t.getMmiId(), t);
		}

		Result result = queryEffectiveDosagesForProductIds(new ArrayList<>(targetIds.keySet()));

		Map<MatchingTarget, Double> scoreMap = new HashMap<>();

		while (result.hasNext()) {
			Record record = result.next();
			long productId = record.get(0).asLong();
			MatchingTarget target = targetIds.get(productId);
			if (target != null) {
				scoreMap.put(target, judge(record, entry));
			}
		}

		List<Double> resultList = new ArrayList<>(targets.size());
		for (MatchingTarget target : targets) {
			Double score = scoreMap.get(target);
			if (score == null) score = NO_PRODUCT_SCORE;
			resultList.add(score);
		}

		return resultList;
	}

	@Override
	public double judge(MatchingTarget target, HouselistEntry entry) {
		if (target.getType() != MatchingTarget.Type.PRODUCT) return NO_PRODUCT_SCORE;

		Result result = queryEffectiveDosagesForProductIds(List.of(target.getMmiId()));
		if (!result.hasNext()) return 0.0;
		return judge(result.next(), entry);
	}

	private double judge(Record record, HouselistEntry entry) {
		if (entry.activeIngredientDosages == null || entry.activeIngredientDosages.isEmpty()) return DOSAGELESS_SCORE;
		List<Dosage> unmatchedDosages = new ArrayList<>(entry.activeIngredientDosages);
		List<ExportDrugDosage> drugDosages = parse(record);
		double score = 0;
		for (Dosage d : unmatchedDosages) {
			score += judgeMatch(drugDosages, d);
		}
		return score;
	}

	private synchronized Result queryEffectiveDosagesForProductIds(List<Long> mmiIds) {
		return session.run(new Query(
				"MATCH (p:" + PRODUCT_LABEL + ")\n" +
						"WHERE p.mmiId IN $mmiIds\n" +
						"MATCH (p)--(d:" + DRUG_LABEL + ")--(i:" + INGREDIENT_LABEL + " {isActive: true})\n" +
						"OPTIONAL MATCH (i)-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]-(ic:" + INGREDIENT_LABEL + ")\n" +
						"OPTIONAL MATCH (d)--(du:" + UNIT_LABEL + ")\n" +
						"WITH p, d, du, [i,ic] AS ingredients\n" +
						"UNWIND ingredients as i\n" +
						"WITH p, d, du, i WHERE NOT i IS NULL\n" +
						"MATCH (i)--(u:Unit)\n" +
						"WITH p.mmiId AS productId, d.mmiId AS drugId,\n" +
						"CASE WHEN du IS NULL THEN NULL ELSE {amount:d.amount, unit:du.print} END AS drugUnit, " +
						"collect({amountFrom:i.massFrom,amountTo:i.massTo,unit:u.print}) AS dosage\n" +
						"WITH productId, collect({drugId:drugId, amount:drugUnit, dosage:dosage}) AS drugs\n" +
						"RETURN productId, drugs",
				parameters("mmiIds", mmiIds)
		));
	}

	private double judgeMatch(List<ExportDrugDosage> drugDosages, Dosage dosage) {
		double bestScore = 0;
		for (ExportDrugDosage drugDosage : drugDosages) {
			bestScore = Math.max(bestScore, drugDosage.judgeMatch(dosage));
		}
		return bestScore;
	}

	private List<ExportDrugDosage> parse(Record record) {
		return record.get(1).asList(ExportDrugDosage::new);
	}

	private static class ExportDrugDosage {

		private final ExportAmount amount;
		private final List<ExportDosage> dosages;

		ExportDrugDosage(Value value) {
			dosages = value.get("dosage").asList(ExportDosage::new);
			Value amount = value.get("amount");
			if (amount.isNull()) {
				this.amount = null;
			} else {
				this.amount = new ExportAmount(amount);
			}
		}

		public double judgeMatch(Dosage dosage) {
			if (dosage.amountDenominator != null) {
				return judgeRelative(dosage);
			} else {
				return judgeAbsolute(dosage);
			}
		}

		private double judgeRelative(Dosage dosage) {
			if (amount == null || amount.unit == null || amount.amount == null) return 0;
			double bestScore = 0;
			for (ExportDosage d : dosages) {
				bestScore = Math.max(bestScore, d.judgeRelative(dosage, this.amount));
			}
			return bestScore;
		}

		private double judgeAbsolute(Dosage dosage) {
			double drugMatch = 0;
			if (amount != null && amount.amount != null) {
				if (DosageMatchJudge.matchesAbsolute(amount.amount, null, dosage.amountNominator.number,
						BigDecimal.ZERO)
						&& Objects.equals(dosage.amountNominator.unit, amount.unit)) {
					drugMatch = DRUG_AMOUNT_MATCH_SCORE;
				}
			}

			double bestAmountMatch = 0;
			for (ExportDosage d : dosages) {
				if (d.matchesAbsolute(dosage)) {
					bestAmountMatch = ABSOLUTE_MATCH_SCORE;
					break;
				}
			}
			return Math.max(drugMatch, bestAmountMatch);
		}

	}

	private static class ExportAmount {

		private final BigDecimal amount;
		private final String unit;

		ExportAmount(Value value) {
			unit = value.get("unit", (String) null);
			amount = fromString(value.get("amount", (String) null));
		}

	}

	private static class ExportDosage {

		private static final BigDecimal EPSILON = new BigDecimal("0.01");

		private final BigDecimal amountFrom;
		private final BigDecimal amountTo;
		private final String unit;

		ExportDosage(Value value) {
			unit = value.get("unit", (String) null);
			amountFrom = fromString(value.get("amountFrom", (String) null));
			amountTo = fromString(value.get("amountTo", (String) null));
		}

		/**
		 * Returns whether the given dosage nominator matches this instance. If the given dosage does not provide a
		 * unit, then no unit comparison is performed, i.e. the unit is always considered to match. But if a unit is
		 * provided, it must match.
		 */
		private boolean matchesAbsolute(Dosage dosage) {
			Amount amount = dosage.amountNominator;
			if (!DosageMatchJudge.matchesAbsolute(amountFrom, amountTo, amount.number, BigDecimal.ZERO)) return false;

			if (amount.unit != null) {
				return amount.unit.equals(unit);
			}
			return true;
		}

		/**
		 * Judges whether the given dosage fraction matches this instance and the given drug. For example, if the drug
		 * is 3ml and this unit is 300mg, this would translate to 100mg/ml. If that is the value provided by the dosage
		 * (or sth else like 300mg/3ml), it is considered a match and the {@link #NORMALIZED_RELATIVE_MATCH_SCORE} is
		 * returned. The units must match perfectly in this case, unlike in the absolute matching.
		 * <p>
		 * If both the dosage of the drug and the given dosage match perfectly in nominator and denominator (e.g. if the
		 * drug is 300mg ingredient and 3ml ingredient and the given dosage has a nominator of exactly 300mg and a
		 * denominator of 3ml), the {@link #PERFECT_RELATIVE_MATCH_SCORE} is returned instead.
		 * <p>
		 * If no such match is achieved, zero is returned.
		 */
		private double judgeRelative(Dosage dosage, ExportAmount drugAmount) {
			if (!Objects.equals(dosage.amountDenominator.unit, drugAmount.unit)) return 0;
			if (!Objects.equals(dosage.amountNominator.unit, unit)) return 0;

			if (DosageMatchJudge.matchesAbsolute(amountFrom, amountTo, dosage.amountNominator.number, BigDecimal.ZERO)
					&& DosageMatchJudge.matchesAbsolute(drugAmount.amount, null, dosage.amountDenominator.number,
					BigDecimal.ZERO)) {
				return PERFECT_RELATIVE_MATCH_SCORE;
			}

			BigDecimal normalizedAmountLocalFrom = amountFrom.setScale(4, RoundingMode.UNNECESSARY)
			                                                 .divide(drugAmount.amount, RoundingMode.HALF_UP);
			BigDecimal normalizedAmountLocalTo = null;
			if (amountTo != null)
				normalizedAmountLocalTo = amountTo.setScale(4, RoundingMode.UNNECESSARY)
				                                  .divide(drugAmount.amount, RoundingMode.HALF_UP);
			BigDecimal normalizedAmountOther = dosage.amountNominator.number.setScale(4, RoundingMode.HALF_UP)
			                                                                .divide(dosage.amountDenominator.number,
					                                                                RoundingMode.HALF_UP);

			if (DosageMatchJudge.matchesAbsolute(normalizedAmountLocalFrom, normalizedAmountLocalTo,
					normalizedAmountOther, EPSILON)) {
				return NORMALIZED_RELATIVE_MATCH_SCORE;
			}
			return 0;
		}

	}

	private static boolean matchesAbsolute(BigDecimal from, BigDecimal to, BigDecimal target, BigDecimal delta) {
		if (target == null) return false;
		if (to == null) {
			return from.subtract(target).abs().compareTo(delta) <= 0;
		} else {
			return from.compareTo(target) <= 0 && to.compareTo(target) >= 0;
		}
	}

	private static BigDecimal fromString(String value) {
		if (value == null) return null;
		return new BigDecimal(value.replace(',', '.'));
	}


}
