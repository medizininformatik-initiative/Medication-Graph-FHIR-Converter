package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.*;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Query refiner capable of turning a {@link RawQuery} into a {@link SearchQuery}
 *
 * @author Markus Budeus
 */
public class QueryRefinerImpl implements QueryRefiner {

	/**
	 * The partial refiner which extracts and refines dosage information.
	 */
	private final DosageQueryRefiner dosageQueryRefiner;
	/**
	 * The partial refiner which extracts and refines dose form information.
	 */
	private final DoseFormQueryRefiner doseFormQueryRefiner;
	/**
	 * The partial refiner which resolves substances based on the query.
	 */
	private final SubstanceQueryRefiner substanceQueryRefiner;

	private final Transformer<String, List<String>> keywordExtractor =
			new WhitespaceTokenizer(true)
					.and(new TrimSpecialSuffixSymbols())
					.and(new RemoveBlankStrings())
					.and(new MinimumTokenLength(2));

	public QueryRefinerImpl(DosageQueryRefiner dosageQueryRefiner, DoseFormQueryRefiner doseFormQueryRefiner,
	                        SubstanceQueryRefiner substanceQueryRefiner) {
		this.dosageQueryRefiner = dosageQueryRefiner;
		this.doseFormQueryRefiner = doseFormQueryRefiner;
		this.substanceQueryRefiner = substanceQueryRefiner;
	}

	public RefinedQuery refine(RawQuery query) {
		StepwiseGeneralQueryParser generalQueryParser = null;
		if (!query.query.isBlank()) {
			generalQueryParser = new StepwiseGeneralQueryParser(query.query);
		}

		SearchQuery.Builder searchQueryBuilder = new SearchQuery.Builder();

		// Dosages
		SubstringUsageStatement dosageUsageStatement =
				applyPartialQueryRefiner(query.dosages, dosageQueryRefiner, searchQueryBuilder);
		SubstringUsageStatement dosageGeneralSearchTermUsageStatement =
				applyPartialQueryRefinerUsingStepwiseQueryParser(generalQueryParser, dosageQueryRefiner,
						searchQueryBuilder);

		// Dose Forms
		SubstringUsageStatement doseFormUsageStatement =
				applyPartialQueryRefiner(query.doseForms, doseFormQueryRefiner, searchQueryBuilder);
		SubstringUsageStatement doseFormGeneralSearchTermUsageStatement =
				applyPartialQueryRefinerUsingStepwiseQueryParser(generalQueryParser, doseFormQueryRefiner,
						searchQueryBuilder);

		// Preparation for Substances and Products, as those both use whatever remains from the general query
		String remainingGeneralQueryParts = "";
		if (generalQueryParser != null) {
			remainingGeneralQueryParts = generalQueryParser.getQueryUsageStatement().getUnusedParts();
		}

		// Substances
		applyPartialQueryRefiner(remainingGeneralQueryParts, substanceQueryRefiner, searchQueryBuilder);
		applyPartialQueryRefiner(query.substance, substanceQueryRefiner, searchQueryBuilder);

		// Product keywords
		List<String> productKeywords = new ArrayList<>(extractKeywords(query.product));
		productKeywords.addAll(extractKeywords(remainingGeneralQueryParts));
		searchQueryBuilder.withProductNameKeywords(productKeywords);

		return new RefinedQuery(
				searchQueryBuilder.build(),
				dosageUsageStatement,
				dosageGeneralSearchTermUsageStatement,
				doseFormUsageStatement,
				doseFormGeneralSearchTermUsageStatement
		);
	}

	/**
	 * Like {@link #applyPartialQueryRefiner(String, PartialQueryRefiner, SearchQuery.Builder)}, but takes the query to
	 * use from the generalQueryParser (via {@link StepwiseGeneralQueryParser#useRemainingQueryParts(Function)}).
	 * <p>
	 * If the generalQueryParser is null, nothing happens and null is returned.
	 */
	private SubstringUsageStatement applyPartialQueryRefinerUsingStepwiseQueryParser(
			StepwiseGeneralQueryParser generalQueryParser, PartialQueryRefiner<?> refiner,
			SearchQuery.Builder searchQueryBuilder
	) {
		if (generalQueryParser != null) {
			return generalQueryParser.useRemainingQueryParts(
					query -> applyPartialQueryRefiner(query, refiner, searchQueryBuilder));
		}
		return null;
	}

	/**
	 * If the given query is not blank, it is passed to the given refiner, whose result is then applied to the given
	 * search query builder. Finally, the usage statement produced by the refiner is returned.
	 * <p>
	 * If the given query is blank, nothing happens and null is returned.
	 */
	private SubstringUsageStatement applyPartialQueryRefiner(String query, PartialQueryRefiner<?> refiner,
	                                                         SearchQuery.Builder searchQueryBuilder) {
		if (query != null && !query.isBlank()) {
			PartialQueryRefiner.Result result = refiner.parse(query);
			result.incrementallyApply(searchQueryBuilder);
			return result.getUsageStatement();
		}
		return null;
	}

	private List<String> extractKeywords(String query) {
		return keywordExtractor.apply(query);
	}

}
