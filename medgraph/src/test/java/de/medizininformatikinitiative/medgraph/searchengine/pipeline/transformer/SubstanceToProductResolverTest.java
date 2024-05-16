package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.Neo4jTest;
import de.medizininformatikinitiative.medgraph.searchengine.TestFactory;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Transformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.searchengine.TestFactory.SAMPLE_PRODUCT_1;
import static de.medizininformatikinitiative.medgraph.searchengine.TestFactory.SAMPLE_SEARCH_QUERY;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class SubstanceToProductResolverTest extends Neo4jTest {

	private SubstanceToProductResolver sut;

	@BeforeEach
	void setUp() {
		sut = new SubstanceToProductResolver(session);
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void transformProduct(boolean batchMode) {
		Transformation t = transform(SAMPLE_PRODUCT_1, batchMode);
		assertEquals(List.of(SAMPLE_PRODUCT_1), t.getResult());
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void resolveSingleProduct(boolean batchMode) {
		Transformation t = transform(TestFactory.Substances.ACETYLSALICYLIC_ACID, batchMode);
		assertEquals(List.of(TestFactory.Products.ASPIRIN), t.getResult());
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void resolveMultipleProducts(boolean batchMode) {
		Transformation t = transform(TestFactory.Substances.MIDAZOLAM_HYDROCHLORIDE, batchMode);
		assertEquals(Set.of(TestFactory.Products.DORMICUM_5, TestFactory.Products.DORMICUM_15),
				new HashSet<>(t.getResult()));
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void resolveMultipleProductsFromCorrespondingIngredient(boolean batchMode) {
		Transformation t = transform(TestFactory.Substances.MIDAZOLAM, batchMode);
		assertEquals(Set.of(TestFactory.Products.DORMICUM_5, TestFactory.Products.DORMICUM_15),
				new HashSet<>(t.getResult()));
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void resolveNonActiveIngredient(boolean batchMode) {
		// Water is a nonactive ingredient and therefore no results should occur
		Transformation t = transform(TestFactory.Substances.WATER, batchMode);
		assertEquals(Collections.emptyList(), t.getResult());
	}

	@Test
	public void batchTransform() {
		List<Transformation> transformations = sut.batchTransform(
				List.of(TestFactory.Substances.MIDAZOLAM, TestFactory.Substances.ACETYLSALICYLIC_ACID),
				SAMPLE_SEARCH_QUERY
		);

		assertEquals(Set.of(TestFactory.Products.DORMICUM_5, TestFactory.Products.DORMICUM_15),
				new HashSet<>(transformations.get(0).getResult()));
		assertEquals(Set.of(TestFactory.Products.ASPIRIN),
				new HashSet<>(transformations.get(1).getResult()));
	}

	private Transformation transform(Matchable matchable, boolean batchMode) {
		if (batchMode) {
			return sut.batchTransform(List.of(matchable), SAMPLE_SEARCH_QUERY).getFirst();
		} else {
			return sut.transform(matchable, SAMPLE_SEARCH_QUERY);
		}
	}

}