package de.medizininformatikinitiative.medgraph.searchengine.pipeline;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.model.ScoreIncorporationStrategy;
import de.medizininformatikinitiative.medgraph.searchengine.model.ScoreJudgedObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.IdMatchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.*;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.PredefinedScoreJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ProductOnlyFilter;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudge;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudgeConfiguration;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.PredefinedMatchTransformer;
import de.medizininformatikinitiative.medgraph.searchengine.tools.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
@SuppressWarnings("unchecked")
public class MatchingPipelineServiceTest extends UnitTest {

	private MatchingPipelineService sut;

	@BeforeEach
	void setUp() {
		sut = new MatchingPipelineService(SAMPLE_SEARCH_QUERY);
	}

	@ParameterizedTest(name = "mayEliminateAll: {0}")
	@ValueSource(booleans = {true, false})
	public void applyFilter(boolean mayEliminateAll) {
		List<MatchingObject<IdMatchable>> initialList = Stream.of(
				SAMPLE_PRODUCT_1,
				SAMPLE_SUBSTANCE_1,
				SAMPLE_SUBSTANCE_3
		).map(OriginalMatch::new).collect(Collectors.toList());

		List<JudgedObject<IdMatchable>> list = sut.applyFilter(initialList, new ProductOnlyFilter(), mayEliminateAll);

		assertEquals(1, list.size());
		JudgedObject<IdMatchable> object = list.getFirst();
		assertEquals(SAMPLE_PRODUCT_1, object.getObject());
		assertTrue(object.getJudgement().passed());
	}

	@ParameterizedTest(name = "mayEliminateAll: {0}")
	@ValueSource(booleans = {true, false})
	public void applyFilterWithSurvivalInQuestion(boolean mayEliminateAll) {
		List<MatchingObject<Substance>> initialList = Stream.of(
				SAMPLE_SUBSTANCE_1,
				SAMPLE_SUBSTANCE_3
		).map(OriginalMatch::new).collect(Collectors.toList());

		List<JudgedObject<Substance>> list = sut.applyFilter(initialList, new ProductOnlyFilter(), mayEliminateAll);

		if (mayEliminateAll) {
			assertTrue(list.isEmpty());
		} else {
			assertEquals(2, list.size());
			JudgedObject<?> object = list.getFirst();
			assertEquals(SAMPLE_SUBSTANCE_1, object.getObject());
			assertFalse(object.getJudgement().passed());
		}
	}

	@ParameterizedTest(name = "mayEliminateAll: {0}")
	@ValueSource(booleans = {true, false})
	public void applyScoreJudge(boolean mayEliminateAll) {
		List<MatchingObject<Matchable>> initialList = Stream.<Matchable>of(
				new Substance(98, ""),
				new Product(102, "Dope"),
				new Product(100, ""),
				new Substance(100, ""),
				new Substance(99, "")
		).map(OriginalMatch::new).collect(Collectors.toList());

		List<ScoreJudgedObject<Matchable>> resultList = sut.applyScoreJudge(initialList,
				new MatchingPipelineServiceTest.IdSizeJudge(),
				new ScoreJudgeConfiguration(100.0, mayEliminateAll));

		assertEquals(3, resultList.size());
		ScoreJudgedObject<?> object = resultList.getFirst();
		assertEquals(new Product(102, "Dope"), object.getObject());
		assertTrue(object.getJudgement().passed());
		assertEquals(new Product(100, ""), resultList.get(1).getObject());
		assertEquals(new Substance(100, ""), resultList.get(2).getObject());
	}

	@Test
	public void applyScoreJudgeWithWeights() {
		List<MatchingObject<Matchable>> initialList = List.of(
				new OriginalMatch<>(new Substance(1, ""), 4.0, Origin.UNKNOWN),
				new OriginalMatch<>(new Product(2, ""), 4.0, Origin.UNKNOWN),
				new OriginalMatch<>(new Product(3, ""), 4.0, Origin.UNKNOWN),
				new OriginalMatch<>(new Substance(4, ""), 10.0, Origin.UNKNOWN),
				new OriginalMatch<>(new Substance(5, ""), 10.0, Origin.UNKNOWN)
		);

		List<ScoreJudgedObject<Matchable>> resultList = sut.applyScoreJudge(initialList,
				new MatchingPipelineServiceTest.IdSizeJudge(),
				new ScoreJudgeConfiguration(1.5, ScoreIncorporationStrategy.MULTIPLY));

		assertEquals(6.0, resultList.get(0).getScore(), 0.01);
		assertEquals(12.0, resultList.get(1).getScore(), 0.01);
		assertEquals(18.0, resultList.get(2).getScore(), 0.01);
		assertEquals(60.0, resultList.get(3).getScore(), 0.01);
		assertEquals(75.0, resultList.get(4).getScore(), 0.01);
	}

