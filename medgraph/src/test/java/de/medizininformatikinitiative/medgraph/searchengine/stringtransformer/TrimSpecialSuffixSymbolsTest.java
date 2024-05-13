package de.medizininformatikinitiative.medgraph.searchengine.stringtransformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class TrimSpecialSuffixSymbolsTest {

	@ParameterizedTest
	@ValueSource(strings = { ",", ".", "|", "®", ":", ";", "-" })
	public void trimSpecialSuffix(String suffix) {
		assertEquals(List.of("Rivaroxaban", "Tabletten"),
				new TrimSpecialSuffixSymbols().transform(List.of("Rivaroxaban"+suffix, "Tabletten")));
	}

	@Test
	public void trimMultiple() {
		assertEquals(List.of("Rivaroxaban", "Film-Tbl"),
				new TrimSpecialSuffixSymbols().transform(List.of("Rivaroxaban®,", "Film-Tbl.")));
	}

	@Test
	public void trimBlank() {
		assertEquals(List.of("Mouse"),
				new TrimSpecialSuffixSymbols().transform(List.of("", "Mouse."))); // Should be filtered out
	}

	@Test
	public void trimToBlank() {
		assertEquals(List.of(),
				new TrimSpecialSuffixSymbols().transform(List.of("-")));
	}

	@Test
	public void trimEmpty() {
		assertEquals(List.of(),
				new TrimSpecialSuffixSymbols().transform(List.of()));
	}

}