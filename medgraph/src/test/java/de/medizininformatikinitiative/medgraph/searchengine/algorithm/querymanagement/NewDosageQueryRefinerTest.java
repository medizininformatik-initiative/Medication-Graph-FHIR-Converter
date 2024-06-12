package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DosageDetectorOrigin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.tools.SearchEngineTools;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.IntRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author Markus Budeus
 */
public class NewDosageQueryRefinerTest extends UnitTest {

	private NewDosageQueryRefiner sut;

	@BeforeEach
	void setUp() {
		sut = new NewDosageQueryRefiner();
	}

	@Test
	public void simpleDosage() {
		NewDosageQueryRefiner.Result result = parse("Aspirin 500 mg");

		assertEquals(List.of(Dosage.of(500, "mg")), SearchEngineTools.unwrap(result.getDosages()));
		assertEquals(List.of(new Amount(new BigDecimal(500), "mg")), SearchEngineTools.unwrap(result.getAmounts()));
		assertEquals(new DistinctMultiSubstringUsageStatement("Aspirin 500 mg",
				Set.of(new IntRange(8, 14))), result.getUsageStatement());


		expectOrigin(result.getDosages().getFirst(), "Aspirin 500 mg", 8, 6);
		expectOrigin(result.getAmounts().getFirst(), "Aspirin 500 mg", 8, 6);
	}

	@Test
	public void multipleDosages() {
		NewDosageQueryRefiner.Result result = parse("Aspirin 500 mg 10mg/ml");

		assertEquals(List.of(Dosage.of(500, "mg"), Dosage.of(10, "mg", 1, "ml")),
				SearchEngineTools.unwrap(result.getDosages()));
		assertEquals(List.of(new Amount(new BigDecimal(500), "mg")),
				SearchEngineTools.unwrap(result.getAmounts()));
		assertEquals(new DistinctMultiSubstringUsageStatement("Aspirin 500 mg 10mg/ml",
				Set.of(new IntRange(8, 14), new IntRange(15, 22))), result.getUsageStatement());

		expectOrigin(result.getDosages().getFirst(), "Aspirin 500 mg 10mg/ml", 8, 6);
		expectOrigin(result.getDosages().get(1), "Aspirin 500 mg 10mg/ml", 15, 7);
		expectOrigin(result.getAmounts().getFirst(), "Aspirin 500 mg 10mg/ml", 8, 6);
	}

	@Test
	public void noDosages() {
		NewDosageQueryRefiner.Result result = parse("Tranexamsäure");

		assertEquals(List.of(), result.getDosages());
		assertEquals(List.of(), result.getAmounts());
		assertEquals(new DistinctMultiSubstringUsageStatement("Tranexamsäure", Set.of()), result.getUsageStatement());
	}

	@Test
	public void incrementallyApply() {
		NewDosageQueryRefiner.Result r1 = parse("500 mg");
		NewDosageQueryRefiner.Result r2 = parse("10 ml");

		NewRefinedQuery.Builder builder = new NewRefinedQuery.Builder()
				.withProductNameKeywords(new OriginalIdentifier<>(Collections.emptyList(), OriginalIdentifier.Source.RAW_QUERY));
		r1.incrementallyApply(builder);
		r2.incrementallyApply(builder);

		Dosage d1 = Dosage.of(500, "mg");
		Dosage d2 = Dosage.of(10, "ml");
		NewRefinedQuery query = builder.build();

		assertEquals(List.of(d1, d2), SearchEngineTools.unwrap(query.getDosages()));
		assertEquals(List.of(d1.amountNominator, d2.amountNominator), SearchEngineTools.unwrap(query.getDrugAmounts()));
	}

	private NewDosageQueryRefiner.Result parse(String query) {
		return sut.parse(new OriginalIdentifier<>(query, OriginalIdentifier.Source.RAW_QUERY));
	}

	private void expectOrigin(MatchingObject<?> object, String originString, int startIndex, int length) {
		assertInstanceOf(OriginalMatch.class, object);
		Origin origin = ((OriginalMatch<?>) object).getOrigin();
		assertInstanceOf(DosageDetectorOrigin.class, origin);
		DosageDetectorOrigin ddOrigin = (DosageDetectorOrigin) origin;
		assertEquals(originString, ddOrigin.identifier().getIdentifier());
		assertEquals(startIndex, ddOrigin.detectedDosage().getStartIndex());
		assertEquals(length, ddOrigin.detectedDosage().getLength());
	}

}