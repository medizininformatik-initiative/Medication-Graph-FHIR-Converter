package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.searchengine.db.Database;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDosagesByProduct;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDrugDosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.Drug;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;

import java.util.*;

/**
 * Judge which assigns scores based on how well drug dosage and drug amount information from the query matches the
 * previously found products. {@link Matchable}s which are not {@link DetailedProduct}s are assigned the
 * {@link #NO_DETAILED_PRODUCT_SCORE}.
 *
 * @author Markus Budeus
 */
public class DosageAndAmountInfoMatchJudge extends ScoreJudge {

	/**
	 * The score which is assigned if the given matching target is not a detailed product.
	 */
	public static final double NO_DETAILED_PRODUCT_SCORE = 0.1;
	/**
	 * The score which is assigned if the search query specifies neither dosage nor drug amount information.
	 */
	public static final double NO_DOSAGE_AND_AMOUNT_SCORE = 0.2;

	private final DosageMatchJudge dosageMatchJudge;
	private final DrugAmountMatchJudge drugAmountMatchJudge;

	public DosageAndAmountInfoMatchJudge(Double passingScore) {
		this(passingScore, new DosageMatchJudge(), new DrugAmountMatchJudge());
	}

	/**
	 * Constructor for testing purposes, which lets you insert mock judges.
	 */
	DosageAndAmountInfoMatchJudge(Double passingScore, DosageMatchJudge dosageMatchJudge,
	                              DrugAmountMatchJudge drugAmountMatchJudge) {
		super(passingScore);
		this.dosageMatchJudge = dosageMatchJudge;
		this.drugAmountMatchJudge = drugAmountMatchJudge;
	}

	@Override
	public String toString() {
		return "Dosage and Amount Match Judge";
	}

	@Override
	public String getDescription() {
		return "Assigns scores to product matches based on how well their active ingredient dosages and drug amounts" +
				" match the search.";
	}

	@Override
	protected double judgeInternal(Matchable target, SearchQuery query) {
		if (!(target instanceof DetailedProduct)) return NO_DETAILED_PRODUCT_SCORE;
		if (specifiesNoRelevantData(query)) return NO_DOSAGE_AND_AMOUNT_SCORE;
		return judge(((DetailedProduct) target).getDrugs(), query);
	}

	private double judge(List<Drug> drugInfo, SearchQuery query) {
		double dosageMatchScore = dosageMatchJudge.judge(drugInfo, query.getActiveIngredientDosages());
		dosageMatchScore += drugAmountMatchJudge.judge(drugInfo.stream().map(dd -> dd.getAmount()).toList(),
				query.getDrugAmounts());
		return dosageMatchScore;
	}

	/**
	 * Returns whether the given search query specifies any data relevant to this judge, i.e. drug dosage or drug amount
	 * information for which is to be searched.
	 */
	private boolean specifiesNoRelevantData(SearchQuery query) {
		return query.getActiveIngredientDosages().isEmpty() && query.getDrugAmounts().isEmpty();
	}

}
