package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.medizininformatikinitiative.medgraph.TestFactory.SUBSTANCES_PROVIDER;
import static de.medizininformatikinitiative.medgraph.TestFactory.Substances.*;
import static de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.SubstanceQueryRefiner.Result;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		Result result = parse("Vino di Italia");

		assertTrue(result.getSubstances().isEmpty());
		assertEquals("", result.getUsageStatement().getUsedParts());
	}

	@Test
	void simpleMatch() {
		Result result = parse("Acetylsalicylsäure");

		assertEquals(List.of(ACETYLSALICYLIC_ACID), result.getSubstances());
		assertEquals("Acetylsalicylsäure", result.getUsageStatement().getUsedParts());
	}

	@Test
	void multiMatch() {
		Result result = parse("Acetylsalicylsäure Prednisolon Bayer");
		assertEqualsIgnoreOrder(List.of(ACETYLSALICYLIC_ACID, PREDNISOLONE, PREDNISOLONE_HYDROGENSUCCINATE), result.getSubstances());
		assertUsedParts(List.of("Acetylsalicylsäure", "Prednisolon"), result.getUsageStatement());
	}

	@Test
	void multiMatchButNotEquallyGood() {
		Substance cimetidin = new Substance(8, "Cimetidin acis");
		sut = new SubstanceQueryRefiner(BaseProvider.ofIdentifiers(List.of(
				new MappedIdentifier<>("Acetylsalicylic acid", ACETYLSALICYLIC_ACID),
				new MappedIdentifier<>("Cimetidin acis", cimetidin)
		)));
		Result result = parse("Cimetidin acis");
		assertEquals(List.of(cimetidin), result.getSubstances());
		assertUsedParts(List.of("Cimetidin", "acis"), result.getUsageStatement());


		result = parse("acis"); // While it matches "acid" closely enough, it matches "acis" better!
		assertEquals(List.of(cimetidin), result.getSubstances());
		assertUsedParts(List.of("acis"), result.getUsageStatement());

		result = parse("acir");
		assertEqualsIgnoreOrder(List.of(cimetidin, ACETYLSALICYLIC_ACID), result.getSubstances());
		assertUsedParts(List.of("acir"), result.getUsageStatement());
	}

	@Test
	void spellingError() {
		Result result = parse("Midazloam");
		assertEqualsIgnoreOrder(List.of(MIDAZOLAM, MIDAZOLAM_HYDROCHLORIDE), result.getSubstances());
		assertUsedParts(List.of("Midazloam"), result.getUsageStatement());
	}

	@Test
	void overlappingMatch() {
		Result result = parse("Midazolam hydrochlorid");
		assertEqualsIgnoreOrder(List.of(MIDAZOLAM_HYDROCHLORIDE), result.getSubstances());
		assertEquals("Midazolam hydrochlorid", result.getUsageStatement().getUsedParts());
	}

	@Test
	void incrementallyApply() {
		Result r1 = parse("Acetylsalicylsäure");
		Result r2 = parse("Prednisolon Bayer");
		SearchQuery.Builder builder = new SearchQuery.Builder();

		r1.incrementallyApply(builder);
		r2.incrementallyApply(builder);

		SearchQuery query = builder.build();

		assertEqualsIgnoreOrder(List.of(PREDNISOLONE, PREDNISOLONE_HYDROGENSUCCINATE, ACETYLSALICYLIC_ACID), query.getSubstances());
	}

	private Result parse(String query) {
		return sut.parse(new OriginalIdentifier<>(query, OriginalIdentifier.Source.RAW_QUERY));
	}

}