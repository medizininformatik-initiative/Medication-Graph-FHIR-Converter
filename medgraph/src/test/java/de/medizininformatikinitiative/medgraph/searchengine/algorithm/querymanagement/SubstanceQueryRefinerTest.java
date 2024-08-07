package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DetailedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.tools.SearchEngineTools;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.InputUsageTraceable;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringSetUsageStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.TestFactory.SUBSTANCES_PROVIDER;
import static de.medizininformatikinitiative.medgraph.TestFactory.Substances.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class SubstanceQueryRefinerTest extends UnitTest {

	private SubstanceQueryRefiner sut;

	@BeforeEach
	void setUp() {
		sut = new SubstanceQueryRefiner(SUBSTANCES_PROVIDER);
	}

	@Test
	void noMatch() {
		SubstanceQueryRefiner.Result result = parse("Vino di Italia");

		assertTrue(result.getSubstances().isEmpty());
		assertEquals("", result.getUsageStatement().getUsedParts());
	}

	@Test
	void simpleMatch() {
		SubstanceQueryRefiner.Result result = parse("Acetylsalicylsäure");

		assertEquals(List.of(ACETYLSALICYLIC_ACID), SearchEngineTools.unwrap(result.getSubstances()));
		assertEquals("Acetylsalicylsäure", result.getUsageStatement().getUsedParts());
		assertSourceTokens(Set.of("acetylsalicylsäure"), result.getSubstances().getFirst());
	}

	@Test
	void multiMatch() {
		SubstanceQueryRefiner.Result result = parse("Acetylsalicylsäure Prednisolon Bayer");
		assertEqualsIgnoreOrder(List.of(ACETYLSALICYLIC_ACID, PREDNISOLONE, PREDNISOLONE_HYDROGENSUCCINATE),
				SearchEngineTools.unwrap(result.getSubstances()));
		assertUsedParts(List.of("Acetylsalicylsäure", "Prednisolon"), result.getUsageStatement());

		assertSourceTokensFor(ACETYLSALICYLIC_ACID, Set.of("acetylsalicylsäure"), result.getSubstances());
		assertSourceTokensFor(PREDNISOLONE, Set.of("prednisolon"), result.getSubstances());
		assertSourceTokensFor(PREDNISOLONE_HYDROGENSUCCINATE, Set.of("prednisolon"), result.getSubstances());
	}

	@Test
	void multiMatchButNotEquallyGood() {
		Substance cimetidin = new Substance(8, "Cimetidin acis");
		sut = new SubstanceQueryRefiner(BaseProvider.ofIdentifiers(List.of(
				new MappedIdentifier<>("Acetylsalicylic acid", ACETYLSALICYLIC_ACID),
				new MappedIdentifier<>("Cimetidin acis", cimetidin)
		)));
		SubstanceQueryRefiner.Result result = parse("Cimetidin acis");
		assertEquals(List.of(cimetidin), SearchEngineTools.unwrap(result.getSubstances()));
		assertUsedParts(List.of("Cimetidin", "acis"), result.getUsageStatement());


		result = parse("acis"); // While it matches "acid" closely enough, it matches "acis" better!
		assertEquals(List.of(cimetidin), SearchEngineTools.unwrap(result.getSubstances()));
		assertUsedParts(List.of("acis"), result.getUsageStatement());

		result = parse("acir");
		assertEqualsIgnoreOrder(List.of(cimetidin, ACETYLSALICYLIC_ACID), SearchEngineTools.unwrap(result.getSubstances()));
		assertUsedParts(List.of("acir"), result.getUsageStatement());

		assertSourceTokensFor(cimetidin, Set.of("acir"), result.getSubstances());
		assertSourceTokensFor(ACETYLSALICYLIC_ACID, Set.of("acir"), result.getSubstances());
	}

	@Test
	void spellingError() {
		SubstanceQueryRefiner.Result result = parse("Midazloam");
		assertEqualsIgnoreOrder(List.of(MIDAZOLAM, MIDAZOLAM_HYDROCHLORIDE), SearchEngineTools.unwrap(result.getSubstances()));
		assertUsedParts(List.of("Midazloam"), result.getUsageStatement());
	}

	@Test
	void overlappingMatch() {
		SubstanceQueryRefiner.Result result = parse("Midazolam hydrochlorid");
		assertEqualsIgnoreOrder(List.of(MIDAZOLAM_HYDROCHLORIDE), SearchEngineTools.unwrap(result.getSubstances()));
		assertEquals("Midazolam hydrochlorid", result.getUsageStatement().getUsedParts());
	}

	@Test
	void incrementallyApply() {
		SubstanceQueryRefiner.Result r1 = parse("Acetylsalicylsäure");
		SubstanceQueryRefiner.Result r2 = parse("Prednisolon Bayer");
		RefinedQuery.Builder builder = new RefinedQuery.Builder()
				.withProductNameKeywords(
						new OriginalIdentifier<>(Collections.emptyList(), OriginalIdentifier.Source.RAW_QUERY));

		r1.incrementallyApply(builder);
		r2.incrementallyApply(builder);

		RefinedQuery query = builder.build();

		assertEqualsIgnoreOrder(List.of(PREDNISOLONE, PREDNISOLONE_HYDROGENSUCCINATE, ACETYLSALICYLIC_ACID),
				SearchEngineTools.unwrap(query.getSubstances()));
	}

	@Test
	void scoreAssigned() {
		SubstanceQueryRefiner.Result result1 = parse("Midazolam hydrochlorid");
		SubstanceQueryRefiner.Result result2 = parse("Midazloam hydrochlorid");
		assertEqualsIgnoreOrder(List.of(MIDAZOLAM_HYDROCHLORIDE), SearchEngineTools.unwrap(result1.getSubstances()));
		assertEqualsIgnoreOrder(List.of(MIDAZOLAM_HYDROCHLORIDE), SearchEngineTools.unwrap(result2.getSubstances()));
		assertTrue(result1.getSubstances().getFirst().getScore() > result2.getSubstances().getFirst().getScore());
	}

	private SubstanceQueryRefiner.Result parse(String query) {
		return sut.parse(new OriginalIdentifier<>(query, OriginalIdentifier.Source.RAW_QUERY));
	}

	/**
	 * Runs {@link #assertSourceTokens(Set, MatchingObject)} for every {@link MatchingObject} whose object is
	 * equal to the passed object.
	 */
	private <T extends Matchable> void assertSourceTokensFor(T object, Set<String> tokens,
	                                                               List<MatchingObject<T>> objects) {
		for (MatchingObject<T> obj : objects) {
			if (Objects.equals(object, obj.getObject())) {
				assertSourceTokens(tokens, obj);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void assertSourceTokens(Set<String> tokens, MatchingObject<?> object) {
		assertInstanceOf(OriginalMatch.class, object);
		Origin origin = ((OriginalMatch<?>) object).getOrigin();
		assertInstanceOf(MatchOrigin.class, origin);
		MatchOrigin<?> matchOrigin = (MatchOrigin<?>) origin;
		assertInstanceOf(DetailedMatch.class, matchOrigin.getMatch());
		DetailedMatch<?, ?, ?> match = (DetailedMatch<?, ?, ?>) matchOrigin.getMatch();
		assertInstanceOf(InputUsageTraceable.class, match.getMatchInfo());
		StringSetUsageStatement listUsageStatement = ((InputUsageTraceable<StringSetUsageStatement>) match.getMatchInfo()).getUsageStatement();
		assertEquals(tokens, listUsageStatement.getUsedParts());
	}

}