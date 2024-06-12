package de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.db.Database;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Transformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.util.*;

import static de.medizininformatikinitiative.medgraph.TestFactory.*;
import static de.medizininformatikinitiative.medgraph.TestFactory.Products.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

/**
 * @author Markus Budeus
 */
public class ProductDetailsResolverTest extends UnitTest {

	private static final Map<Long, DetailedProduct> productInfoMap = new HashMap<>();

	static {
		productInfoMap.put(ASPIRIN.getId(), Detailed.ASPIRIN);
		productInfoMap.put(PREDNISOLUT.getId(), Detailed.PREDNISOLUT);
		productInfoMap.put(DORMICUM_5.getId(), Detailed.DORMICUM_5);
		productInfoMap.put(DORMICUM_15.getId(), Detailed.DORMICUM_15);
		productInfoMap.put(ANAPEN.getId(), Detailed.ANAPEN);
		productInfoMap.put(ASEPTODERM.getId(), Detailed.ASEPTODERM);
	}

	@Mock
	private Database database;

	private ProductDetailsResolver sut;

	@BeforeEach
	void setUp() {
		when(database.getDetailedProductInfo(anyCollection())).thenAnswer(req -> {
			Collection<Long> ids = req.getArgument(0);
			Set<DetailedProduct> resultSet = new HashSet<>();
			ids.forEach(i -> {
				DetailedProduct p = productInfoMap.get(i);
				if (p != null) resultSet.add(p);
			});
			return resultSet;
		});

		sut = new ProductDetailsResolver(database);
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = { false, true })
	public void transformSingleProduct(boolean batchMode) {
		assertEquals(List.of(Detailed.ANAPEN), transform(ANAPEN, batchMode));
		assertEquals(List.of(Detailed.PREDNISOLUT), transform(PREDNISOLUT, batchMode));
		assertEquals(List.of(Detailed.DORMICUM_15), transform(DORMICUM_15, batchMode));
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = { false, true })
	public void transformNonProduct(boolean batchMode) {
		// Non-Products stay the same
		assertEquals(List.of(SAMPLE_SUBSTANCE_1), transform(SAMPLE_SUBSTANCE_1, batchMode));
		assertEquals(List.of(SAMPLE_SUBSTANCE_2), transform(SAMPLE_SUBSTANCE_2, batchMode));
		assertEquals(List.of(SAMPLE_SUBSTANCE_3), transform(SAMPLE_SUBSTANCE_3, batchMode));
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = { false, true })
	public void transformAlreadyDetailedProduct(boolean batchMode) {
		assertEquals(List.of(Detailed.ANAPEN), transform(Detailed.ANAPEN, batchMode));
		assertEquals(List.of(Detailed.PREDNISOLUT), transform(Detailed.PREDNISOLUT, batchMode));
		assertEquals(List.of(Detailed.DORMICUM_15), transform(Detailed.DORMICUM_15, batchMode));
	}

	@Test
	public void transformEmptyList() {
		assertEquals(List.of(), sut.batchTransform(List.of(), SAMPLE_SEARCH_QUERY));
	}

	@ParameterizedTest(name = "batchMode: {0}")
	@ValueSource(booleans = { false, true })
	public void transformNotExisting(boolean batchMode) {
		assertEquals(List.of(), transform(new Product(2135453, "DOES NOT EXIST"), batchMode));
	}

	@Test
	public void batchTransform() {
		List<Transformation> transformations = sut.batchTransform(List.of(
				ASPIRIN,
				DORMICUM_5,
				SAMPLE_SUBSTANCE_3,
				PREDNISOLUT,
				ASEPTODERM
		), SAMPLE_SEARCH_QUERY);

		assertEquals(List.of(Detailed.ASPIRIN), transformations.get(0).result());
		assertEquals(List.of(Detailed.DORMICUM_5), transformations.get(1).result());
		assertEquals(List.of(SAMPLE_SUBSTANCE_3), transformations.get(2).result());
		assertEquals(List.of(Detailed.PREDNISOLUT), transformations.get(3).result());
		assertEquals(List.of(Detailed.ASEPTODERM), transformations.get(4).result());
	}

	private List<Matchable> transform(Matchable matchable, boolean batchMode) {
		if (batchMode) {
			List<Transformation> transformations = sut.batchTransform(List.of(matchable), SAMPLE_SEARCH_QUERY);
			assertEquals(1, transformations.size(), "Exactly one transformation was requested, but "+transformations.size()+" were returned!");
			return transformations.getFirst().result();
		} else {
			return sut.transform(matchable, SAMPLE_SEARCH_QUERY).result();
		}
	}
}