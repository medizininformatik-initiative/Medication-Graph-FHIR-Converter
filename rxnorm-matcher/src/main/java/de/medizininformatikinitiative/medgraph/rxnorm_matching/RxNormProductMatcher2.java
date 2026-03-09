package de.medizininformatikinitiative.medgraph.rxnorm_matching;

import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphEdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.model.*;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model.ActiveIngredient;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model.Drug;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model.SimpleActiveIngredient;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.NormalizedStrength;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.UcumNormalizer;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.db.RxNormDatabase;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Amount;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Strength;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A re-implementation of the product matcher.
 *
 * @author Markus Budeus
 */
public class RxNormProductMatcher2 {

	private final Logger logger = LogManager.getLogger(RxNormProductMatcher2.class);

	// We allow a 5% threshold for strength differences
	private static final BigDecimal STRENGTH_DELTA = new BigDecimal("0.01");

	private final RxNormDatabase database;
	private final DoseFormMapper doseFormMapper;
	private final boolean enableProfiling;

	public RxNormProductMatcher2(RxNormDatabase database, DoseFormMapper doseFormMapper,
	                             boolean enableProfiling) {
		this.database = database;
		this.doseFormMapper = doseFormMapper;
		this.enableProfiling = enableProfiling;
	}

	/**
	 * Attempts to find an SCD match for the given {@link Drug}. Returns a {@link MatchResult} containing matched SCDs
	 * and counts of {@link ValidationResult} or an {@link EarlyMatchingFailure} indicating why this drug cannot be
	 * matched
	 */
	public MatchResult matchSCD(Drug drug) {
		// Perform early abort if dose form not matchable

		long t0 = System.currentTimeMillis();
		if (drug.edqmDoseForm() == null) {
			return new MatchResult(drug, EarlyMatchingFailure.NO_EDQM_DOSE_FORM);
		}
		if (doseFormMapper.getRxNormDoseForm(drug.edqmDoseForm().getName()) == null) {
			return new MatchResult(drug, EarlyMatchingFailure.NO_RXNORM_DOSE_FORM);
		}


		if (drug.activeIngredients().isEmpty()) {
			return new MatchResult(drug, EarlyMatchingFailure.NO_ACTIVE_INGREDIENT);
		}
		Set<String> ingredientRxCUIs = getActiveIngredientRxCUIs(drug);
		if (ingredientRxCUIs.isEmpty()) {
			return new MatchResult(drug, EarlyMatchingFailure.NO_RXCUI);
		}

		long t1 = System.currentTimeMillis();

		Set<String> scdCandidateRxCodes = database.getSCDRxCUIsForIngredientRxCUIs(ingredientRxCUIs);
		if (scdCandidateRxCodes.isEmpty()) {
			return new MatchResult(drug, EarlyMatchingFailure.NO_SCD_CANDIDATE);
		}

		long t2 = System.currentTimeMillis();

		Set<DetailedRxNormSCD> scdCandidates = database.resolveDetails(scdCandidateRxCodes);

		long t3 = System.currentTimeMillis();

		// Validate all candidates and collect results
		Map<ValidationResult, Integer> validationResultCounts = new HashMap<>();
		List<DetailedRxNormSCD> matches = new ArrayList<>();
		for (DetailedRxNormSCD candidate : scdCandidates) {
			ValidationResult result = validateCandidate(drug, candidate);
			validationResultCounts.compute(result, (k, v) -> v == null ? 1 : v + 1);
			if (result == ValidationResult.SUCCESS) {
				matches.add(candidate);
			}
		}

		long t4 = System.currentTimeMillis();

//		if (matches.size() > 1) {
//			reduceMatches(matches);
//			if (matches.isEmpty()) {
//				logger.log(Level.WARN, "All matches for drug "+drug+" were filtered out in post processing!");
//			}
//		}
//
//		long t5 = System.currentTimeMillis();

		if (enableProfiling) {
			System.out.println("Early drug rejection time: " + (t1 - t0) + "ms");
			System.out.println("Candidate search time: " + (t2 - t1) + "ms");
			System.out.println("Candidate details lookup time: " + (t3 - t2) + "ms");
			System.out.println("Validation time: " + (t4 - t3) + "ms");
//			System.out.println("Match filtering time: " + (t5 - t4) + "ms");
		}
		return new MatchResult(drug, matches, validationResultCounts);
	}

