package de.medizininformatikinitiative.medgraph.searchengine.algorithm.refining;

import de.medizininformatikinitiative.medgraph.searchengine.db.Database;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.OngoingMatching;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ProductOnlyFilter;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage.DosageAndAmountInfoMatchJudge;
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

	public ExperimentalRefiner(Session session, Database database) {
		substanceToProductResolver = new SubstanceToProductResolver(session);
		dosageJudge = new DosageAndAmountInfoMatchJudge(database, 0.1);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This particular implementation guarantees the resulting tree only contains product matches.
	 */
	@Override
	public SubSortingTree<MatchingObject> refineMatches(List<? extends MatchingObject> initialMatches,
	                                                    SearchQuery query) {

		if (initialMatches.isEmpty()) return new SubSortingTree<>(initialMatches);

		OngoingMatching matching = new OngoingMatching(initialMatches, query);

		List<MatchingObject> currentMatches = matching.getCurrentMatches();
		if (currentMatches.size() == 1 && currentMatches.getFirst().getObject() instanceof Product) {
			return matching.getCurrentMatchesTree();
		}

		// Only use product matches, unless this leaves us without a result. In that case, transform substances
		// to products.
		if (!matching.applyFilter(productOnlyFilter, true)) {
			matching.transformMatches(substanceToProductResolver);
		}

		matching.applyScoreJudge(dosageJudge, true);

		// TODO Master's Thesis Matcher included a sort by substrings found step here, I'll leave it out for now
		//      as it is of little importance

		return matching.getCurrentMatchesTree();
	}

}
