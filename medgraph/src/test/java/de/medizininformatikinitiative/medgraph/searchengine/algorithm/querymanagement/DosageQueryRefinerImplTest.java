package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.DosageQueryRefiner.Result;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class DosageQueryRefinerImplTest extends UnitTest {

	private DosageQueryRefiner sut;

	@BeforeEach
	void setUp() {
		sut = new DosageQueryRefiner();
	}

	@Test
	public void simpleDosage() {
		Result result = parse("Aspirin 500 mg");

		assertEquals(List.of(Dosage.of(500, "mg")), result.getDosages());
		assertEquals(List.of(new Amount(new BigDecimal(500), "mg")), result.getAmounts());
		assertEquals(new DistinctMultiSubstringUsageStatement("Aspirin 500 mg",
				Set.of(new IntRange(8, 14))), result.getUsageStatement());
	}

	@Test
	public void multipleDosages() {
		Result result = parse("Aspirin 500 mg 10mg/ml");

		assertEquals(List.of(Dosage.of(500, "mg"), Dosage.of(10, "mg", 1, "ml")), result.getDosages());
		assertEquals(List.of(new Amount(new BigDecimal(500), "mg")), result.getAmounts());
		assertEquals(new DistinctMultiSubstringUsageStatement("Aspirin 500 mg 10mg/ml",
				Set.of(new IntRange(8, 14), new IntRange(15, 22))), result.getUsageStatement());
	}

	@Test
	public void noDosages() {
		Result result = parse("Tranexamsäure");

		assertEquals(List.of(), result.getDosages());
		assertEquals(List.of(), result.getAmounts());
		assertEquals(new DistinctMultiSubstringUsageStatement("Tranexamsäure", Set.of()), result.getUsageStatement());
	}

	@Test
	public void incrementallyApply() {
		Result r1 = parse("500 mg");
		Result r2 = parse("10 ml");

		SearchQuery.Builder builder = new SearchQuery.Builder();
		r1.incrementallyApply(builder);
		r2.incrementallyApply(builder);

		Dosage d1 = Dosage.of(500, "mg");
		Dosage d2 = Dosage.of(10, "ml");
		SearchQuery query = builder.build();

		assertEquals(List.of(d1, d2), query.getActiveIngredientDosages());
		assertEquals(List.of(d1.amountNominator, d2.amountNominator), query.getDrugAmounts());
	}

	private Result parse(String query) {
		return sut.parse(new OriginalIdentifier<>(query, OriginalIdentifier.Source.RAW_QUERY));
	}

}