package de.medizininformatikinitiative.medgraph.searchengine.model;

import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
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
	private final List<Substance> substances;

	@NotNull
	private final List<Dosage> activeIngredientDosages;

	@NotNull
	private final List<Amount> drugAmounts;

	@NotNull
	private final List<EdqmPharmaceuticalDoseForm> doseForms;

	@NotNull
	private final List<EdqmConcept> doseFormCharacteristics;

	public SearchQuery(@NotNull List<String> productNameKeywords,
	                   @NotNull List<Substance> substances,
	                   @NotNull List<? extends Dosage> activeIngredientDosages,
	                   @NotNull List<? extends Amount> drugAmounts,
	                   @NotNull List<EdqmPharmaceuticalDoseForm> doseForms,
	                   @NotNull List<EdqmConcept> doseFormCharacteristics) {
		this.productNameKeywords = productNameKeywords;
		this.substances = substances;
		this.activeIngredientDosages = new ArrayList<>(activeIngredientDosages);
		this.drugAmounts = new ArrayList<>(drugAmounts);
		this.doseForms = doseForms;
		this.doseFormCharacteristics = doseFormCharacteristics;
	}

	/**
	 * Returns a list of keywords for which to search in product names.
	 */
	@NotNull
	public List<String> getProductNameKeywords() {
		return productNameKeywords;
	}

	/**
	 * Returns a list of substances which the searched product should contain.
	 */
	public @NotNull List<Substance> getSubstances() {
		return substances;
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

	/**
	 * Returns a list of dose forms that the searched products should ideally match.
	 */
	public @NotNull List<EdqmPharmaceuticalDoseForm> getDoseForms() {
		return doseForms;
	}

	/**
	 * Returns a list of dose forms concepts that the dose forms of the found products should ideally have.
	 */
	public @NotNull List<EdqmConcept> getDoseFormCharacteristics() {
		return doseFormCharacteristics;
	}

	public static class Builder {
		@Nullable
		private List<String> productNameKeywords = null;
		@Nullable
		private List<Substance> substances = null;
		@Nullable
		private List<Dosage> activeIngredientDosages = null;
		@Nullable
		private List<Amount> drugAmounts = null;
		@Nullable
		private List<EdqmPharmaceuticalDoseForm> doseForms = null;
		@Nullable
		private List<EdqmConcept> doseFormCharacteristics = null;

		public Builder() {

		}

		public Builder withProductNameKeywords(@Nullable List<String> productNameKeywords) {
			this.productNameKeywords = productNameKeywords;
			return this;
		}

		public Builder withSubstances(
				@Nullable List<Substance> substances) {
			this.substances = substances;
			return this;
		}

		public Builder withActiveIngredientDosages(
				@Nullable List<Dosage> activeIngredientDosages) {
			this.activeIngredientDosages = activeIngredientDosages;
			return this;
		}

		public Builder withDrugAmounts(
				@Nullable List<Amount> drugAmounts) {
			this.drugAmounts = drugAmounts;
			return this;
		}

		public Builder withDoseFormCharacteristics(
				@Nullable List<EdqmConcept> doseFormCharacteristics) {
			this.doseFormCharacteristics = doseFormCharacteristics;
			return this;
		}

		public Builder withDoseForms(
				@Nullable List<EdqmPharmaceuticalDoseForm> doseForms) {
			this.doseForms = doseForms;
			return this;
		}

		@SuppressWarnings("ConstantConditions")
		public SearchQuery build() {
			return new SearchQuery(
					emptyIfNull(productNameKeywords),
					emptyIfNull(substances),
					emptyIfNull(activeIngredientDosages),
					emptyIfNull(drugAmounts),
					emptyIfNull(doseForms),
					emptyIfNull(doseFormCharacteristics)
			);
		}

		private <T> @NotNull List<T> emptyIfNull(@Nullable List<T> list) {
			if (list == null) return Collections.emptyList();
			return list;
		}
	}
}
