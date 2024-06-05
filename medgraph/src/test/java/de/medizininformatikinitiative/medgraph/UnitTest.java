package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Markus Budeus
 */
public class UnitTest {

	private AutoCloseable mocks;

	@BeforeEach
	void setUpMocks() {
		mocks = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void closeMocks() throws Exception {
		mocks.close();
	}

	/**
	 * Asserts the contents of the two lists are equal when ignoring the order of elements.
	 */
	protected void assertEqualsIgnoreOrder(List<?> expected, List<?> actual) {
		if (expected == null && actual == null) return;
		assertNotNull(expected, "Expected null, but got "+ actual);
		assertNotNull(actual, "Expected "+expected+", but got null!");
		assertEquals(expected.size(), actual.size(), "The two lists have different sizes!");

		// Their sets may overlap, but duplicate elements may still differ in how often they occur
		assertEquals(toOccurrenceMap(expected), toOccurrenceMap(actual),
				"Expected "+expected+", but got "+actual);
	}

	/**
	 * Returns a map whose keys are the values in the given list and whose values are how often
	 * the respective key exists in the input list.
	 */
	private <T> Map<T, Integer> toOccurrenceMap(List<T> list) {
		Map<T, Integer> map = new HashMap<>();
		for (T element: list) {
			map.compute(element, (key, value) -> value == null ? 1 : value + 1);
		}
		return map;
	}

	/**
	 * Asserts the used parts of the given substring usage statement are exactly those given as used parts. Note that
	 * altough you pass the used parts as list, their ordering is irrelevant. (Duplicates, however, are not!)
	 *
	 * @param usedParts      the used parts of the original which the given usage statement should use
	 * @param usageStatement the usage statement to check
	 */
	protected void assertUsedParts(List<String> usedParts, SubstringUsageStatement usageStatement) {
		assertUsedParts(usedParts, usageStatement, true);
	}

	/**
	 * Asserts the used parts of the given substring usage statement are exactly those given as used parts. Note that *
	 * altough you pass the used parts as list, their ordering is irrelevant. (Duplicates, however, are not!)
	 *
	 * @param usedParts      the used parts of the original which the given usage statement should use
	 * @param usageStatement the usage statement to check
	 * @param trim           if true, the actual substrings specified by the usage statement may differ in preceding and
	 *                       succeeding spaces
	 */
	protected void assertUsedParts(List<String> usedParts, SubstringUsageStatement usageStatement,
	                               boolean trim) {
		List<String> actualUsedParts = new ArrayList<>();
		for (IntRange range : usageStatement.getUsedRanges()) {
			actualUsedParts.add(usageStatement.getOriginal().substring(range.getFrom(), range.getTo()));
		}

		if (trim) {
			usedParts = trimAll(usedParts);
			actualUsedParts = trimAll(actualUsedParts);
		}

		usedParts.sort(Comparator.naturalOrder());
		actualUsedParts.sort(Comparator.naturalOrder());

		assertEquals(usedParts, actualUsedParts);
	}

	/**
	 * Returns a list which contains all strings in the input list, but trimmed.
	 */
	private List<String> trimAll(List<String> parts) {
		return parts.stream().map(String::trim).collect(Collectors.toList());
	}

}
