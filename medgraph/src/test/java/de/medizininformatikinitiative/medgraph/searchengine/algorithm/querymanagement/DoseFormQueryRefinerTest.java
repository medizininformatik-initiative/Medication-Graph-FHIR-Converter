package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DetailedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.tools.SearchEngineTools;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.InputUsageTraceable;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.TestFactory.DoseForms.*;
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
		DoseFormQueryRefiner.Result result = parse("Aspirin 100mg");
		assertEquals(Collections.emptyList(), result.getDoseForms());
		assertEquals(Collections.emptyList(), result.getCharacteristics());
		assertEquals(Collections.emptySet(), result.getUsageStatement().getUsedRanges());
	}

	@Test
	void simpleMatch() {
		DoseFormQueryRefiner.Result result = parse("Tranexamic acid granules");
		assertEquals(List.of(GRANULES), SearchEngineTools.unwrap(result.getDoseForms()));
		assertEquals(List.of(TestFactory.DoseForms.Characteristics.GRANULES),
				SearchEngineTools.unwrap(result.getCharacteristics()));
		assertEquals("granules", result.getUsageStatement().getUsedParts().trim());
		assertSourceTokenIndices(Set.of(2), result.getDoseForms().getFirst());
		assertSourceTokenIndices(Set.of(2), result.getCharacteristics().getFirst());
	}

	@Test
	void multiMatch() {
		DoseFormQueryRefiner.Result result = parse("Prednisolon oral granules");
		assertEquals(List.of(GRANULES), SearchEngineTools.unwrap(result.getDoseForms()));
		assertEqualsIgnoreOrder(List.of(Characteristics.ORAL, Characteristics.GRANULES),
				SearchEngineTools.unwrap(result.getCharacteristics()));
		assertEquals("oral granules", result.getUsageStatement().getUsedParts().trim());

		assertSourceTokenIndices(Set.of(2), result.getDoseForms().getFirst());
		assertSourceTokenIndicesFor(Characteristics.GRANULES, Set.of(2), result.getCharacteristics());
		assertSourceTokenIndicesFor(Characteristics.ORAL, Set.of(1), result.getCharacteristics());
	}

	@Test
	void overlappingMatch() {
		DoseFormQueryRefiner.Result result = parse("Prednisolon powder for solution for injection");
		assertEqualsIgnoreOrder(List.of(POWDER_FOR_SOLUTION_FOR_INJECTION, SOLUTION_FOR_INJECTION),
				SearchEngineTools.unwrap(result.getDoseForms()));
		assertEqualsIgnoreOrder(List.of(Characteristics.POWDER, Characteristics.SOLUTION),
				SearchEngineTools.unwrap(result.getCharacteristics()));
		assertEquals("powder for solution for injection", result.getUsageStatement().getUsedParts().trim());

		assertSourceTokenIndicesFor(POWDER_FOR_SOLUTION_FOR_INJECTION, Set.of(1, 2, 3, 4, 5),
				result.getDoseForms());
		assertSourceTokenIndicesFor(SOLUTION_FOR_INJECTION, Set.of(3, 4, 5), result.getDoseForms());
		assertSourceTokenIndicesFor(Characteristics.POWDER, Set.of(1), result.getCharacteristics());
		assertSourceTokenIndicesFor(Characteristics.SOLUTION, Set.of(3), result.getCharacteristics());
	}

	@Test
	void multiMatchWithOverlap() {
		DoseFormQueryRefiner.Result result = parse(
				"Prednisolon oral powder for solution for injection"); // Yeah I know this one makes no sense...
		assertEqualsIgnoreOrder(List.of(POWDER_FOR_SOLUTION_FOR_INJECTION, SOLUTION_FOR_INJECTION),
				SearchEngineTools.unwrap(result.getDoseForms()));
		assertEqualsIgnoreOrder(List.of(Characteristics.ORAL, Characteristics.POWDER, Characteristics.SOLUTION),
				SearchEngineTools.unwrap(result.getCharacteristics()));
		assertEquals("oral powder for solution for injection", result.getUsageStatement().getUsedParts().trim());
	}

	@Test
	void incrementallyApply() {
		DoseFormQueryRefiner.Result r1 = parse("Prednisolon parenteral solution for injection");
		DoseFormQueryRefiner.Result r2 = parse("Tranexamsäure oral granules parenteral");

		RefinedQuery.Builder builder = new RefinedQuery.Builder()
				.withProductNameKeywords(
						new OriginalIdentifier<>(Collections.emptyList(), OriginalIdentifier.Source.RAW_QUERY));
		r1.incrementallyApply(builder);
		r2.incrementallyApply(builder);
		RefinedQuery query = builder.build();

		assertEquals(List.of(SOLUTION_FOR_INJECTION, GRANULES), SearchEngineTools.unwrap(query.getDoseForms()));
		assertEqualsIgnoreOrder(List.of(Characteristics.ORAL, Characteristics.PARENTERAL, Characteristics.GRANULES,
				Characteristics.SOLUTION), SearchEngineTools.unwrap(query.getDoseFormCharacteristics()));
	}

	@Test
	void problematicOverlap() {
		EdqmPharmaceuticalDoseForm suspension = new EdqmPharmaceuticalDoseForm("PDF-11111111", "oral suspension",
				List.of());
		EdqmPharmaceuticalDoseForm gum = new EdqmPharmaceuticalDoseForm("PDF-11111112", "gum", List.of());

		sut = new DoseFormQueryRefiner(BaseProvider.ofIdentifiers(List.of(
				new MappedIdentifier<>("Susp. zum Einnehmen", suspension),
				new MappedIdentifier<>("Gum", gum)
		)));

		// Problem is: "Gum" only has an edit distance of "1" to "zum". But we still don't want it as a result.
		DoseFormQueryRefiner.Result r1 = parse("Sulfamethoxzaol 400mg Susp. zum Einnehmen");
		assertEquals(List.of(suspension), SearchEngineTools.unwrap(r1.getDoseForms()));

		// Here however, Gum is the only one written correctly, so it should win - altough this is probably not
		// what the user meant...
		DoseFormQueryRefiner.Result r2 = parse("Sulfamethoxzaol 400mg Susp. gum Einnehmen");
		assertEquals(List.of(gum), SearchEngineTools.unwrap(r2.getDoseForms()));
	}

	@Test
	void scoreAssigned() {
		DoseFormQueryRefiner.Result result1 = parse("Granuls");
		DoseFormQueryRefiner.Result result2 = parse("Granules");
		assertEqualsIgnoreOrder(List.of(GRANULES), SearchEngineTools.unwrap(result1.getDoseForms()));
		assertEqualsIgnoreOrder(List.of(GRANULES), SearchEngineTools.unwrap(result2.getDoseForms()));
		assertTrue(result1.getDoseForms().getFirst().getScore() < result2.getDoseForms().getFirst().getScore());
	}

	private DoseFormQueryRefiner.Result parse(String query) {
		return sut.parse(new OriginalIdentifier<>(query, OriginalIdentifier.Source.RAW_QUERY));
	}

	/**
	 * Runs {@link #assertSourceTokenIndices(Set, MatchingObject)} for every {@link MatchingObject} whose object is
	 * equal to the passed object.
	 */
	private <T extends Matchable> void assertSourceTokenIndicesFor(T object, Set<Integer> tokens,
	                                                               List<MatchingObject<T>> objects) {
		for (MatchingObject<T> obj : objects) {
			if (Objects.equals(object, obj.getObject())) {
				assertSourceTokenIndices(tokens, obj);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void assertSourceTokenIndices(Set<Integer> tokens, MatchingObject<?> object) {
		assertInstanceOf(OriginalMatch.class, object);
		Origin origin = ((OriginalMatch<?>) object).getOrigin();
		assertInstanceOf(MatchOrigin.class, origin);
		MatchOrigin<?> matchOrigin = (MatchOrigin<?>) origin;
		assertInstanceOf(DetailedMatch.class, matchOrigin.getMatch());
		DetailedMatch<?, ?, ?> match = (DetailedMatch<?, ?, ?>) matchOrigin.getMatch();
		assertInstanceOf(InputUsageTraceable.class, match.getMatchInfo());
		StringListUsageStatement listUsageStatement = ((InputUsageTraceable<StringListUsageStatement>) match.getMatchInfo()).getUsageStatement();
		assertEquals(tokens, listUsageStatement.getUsedIndices());
	}

}