package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.doseform;

import de.medizininformatikinitiative.medgraph.searchengine.model.Drug;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;

/**
 * Judge which scores {@link DetailedProduct}s based on how well the characteristics of the dose forms of their
 * assigned drugs match the characteristics in the search term.
 *
 * @author Markus Budeus
 */
public class DoseFormCharacteristicJudge extends ScoreJudge {

	/**
	 * Score which is assigned for each dose form characteristc of a drug in a product which overlaps with a dose form
	 * characteristic in the search term.
	 */
	public static final double SCORE_PER_OVERLAP = 1.0;

	/**
	 * Score which is assigned if the given instance is not a {@link DetailedProduct}.
	 */
	public static final double NOT_A_DETAILED_PRODUCT_SCORE = 0.1;

	public DoseFormCharacteristicJudge(Double passingScore) {
		super(passingScore);
	}

	@Override
	protected double judgeInternal(Matchable matchable, SearchQuery query) {
		if (matchable instanceof DetailedProduct product) {
			double score = 0;
			for (Drug d: product.getDrugs()) {
				score += scoreCharacteristicsOverlap(d.getEdqmDoseForm(), query);
			}
			return score;
		}

		return NOT_A_DETAILED_PRODUCT_SCORE;
	}

	private double scoreCharacteristicsOverlap(EdqmPharmaceuticalDoseForm doseForm, SearchQuery query) {
		if (doseForm == null) return 0;

		double score = 0;
		for (EdqmConcept characteristic: doseForm.getCharacteristics()) {
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
