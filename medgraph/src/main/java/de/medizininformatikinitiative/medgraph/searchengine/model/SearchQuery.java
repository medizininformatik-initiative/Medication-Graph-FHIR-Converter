package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Class which represents the search query information entered by the user.
 *
 * @author Markus Budeus
 */
public class SearchQuery {

	@Nullable
	private final String productName;
	@Nullable
	private final String substanceName;

	@NotNull
	private final List<Dosage> activeIngredientDosages;

	@NotNull
	private final List<Amount> drugAmounts;

	public SearchQuery(@Nullable String productName, @Nullable String substanceName,
	                   @NotNull List<Dosage> activeIngredientDosages, @NotNull List<Amount> drugAmounts) {
		this.productName = productName;
		this.substanceName = substanceName;
		this.activeIngredientDosages = activeIngredientDosages;
		this.drugAmounts = drugAmounts;
	}

	/**
	 * Returns a search string for product names to be searched. Null if product name searching is not intended to be
	 * performed.
	 */
	@Nullable
	public String getProductName() {
		return productName;
	}

	/**
	 * Returns a search string for substance names to be searched. Null if substance name searching is not intended to be
	 * performed.
	 */
	@Nullable
	public String getSubstanceName() {
		return substanceName;
	}

	/**
	 * Returns a list of active ingredient dosages. Matching products should have active ingredient dosages as close
	 * to those specified as possible.
	 * If dosages are not part of the search, the list is empty.
	 */
	@NotNull
	public List<Dosage> getActiveIngredientDosages() {
		return activeIngredientDosages;
	}

	/**
	 * Returns a list of drug amounts products could match.
	 * If drug amounts are not part of the search, the list is empty.
	 */
	@NotNull
	public List<Amount> getDrugAmounts() {
		return drugAmounts;
	}

}
