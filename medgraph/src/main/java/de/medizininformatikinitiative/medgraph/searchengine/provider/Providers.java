package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;
import org.neo4j.driver.Session;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Markus Budeus
 */
public class Providers {

	private static final LazyProvider PRODUCT_SYNONYMES = new LazyProvider(DatabaseProviders::getProductSynonyms);
	private static final LazyProvider SUBSTANCE_SYNONYMES = new LazyProvider(DatabaseProviders::getSubstanceSynonyms);
	private static final LazyProvider EDQM_CONCEPT_IDENTIFIERS = new LazyProvider(
			DatabaseProviders::getEdqmConceptIdentifiers);

	/**
	 * Returns a {@link BaseProvider} which contains all product names known in the database and their corresponding
	 * products.
	 *
	 * @param session the session to access the data
	 * @return the provider for product names
	 */
	public static BaseProvider<String> getProductSynonymes(Session session) {
		return PRODUCT_SYNONYMES.get(session);
	}

	/**
	 * Returns a {@link BaseProvider} which contains all substance names known in the database and their corresponding
	 * substances.
	 *
	 * @param session the session to access the data
	 * @return the provider for substance names
	 */
	public static BaseProvider<String> getSubstanceSynonymes(Session session) {
		return SUBSTANCE_SYNONYMES.get(session);
	}

	/**
	 * Returns a {@link BaseProvider} which contains all known EDQM Standard Term concepts with their known identifiers.
	 * The provided
	 * {@link Identifiable Identifiables} are
	 * either {@link EdqmConcept EdqmConcepts}
	 * or
	 * {@link EdqmPharmaceuticalDoseForm
	 * PharmaceuticalDoseForms}.
	 */
	public static BaseProvider<String> getEdqmConceptIdentifiers(Session session) {
		return EDQM_CONCEPT_IDENTIFIERS.get(session);
	}

	public static class LazyProvider {

		private final Function<Session, Stream<MappedIdentifier<String>>> instantiator;
		private volatile BaseProvider<String> instance;

		public LazyProvider(Function<Session, Stream<MappedIdentifier<String>>> instantiator) {
			this.instantiator = instantiator;
		}

		public BaseProvider<String> get(Session session) {
			if (instance == null) {
				synchronized (Providers.class) {
					if (instance == null) {
						instance = BaseProvider.ofIdentifiers(instantiator.apply(session).toList());
					}
				}
			}
			return instance;
		}

	}

}
