package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.Transformer;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.TrimSpecialSuffixSymbols;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.WhitespaceTokenizer;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Query refiner capable of turning a {@link RawQuery} into a {@link SearchQuery}
 *
 * @author Markus Budeus
 */
public class QueryRefiner {

	// TODO Substance handling.

	/**
	 * The partial refiner which extracts and refines dosage information.
	 */
	private final DosageQueryRefiner dosageQueryRefiner;
	/**
	 * The partial refiner which extracts and refines dose form information.
	 */
	private final DoseFormQueryRefiner doseFormQueryRefiner;

	private final Transformer<String, List<String>> keywordExtractor =
			new WhitespaceTokenizer(true)
					.and(new TrimSpecialSuffixSymbols());

	public QueryRefiner(DosageQueryRefiner dosageQueryRefiner, DoseFormQueryRefiner doseFormQueryRefiner) {
		this.dosageQueryRefiner = dosageQueryRefiner;
		this.doseFormQueryRefiner = doseFormQueryRefiner;
	}

	/**
	 * Refines the given query. The returned {@link RefinedQuery} contains the {@link SearchQuery} to which the raw
	 * query was refined, as well as information about which parts of the raw query were used and for what.
	 * @param query the raw query to refine.
	 * @return a {@link RefinedQuery} holding the result {@link SearchQuery} and additional information
	 */
	public RefinedQuery refine(RawQuery query) {
		StepwiseGeneralQueryParser generalQueryParser = null;
		if (!query.query.isBlank()) {
			generalQueryParser = new StepwiseGeneralQueryParser(query.query);
		}

		SearchQuery.Builder searchQueryBuilder = new SearchQuery.Builder();

		SubstringUsageStatement dosageUsageStatement =
				applyPartialQueryRefiner(query.dosages, dosageQueryRefiner, searchQueryBuilder);
		SubstringUsageStatement dosageGeneralSearchTermUsageStatement =
				applyPartialQueryRefinerUsingStepwiseQueryParser(generalQueryParser, dosageQueryRefiner,
						searchQueryBuilder);
		SubstringUsageStatement doseFormUsageStatement =
				applyPartialQueryRefiner(query.doseForms, doseFormQueryRefiner, searchQueryBuilder);
		SubstringUsageStatement doseFormGeneralSearchTermUsageStatement =
				applyPartialQueryRefinerUsingStepwiseQueryParser(generalQueryParser, doseFormQueryRefiner,
						searchQueryBuilder);

		List<String> productKeywords = new ArrayList<>(extractKeywords(query.product));
		String remainingGeneralQueryParts = "";
		if (generalQueryParser != null) {
			remainingGeneralQueryParts = generalQueryParser.getQueryUsageStatement().getUnusedParts();
		}
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
	 * If the given query is not blank, it
	 * @param query
	 * @param refiner
	 * @param searchQueryBuilder
	 * @return
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

	public static class RefinedQuery {

		@NotNull
		private final SearchQuery searchQuery;
		@Nullable
		private final SubstringUsageStatement dosageUsageStatement;
		@Nullable
		private final SubstringUsageStatement dosageGeneralSearchTermUsageStatement;
		@Nullable
		private final SubstringUsageStatement doseFormUsageStatement;
		@Nullable
		private final SubstringUsageStatement doseFormGeneralSearchTermUsageStatement;

		private RefinedQuery(@NotNull SearchQuery searchQuery, @Nullable SubstringUsageStatement dosageUsageStatement,
		                     @Nullable SubstringUsageStatement dosageGeneralSearchTermUsageStatement,
		                     @Nullable SubstringUsageStatement doseFormUsageStatement,
		                     @Nullable SubstringUsageStatement doseFormGeneralSearchTermUsageStatement) {
			this.searchQuery = searchQuery;
			this.dosageUsageStatement = dosageUsageStatement;
			this.dosageGeneralSearchTermUsageStatement = dosageGeneralSearchTermUsageStatement;
			this.doseFormUsageStatement = doseFormUsageStatement;
			this.doseFormGeneralSearchTermUsageStatement = doseFormGeneralSearchTermUsageStatement;
		}

		public @NotNull SearchQuery getSearchQuery() {
			return searchQuery;
		}

		public @Nullable SubstringUsageStatement getDosageUsageStatement() {
			return dosageUsageStatement;
		}

		public @Nullable SubstringUsageStatement getDosageGeneralSearchTermUsageStatement() {
			return dosageGeneralSearchTermUsageStatement;
		}

		public @Nullable SubstringUsageStatement getDoseFormUsageStatement() {
			return doseFormUsageStatement;
		}

		public @Nullable SubstringUsageStatement getDoseFormGeneralSearchTermUsageStatement() {
			return doseFormGeneralSearchTermUsageStatement;
		}
	}

}
