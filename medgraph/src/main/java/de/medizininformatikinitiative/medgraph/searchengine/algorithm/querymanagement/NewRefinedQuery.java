package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Merge;
import de.medizininformatikinitiative.medgraph.searchengine.tools.SearchEngineTools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Markus Budeus
 */
public class NewRefinedQuery {

	@NotNull
	private final Identifier<List<String>> productNameKeywords;

	@NotNull
	private final List<MatchingObject<Substance>> substances;

	@NotNull
	private final List<MatchingObject<Dosage>> dosages;

	@NotNull
	private final List<MatchingObject<Amount>> drugAmounts;

	@NotNull
	private final List<MatchingObject<EdqmPharmaceuticalDoseForm>> doseForms;

	@NotNull
	private final List<MatchingObject<EdqmConcept>> doseFormCharacteristics;


	public NewRefinedQuery(@NotNull Identifier<List<String>> productNameKeywords,
	                       @NotNull List<MatchingObject<Substance>> substances,
	                       @NotNull List<MatchingObject<Dosage>> dosages,
	                       @NotNull List<MatchingObject<Amount>> drugAmounts,
	                       @NotNull List<MatchingObject<EdqmPharmaceuticalDoseForm>> doseForms,
	                       @NotNull List<MatchingObject<EdqmConcept>> doseFormCharacteristics) {
		this.productNameKeywords = productNameKeywords;
		this.substances = substances;
		this.dosages = dosages;
		this.drugAmounts = drugAmounts;
		this.doseForms = doseForms;
		this.doseFormCharacteristics = doseFormCharacteristics;
	}

	public @NotNull Identifier<List<String>> getProductNameKeywords() {
		return productNameKeywords;
	}

	public @NotNull List<MatchingObject<Substance>> getSubstances() {
		return substances;
	}

	public @NotNull List<MatchingObject<Dosage>> getDosages() {
		return dosages;
	}

	public @NotNull List<MatchingObject<Amount>> getDrugAmounts() {
		return drugAmounts;
	}

	public @NotNull List<MatchingObject<EdqmPharmaceuticalDoseForm>> getDoseForms() {
		return doseForms;
	}

	public @NotNull List<MatchingObject<EdqmConcept>> getDoseFormCharacteristics() {
		return doseFormCharacteristics;
	}

	/**
	 * Converts this {@link RefinedQuery} into a {@link SearchQuery}.
	 */
	public SearchQuery toSearchQuery() {
		return new SearchQuery(
				productNameKeywords.getIdentifier(),
				SearchEngineTools.unwrap(substances),
				SearchEngineTools.unwrap(dosages),
				SearchEngineTools.unwrap(drugAmounts),
				SearchEngineTools.unwrap(doseForms),
				SearchEngineTools.unwrap(doseFormCharacteristics)
		);
	}

	public static class Builder {

		@Nullable
		private Identifier<List<String>> productNameKeywords;

		@NotNull
		private final MatchingObjectStorage<Substance> substances = new MatchingObjectStorage<>();

		@NotNull
		private final MatchingObjectStorage<Dosage> dosages = new MatchingObjectStorage<>();

		@NotNull
		private final MatchingObjectStorage<Amount> drugAmounts = new MatchingObjectStorage<>();

		@NotNull
		private final MatchingObjectStorage<EdqmPharmaceuticalDoseForm> doseForms = new MatchingObjectStorage<>();

		@NotNull
		private final MatchingObjectStorage<EdqmConcept> doseFormCharacteristics = new MatchingObjectStorage<>();

		/**
		 * Creates a new builder.
		 */
		public Builder() {

		}

		/**
		 * Sets the product name keywords to use in the refined query.
		 *
		 * @return this instance
		 */
		public Builder withProductNameKeywords(Identifier<List<String>> productNameKeywords) {
			this.productNameKeywords = productNameKeywords;
			return this;
		}

		/**
		 * Incrementally adds the given substance to the substances managed by this refined query.
		 *
		 * @return this instance
		 */
		public Builder withSubstance(MatchingObject<Substance> substance) {
			substances.add(substance);
			return this;
		}

		/**
		 * Incrementally adds the given substance to the substances managed by this refined query.
		 *
		 * @return this instance
		 */
		public Builder withDosage(MatchingObject<Dosage> dosage) {
			dosages.add(dosage);
			return this;
		}

		/**
		 * Incrementally adds the given drug to the drugs managed by this refined query.
		 *
		 * @return this instance
		 */
		public Builder withDrugAmount(MatchingObject<Amount> drugAmount) {
			drugAmounts.add(drugAmount);
			return this;
		}

		/**
		 * Incrementally adds the given dose form to the dose forms managed by this refined query.
		 *
		 * @return this instance
		 */
		public Builder withDoseForm(MatchingObject<EdqmPharmaceuticalDoseForm> doseForm) {
			doseForms.add(doseForm);
			return this;
		}

		/**
		 * Incrementally adds the given characteristic to the dose form characteristics managed by this refined query.
		 *
		 * @return this instance
		 */
		public Builder withDoseFormCharacteristic(MatchingObject<EdqmConcept> doseFormCharacteristic) {
			doseFormCharacteristics.add(doseFormCharacteristic);
			return this;
		}

		/**
		 * Builds the {@link NewRefinedQuery}. Note the product name keywords must be set before a refined query can be
		 * built.
		 *
		 * @throws IllegalStateException if the product name keywords have not been set (or set to null) using
		 *                               {@link #withProductNameKeywords(Identifier)}
		 */
		public NewRefinedQuery build() {
			if (productNameKeywords == null) throw new IllegalStateException("No product name keywords are set!");

			return new NewRefinedQuery(
					productNameKeywords,
					substances.getWithDuplicatesMerged(),
					dosages.getWithDuplicatesMerged(),
					drugAmounts.getWithDuplicatesMerged(),
					doseForms.getWithDuplicatesMerged(),
					doseFormCharacteristics.getWithDuplicatesMerged()
			);
		}

	}

	/**
	 * Class which incrementally builds a list of {@link MatchingObject}s. Duplicates in the list are merged when
	 * calling {@link #getWithDuplicatesMerged()}
	 */
	private static class MatchingObjectStorage<T extends Matchable> {
		private final List<MatchingObject<T>> matchingObjects = new ArrayList<>();

		/**
		 * Adds the given {@link MatchingObject} to this storage.
		 */
		void add(MatchingObject<T> matchingObject) {
			matchingObjects.add(matchingObject);
		}

		/**
		 * Returns all previously added {@link MatchingObject}s, but merges all beforehand which reference the same
		 * {@link Matchable}.
		 */
		List<MatchingObject<T>> getWithDuplicatesMerged() {
			Map<Matchable, List<MatchingObject<T>>> matchingObjectsByMatchable = new LinkedHashMap<>();
			matchingObjects.forEach(m -> matchingObjectsByMatchable
					.computeIfAbsent(m.getObject(), o -> new LinkedList<>()).add(m));

			List<MatchingObject<T>> outList = new ArrayList<>();
			for (List<MatchingObject<T>> equalObjects : matchingObjectsByMatchable.values()) {
				assert !equalObjects.isEmpty();
				if (equalObjects.size() == 1) {
					outList.add(equalObjects.getFirst());
				} else {
					outList.add(new Merge<>(equalObjects));
				}
			}
			return outList;
		}

	}
}
