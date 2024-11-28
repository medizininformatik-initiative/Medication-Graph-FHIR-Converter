package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This judge assigns a negative score based on how many distinct active substances a product contains which are not
 * part of the search query. The idea is that if a user searches for a set of active ingredients, drugs which contain
 * exactly these ingredients are preferred over those that contain more ingredients.
 * <p>
 * If no substances are part of the search query, this judge always assigns a score of 0.
 *
 * @author Markus Budeus
 */
public class ExcessSubstanceJudge extends ScoreJudge<DetailedProduct> {

	@Override
	protected List<Double> batchJudgeInternal(List<? extends DetailedProduct> matchables, SearchQuery query) {
		// Abort quickly if no substances are being searched
		if (query.getSubstances().isEmpty()) {
			return Collections.nCopies(matchables.size(), 0.0);
		}
		return super.batchJudgeInternal(matchables, query);
	}

	@Override
	protected double judgeInternal(DetailedProduct matchable, SearchQuery query) {
		List<Substance> querySubstances = query.getSubstances();
		if (querySubstances.isEmpty()) return 0.0;
		Set<String> substanceNames = new HashSet<>();
		matchable.getDrugs().forEach(d ->
				d.activeIngredients().forEach(ingredient -> substanceNames.add(ingredient.getSubstanceName()))
		);
		querySubstances.forEach(s -> substanceNames.remove(s.getName()));
		return -substanceNames.size();
	}

	@Override
	public String getDescription() {
		return "Deducts one point for each active ingredient of the product which is not part of the search query.";
	}

	@Override
	public String toString() {
		return "ExcessSubstanceJudge";
	}
}
