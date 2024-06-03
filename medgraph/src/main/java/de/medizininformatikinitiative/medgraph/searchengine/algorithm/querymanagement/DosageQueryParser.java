package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.tools.DosageDetector;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.InputUsageTraceable;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extracts dosage information from a a human-written query string.
 *
 * @author Markus Budeus
 */
public class DosageQueryParser {

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

	public static class Result implements InputUsageTraceable<DistinctMultiSubstringUsageStatement> {
		private final List<Dosage> dosages;
		private final List<Amount> amounts;
		private final DistinctMultiSubstringUsageStatement usageStatement;

		private Result(List<Dosage> dosages, List<Amount> amounts,
		              DistinctMultiSubstringUsageStatement usageStatement) {
			this.dosages = dosages;
			this.amounts = amounts;
			this.usageStatement = usageStatement;
		}

		@Override
		public DistinctMultiSubstringUsageStatement getUsageStatement() {
			return usageStatement;
		}

		public List<Dosage> getDosages() {
			return dosages;
		}

		public List<Amount> getAmounts() {
			return amounts;
		}
	}

}
