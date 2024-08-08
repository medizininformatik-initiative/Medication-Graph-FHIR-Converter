package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.InitialMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.LevenshteinSearchMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import de.medizininformatikinitiative.medgraph.searchengine.model.ScoreIncorporationStrategy;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.ScoreMergingStrategy;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.MatchingPipelineService;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudgeConfiguration;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DosageAndAmountInfoMatchJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DosagesInProductNameJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.doseform.DoseFormCharacteristicJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.doseform.PharmaceuticalDoseFormJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.ProductDetailsResolver;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.SubstanceToProductResolver;
import de.medizininformatikinitiative.medgraph.searchengine.provider.Providers;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Query executor which uses the {@link MatchingObject} score variable to keep track of the best matches.
 *
 * @author Markus Budeus
 */
public class WeightedScoringBasedQueryExecutor implements QueryExecutor<DetailedProduct> {

	private final InitialMatchFinder<Product> initialMatchFinder;

	private final SubstanceToProductResolver substanceToProductResolver;

	private final DosageAndAmountInfoMatchJudge dosageJudge;

	private final ProductDetailsResolver productDetailsResolver;

	private final PharmaceuticalDoseFormJudge doseFormJudge;
	private final DoseFormCharacteristicJudge doseFormCharacteristicJudge;
	private final DosagesInProductNameJudge dosagesInProductNameJudge;

	public WeightedScoringBasedQueryExecutor(Session session) {
		initialMatchFinder = new LevenshteinSearchMatchFinder(
				Providers.getProductSynonyms(session)
		);
		substanceToProductResolver = new SubstanceToProductResolver(session);
		dosageJudge = new DosageAndAmountInfoMatchJudge();
		productDetailsResolver = new ProductDetailsResolver(new Neo4jCypherDatabase(session));
		doseFormJudge = new PharmaceuticalDoseFormJudge();
		doseFormCharacteristicJudge = new DoseFormCharacteristicJudge();
		dosagesInProductNameJudge = new DosagesInProductNameJudge();
	}

	@Override
	public List<MatchingObject<DetailedProduct>> executeQuery(RefinedQuery query) {
		SearchQuery searchQuery = query.toSearchQuery();
		MatchingPipelineService service = new MatchingPipelineService(searchQuery);

		// Find initial product matches
		List<MatchingObject<Product>> products = initialMatchFinder.findInitialMatches(searchQuery)
		                                                           .collect(Collectors.toList());
		products = service.mergeDuplicates(products, ScoreMergingStrategy.MAX);

		// Resolve products from substances
		List<MatchingObject<Product>> productsFromSubstances = resolveProductsFromSubstances(query.getSubstances(),
				service);

		// Merge initial product matches with products resolved from substances
		products = merge(service, products, productsFromSubstances);

		// Product details are required for the subsequent judges.
		List<? extends MatchingObject<DetailedProduct>> detailedProducts = service.transformMatches(products,
				productDetailsResolver, ScoreMergingStrategy.MAX);

		// Judge using different metrics
		detailedProducts = service.applyScoreJudge(detailedProducts, dosageJudge,
				new ScoreJudgeConfiguration(DosageAndAmountInfoMatchJudge.NO_DOSAGE_AND_AMOUNT_SCORE, false));
		detailedProducts = service.applyScoreJudge(detailedProducts, doseFormJudge,
				new ScoreJudgeConfiguration(0.1, false, 1.5, ScoreIncorporationStrategy.ADD));
		detailedProducts = service.applyScoreJudge(detailedProducts, doseFormCharacteristicJudge,
				new ScoreJudgeConfiguration(0.1, false,0.8, ScoreIncorporationStrategy.ADD));
		detailedProducts = service.applyScoreJudge(detailedProducts, dosagesInProductNameJudge,
				new ScoreJudgeConfiguration(0.5, ScoreIncorporationStrategy.ADD));

		detailedProducts.sort(Comparator.naturalOrder());

		return new ArrayList<>(detailedProducts);
	}

	private List<MatchingObject<Product>> resolveProductsFromSubstances(List<MatchingObject<Substance>> substances,
	                                                                    MatchingPipelineService service) {
		return service.transformMatches(
				substances,
				substanceToProductResolver,
				ScoreMergingStrategy.SUM
		);
	}

	@SafeVarargs
	private <T extends Matchable> List<MatchingObject<T>> merge(
			MatchingPipelineService service,
			List<MatchingObject<T>>... lists) {

		int size = 0;
		for (List<MatchingObject<T>> list : lists) {
			size += list.size();
		}
		List<MatchingObject<T>> all = new ArrayList<>(size);
		for (List<MatchingObject<T>> list : lists) {
			all.addAll(list);
		}
		return service.mergeDuplicates(all, ScoreMergingStrategy.SUM);
	}

}
