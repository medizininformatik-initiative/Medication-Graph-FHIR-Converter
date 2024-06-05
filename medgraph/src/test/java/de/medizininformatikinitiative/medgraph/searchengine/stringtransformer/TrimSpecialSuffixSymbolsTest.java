package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class TrimSpecialSuffixSymbolsTest {

	@ParameterizedTest
	@ValueSource(strings = { ",", ".", "|", "®", ":", ";", "-" })
	public void trimSpecialSuffix(String suffix) {
		assertEquals(List.of("Rivaroxaban", "Tabletten"),
				new TrimSpecialSuffixSymbols().apply(List.of("Rivaroxaban"+suffix, "Tabletten")));
	}

	@Test
	public void trimMultiple() {
		assertEquals(List.of("Rivaroxaban", "Film-Tbl"),
				new TrimSpecialSuffixSymbols().apply(List.of("Rivaroxaban®,", "Film-Tbl.")));
	}

	@Test
	public void trimBlank() {
		assertEquals(List.of("Mouse"),
				new TrimSpecialSuffixSymbols().apply(List.of("", "Mouse."))); // Should be filtered out
	}

	@Test
	public void trimToBlank() {
		assertEquals(List.of(""),
				new TrimSpecialSuffixSymbols().apply(List.of("-")));
	}

	@Test
	public void trimEmpty() {
		assertEquals(List.of(),
				new TrimSpecialSuffixSymbols().apply(List.of()));
	}

	@Test
	public void reverseTransformTrimSpecialSuffix() {
		List<String> input = List.of("Rivaroxaban®", "Tabletten");
		StringListUsageStatement usageStatement = new TrimSpecialSuffixSymbols().reverseTransformUsageStatement(input,
				new StringListUsageStatement(List.of("Rivaroxaban", "Tabletten"), Set.of(1)));

		assertEquals(new StringListUsageStatement(input, Set.of(1)), usageStatement);
	}

	@Test
	public void reverseTransformTrimMultiple() {
		List<String> input = List.of("Rivaroxaban®,", "Film-Tbl.");
		StringListUsageStatement usageStatement = new TrimSpecialSuffixSymbols().reverseTransformUsageStatement(input,
				new StringListUsageStatement(List.of("Rivaroxaban", "Film-Tbl"), Set.of(0, 1)));

		assertEquals(new StringListUsageStatement(input, Set.of(0, 1)), usageStatement);
	}

	@Test
	public void reverseTransformTrimToBlank() {
		List<String> input = List.of("-");
		StringListUsageStatement usageStatement = new TrimSpecialSuffixSymbols().reverseTransformUsageStatement(input,
				new StringListUsageStatement(List.of(""), Set.of()));

		assertEquals(new StringListUsageStatement(input, Set.of()), usageStatement);
	}

	@Test
	public void reverseTransformTrimEmpty() {
		List<String> input = List.of();
		StringListUsageStatement usageStatement = new TrimSpecialSuffixSymbols().reverseTransformUsageStatement(input,
				new StringListUsageStatement(List.of(), Set.of()));

		assertEquals(new StringListUsageStatement(input, Set.of()), usageStatement);
	}

}