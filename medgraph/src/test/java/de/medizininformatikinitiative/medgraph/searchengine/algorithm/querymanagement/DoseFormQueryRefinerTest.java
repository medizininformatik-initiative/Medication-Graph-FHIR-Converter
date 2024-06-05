package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.TestFactory.DoseForms.*;
import static de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.DoseFormQueryRefiner.Result;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class DoseFormQueryRefinerTest extends UnitTest {

	private DoseFormQueryRefiner sut;

	@BeforeEach
	void setUp() {
		sut = new DoseFormQueryRefiner(TestFactory.EDQM_PROVIDER);
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
		assertEquals(List.of(GRANULES), result.getDoseForms());
		assertEquals(List.of(Characteristics.GRANULES), result.getCharacteristics());
		assertEquals("granules", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void multiMatch() {
		Result result = sut.parse("Prednisolon oral granules");
		assertEquals(List.of(GRANULES), result.getDoseForms());
		assertEqualsIgnoreOrder(List.of(Characteristics.ORAL, Characteristics.GRANULES), result.getCharacteristics());
		assertEquals("oral granules", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void overlappingMatch() {
		Result result = sut.parse("Prednisolon powder for solution for injection");
		assertEqualsIgnoreOrder(List.of(POWDER_FOR_SOLUTION_FOR_INJECTION, SOLUTION_FOR_INJECTION), result.getDoseForms());
		assertEqualsIgnoreOrder(List.of(Characteristics.POWDER, Characteristics.SOLUTION), result.getCharacteristics());
		assertEquals("powder for solution for injection", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void multiMatchWithOverlap() {
		Result result = sut.parse(
				"Prednisolon oral powder for solution for injection"); // Yeah I know this one makes no sense...
		assertEqualsIgnoreOrder(List.of(POWDER_FOR_SOLUTION_FOR_INJECTION, SOLUTION_FOR_INJECTION), result.getDoseForms());
		assertEqualsIgnoreOrder(List.of(Characteristics.ORAL, Characteristics.POWDER, Characteristics.SOLUTION),
				result.getCharacteristics());
		assertEquals("oral powder for solution for injection", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void incrementallyApply() {
		Result r1 = sut.parse("Prednisolon parenteral solution for injection");
		Result r2 = sut.parse("Tranexamsäure oral granules parenteral");

		SearchQuery.Builder builder = new SearchQuery.Builder();
		r1.incrementallyApply(builder);
		r2.incrementallyApply(builder);
		SearchQuery query = builder.build();

		assertEquals(List.of(SOLUTION_FOR_INJECTION, GRANULES), query.getDoseForms());
		assertEqualsIgnoreOrder(List.of(Characteristics.ORAL, Characteristics.PARENTERAL, Characteristics.GRANULES,
				Characteristics.SOLUTION), query.getDoseFormCharacteristics());
	}
}