	@ParameterizedTest(name = "mayEliminateAll: {0}")
	@ValueSource(booleans = {true, false})
	public void applyScoreJudgeWithSurvivalInQuestion(boolean mayEliminateAll) {
		List<MatchingObject<Matchable>> initialList = Stream.<Matchable>of(
				new Substance(98, ""),
				new Product(102, "Dope"),
				new Product(100, ""),
				new Substance(100, ""),
				new Substance(99, "")
		).map(OriginalMatch::new).collect(Collectors.toList());

		List<ScoreJudgedObject<Matchable>> resultList = sut.applyScoreJudge(initialList,
				new MatchingPipelineServiceTest.IdSizeJudge(),
				new ScoreJudgeConfiguration(105.0, mayEliminateAll));


		if (mayEliminateAll) {
			assertEquals(0, resultList.size());
		} else {
			assertEquals(5, resultList.size());
			// Order should be preserved
			ScoreJudgedObject<?> object = resultList.getFirst();
			assertEquals(new Substance(98, ""), object.getObject());
			assertFalse(object.getJudgement().passed());
		}
	}

	@Test
	public void transform() {
		List<MatchingObject<Matchable>> initialList = Stream.<Matchable>of(
				SAMPLE_SUBSTANCE_1,
				SAMPLE_PRODUCT_1,
				SAMPLE_PRODUCT_2
		).map(OriginalMatch::new).collect(Collectors.toList());

		List<TransformedObject<Matchable, Matchable>> objects = sut.transformMatches(initialList,
				new PredefinedMatchTransformer(Map.of(
						SAMPLE_SUBSTANCE_1, List.of(SAMPLE_SUBSTANCE_2, SAMPLE_SUBSTANCE_3)
				)), ScoreMergingStrategy.SUM);

		assertEquals(2, objects.size());

		assertEquals(SAMPLE_SUBSTANCE_2, objects.get(0).getObject());
		assertEquals(SAMPLE_SUBSTANCE_3, objects.get(1).getObject());
		assertInstanceOf(TransformedObject.class, objects.getFirst());
		assertNotNull(((TransformedObject<?, ?>) objects.getFirst()).getTransformation());
		MatchingObject<Matchable> sourceObject1 = initialList.getFirst();
		assertEquals(sourceObject1, ((TransformedObject<?, ?>) objects.get(0)).getSource());
		assertEquals(sourceObject1, ((TransformedObject<?, ?>) objects.get(1)).getSource());
	}

	@Test
	public void transformWithMerge() {
		List<MatchingObject<Matchable>> initialList = Stream.<Matchable>of(
				SAMPLE_SUBSTANCE_1,
				SAMPLE_PRODUCT_1,
				SAMPLE_PRODUCT_2
		).map(OriginalMatch::new).collect(Collectors.toList());

		List<TransformedObject<Matchable, Matchable>> objects = sut.transformMatches(initialList,
				new PredefinedMatchTransformer(Map.of(
						SAMPLE_SUBSTANCE_1, List.of(SAMPLE_SUBSTANCE_2, SAMPLE_SUBSTANCE_3)
				)), ScoreMergingStrategy.SUM);

		assertEquals(2, objects.size());

		List<MatchingObject<?>> resultObjects = sut.transformMatches(initialList,
				new PredefinedMatchTransformer(Map.of(
						SAMPLE_SUBSTANCE_1, List.of(SAMPLE_SUBSTANCE_2),
						SAMPLE_PRODUCT_1, List.of(SAMPLE_SUBSTANCE_2),
						SAMPLE_PRODUCT_2, List.of(SAMPLE_SUBSTANCE_3)
				)), ScoreMergingStrategy.SUM);

		assertEquals(2, resultObjects.size());

		MatchingObject<?> obj = resultObjects.getFirst();
		assertInstanceOf(Merge.class, obj);
		Merge<?> merge = (Merge<?>) obj;

		assertEquals(SAMPLE_SUBSTANCE_2, merge.getObject());
		assertEquals(2, merge.getScore());
		// Source objects of the merge are the TransformedObject-instances, so we need to get their source to get back
		// to the original MatchingObject instances.
		assertEquals(List.of(initialList.get(0), initialList.get(1)),
				merge.getSourceObjects()
				     .stream()
				     .map(m -> ((TransformedObject<?, ?>) m).getSource())
				     .toList());
	}

