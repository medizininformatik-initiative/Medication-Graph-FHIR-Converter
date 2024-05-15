package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.dosage;

import de.medizininformatikinitiative.medgraph.searchengine.db.Database;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDosagesByProduct;
import de.medizininformatikinitiative.medgraph.searchengine.db.DbDrugDosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;

import java.util.*;

/**
 * Judge which assigns scores based on how well drug dosage and drug amount information from the query matches the
 * previously found products. {@link Matchable}s which are not {@link Product}s are assigned the
 * {@link #NO_PRODUCT_SCORE}.
 *
 * @author Markus Budeus
 */
public class DosageAndAmountInfoMatchJudge extends ScoreJudge {

	// TODO Test this class!

	/**
	 * The score which is assigned if the given matching target is not a product.
	 */
	public static final double NO_PRODUCT_SCORE = 0.0;
	/**
	 * The score which is assigned if the search query specifies neither dosage nor drug amount information.
	 */
	public static final double NO_DOSAGE_AND_AMOUNT_SCORE = 0.2;

	private final Database database;
	private final DosageMatchJudge dosageMatchJudge;

	public DosageAndAmountInfoMatchJudge(Database database, Double passingScore) {
		super(passingScore);
		this.database = database;
		this.dosageMatchJudge = new DosageMatchJudge();
	}

	@Override
	public String getDescription() {
		return "Assigns scores to product matches based on how well their active ingredient dosages and drug amounts" +
				" match the search.";
	}

	@Override
	protected List<Double> batchJudgeInternal(List<Matchable> targets, SearchQuery query) {
		if (specifiesNoRelevantData(query))
			return Collections.nCopies(targets.size(), NO_DOSAGE_AND_AMOUNT_SCORE);

		Map<Long, Matchable> targetIds = new HashMap<>(targets.size());
		for (Matchable t : targets) {
			if (t instanceof Product p)
				targetIds.put(p.getId(), t);
		}


		Map<Matchable, Double> scoreMap = new HashMap<>();
		if (!targetIds.isEmpty()) {

			Set<DbDosagesByProduct> dosagesByProducts = database.getDrugDosagesByProduct(targetIds.keySet());

			for (DbDosagesByProduct productDosageInfo : dosagesByProducts) {
				Matchable target = targetIds.get(productDosageInfo.productId);
				if (target != null) {
					scoreMap.put(target, judge(productDosageInfo.drugDosages, query));
				}
			}

		}

		List<Double> resultList = new ArrayList<>(targets.size());
		for (Matchable target : targets) {
			Double score = scoreMap.get(target);
			if (score == null) score = NO_PRODUCT_SCORE;
			resultList.add(score);
		}

		return resultList;
	}

	@Override
	protected double judgeInternal(Matchable target, SearchQuery query) {
		if (!(target instanceof Product)) return NO_PRODUCT_SCORE;
		if (specifiesNoRelevantData(query)) return NO_DOSAGE_AND_AMOUNT_SCORE;

		Set<DbDosagesByProduct> dosagesByProduct = database.getDrugDosagesByProduct(
				List.of(((Product) target).getId()));
		if (dosagesByProduct.isEmpty()) return 0.0;
		assert dosagesByProduct.size() < 2;
		return judge(dosagesByProduct.iterator().next().drugDosages, query);
	}

	private double judge(List<DbDrugDosage> drugDosageInfo, SearchQuery query) {
		double dosageMatchScore = dosageMatchJudge.judge(drugDosageInfo, query.getActiveIngredientDosages());
		// TODO Add amount match score
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