	/**
	 * Validates whether the given drug matches the given RxNorm SCD candidate in terms of dose form, ingredients, and
	 * strengths.
	 */
	private ValidationResult validateCandidate(Drug drug, DetailedRxNormSCD candidate) {
		// Dose Form Matching
		GraphEdqmPharmaceuticalDoseForm doseForm = drug.edqmDoseForm();
		if (doseForm == null) return ValidationResult.DOSE_FORM_MISMATCH;
		if (!validateDoseForm(doseForm, candidate.getDoseForm().getName())) return ValidationResult.DOSE_FORM_MISMATCH;

		// Ingredients matching
		// Check ingredient numbers
		if (candidate.getComponents().size() != drug.activeIngredients().size()) {
			return ValidationResult.INGREDIENTS_MISMATCH;
		}


		// The map below shall contain all ingredients in MMI Pharmindex which have the same RxCUI as the canonical
		// ingredient in RxNorm, respectively.
		Map<RxNormSCDC, List<IngredientByRxcui>> ingredientsToCompare = new HashMap<>();
		for (RxNormSCDCWithIngredients rxNormIngredient : candidate.getComponents()) {
			// For each SCDC, acquire the canonical ingredient.
			// E.g. if the SCDC is "metoclopramide hydrochloride 5 MG/ML", then the canonical ingredient is
			// "metoclopramide hydrochloride" (and NOT "metoclopramide"!)
			RxNormIngredient canonicalIngredient = rxNormIngredient.getCanonicalIngredient();
			// Get all ingredients and corresponding ingredients from the knowledge graph which carry the same
			// RXCUI as the canonical ingredient. For the MCP example, this would be
			// - 10.54mg Metoclopramid hydrochlord-1-Wasser
			// - 10mg Metoclopramid hydrochlorid
			// because both have the RXCUI 267036 in the knowledge graph.
			List<IngredientByRxcui> toCompare = findIngredientsByRxcui(drug, canonicalIngredient.getRxcui());
			// If no ingredient has that RXCUI, our drug does not match the RxNorm candidate (or is missing an RXCUI)
			if (toCompare.isEmpty()) {
				return ValidationResult.INGREDIENTS_MISMATCH;
			}
			ingredientsToCompare.put(rxNormIngredient, toCompare);
		}

		// Verify that all base ingredients in the MMI Pharmindex have an RxNorm SCDC partner.
		for (ActiveIngredient ingredient : drug.activeIngredients()) {
			boolean found = false;
			for (Map.Entry<RxNormSCDC, List<IngredientByRxcui>> entry : ingredientsToCompare.entrySet()) {
				// We assume all elements in the list have the same base ingredient. Otherwise, findIngredientsByRxcui()
				// throws an exception anyway.
				if (entry.getValue().getFirst().baseIngredient == ingredient) {
					found = true;
					break;
				}
			}
			if (!found) {
				return ValidationResult.INGREDIENTS_MISMATCH;
			}
		}

		// Verify Strengths
		NormalizedStrength drugStrength = UcumNormalizer.normalize(drug.amount(), null, drug.getUnitName());
		// Loop over all ingredient pairs. All of them must match by strength.
		for (Map.Entry<RxNormSCDC, List<IngredientByRxcui>> entry : ingredientsToCompare.entrySet()) {
			// First, caluclate the normalized strength for each ingredient.
			// E.g. if the Drug is 10ml and the Ingredient is 100mg, the Strengh object for that ingredient
			// will say: 100mg absolute strength and 10mg/ml relative strength.

			// Notable Caveat: This will not work properly in some cases where the ingredient has a relative strength.
			// For example, there is the drug with mmiId 891379, which has 1.2mg/ml MCP-hydrochloride. The drug lists
			// 1ml base amount. This yields 1.2mg absolute ingredient amount, but this is inaccurate, because the
			// formulation only exists as 30ml and 100ml packages.
			// In that case, if RxNorm provides SCDs with absolute ingredient strength, each package would require
			// a different SCD match (once 36mg and once 120mg). If RxNorm provides SCDs with relative strength (which
			// in this specific case, it does as far as I have seen, it still works, because the relative strength
			// applies correctly to either case.
			// So in short: Relative Strength in the MMI Pharmindex vs Absolute Strength in RxNorm can lead to matching
			// failures.
			List<IngredientWithStrength> list = entry.getValue()
			                                         .stream()
			                                         .map(IngredientByRxcui::specificIngredient)
			                                         .map(i -> mapToIngredientWithStrength(i, drugStrength))
			                                         .filter(Objects::nonNull) // Remove nulls, those occur if an active ingredient lacks a strength
			                                         .toList();

			// Also normalize the strength of the SCDC candidate
			RxNormSCDCWithStrength rxNormSCDCWithStrength = mapToSCDCWithStrength(
					entry.getKey(), candidate.getDrugAmount()
			);
			// Then, simply check if ANY of the given applicable ingredients
			// (e.g. "10mg Metoclopramid hydrochlorid" and "10.54mg Metoclopramid hydrochlorid-1-Wasser")
			// has the same strength as the RxNorm SCDC. (e.g. "metoclopramide 10 MG")
			if (!validateStrength(rxNormSCDCWithStrength, list)) {
				// If no entry matches, we discard the whole match.
				return ValidationResult.STRENGTH_MISMATCH;
			}
		}

		return ValidationResult.SUCCESS;

	}

//	private void reduceMatches(List<DetailedRxNormSCD> matches) {
//		for (int i = matches.size() - 1; i >= 0; i--) {
//			DetailedRxNormSCD match = matches.get(i);
//			String[] nameTokens = match.getName().split(" ", 4);
//			if (nameTokens.length < 4) continue;
//			if (
//					(nameTokens[0].equals("BX") && nameTokens[1].equals("Rating")) ||
//							(nameTokens[1].equals("HR")) ||
//							(nameTokens[0].equals("Sensor")) ||
//							(nameTokens[0].equals("Hyponatremia"))
//			) {
//				matches.remove(i);
//			}
//		}
//	}

