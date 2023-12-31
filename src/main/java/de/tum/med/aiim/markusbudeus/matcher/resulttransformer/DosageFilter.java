package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
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

public class DosageFilter implements Filter {

	private final Session session;

	public DosageFilter(Session session) {
		this.session = session;
	}

	@Override
	public List<Boolean> batchPassesFilter(List<MatchingTarget> targets, HouselistEntry entry) {
		Map<Long, MatchingTarget> targetIds = new HashMap<>(targets.size());
		for (MatchingTarget t : targets) {
			if (t.getType() == MatchingTarget.Type.PRODUCT)
				targetIds.put(t.getMmiId(), t);
		}

		Result result = queryEffectiveDosagesForProductIds(new ArrayList<>(targetIds.keySet()));

		List<Boolean> resultList = new ArrayList<>(targets.size());
		for (int i = 0; i < targets.size(); i++) {
			resultList.add(false);
		}

		while (result.hasNext()) {
			Record record = result.next();
			long productId = record.get(0).asLong();
			MatchingTarget target = targetIds.get(productId);
			if (target != null && passesFilter(record, entry)) {
				resultList.set(targets.indexOf(target), true);
			}
		}

		return resultList;
	}

	@Override
	public boolean passesFilter(MatchingTarget target, HouselistEntry entry) {
		if (target.getType() != MatchingTarget.Type.PRODUCT) return false;

		Result result = queryEffectiveDosagesForProductIds(List.of(target.getMmiId()));
		if (!result.hasNext()) return false;
		return passesFilter(result.next(), entry);
	}

	private boolean passesFilter(Record record, HouselistEntry entry) {
		if (entry.activeIngredientDosages == null) return false;
		List<Dosage> unmatchedDosages = new ArrayList<>(entry.activeIngredientDosages);
		List<ExportDrugDosage> drugDosages = parse(record);
		for (Dosage d : unmatchedDosages) {
			if (!hasMatch(drugDosages, d)) return false;
		}
		return true;
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

	private boolean hasMatch(List<ExportDrugDosage> drugDosages, Dosage dosage) {
		for (ExportDrugDosage drugDosage : drugDosages) {
			if (drugDosage.matchesDosage(dosage))
				return true;
		}
		return false;
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

		public boolean matchesDosage(Dosage dosage) {
			if (dosage.amountDenominator != null) {
				return matchesRelative(dosage);
			} else {
				return matchesAbsolute(dosage);
			}
		}

		private boolean matchesRelative(Dosage dosage) {
			if (amount == null || amount.unit == null || amount.amount == null) return false;
			for (ExportDosage d : dosages) {
				if (d.matchesRelative(dosage, this.amount))
					return true;
			}
			return false;
		}

		private boolean matchesAbsolute(Dosage dosage) {
			for (ExportDosage d : dosages) {
				if (d.matchesAbsolute(dosage))
					return true;
			}
			// If it doesn't match the ingredients, it may still match the total drug amount,
			// but only if a unit is given!
			if (amount != null && amount.amount != null) {
				if (!DosageFilter.matchesAbsolute(amount.amount, null, dosage.amountNominator.number,
						BigDecimal.ZERO)) return false;
				return Objects.equals(dosage.amountNominator.unit, amount.unit);
			}
			return false;
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
			if (!DosageFilter.matchesAbsolute(amountFrom, amountTo, amount.number, BigDecimal.ZERO)) return false;

			if (amount.unit != null) {
				return amount.unit.equals(unit);
			}
			return true;
		}

		/**
		 * Returns whether the given dosage fraction matches this instance and the given drug. For example, if the drug
		 * is 3ml and this unit is 300mg, this would translate to 100mg/ml. If that is the value provided by the dosage
		 * (or sth else like 300mg/3ml), it is considered a match. The units must match perfectly in this case, unlike
		 * in the absolute matching.
		 */
		private boolean matchesRelative(Dosage dosage, ExportAmount drugAmount) {
			if (!Objects.equals(dosage.amountDenominator.unit, drugAmount.unit)) return false;
			if (!Objects.equals(dosage.amountNominator.unit, unit)) return false;

			BigDecimal normalizedAmountLocalFrom = amountFrom.setScale(4, RoundingMode.UNNECESSARY)
			                                                 .divide(drugAmount.amount, RoundingMode.HALF_UP);
			BigDecimal normalizedAmountLocalTo = null;
			if (amountTo != null)
				normalizedAmountLocalTo = amountTo.setScale(4, RoundingMode.UNNECESSARY)
				                                  .divide(drugAmount.amount, RoundingMode.HALF_UP);
			BigDecimal normalizedAmountOther = dosage.amountNominator.number.setScale(4, RoundingMode.HALF_UP)
			                                                                .divide(dosage.amountDenominator.number,
					                                                                RoundingMode.HALF_UP);

			return DosageFilter.matchesAbsolute(normalizedAmountLocalFrom, normalizedAmountLocalTo,
					normalizedAmountOther, EPSILON);
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
