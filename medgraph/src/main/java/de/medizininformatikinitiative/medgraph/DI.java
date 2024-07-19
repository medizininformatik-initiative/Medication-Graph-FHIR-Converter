package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.common.db.*;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExport;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportFactory;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulation;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulationFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class serves as central dependency injection manager. Using {@link #get(Class)}, you can request whatever object
 * you need.
 *
 * @author Markus Budeus
 */
public class DI {

	private static final DependencyMap dependencyMap = new DependencyMap();

	static {
		reset();
	}

	/**
	 * Returns the configured dependency for the given object type.
	 *
	 * @param target the object type for which to return the configured dependency
	 * @return an implementation of the given object type
	 * @throws NoSuchElementException if no implementation is available for the given object type
	 */
	@NotNull
	public static <T> T get(Class<T> target) {
		T obj = dependencyMap.get(target);
		if (obj == null) throw new NoSuchElementException(
				"No implementation is configured for dependency type " + target.getName() + "!");
		return obj;
	}

	/**
	 * Sets all configured dependencies back to their defaults.
	 */
	static void reset() {
		dependencyMap.clear();

		dependencyMap.put(GraphDbPopulationFactory.class, GraphDbPopulation::new);
		dependencyMap.put(FhirExportFactory.class, FhirExport::new);
		ConnectionPreferences preferences = ApplicationPreferences.getDatabaseConnectionPreferences();
		dependencyMap.put(ConnectionPreferences.class, preferences);
		dependencyMap.put(ConnectionTestService.class, new ConnectionTestServiceImpl());

		ApplicationDatabaseConnectionManager conManager = new ApplicationDatabaseConnectionManager(preferences);
		dependencyMap.put(ConnectionConfigurationService.class, conManager);
		dependencyMap.put(DatabaseConnectionService.class, conManager);
	}

	/**
	 * Inserts a dependency into the configured dependencies. Use this to insert mocks for testing.
	 *
	 * @param target         the object type for which to provide the given implementation
	 * @param implementation the implementation to provide for the given object type
	 * @param <T>            the type of this implementation
	 */
	static <T> void insertDependency(Class<T> target, T implementation) {
		dependencyMap.put(target, implementation);
	}

	private static class DependencyMap {

		private final Map<Class<?>, Object> map = new HashMap<>();

		<T> void put(Class<T> key, T value) {
			map.put(key, value);
		}

		@SuppressWarnings("unchecked")
		@Nullable
		<T> T get(Class<T> key) {
			return (T) map.get(key);
		}

		void clear() {
			map.clear();
		}

	}


}
