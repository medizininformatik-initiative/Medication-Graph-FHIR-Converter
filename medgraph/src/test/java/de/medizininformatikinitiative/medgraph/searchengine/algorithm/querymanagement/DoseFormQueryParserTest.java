package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.TestFactory.DoseForms.*;
import static de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.DoseFormQueryParser.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class DoseFormQueryParserTest extends UnitTest {

	private final BaseProvider<String> edqmProvider = BaseProvider.ofIdentifiers(List.of(
			new MappedIdentifier<>("Oral Granules", GRANULES),
			new MappedIdentifier<>("Granules", Characteristics.GRANULES),
			new MappedIdentifier<>("Oral", Characteristics.ORAL),
			new MappedIdentifier<>("solution for injection", SOLUTION_FOR_INJECTION),
			new MappedIdentifier<>("Powder for solution for injection", POWDER_FOR_SOLUTION_FOR_INJECTION)
	));

	private DoseFormQueryParser sut;

	@BeforeEach
	void setUp() {
		sut = new DoseFormQueryParser(edqmProvider);
	}

	@Test
	void noMatch() {
		Result result = sut.parse("Aspirin 100mg");
		assertEquals(Collections.emptyList(), result.getDoseForms());
		assertEquals(Collections.emptyList(), result.getCharacteristics());
		assertEquals(Collections.emptySet(), result.getUsageStatement().getUsedRanges());
	}

	@Test
	void simpleMatch() {
		Result result = sut.parse("Tranexamic acid granules");
		assertEquals(Collections.emptyList(), result.getDoseForms());
		assertEquals(List.of(Characteristics.GRANULES), result.getCharacteristics());
		assertEquals("granules", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void multiMatch() {
		Result result = sut.parse("Prednisolon oral granules");
		assertEquals(List.of(GRANULES), result.getDoseForms());
		assertEquals(Collections.emptySet(), new HashSet<>(result.getCharacteristics()));
		assertEquals("oral granules", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void overlappingMatch() {
		Result result = sut.parse("Prednisolon powder for solution for injection");
		assertEquals(List.of(POWDER_FOR_SOLUTION_FOR_INJECTION), result.getDoseForms());
		assertEquals(List.of(), result.getCharacteristics());
		assertEquals("powder for solution for injection", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void multiMatchWithOverlap() {
		Result result = sut.parse("Prednisolon oral powder for solution for injection"); // Yeah I know this one makes no sense...
		assertEquals(List.of(POWDER_FOR_SOLUTION_FOR_INJECTION), result.getDoseForms());
		assertEquals(List.of(Characteristics.ORAL), result.getCharacteristics());
		assertEquals("oral powder for solution for injection", result.getUsageStatement().getUsedParts().trim());
	}
}