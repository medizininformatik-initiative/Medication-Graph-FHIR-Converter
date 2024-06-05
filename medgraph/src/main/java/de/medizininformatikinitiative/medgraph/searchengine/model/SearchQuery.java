package de.medizininformatikinitiative.medgraph.searchengine.model;

import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
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

	/**
	 * Builder for search queries. Note that when using this builder, you cannot remove already-added information
	 * and the created lists will remove duplicates.
	 */
	public static class Builder {
		@NotNull
		private final List<String> productNameKeywords = new ArrayList<>();
		@NotNull
		private final List<Substance> substances = new ArrayList<>();
		@NotNull
		private final List<Dosage> activeIngredientDosages = new ArrayList<>();
		@NotNull
		private final List<Amount> drugAmounts = new ArrayList<>();
		@NotNull
		private final List<EdqmPharmaceuticalDoseForm> doseForms = new ArrayList<>();
		@NotNull
		private final List<EdqmConcept> doseFormCharacteristics = new ArrayList<>();

		public Builder() {

		}

		/**
		 * Adds the given keywords to the list of product name keywords maintained by this builder.
		 * @return this instance
		 */
		public Builder withProductNameKeywords(@NotNull List<String> productNameKeywords) {
			this.productNameKeywords.addAll(productNameKeywords);
			return this;
		}

		/**
		 * Adds the given substances to the list of substances maintained by this builder.
		 * @return this instance
		 */
		public Builder withSubstances(
				@NotNull List<Substance> substances) {
			this.substances.addAll(substances);
			return this;
		}

		/**
		 * Adds the given dosages to the list of active ingredient dosages maintained by this builder.
		 * @return this instance
		 */
		public Builder withActiveIngredientDosages(
				@NotNull List<Dosage> activeIngredientDosages) {
			this.activeIngredientDosages.addAll(activeIngredientDosages);
			return this;
		}

		/**
		 * Adds the given amounts to the list of drug amounts maintained by this builder.
		 * @return this instance
		 */
		public Builder withDrugAmounts(
				@NotNull List<Amount> drugAmounts) {
			this.drugAmounts.addAll(drugAmounts);
			return this;
		}

		/**
		 * Adds the given EDQM concepts to the list of dose form characteristics maintained by this builder.
		 * @return this instance
		 */
		public Builder withDoseFormCharacteristics(
				@NotNull List<EdqmConcept> doseFormCharacteristics) {
			this.doseFormCharacteristics.addAll(doseFormCharacteristics);
			return this;
		}

		/**
		 * Adds the given EDQM pharmaceutical dose forms to the list of dose forms maintained by this builder.
		 * @return this instance
		 */
		public Builder withDoseForms(
				@NotNull List<EdqmPharmaceuticalDoseForm> doseForms) {
			this.doseForms.addAll(doseForms);
			return this;
		}

		@SuppressWarnings("ConstantConditions")
		public SearchQuery build() {
			return new SearchQuery(
					distinct(productNameKeywords),
					distinct(substances),
					distinct(activeIngredientDosages),
					distinct(drugAmounts),
					distinct(doseForms),
					distinct(doseFormCharacteristics)
			);
		}

		private <T> @NotNull List<T> distinct(@NotNull List<T> list) {
			return new ArrayList<>(new LinkedHashSet<>(list));
		}
	}
}
