package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.doseform;

import de.medizininformatikinitiative.medgraph.searchengine.model.Drug;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;

/**
 * Judge which scores {@link DetailedProduct}s based on how well the characteristics of the dose forms of their assigned
 * drugs match the characteristics in the search term.
 *
 * @author Markus Budeus
 */
public class DoseFormCharacteristicJudge extends ScoreJudge<DetailedProduct> {

	/**
	 * Score which is assigned for each dose form characteristc of a drug in a product which overlaps with a dose form
	 * characteristic in the search term.
	 */
	public static final double SCORE_PER_OVERLAP = 1.0;

	@Override
	protected double judgeInternal(DetailedProduct product, SearchQuery query) {
		double score = 0;
		for (Drug d : product.getDrugs()) {
			score += scoreCharacteristicsOverlap(d.edqmDoseForm(), query);
		}
		return score;
	}

	private double scoreCharacteristicsOverlap(EdqmPharmaceuticalDoseForm doseForm, SearchQuery query) {
		if (doseForm == null) return 0;

		double score = 0;
		for (EdqmConcept characteristic : doseForm.getCharacteristics()) {
			if (query.getDoseFormCharacteristics().contains(characteristic)) score += SCORE_PER_OVERLAP;
		}
		return score;
	}

	@Override
	public String getDescription() {
		return "Judges products based on how many of their drugs' pharmaceutical dose form characteristic match the " +
				"searched dose form characteristics.";
	}

	@Override
	public String toString() {
		return "Dose Form Characteristic Judge";
	}

}
