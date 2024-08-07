package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.doseform;

import de.medizininformatikinitiative.medgraph.searchengine.model.Drug;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;

/**
 * Judges {@link DetailedProduct}s based on how well their dose forms overlap with dose forms in the query.
 *
 * @author Markus Budeus
 */
public class PharmaceuticalDoseFormJudge extends ScoreJudge<DetailedProduct> {

	/**
	 * Score which is assigned for each dose form of a drug in a product which overlaps with a dose form in the search
	 * term.
	 */
	public static final double SCORE_PER_OVERLAP = 1.0;

	@Override
	protected double judgeInternal(DetailedProduct product, SearchQuery query) {
		double score = 0;
		for (Drug d : product.getDrugs()) {
			EdqmPharmaceuticalDoseForm doseForm = d.edqmDoseForm();
			if (doseForm != null) {
				if (query.getDoseForms().contains(doseForm)) {
					score += SCORE_PER_OVERLAP;
				}
			}
		}

		return score;
	}

	@Override
	public String getDescription() {
		return "Judges products based on how many of their drugs' pharmaceutical dose forms match the searched " +
				"dose forms.";
	}

	@Override
	public String toString() {
		return "Pharmaceutical Dose Form Judge";
	}

}
