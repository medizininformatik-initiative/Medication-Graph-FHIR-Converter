package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static de.medizininformatikinitiative.medgraph.TestFactory.Substances.*;
import static de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.SubstanceQueryRefiner.Result;
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
		Result result = sut.parse("Vino di Italia");

		assertTrue(result.getSubstances().isEmpty());
		assertEquals("", result.getUsageStatement().getUsedParts());
	}

	@Test
	void simpleMatch() {
		Result result = sut.parse("Acetylsalicylsäure");

		assertEquals(List.of(ACETYLSALICYLIC_ACID), result.getSubstances());
		assertEquals("Acetylsalicylsäure", result.getUsageStatement().getUsedParts());
	}

	@Test
	void multiMatch() {
		Result result = sut.parse("Acetylsalicylsäure Prednisolon Bayer");
		assertEqualsIgnoreOrder(List.of(ACETYLSALICYLIC_ACID, PREDNISOLONE, PREDNISOLONE_HYDROGENSUCCINATE), result.getSubstances());
		assertUsedParts(List.of("Acetylsalicylsäure", "Prednisolon"), result.getUsageStatement());
	}

	@Test
	void spellingError() {
		Result result = sut.parse("Midazloam");
		assertEqualsIgnoreOrder(List.of(MIDAZOLAM, MIDAZOLAM_HYDROCHLORIDE), result.getSubstances());
		assertUsedParts(List.of("Midazloam"), result.getUsageStatement());
	}

	@Test
	void overlappingMatch() {
		Result result = sut.parse("Midazolam hydrochlorid");
		assertEqualsIgnoreOrder(List.of(MIDAZOLAM_HYDROCHLORIDE, MIDAZOLAM), result.getSubstances());
		assertEquals("Midazolam hydrochlorid", result.getUsageStatement().getUsedParts());
	}

	@Test
	void incrementallyApply() {
		Result r1 = sut.parse("Acetylsalicylsäure");
		Result r2 = sut.parse("Prednisolon Bayer");
		SearchQuery.Builder builder = new SearchQuery.Builder();

		r1.incrementallyApply(builder);
		r2.incrementallyApply(builder);

		SearchQuery query = builder.build();

		assertEqualsIgnoreOrder(List.of(PREDNISOLONE, PREDNISOLONE_HYDROGENSUCCINATE, ACETYLSALICYLIC_ACID), query.getSubstances());
	}

}