package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Transformation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Markus Budeus
 */
public class MatchTransformerTest {

	public static final String TRANSFORMER_NAME = "TestTransform";
	public static final String TRANSFORMER_DESC = "Makes transformations for testing";

	@Test
	public void singleTransform() {
		Map<Matchable, List<Matchable>> tfMap = new HashMap<>();
		tfMap.put(SAMPLE_PRODUCT_1, List.of(SAMPLE_SUBSTANCE_1));

		TestMatchTransformer transformer = new TestMatchTransformer(tfMap);

		Transformation tf = transformer.transform(SAMPLE_PRODUCT_1, null);
		assertEquals(List.of(SAMPLE_SUBSTANCE_1), tf.result());
		assertEquals(TRANSFORMER_NAME, tf.name());
		assertEquals(TRANSFORMER_DESC, tf.description());
	}

	@Test
	public void singleTransform2() {
		Map<Matchable, List<Matchable>> tfMap = new HashMap<>();
		tfMap.put(SAMPLE_PRODUCT_1, List.of(SAMPLE_SUBSTANCE_1, SAMPLE_PRODUCT_2));

		TestMatchTransformer transformer = new TestMatchTransformer(tfMap);

		assertEquals(List.of(SAMPLE_SUBSTANCE_1, SAMPLE_PRODUCT_2),
				transformer.transform(SAMPLE_PRODUCT_1, null).result());
		assertEquals(List.of(), transformer.transform(SAMPLE_PRODUCT_2, null).result());
	}

	@Test
	public void batchTransform() {

		Map<Matchable, List<Matchable>> tfMap = new HashMap<>();
		tfMap.put(SAMPLE_PRODUCT_1, List.of(SAMPLE_SUBSTANCE_1, SAMPLE_PRODUCT_2));
		tfMap.put(SAMPLE_PRODUCT_2, List.of(SAMPLE_SUBSTANCE_2));
		tfMap.put(SAMPLE_PRODUCT_3, List.of(SAMPLE_SUBSTANCE_3));

		TestMatchTransformer transformer = new TestMatchTransformer(tfMap);

		List<Transformation> tfResults = transformer.batchTransform(
				List.of(SAMPLE_PRODUCT_1, SAMPLE_PRODUCT_2, SAMPLE_PRODUCT_3, new Substance(0, "goodbye")), null);

		assertTrue(transformer.isUsedBatchTransform(),
				"The MatchTransformer did not use the batchTransformInternal-function to execute a batch transformation!");

		assertEquals(4, tfResults.size());
		assertEquals(List.of(SAMPLE_SUBSTANCE_1, SAMPLE_PRODUCT_2), tfResults.get(0).result());
		assertEquals(List.of(SAMPLE_SUBSTANCE_2), tfResults.get(1).result());
		assertEquals(List.of(SAMPLE_SUBSTANCE_3), tfResults.get(2).result());
		assertEquals(List.of(), tfResults.get(3).result());
	}

	private static class TestMatchTransformer extends MatchTransformer {

		private boolean usedBatchTransform = false;
		private final Map<Matchable, List<Matchable>> transformationMap;

		private TestMatchTransformer(Map<Matchable, List<Matchable>> transformationMap) {
			this.transformationMap = transformationMap;
		}

		@Override
		protected List<Matchable> transformInternal(Matchable matchable, SearchQuery query) {
			usedBatchTransform = false;
			List<Matchable> result = transformationMap.get(matchable);
			if (result == null) result = new ArrayList<>();
			return result;
		}

		@Override
		protected List<List<Matchable>> batchTransformInternal(List<? extends Matchable> matchables, SearchQuery query) {
			List<List<Matchable>> resultList = super.batchTransformInternal(matchables, query);
			usedBatchTransform = true;
			return resultList;
		}

		@Override
		public String getDescription() {
			return TRANSFORMER_DESC;
		}

		public boolean isUsedBatchTransform() {
			return usedBatchTransform;
		}

		@Override
		public String toString() {
			return TRANSFORMER_NAME;
		}
	}

}