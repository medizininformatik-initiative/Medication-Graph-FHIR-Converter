package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.DosageQueryParser.Result;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class DosageQueryParserTest extends UnitTest {

	private DosageQueryParser sut;

	@BeforeEach
	void setUp() {
		sut = new DosageQueryParser();
	}

	@Test
	public void simpleDosage() {
		Result result = sut.parse("Aspirin 500 mg");

		assertEquals(List.of(Dosage.of(500, "mg")), result.getDosages());
		assertEquals(List.of(new Amount(new BigDecimal(500), "mg")), result.getAmounts());
		assertEquals(new DistinctMultiSubstringUsageStatement("Aspirin 500 mg",
				Set.of(new IntRange(8, 14))), result.getUsageStatement());
	}

	@Test
	public void multipleDosages() {
		Result result = sut.parse("Aspirin 500 mg 10mg/ml");

		assertEquals(List.of(Dosage.of(500, "mg"), Dosage.of(10, "mg", 1, "ml")), result.getDosages());
		assertEquals(List.of(new Amount(new BigDecimal(500), "mg")), result.getAmounts());
		assertEquals(new DistinctMultiSubstringUsageStatement("Aspirin 500 mg 10mg/ml",
				Set.of(new IntRange(8, 14), new IntRange(15, 22))), result.getUsageStatement());
	}

	@Test
	public void noDosages() {
		Result result = sut.parse("Tranexamsäure");

		assertEquals(List.of(), result.getDosages());
		assertEquals(List.of(), result.getAmounts());
		assertEquals(new DistinctMultiSubstringUsageStatement("Tranexamsäure", Set.of()), result.getUsageStatement());
	}

}