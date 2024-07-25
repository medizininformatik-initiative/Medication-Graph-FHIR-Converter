package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;
import de.medizininformatikinitiative.medgraph.searchengine.tools.DosageDetector;

import java.util.List;

/**
 * Searches for dosages given in product names and compares them to the dosages searched.
 *
 * @author Markus Budeus
 */
public class DosagesInProductNameJudge extends ScoreJudge<Product> {

	/**
	 * Score applied for every searched dosage found in the product name.
	 */
	public static final double SCORE_PER_MATCH = 1;

	public DosagesInProductNameJudge(Double passingScore) {
		super(passingScore);
	}

	@Override
	protected double judgeInternal(Product product, SearchQuery query) {
		List<DosageDetector.DetectedDosage> detectedDosages = DosageDetector.detectDosages(product.getName());
		List<Dosage> searchDosages = query.getActiveIngredientDosages();

		int matches = 0;
		for (DosageDetector.DetectedDosage dd : detectedDosages) {
			if (searchDosages.contains(dd.getDosage())) matches++;
		}

		return matches * SCORE_PER_MATCH;
	}

	@Override
	public String getDescription() {
		return "Searches for dosages given in product names and compares them to the dosages searched.";
	}

	@Override
	public String toString() {
		return "Dosages in Product Name Judge";
	}
}