	private boolean validateDoseForm(GraphEdqmPharmaceuticalDoseForm doseForm, String rxNormDoseForm) {
		return rxNormDoseForm.equals(doseFormMapper.getRxNormDoseForm(doseForm.getName()));
	}

	/**
	 * Searches all active ingredients and their correspondences for ingredients carrying the given RXCUI. Returns all
	 * matching ingredients and the base active ingredients they belong to. Usually, only a single Ingredient should be
	 * returned. But in some cases, it can happen that multiple corresponding ingredients are annotated with the same
	 * RxCUI (e.g. midazolam and midazolam hydrochlorid).
	 */
	private List<IngredientByRxcui> findIngredientsByRxcui(Drug drug, String rxcui) {
		List<IngredientByRxcui> ingredients = new ArrayList<>(3);
		ActiveIngredient chosenBase = null;
		for (ActiveIngredient base : drug.activeIngredients()) {
			boolean ingredientAdded = false;
			if (base.getRxcuiCodes().contains(rxcui)) {
				ingredients.add(new IngredientByRxcui(base, base));
				ingredientAdded = true;
			}
			for (SimpleActiveIngredient correspondence : base.getCorrespondingIngredients()) {
				if (correspondence.getRxcuiCodes().contains(rxcui)) {
					ingredients.add(new IngredientByRxcui(base, correspondence));
					ingredientAdded = true;
				}
			}

			if (ingredientAdded) {
				if (chosenBase != null) {
					// We assume multiple ingredients for one RXCUI only works if all belong to the same base ingredient (IN)
					// If not, throw! That's not a case we handle!
					logger.log(Level.ERROR, "Two distinct ingredients from the same drug ("
							+ base.getSubstanceName() + ", " + chosenBase.getSubstanceName()
							+ ") are mapped to the same RXCUI " + rxcui +
							"! This ingredient is excluded from the mapping, causing the mapping to fail.");
					return Collections.emptyList();
				} else {
					chosenBase = base;
				}
			}
		}
		return ingredients;
	}

