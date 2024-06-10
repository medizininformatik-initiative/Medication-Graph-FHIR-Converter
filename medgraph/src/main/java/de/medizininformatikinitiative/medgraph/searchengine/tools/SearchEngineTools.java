package de.medizininformatikinitiative.medgraph.searchengine.tools;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Markus Budeus
 */
public class SearchEngineTools {

	public enum OverlapResolutionStrategy {
		KEEP_FIRST,
		KEEP_SECOND,
		KEEP_BOTH
	}

	/**
	 * Removes elements from this list which are in conflict with each other. What exactly a conflict is, is left to the
	 * caller. Please note the order of elements passed to doOverlap must not be relevant, otherwise this function might
	 * not work as expected.
	 * <p>
	 * If the resolutionStrategy {@link OverlapResolutionStrategy#KEEP_BOTH} is never used and doOverlap ignores the
	 * order of input elements, this function guarantees that no conflicting elements remain in the list.
	 *
	 * @param elements           the elements to clear of conflicts
	 * @param doOverlap          a function which decides whether two objects from the list are conflicting
	 * @param resolutionStrategy a function which decides how to resolve a conflict between two elements in the list
	 * @param <T>                the type of elements in the list
	 */
	public static <T> void removeConflictingOverlaps(List<T> elements,
	                                          BiFunction<T, T, Boolean> doOverlap,
	                                          BiFunction<T, T, OverlapResolutionStrategy> resolutionStrategy) {
		for (int i = elements.size() - 1; i > 0; i--) {
			T second = elements.get(i);
			inner: for (int j = i - 1; j >= 0; j--) {
				T first = elements.get(j);
				if (doOverlap.apply(second, first)) {
					// Remove Overlap if required
					OverlapResolutionStrategy strategy = resolutionStrategy.apply(first, second);
					switch (strategy) {
						case KEEP_FIRST -> {
							elements.remove(i);
							break inner;
						}
						case KEEP_SECOND -> {
							elements.remove(j);
							i--;
						}
					}
				}
			}
		}
	}
}
