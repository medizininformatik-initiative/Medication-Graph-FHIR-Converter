package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.tools.DosageDetector;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extracts dosage information from a human-written query string.
 *
 * @author Markus Budeus
 */
public class DosageQueryRefiner implements PartialQueryRefiner<DosageQueryRefiner.Result> {

	/**
	 * Extracts dosage information from the given query string.
	 *
	 * @param query the query string from which to extract information
	 * @return a {@link Result}-object providing the detected dosages and amounts as well as information on where in the
	 * query string it was found
	 */
	public Result parse(String query) {
		List<DosageDetector.DetectedDosage> detectedDosages = DosageDetector.detectDosages(query);

		List<Dosage> searchDosages = new ArrayList<>();
		List<Amount> searchAmounts = new ArrayList<>();


		Set<IntRange> usedRanges = new HashSet<>();
		for (DosageDetector.DetectedDosage dd : detectedDosages) {
			Dosage dosage = dd.getDosage();
			searchDosages.add(dosage);
			usedRanges.add(new IntRange(dd.getStartIndex(), dd.getStartIndex() + dd.getLength()));
			if (dosage.amountDenominator == null)
				searchAmounts.add(dosage.amountNominator);
		}

		return new Result(searchDosages, searchAmounts, new DistinctMultiSubstringUsageStatement(query, usedRanges));
	}

	public static class Result implements PartialQueryRefiner.Result {
		@NotNull
		private final List<Dosage> dosages;
		@NotNull
		private final List<Amount> amounts;
		@NotNull
		private final DistinctMultiSubstringUsageStatement usageStatement;

		private Result(@NotNull List<Dosage> dosages, @NotNull List<Amount> amounts,
		               @NotNull DistinctMultiSubstringUsageStatement usageStatement) {
			this.dosages = dosages;
			this.amounts = amounts;
			this.usageStatement = usageStatement;
		}

		@Override
		@NotNull
		public DistinctMultiSubstringUsageStatement getUsageStatement() {
			return usageStatement;
		}

		@NotNull
		public List<Dosage> getDosages() {
			return dosages;
		}

		@NotNull
		public List<Amount> getAmounts() {
			return amounts;
		}

		@Override
		public void incrementallyApply(SearchQuery.Builder searchQueryBuilder) {
			searchQueryBuilder.withActiveIngredientDosages(dosages)
					.withDrugAmounts(amounts);
		}
	}

}