	/**
	 * Returns true if the strength of the given SCDC matches AT LEAST one of the given candidates' strenghts.
	 */
	private boolean validateStrength(RxNormSCDCWithStrength scdc, List<IngredientWithStrength> candidates) {
		// For each candidate,
		// (e.g. "10mg Metoclopramid hydrochlorid" and "10.54mg Metoclopramid hydrochlorid-1-Wasser")
		// check whether the strength matches the strength of the RxNorm SCDC. If any strength matches, we return true.
		for (IngredientWithStrength candidate : candidates) {
			if (candidate.strength.matchesAbsoluteOrRelative(scdc.strength, STRENGTH_DELTA)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This function builds a {@link IngredientWithStrength} object by using an ingredient's own strength and the
	 * reference strength of its drug to calculate both the absolute (e.g. 100mg) and the relative (e.g. 10mg/ml)
	 * ingredient strength.
	 */
	@Nullable
	private IngredientWithStrength mapToIngredientWithStrength(SimpleActiveIngredient ingredient,
	                                                           NormalizedStrength drugStrength) {
		if (ingredient.getMassFrom() == null) return null;
		NormalizedStrength ingredientStrength = UcumNormalizer.normalize(ingredient.getMassFrom(),
				ingredient.getMassTo(),
				ingredient.getUnitName());

		return new IngredientWithStrength(
				ingredient.getSubstanceName(),
				Strength.fromNormalizedIngredientAndDrugStrength(ingredientStrength, drugStrength)
		);
	}

	/**
	 * Maps an {@link RxNormSCDC} to an {@link RxNormSCDCWithStrength}
	 *
	 * @param rxNormSCDC The RxNormSCDC to normalize.
	 * @param scdAmount  If an amount is given for the parent SCD which to use as base, that amount. Otherwise, null.
	 */
	private RxNormSCDCWithStrength mapToSCDCWithStrength(RxNormSCDC rxNormSCDC, Amount scdAmount) {
		// Normalize Units Convert "100 MG" to "100 mg"
		NormalizedStrength normalizedStrength = UcumNormalizer.normalize(
				rxNormSCDC.getAmount(),
				null,
				rxNormSCDC.getUnit());
		Strength strength;
		if (scdAmount != null) {
			NormalizedStrength drugStrength = UcumNormalizer.normalize(
					scdAmount.getNumber(),
					null,
					scdAmount.getUnit().toString()
			);
			strength = Strength.fromNormalizedIngredientAndDrugStrength(
					normalizedStrength, drugStrength
			);
		} else {
			strength = Strength.fromNormalizedStrength(normalizedStrength);
		}
		return new RxNormSCDCWithStrength(
				rxNormSCDC,
				strength
		);
	}

	/**
	 * Returns all RXCUIs of the active ingredients of the given drug. Also includes RxCUIs of corresponding
	 * ingredients. E.g. if a medication contains - 5mg midazolam: RXCUI 6960 - corresponds to 5.56 mg midazolam
	 * hydrochloride: RXCUI 203128 - 1.2mg metoclopramid-hydrochlorid: RXCUI 267036 - corresponds to 1mg metoclopramid:
	 * RXCUI 6915 then this returns [6960, 203128, 267036, 6915]
	 */
	private Set<String> getActiveIngredientRxCUIs(Drug drug) {
		return drug.activeIngredients()
		           .stream()
		           .flatMap(baseIngredient -> {
			           List<SimpleActiveIngredient> corresponding = baseIngredient.getCorrespondingIngredients();
			           List<SimpleActiveIngredient> list = new ArrayList<>(corresponding.size() + 1);
			           list.add(baseIngredient);
			           list.addAll(corresponding);
			           return list.stream();
		           })
		           .flatMap(i -> i.getRxcuiCodes().stream())
		           .collect(Collectors.toSet());
	}

	/**
	 * Represents an ingredient from the knowledge graph that has been resolved through its RXCUI, though the RXCUI is
	 * not actually specified here.
	 *
	 * @param specificIngredient The specific ingredient that carried the substance RXCUI we searched for before.
	 * @param baseIngredient     The base ingredient the specific ingredient is assigned to. The base ingredient is
	 *                           always based on a :MmiIngredient node in the knowledge graph and is the ingredient
	 *                           which is directly linked to the Drug. The specific ingredient can be an ingredient that
	 *                           is only indirectly linked via :CORRESPONDS-TO, but it can also be the :MmiIngredient
	 *                           itself. In the latter case, specificIngredient and baseIngredient are the exact same
	 *                           object.
	 */
	private record IngredientByRxcui(
			ActiveIngredient baseIngredient,
			SimpleActiveIngredient specificIngredient
	) {
	}


	/**
	 * Represents strength ínformation of an ingredient.
	 *
	 * @param substanceName The substance name of the ingredient, just for reference when logging.
	 * @param strength      The ingredient strength.
	 */
	private record IngredientWithStrength(
			String substanceName,
			Strength strength
	) {

		@Override
		public @NotNull String toString() {
			return substanceName + " " + strength;
		}
	}

	public static class MatchResult {

		private final Drug drug;

		private final List<DetailedRxNormSCD> matches = new ArrayList<>();

		@Nullable
		private final EarlyMatchingFailure earlyMatchingFailure;
		@NotNull
		private final Map<ValidationResult, Integer> validationResultCounts = new HashMap<>();

		MatchResult(Drug drug, List<DetailedRxNormSCD> matches, Map<ValidationResult, Integer> validationResultCounts) {
			this.drug = drug;
			this.matches.addAll(matches);
			this.validationResultCounts.putAll(validationResultCounts);
			earlyMatchingFailure = null;
		}

		MatchResult(Drug drug, @NotNull EarlyMatchingFailure earlyMatchingFailure) {
			this.drug = drug;
			this.earlyMatchingFailure = earlyMatchingFailure;
		}

		int getNumberOfValidationResults(ValidationResult type) {
			return validationResultCounts.getOrDefault(type, 0);
		}

		public List<DetailedRxNormSCD> getMatches() {
			return matches;
		}

		public @Nullable EarlyMatchingFailure getEarlyMatchingFailure() {
			return earlyMatchingFailure;
		}

		public Drug getDrug() {
			return drug;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(drug.getDetailedInfo());
			if (earlyMatchingFailure != null) {
				builder.append(" EARLY MATCHING FAILURE: ").append(earlyMatchingFailure);
			} else {
				builder.append(" Candidate Results: ")
				       .append(getNumberOfValidationResults(ValidationResult.SUCCESS))
				       .append(" successful, ")
				       .append(getNumberOfValidationResults(ValidationResult.DOSE_FORM_MISMATCH))
				       .append(" dose form mismatches, ")
				       .append(getNumberOfValidationResults(ValidationResult.INGREDIENTS_MISMATCH))
				       .append(" ingredients mismatches, ")
				       .append(getNumberOfValidationResults(ValidationResult.STRENGTH_MISMATCH))
				       .append(" strength mismatches");
				if (!matches.isEmpty()) {
					builder.append("\nMatches:");
					for (DetailedRxNormSCD match : matches) {
						builder.append("\n- ").append(match.toString());
					}
				}
			}

			return builder.toString();
		}

	}

	/**
	 * An RxNorm SCDC with precalculated strength.
	 */
	private record RxNormSCDCWithStrength(
			RxNormSCDC scdc,
			Strength strength
	) {
	}

	public enum EarlyMatchingFailure {
		NO_ACTIVE_INGREDIENT,
		NO_RXCUI,
		NO_EDQM_DOSE_FORM,
		NO_RXNORM_DOSE_FORM,
		NO_SCD_CANDIDATE
	}

	public enum ValidationResult {
		DOSE_FORM_MISMATCH,
		INGREDIENTS_MISMATCH,
		STRENGTH_MISMATCH,
		SUCCESS,
	}

}
