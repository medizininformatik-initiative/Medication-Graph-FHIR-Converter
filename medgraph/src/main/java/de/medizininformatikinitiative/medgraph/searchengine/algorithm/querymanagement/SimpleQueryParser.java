package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.*;
import de.medizininformatikinitiative.medgraph.searchengine.tools.DosageDetector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Implementation for {@link QueryParser}.
 *
 * @author Markus Budeus
 */
public class SimpleQueryParser implements QueryParser {

	private final RemoveDosageInformation dosageInfoRemover = new RemoveDosageInformation();
	private final Transformer<String, List<String>> tokenizer =
			new WhitespaceTokenizer()
					.and(new TrimSpecialSuffixSymbols())
					.and(new RemoveBlankStrings());

	@Override
	public @NotNull SearchQuery parse(@NotNull RawQuery query) {
		List<String> queryTokens = dosageInfoRemover.and(tokenizer).apply(query.query);

		List<String> productTokens = tokenizer.apply(query.product);
		productTokens.addAll(queryTokens);
		productTokens = productTokens.stream().distinct().toList();

		List<String> substanceTokens = tokenizer.apply(query.substance);
		substanceTokens.addAll(queryTokens);
		substanceTokens = substanceTokens.stream().distinct().toList();

		List<DosageDetector.DetectedDosage> detectedDosages = DosageDetector.detectDosages(query.query);

		List<Dosage> searchDosages = new ArrayList<>();
		List<Amount> searchAmounts = new ArrayList<>();

		for (DosageDetector.DetectedDosage dd: detectedDosages) {
			Dosage dosage = dd.getDosage();
			searchDosages.add(dosage);
			if (dosage.amountDenominator == null)
				searchAmounts.add(dosage.amountNominator);
		}

		return new SearchQuery(
				concatenateWithWhitespaceOrNullIfEmpty(productTokens),
				concatenateWithWhitespaceOrNullIfEmpty(substanceTokens),
				searchDosages,
				searchAmounts
				);
	}

	private String concatenateWithWhitespaceOrNullIfEmpty(List<String> elements) {
		if (elements.isEmpty()) return null;
		return String.join(" ", elements.toArray(new String[0]));
	}

}
