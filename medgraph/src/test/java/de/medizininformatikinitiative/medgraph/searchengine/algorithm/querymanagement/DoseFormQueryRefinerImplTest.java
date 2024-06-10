package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.TestFactory.DoseForms.*;
import static de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.DoseFormQueryRefiner.Result;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class DoseFormQueryRefinerImplTest extends UnitTest {

	private DoseFormQueryRefiner sut;

	@BeforeEach
	void setUp() {
		sut = new DoseFormQueryRefiner(TestFactory.EDQM_PROVIDER);
	}

	@Test
	void noMatch() {
		Result result = parse("Aspirin 100mg");
		assertEquals(Collections.emptyList(), result.getDoseForms());
		assertEquals(Collections.emptyList(), result.getCharacteristics());
		assertEquals(Collections.emptySet(), result.getUsageStatement().getUsedRanges());
	}

	@Test
	void simpleMatch() {
		Result result = parse("Tranexamic acid granules");
		assertEquals(List.of(GRANULES), result.getDoseForms());
		assertEquals(List.of(Characteristics.GRANULES), result.getCharacteristics());
		assertEquals("granules", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void multiMatch() {
		Result result = parse("Prednisolon oral granules");
		assertEquals(List.of(GRANULES), result.getDoseForms());
		assertEqualsIgnoreOrder(List.of(Characteristics.ORAL, Characteristics.GRANULES), result.getCharacteristics());
		assertEquals("oral granules", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void overlappingMatch() {
		Result result = parse("Prednisolon powder for solution for injection");
		assertEqualsIgnoreOrder(List.of(POWDER_FOR_SOLUTION_FOR_INJECTION, SOLUTION_FOR_INJECTION),
				result.getDoseForms());
		assertEqualsIgnoreOrder(List.of(Characteristics.POWDER, Characteristics.SOLUTION), result.getCharacteristics());
		assertEquals("powder for solution for injection", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void multiMatchWithOverlap() {
		Result result = parse(
				"Prednisolon oral powder for solution for injection"); // Yeah I know this one makes no sense...
		assertEqualsIgnoreOrder(List.of(POWDER_FOR_SOLUTION_FOR_INJECTION, SOLUTION_FOR_INJECTION),
				result.getDoseForms());
		assertEqualsIgnoreOrder(List.of(Characteristics.ORAL, Characteristics.POWDER, Characteristics.SOLUTION),
				result.getCharacteristics());
		assertEquals("oral powder for solution for injection", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void incrementallyApply() {
		Result r1 = parse("Prednisolon parenteral solution for injection");
		Result r2 = parse("Tranexamsäure oral granules parenteral");

		SearchQuery.Builder builder = new SearchQuery.Builder();
		r1.incrementallyApply(builder);
		r2.incrementallyApply(builder);
		SearchQuery query = builder.build();

		assertEquals(List.of(SOLUTION_FOR_INJECTION, GRANULES), query.getDoseForms());
		assertEqualsIgnoreOrder(List.of(Characteristics.ORAL, Characteristics.PARENTERAL, Characteristics.GRANULES,
				Characteristics.SOLUTION), query.getDoseFormCharacteristics());
	}

	@Test
	void problematicOverlap() {
		EdqmPharmaceuticalDoseForm suspension = new EdqmPharmaceuticalDoseForm("PDF-11111111", "oral suspension", List.of());
		EdqmPharmaceuticalDoseForm gum = new EdqmPharmaceuticalDoseForm("PDF-11111112", "gum", List.of());

		sut = new DoseFormQueryRefiner(BaseProvider.ofIdentifiers(List.of(
				new MappedIdentifier<>("Susp. zum Einnehmen", suspension),
				new MappedIdentifier<>("Gum", gum)
		)));

		// Problem is: "Gum" only has an edit distance of "1" to "zum". But we still don't want it as a result.
		Result r1 = parse("Sulfamethoxzaol 400mg Susp. zum Einnehmen");
		assertEquals(List.of(suspension), r1.getDoseForms());

		// Here however, Gum is the only one written correctly, so it should win - altough this is probably not
		// what the user meant...
		Result r2 = parse("Sulfamethoxzaol 400mg Susp. gum Einnehmen");
		assertEquals(List.of(gum), r2.getDoseForms());
	}

	private Result parse(String query) {
		return sut.parse(new OriginalIdentifier<>(query, OriginalIdentifier.Source.RAW_QUERY));
	}
}