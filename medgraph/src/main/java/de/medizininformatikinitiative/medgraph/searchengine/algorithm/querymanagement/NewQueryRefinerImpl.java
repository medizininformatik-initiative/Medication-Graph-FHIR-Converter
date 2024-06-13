package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.*;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Query refiner capable of turning a {@link RawQuery} into a {@link NewRefinedQuery}.
 *
 * @author Markus Budeus
 */
public class NewQueryRefinerImpl implements NewQueryRefiner {

	/**
	 * The partial refiner which extracts and refines dosage information.
	 */
	private final NewDosageQueryRefiner dosageQueryRefiner;
	/**
	 * The partial refiner which extracts and refines dose form information.
	 */
	private final NewDoseFormQueryRefiner doseFormQueryRefiner;
	/**
	 * The partial refiner which resolves substances based on the query.
	 */
	private final NewSubstanceQueryRefiner substanceQueryRefiner;

	private final Transformer<String, List<String>> keywordExtractor =
			new WhitespaceTokenizer(true)
					.and(new TrimSpecialSuffixSymbols())
					.and(new RemoveBlankStrings())
					.and(new MinimumTokenLength(2));

	public NewQueryRefinerImpl(NewDosageQueryRefiner dosageQueryRefiner, NewDoseFormQueryRefiner doseFormQueryRefiner,
	                           NewSubstanceQueryRefiner substanceQueryRefiner) {
		this.dosageQueryRefiner = dosageQueryRefiner;
		this.doseFormQueryRefiner = doseFormQueryRefiner;
		this.substanceQueryRefiner = substanceQueryRefiner;
	}

	public NewRefinedQuery refine(RawQuery query) {
		StepwiseGeneralQueryParser generalQueryParser = null;
		if (!query.query.isBlank()) {
			generalQueryParser = new StepwiseGeneralQueryParser(query.query);
		}

		NewRefinedQuery.Builder queryBuilder = new NewRefinedQuery.Builder();

		// Dosages
		SubstringUsageStatement dosageUsageStatement =
				applyPartialQueryRefiner(wrap(query.dosages), dosageQueryRefiner, queryBuilder);
		SubstringUsageStatement dosageGeneralSearchTermUsageStatement =
				applyPartialQueryRefinerUsingStepwiseQueryParser(generalQueryParser, dosageQueryRefiner,
						queryBuilder);

		// Dose Forms
		SubstringUsageStatement doseFormUsageStatement =
				applyPartialQueryRefiner(wrap(query.doseForms), doseFormQueryRefiner, queryBuilder);
		SubstringUsageStatement doseFormGeneralSearchTermUsageStatement =
				applyPartialQueryRefinerUsingStepwiseQueryParser(generalQueryParser, doseFormQueryRefiner,
						queryBuilder);

		// Preparation for Substances and Products, as those both use whatever remains from the general query
		String remainingGeneralQueryParts = "";
		if (generalQueryParser != null) {
			remainingGeneralQueryParts = generalQueryParser.getQueryUsageStatement().getUnusedParts();
		}

		// Substances
		SubstringUsageStatement substanceUsageStatement =
				applyPartialQueryRefiner(wrap(query.substance), substanceQueryRefiner, queryBuilder);
		SubstringUsageStatement substanceGeneralSearchTermUsageStatement =
				applyPartialQueryRefiner(wrap(remainingGeneralQueryParts), substanceQueryRefiner, queryBuilder);

		// Product keywords
		List<String> productKeywords = new ArrayList<>(extractKeywords(query.product));
		productKeywords.addAll(extractKeywords(remainingGeneralQueryParts));
		// TODO Don't just make an OriginalIdentifier and cite RAW_QUERY as source.
		//  Make clear the general query was reduced part by part.
		queryBuilder.withProductNameKeywords(
				new OriginalIdentifier<>(productKeywords, OriginalIdentifier.Source.RAW_QUERY));

		return queryBuilder
				.withUsageStatements(
						dosageUsageStatement,
						dosageGeneralSearchTermUsageStatement,
						doseFormUsageStatement,
						doseFormGeneralSearchTermUsageStatement,
						substanceUsageStatement,
						substanceGeneralSearchTermUsageStatement
				).build();
	}

	/**
	 * Like {@link #applyPartialQueryRefiner(Identifier, NewPartialQueryRefiner, NewRefinedQuery.Builder)}, but takes
	 * the query to use from the generalQueryParser (via
	 * {@link StepwiseGeneralQueryParser#useRemainingQueryParts(Function)}).
	 * <p>
	 * If the generalQueryParser is null, nothing happens and null is returned.
	 */
	private SubstringUsageStatement applyPartialQueryRefinerUsingStepwiseQueryParser(
			StepwiseGeneralQueryParser generalQueryParser, NewPartialQueryRefiner<?> refiner,
			NewRefinedQuery.Builder queryBuilder
	) {
		if (generalQueryParser != null) {
			return generalQueryParser.useRemainingQueryParts(
					query -> applyPartialQueryRefiner(wrap(query), refiner, queryBuilder));
		}
		return null;
	}

	/**
	 * If the given query is not blank, it is passed to the given refiner, whose result is then applied to the given
	 * search query builder. Finally, the usage statement produced by the refiner is returned.
	 * <p>
	 * If the given query is blank, nothing happens and null is returned.
	 */
	private SubstringUsageStatement applyPartialQueryRefiner(Identifier<String> query,
	                                                         NewPartialQueryRefiner<?> refiner,
	                                                         NewRefinedQuery.Builder queryBuilder) {
		if (query != null && !query.getIdentifier().isBlank()) {
			NewPartialQueryRefiner.Result result = refiner.parse(query);
			result.incrementallyApply(queryBuilder);
			return result.getUsageStatement();
		}
		return null;
	}

	private List<String> extractKeywords(String query) {
		return keywordExtractor.apply(query);
	}

	/**
	 * Returns the given query wrapped into an {@link OriginalIdentifier} with the
	 * {@link OriginalIdentifier.Source#RAW_QUERY} source. If the given query is null, returns null.
	 */
	private OriginalIdentifier<String> wrap(String query) {
		if (query == null) return null;
		return new OriginalIdentifier<>(query, OriginalIdentifier.Source.RAW_QUERY);
	}

}
