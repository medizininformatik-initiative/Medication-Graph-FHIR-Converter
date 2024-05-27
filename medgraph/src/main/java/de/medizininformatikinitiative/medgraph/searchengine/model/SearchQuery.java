package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which represents the search query information entered by the user.
 *
 * @author Markus Budeus
 */
public class SearchQuery {

	@NotNull
	private final List<String> productNameKeywords;
	@NotNull
	private final List<String> substanceNameKeywords;

	@NotNull
	private final List<Dosage> activeIngredientDosages;

	@NotNull
	private final List<Amount> drugAmounts;

	public SearchQuery(@NotNull List<String> productNameKeywords,
	                   @NotNull List<String> substanceNameKeywords,
	                   @NotNull List<? extends Dosage> activeIngredientDosages,
	                   @NotNull List<? extends Amount> drugAmounts) {
		this.productNameKeywords = productNameKeywords;
		this.substanceNameKeywords = substanceNameKeywords;
		this.activeIngredientDosages = new ArrayList<>(activeIngredientDosages);
		this.drugAmounts = new ArrayList<>(drugAmounts);
	}

	/**
	 * Returns a list of keywords for which so search in product names.
	 */
	@NotNull
	public List<String> getProductNameKeywords() {
		return productNameKeywords;
	}

	/**
	 * Returns a list of keywords for which so search in substance names.
	 */
	@NotNull
	public List<String> getSubstanceNameKeywords() {
		return substanceNameKeywords;
	}

	/**
	 * Returns a list of active ingredient dosages. Matching products should have active ingredient dosages as close to
	 * those specified as possible. If dosages are not part of the search, the list is empty.
	 */
	@NotNull
	public List<Dosage> getActiveIngredientDosages() {
		return activeIngredientDosages;
	}

	/**
	 * Returns a list of drug amounts products could match. If drug amounts are not part of the search, the list is
	 * empty.
	 */
	@NotNull
	public List<Amount> getDrugAmounts() {
		return drugAmounts;
	}

}
