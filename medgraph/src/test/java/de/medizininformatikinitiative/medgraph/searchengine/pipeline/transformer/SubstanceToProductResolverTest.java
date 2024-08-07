package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.Neo4jTest;
import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.TestFactory.SAMPLE_SEARCH_QUERY;
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
	public void resolveSingleProduct(boolean batchMode) {
		Transformation<Product> t = transform(TestFactory.Substances.EPINEPHRINE, batchMode);
		assertEquals(List.of(TestFactory.Products.ANAPEN), t.result());
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void resolveMultipleProducts(boolean batchMode) {
		Transformation<Product> t = transform(TestFactory.Substances.MIDAZOLAM_HYDROCHLORIDE, batchMode);
		assertEquals(Set.of(TestFactory.Products.DORMICUM_5, TestFactory.Products.DORMICUM_15),
				new HashSet<>(t.result()));
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void resolveMultipleProductsFromCorrespondingIngredient(boolean batchMode) {
		Transformation<Product> t = transform(TestFactory.Substances.MIDAZOLAM, batchMode);
		assertEquals(Set.of(TestFactory.Products.DORMICUM_5, TestFactory.Products.DORMICUM_15),
				new HashSet<>(t.result()));
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void resolveNonActiveIngredient(boolean batchMode) {
		// Water is a nonactive ingredient and therefore no results should occur
		Transformation<Product> t = transform(TestFactory.Substances.WATER, batchMode);
		assertEquals(Collections.emptyList(), t.result());
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = {false, true})
	public void resolveProductWhereSubstanceIsIncludedTwice(boolean batchMode) {
		// Dolomo contains two drugs, both of which contain ASS. Despite that, it may only exist once in the result!
		Transformation<Product> t = transform(TestFactory.Substances.ACETYLSALICYLIC_ACID, batchMode);
		List<Product> result = t.result();
		assertEquals(2, result.size());
		assertEqualsIgnoreOrder(List.of(TestFactory.Products.ASPIRIN, TestFactory.Products.DOLOMO), result);
	}

	@Test
	public void batchTransform() {
		List<Transformation<Product>> transformations = sut.batchTransform(
				List.of(TestFactory.Substances.MIDAZOLAM, TestFactory.Substances.ACETYLSALICYLIC_ACID),
				SAMPLE_SEARCH_QUERY
		);

		assertEquals(Set.of(TestFactory.Products.DORMICUM_5, TestFactory.Products.DORMICUM_15),
				new HashSet<>(transformations.get(0).result()));
		assertEquals(Set.of(TestFactory.Products.ASPIRIN, TestFactory.Products.DOLOMO),
				new HashSet<>(transformations.get(1).result()));
	}

	private Transformation<Product> transform(Substance matchable, boolean batchMode) {
		return transform(sut, matchable, batchMode);
	}

	static Transformation<Product> transform(SubstanceToProductResolver sut, Substance matchable, boolean batchMode) {
		if (batchMode) {
			return sut.batchTransform(List.of(matchable), SAMPLE_SEARCH_QUERY).getFirst();
		} else {
			return sut.transform(matchable, SAMPLE_SEARCH_QUERY);
		}
	}

}