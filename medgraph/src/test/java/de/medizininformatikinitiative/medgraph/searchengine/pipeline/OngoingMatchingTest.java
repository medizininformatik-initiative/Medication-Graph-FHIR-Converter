package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.*;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.PredefinedScoreJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ProductOnlyFilter;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.PredefinedMatchTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static de.medizininformatikinitiative.medgraph.searchengine.TestFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class OngoingMatchingTest {

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void applyFilter(boolean ensureSurvival) {
		OngoingMatching sut = createSut(List.of(
				SAMPLE_PRODUCT_1,
				SAMPLE_SUBSTANCE_1,
				SAMPLE_SUBSTANCE_3
		));

		assertTrue(sut.applyFilter(new ProductOnlyFilter(), ensureSurvival));

		List<MatchingObject> currentMatches = sut.getCurrentMatches();
		assertEquals(1, currentMatches.size());
		MatchingObject object = currentMatches.getFirst();
		assertEquals(SAMPLE_PRODUCT_1, object.getObject());
		assertTrue(object.getAppliedJudgements().getFirst().isPassed());
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void applyFilterWithSurvivalInQuestion(boolean ensureSurvival) {
		OngoingMatching sut = createSut(List.of(
				SAMPLE_SUBSTANCE_1,
				SAMPLE_SUBSTANCE_3
		));

		assertFalse(sut.applyFilter(new ProductOnlyFilter(), ensureSurvival));

		List<MatchingObject> currentMatches = sut.getCurrentMatches();

		if (ensureSurvival) {
			assertEquals(2, currentMatches.size());
			MatchingObject object = currentMatches.getFirst();
			assertEquals(SAMPLE_SUBSTANCE_1, object.getObject());
			assertFalse(object.getAppliedJudgements().getFirst().isPassed());
		} else {
			assertTrue(currentMatches.isEmpty());
		}
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void applyScoreJudge(boolean ensureSurvival) {
		OngoingMatching sut = createSut(List.of(
				new Substance(98, ""),
				new Product(102, "Dope"),
				new Product(100, ""),
				new Substance(100, ""),
				new Substance(99, "")
		));

		sut.applyScoreJudge(new IdSizeJudge(100.0), ensureSurvival);


		List<MatchingObject> currentMatches = sut.getCurrentMatches();
		assertEquals(3, currentMatches.size());
		MatchingObject object = currentMatches.getFirst();
		assertEquals(new Product(102, "Dope"), object.getObject());
		assertTrue(object.getAppliedJudgements().getFirst().isPassed());
		assertEquals(new Product(100, ""), currentMatches.get(1).getObject());
		assertEquals(new Substance(100, ""), currentMatches.get(2).getObject());
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void applyScoreJudgeWithSurvivalInQuestion(boolean ensureSurvival) {
		OngoingMatching sut = createSut(List.of(
				new Substance(98, ""),
				new Product(102, "Dope"),
				new Product(100, ""),
				new Substance(100, ""),
				new Substance(99, "")
		));

		sut.applyScoreJudge(new IdSizeJudge(105.0), ensureSurvival);


		List<MatchingObject> currentMatches = sut.getCurrentMatches();

		if (ensureSurvival) {
			assertEquals(5, currentMatches.size());
			MatchingObject object = currentMatches.getFirst();
			// Despite all failing, higher scores still win!
			assertEquals(new Product(102, ""), object.getObject());
			assertFalse(object.getAppliedJudgements().getFirst().isPassed());
		} else {
			assertEquals(0, currentMatches.size());
		}
	}

	@Test
	public void transform() {
		OngoingMatching sut = createSut(List.of(
				SAMPLE_SUBSTANCE_1,
				SAMPLE_PRODUCT_1,
				SAMPLE_PRODUCT_2
		));

		MatchingObject sourceObject1 = sut.getCurrentMatches().getFirst();

		sut.transformMatches(new PredefinedMatchTransformer(Map.of(
				SAMPLE_SUBSTANCE_1, List.of(SAMPLE_SUBSTANCE_2, SAMPLE_SUBSTANCE_3)
		)));

		List<MatchingObject> objects = sut.getCurrentMatches();
		assertEquals(2, objects.size());

		assertEquals(SAMPLE_SUBSTANCE_2, objects.get(0).getObject());
		assertEquals(SAMPLE_SUBSTANCE_3, objects.get(1).getObject());
		assertInstanceOf(TransformedObject.class, objects.getFirst());
		assertNotNull(((TransformedObject) objects.getFirst()).getTransformation());
		assertTrue(objects.getFirst().getAppliedJudgements().isEmpty());
		assertEquals(sourceObject1, ((TransformedObject) objects.get(0)).getSourceObject());
		assertEquals(sourceObject1, ((TransformedObject) objects.get(1)).getSourceObject());
	}

	@Test
	public void transformWithMerge() {
		OngoingMatching sut = createSut(List.of(
				SAMPLE_SUBSTANCE_1,
				SAMPLE_PRODUCT_1,
				SAMPLE_PRODUCT_2
		));

		List<MatchingObject> sourceObjects = sut.getCurrentMatches();

		sut.transformMatches(new PredefinedMatchTransformer(Map.of(
				SAMPLE_SUBSTANCE_1, List.of(SAMPLE_SUBSTANCE_2),
				SAMPLE_PRODUCT_1, List.of(SAMPLE_SUBSTANCE_2),
				SAMPLE_PRODUCT_2, List.of(SAMPLE_SUBSTANCE_2)
		)));

		List<MatchingObject> resultObjects = sut.getCurrentMatches();
		assertEquals(1, resultObjects.size());

		MatchingObject obj = resultObjects.getFirst();
		assertInstanceOf(Merge.class, obj);
		Merge merge = (Merge) obj;

		assertEquals(SAMPLE_SUBSTANCE_2, merge.getObject());
		// Source objects of the merge are the TransformedObject-instances, so we need to get their source to get back
		// to the original MatchingObject instances.
		assertEquals(sourceObjects, merge.getSourceObjects()
		                                 .stream()
		                                 .map(m -> ((TransformedObject) m).getSourceObject())
		                                 .toList());
	}

	@Test
	public void sortAndTransformWithMerge() {
		OngoingMatching sut = createSut(List.of(
				SAMPLE_SUBSTANCE_2,
				SAMPLE_SUBSTANCE_1,
				SAMPLE_PRODUCT_1,
				SAMPLE_PRODUCT_2
		));

		sut.applyScoreJudge(new PredefinedScoreJudge(Map.of(
				SAMPLE_SUBSTANCE_2, 2.5,
				SAMPLE_SUBSTANCE_1, 2.0,
				SAMPLE_PRODUCT_2, 1.5,
				SAMPLE_PRODUCT_1, 1.0
		), 1.0), false);

		sut.transformMatches(new PredefinedMatchTransformer(Map.of(
				SAMPLE_SUBSTANCE_2, List.of(SAMPLE_SUBSTANCE_2),
				SAMPLE_SUBSTANCE_1, List.of(SAMPLE_SUBSTANCE_3),
				SAMPLE_PRODUCT_2, List.of(SAMPLE_PRODUCT_2, SAMPLE_PRODUCT_3),
				SAMPLE_PRODUCT_1, List.of(SAMPLE_SUBSTANCE_3) // Should merge with 2nd-previous row
		)));

		List<MatchingObject> matchingObjects = sut.getCurrentMatches();
		assertEquals(List.of(SAMPLE_SUBSTANCE_2, SAMPLE_SUBSTANCE_3, SAMPLE_PRODUCT_2, SAMPLE_PRODUCT_3),
				matchingObjects.stream().map(MatchingObject::getObject).toList());

		assertInstanceOf(TransformedObject.class, matchingObjects.get(0));
		assertInstanceOf(Merge.class, matchingObjects.get(1));
		assertInstanceOf(TransformedObject.class, matchingObjects.get(2));
		assertInstanceOf(TransformedObject.class, matchingObjects.get(3));
	}

	@Test
	public void multipleActions() {
		OngoingMatching sut = createSut(List.of(
				new Substance(97, ""),
				new Product(32, "Not so dope"),
				new Product(67, ""),
				new Product(100, "GREAT"),
				new Substance(107, "")
		));

		sut.applyScoreJudge(new IdSizeJudge(200.0), true);

		sut.applyScoreJudge(new IdSizeJudge(40.0), false);

		assertEquals(4, sut.getCurrentMatches().size());

		sut.transformMatches(new PredefinedMatchTransformer(Map.of(
				new Substance(107, ""), List.of(SAMPLE_SUBSTANCE_3),
				new Product(100, "GREAT"), List.of(),
				new Substance(97, ""), List.of(SAMPLE_SUBSTANCE_1, SAMPLE_PRODUCT_2),
				new Product(67, ""), List.of(SAMPLE_PRODUCT_3)
		)));

		assertTrue(sut.applyFilter(new ProductOnlyFilter(), true));

		List<MatchingObject> currentMatches = sut.getCurrentMatches();
		assertEquals(2, currentMatches.size());
		assertEquals(SAMPLE_PRODUCT_2, currentMatches.getFirst().getObject());
		assertEquals(SAMPLE_PRODUCT_3, currentMatches.getLast().getObject());


		List<Judgement> judgementList = currentMatches.getFirst().getAppliedJudgements();
		assertEquals(1, judgementList.size());
		assertTrue(judgementList.getFirst().isPassed());
		List<Judgement> judgementList2 = ((TransformedObject) currentMatches.getFirst()).getSourceObject().getAppliedJudgements();
		assertEquals(2, judgementList2.size());
		assertFalse(judgementList2.get(0).isPassed());
		assertTrue(judgementList2.get(1).isPassed());
		assertEquals(IdSizeJudge.NAME, judgementList2.get(0).getName());
		assertEquals(IdSizeJudge.NAME, judgementList2.get(1).getName());
		assertEquals(ProductOnlyFilter.NAME, judgementList.getFirst().getName());
	}

	private OngoingMatching createSut(List<Matchable> matchables) {
		return new OngoingMatching(matchables.stream().map(OriginalMatch::new).toList(), SAMPLE_SEARCH_QUERY);
	}


	private static class IdSizeJudge extends ScoreJudge {

		public static final String NAME = "Id Judge";

		public IdSizeJudge(Double passingScore) {
			super(passingScore);
		}

		@Override
		protected double judgeInternal(Matchable matchable, SearchQuery query) {
			return ((IdMatchable) matchable).getId();
		}

		@Override
		public String toString() {
			return NAME;
		}

		@Override
		public String getDescription() {
			return "Rates matches by their mmi id. I know, it makes no sense.";
		}
	}

}