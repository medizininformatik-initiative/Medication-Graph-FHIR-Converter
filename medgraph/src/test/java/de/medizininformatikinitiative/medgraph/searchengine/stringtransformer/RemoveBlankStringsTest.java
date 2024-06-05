package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class RemoveBlankStringsTest {

	@Test
	public void someBlankStrings() {
		assertEquals(List.of("Apfel", "Haus", "-"),
				new RemoveBlankStrings().apply(List.of("", "Apfel", "", "Haus", "-")));
	}

	@Test
	public void onlyBlankStrings() {
		assertEquals(List.of(),
				new RemoveBlankStrings().apply(List.of("", "", "")));
	}

	@Test
	public void emptyList() {
		assertEquals(List.of(),
				new RemoveBlankStrings().apply(List.of()));
	}

	@Test
	public void noBlankStrings() {
		assertEquals(List.of("Marie", "Eisenhower", "Burkhardt", "Neverwinter"),
				new RemoveBlankStrings().apply(List.of("Marie", "Eisenhower", "Burkhardt", "Neverwinter")));
	}

	@Test
	public void reverseTransformWithoutBlanks() {
		List<String> input = List.of("Mouse", "---", "Water", "Free");
		StringListUsageStatement usageStatement = new RemoveBlankStrings().reverseTransformUsageStatement(input,
				new StringListUsageStatement(input, Set.of(0, 2)));

		assertEquals(new StringListUsageStatement(input, Set.of(0, 2)), usageStatement);
	}

	@Test
	public void reverseTransformWithBlanks() {
		List<String> input = List.of("Mouse", "", "Water", "Free", "");
		StringListUsageStatement usageStatement = new RemoveBlankStrings().reverseTransformUsageStatement(input,
				new StringListUsageStatement(List.of("Mouse", "Water", "Free"), Set.of(0, 2)));

		assertEquals(new StringListUsageStatement(input, Set.of(0, 3)), usageStatement);
	}

	@Test
	public void reverseTransformOnlyBlanks() {
		List<String> input = List.of("\t", "", "   ");
		StringListUsageStatement usageStatement = new RemoveBlankStrings().reverseTransformUsageStatement(input,
				new StringListUsageStatement(List.of(), Set.of()));

		assertEquals(new StringListUsageStatement(input, Set.of()), usageStatement);
	}

	@Test
	public void reverseTransformTrimEmpty() {
		List<String> input = List.of();
		StringListUsageStatement usageStatement = new RemoveBlankStrings().reverseTransformUsageStatement(input,
				new StringListUsageStatement(List.of(), Set.of()));

		assertEquals(new StringListUsageStatement(input, Set.of()), usageStatement);
	}

}