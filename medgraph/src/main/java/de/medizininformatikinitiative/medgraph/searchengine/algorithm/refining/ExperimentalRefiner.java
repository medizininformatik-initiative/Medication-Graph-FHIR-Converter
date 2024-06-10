package de.medizininformatikinitiative.medgraph.searchengine.algorithm.refining;

import de.medizininformatikinitiative.medgraph.searchengine.db.Database;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.OngoingRefinement;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ProductOnlyFilter;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DosageAndAmountInfoMatchJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DosagesInProductNameJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.doseform.DoseFormCharacteristicJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.doseform.PharmaceuticalDoseFormJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.ProductDetailsResolver;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.SubstanceToProductResolver;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.SubSortingTree;
import org.neo4j.driver.Session;

import java.util.List;

/**
 * The (as of 2024-05-14) one and only {@link MatchRefiner}-implementation and the one I am
 * experimenting with.
 *
 * @author Markus Budeus
 */
public class ExperimentalRefiner implements MatchRefiner {

	private final ProductOnlyFilter productOnlyFilter = new ProductOnlyFilter();

	private final SubstanceToProductResolver substanceToProductResolver;

	private final DosageAndAmountInfoMatchJudge dosageJudge;

	private final ProductDetailsResolver productDetailsResolver;

	private final PharmaceuticalDoseFormJudge doseFormJudge;
	private final DoseFormCharacteristicJudge doseFormCharacteristicJudge;
	private final DosagesInProductNameJudge dosagesInProductNameJudge;

	public ExperimentalRefiner(Session session, Database database) {
		substanceToProductResolver = new SubstanceToProductResolver(session);
		dosageJudge = new DosageAndAmountInfoMatchJudge(0.1);
		productDetailsResolver = new ProductDetailsResolver(database);
		doseFormJudge = new PharmaceuticalDoseFormJudge(0.1);
		doseFormCharacteristicJudge = new DoseFormCharacteristicJudge(0.1);
		dosagesInProductNameJudge = new DosagesInProductNameJudge(null);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This particular implementation guarantees the resulting tree only contains product matches.
	 */
	@Override
	public SubSortingTree<MatchingObject> refineMatches(List<? extends MatchingObject> initialMatches,
	                                                    SearchQuery query) {

		// Use the substances from the search query to resolve products
		List<OriginalMatch> substanceObjects = query.getSubstances().stream()
		                                            .map(OriginalMatch::new).toList();
		OngoingRefinement substanceBasedSearch = new OngoingRefinement(substanceObjects, query);
		substanceBasedSearch.transformMatches(substanceToProductResolver);
		// TODO Ordering, and therefore relevance of substance search terms is completely lost as of now

		// Merge the products resolved from substances with the products from the initial search
		SubSortingTree<MatchingObject> productMatches = new SubSortingTree<>(initialMatches);
		SubSortingTree<MatchingObject> substanceMatches = substanceBasedSearch.getCurrentMatchesTree();
		SubSortingTree<MatchingObject> combinedMatches = SubSortingTree.merge(productMatches, substanceMatches);
		combinedMatches.clearDuplicates();

		OngoingRefinement matching = new OngoingRefinement(combinedMatches, query);

		List<MatchingObject> currentMatches = matching.getCurrentMatches();
		if (currentMatches.size() == 1 && currentMatches.getFirst().getObject() instanceof Product) {
			return matching.getCurrentMatchesTree();
		}

		// If we only recieved products as input, everything should pass this filter
		matching.applyFilter(productOnlyFilter, false);

		matching.transformMatches(productDetailsResolver); // Product details are required for the subsequent judges.

		matching.applyScoreJudge(dosageJudge, true);
		matching.applyScoreJudge(doseFormJudge, true);
		matching.applyScoreJudge(doseFormCharacteristicJudge, true);
		matching.applyScoreJudge(dosagesInProductNameJudge, false);

		return matching.getCurrentMatchesTree();
	}

}
