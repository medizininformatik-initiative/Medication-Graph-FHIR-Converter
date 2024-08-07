package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.searchengine.model.Drug;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;

import java.util.List;

/**
 * Judge which assigns scores based on how well drug dosage and drug amount information from the query matches the
 * previously found products.
 *
 * @author Markus Budeus
 */
public class DosageAndAmountInfoMatchJudge extends ScoreJudge<DetailedProduct> {

	/**
	 * The score which is assigned if the search query specifies neither dosage nor drug amount information.
	 */
	public static final double NO_DOSAGE_AND_AMOUNT_SCORE = 0.2;

	private final DosageMatchJudge dosageMatchJudge;
	private final DrugAmountMatchJudge drugAmountMatchJudge;

	public DosageAndAmountInfoMatchJudge() {
		this(new DosageMatchJudge(), new DrugAmountMatchJudge());
	}

	/**
	 * Constructor for testing purposes, which lets you insert mock judges.
	 */
	DosageAndAmountInfoMatchJudge(DosageMatchJudge dosageMatchJudge,
	                              DrugAmountMatchJudge drugAmountMatchJudge) {
		super();
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
	protected double judgeInternal(DetailedProduct target, SearchQuery query) {
		if (specifiesNoRelevantData(query)) return NO_DOSAGE_AND_AMOUNT_SCORE;
		return judge(target.getDrugs(), query);
	}

	private double judge(List<Drug> drugInfo, SearchQuery query) {
		double dosageMatchScore = dosageMatchJudge.judge(drugInfo, query.getActiveIngredientDosages());
		dosageMatchScore += drugAmountMatchJudge.judge(drugInfo.stream().map(Drug::amount).toList(),
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