	@Test
	public void sortAndTransformWithMerge() {
		List<? extends MatchingObject<Matchable>> list = Stream.<Matchable>of(
				SAMPLE_SUBSTANCE_2,
				SAMPLE_SUBSTANCE_1,
				SAMPLE_PRODUCT_1,
				SAMPLE_PRODUCT_2
		).map(OriginalMatch::new).collect(Collectors.toList());

		list = sut.applyScoreJudge(
				list,
				new PredefinedScoreJudge(Map.of(
						SAMPLE_SUBSTANCE_2, 2.5,
						SAMPLE_SUBSTANCE_1, 1.0,
						SAMPLE_PRODUCT_2, 1.5,
						SAMPLE_PRODUCT_1, 2.0
				)),
				new ScoreJudgeConfiguration(1.0, true, 1.0,
						ScoreIncorporationStrategy.OVERWRITE)
		);

		list = sut.transformMatches(list,
				new PredefinedMatchTransformer(Map.of(
						SAMPLE_SUBSTANCE_2, List.of(SAMPLE_SUBSTANCE_2),
						SAMPLE_SUBSTANCE_1, List.of(SAMPLE_SUBSTANCE_3),
						SAMPLE_PRODUCT_2, List.of(SAMPLE_PRODUCT_2, SAMPLE_PRODUCT_3),
						SAMPLE_PRODUCT_1, List.of(SAMPLE_SUBSTANCE_3) // Should merge with 2nd-previous row
				)),
				ScoreMergingStrategy.MAX);

		assertEquals(List.of(SAMPLE_SUBSTANCE_2, SAMPLE_SUBSTANCE_3, SAMPLE_PRODUCT_2, SAMPLE_PRODUCT_3),
				Util.unpack(list));

		assertInstanceOf(TransformedObject.class, list.get(0));
		assertInstanceOf(Merge.class, list.get(1));
		assertInstanceOf(TransformedObject.class, list.get(2));
		assertInstanceOf(TransformedObject.class, list.get(3));
		assertEquals(2.5, list.get(0).getScore());
		assertEquals(2.0, list.get(1).getScore()); // Previous score from SAMPLE_PRODUCT_1
		assertEquals(1.5, list.get(2).getScore());
		assertEquals(1.5, list.get(3).getScore());
	}

	@Test
	public void multipleActions() {
		List<? extends MatchingObject<Matchable>> list = Stream.<Matchable>of(
				new Substance(97, ""),
				new Product(32, "Not so dope"),
				new Product(67, ""),
				new Product(100, "GREAT"),
				new Substance(107, "")
		).map(OriginalMatch::new).collect(Collectors.toList());

		list = sut.applyScoreJudge(list,
				new MatchingPipelineServiceTest.IdSizeJudge(),
				new ScoreJudgeConfiguration(200.0, false));

		list = sut.applyScoreJudge(list,
				new MatchingPipelineServiceTest.IdSizeJudge(),
				new ScoreJudgeConfiguration(40.0, true));

		assertEquals(4, list.size());

		list = sut.transformMatches(list, new PredefinedMatchTransformer(Map.of(
				new Substance(97, ""), List.of(SAMPLE_SUBSTANCE_1, SAMPLE_PRODUCT_2),
				new Product(67, ""), List.of(SAMPLE_PRODUCT_3),
				new Product(100, "GREAT"), List.of(),
				new Substance(107, ""), List.of(SAMPLE_SUBSTANCE_3)
		)), ScoreMergingStrategy.SUM);

		list = sut.applyFilter(list, new ProductOnlyFilter(), true);

		assertEquals(2, list.size());
		assertEquals(SAMPLE_PRODUCT_2, list.getFirst().getObject());
		assertEquals(SAMPLE_PRODUCT_3, list.getLast().getObject());

		MatchingObject<Matchable> object = list.getFirst();

		assertInstanceOf(JudgedObject.class, object);
		assertTrue(((JudgedObject<Matchable>) object).getJudgement().passed());
		assertEquals(ProductOnlyFilter.NAME, ((JudgedObject<Matchable>) object).getJudgement().name());

		object = ((JudgedObject<Matchable>) object).getSource();
		assertInstanceOf(TransformedObject.class, object);

		object = ((TransformedObject<Matchable, ?>) object).getSource();
		assertInstanceOf(ScoreJudgedObject.class, object);
		assertEquals(IdSizeJudge.NAME, ((ScoreJudgedObject<Matchable>) object).getJudgement().name());
		assertEquals(1 + 97 + 97, object.getScore(), 0.1);

		object = ((ScoreJudgedObject<Matchable>) object).getSource();
		assertInstanceOf(ScoreJudgedObject.class, object);
		assertEquals(1 + 97, object.getScore(), 0.1);

		object = ((ScoreJudgedObject<Matchable>) object).getSource();
		assertInstanceOf(OriginalMatch.class, object);
		assertEquals(1, object.getScore());

	}

	private static class IdSizeJudge extends ScoreJudge<Matchable> {

		public static final String NAME = "Id Judge";

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