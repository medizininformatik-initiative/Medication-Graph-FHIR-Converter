package de.medizininformatikinitiative.medgraph.searchengine.db;

import de.medizininformatikinitiative.medgraph.Neo4jTest;
import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Markus Budeus
 */
public class Neo4jCypherDatabaseGetDetailedProductInfoTest extends Neo4jTest {

	private Neo4jCypherDatabase sut;

	@BeforeEach
	void setUp() {
		sut = new Neo4jCypherDatabase(session);
	}

	@ParameterizedTest
	@MethodSource("detailedProductSamplesProvider")
	public void getSampleProduct(DetailedProduct targetProduct) {
		Set<DetailedProduct> resultSet = sut.getDetailedProductInfo(Set.of(targetProduct.getId()));
		assertEquals(Set.of(targetProduct), resultSet);
	}

	@Test
	public void getNonExisting() {
		assertTrue(sut.getDetailedProductInfo(Set.of(16845165465454L, 25435165432131L)).isEmpty());
	}

	@Test
	public void batchGet() {
		Set<DetailedProduct> resultSet = sut.getDetailedProductInfo(Set.of(
				TestFactory.Products.ASPIRIN.getId(),
				TestFactory.Products.ANAPEN.getId(),
				-5184516513651L,
				TestFactory.Products.DORMICUM_5.getId(),
				TestFactory.Products.PREDNISOLUT.getId()
		));
		assertEquals(Set.of(
				TestFactory.Products.Detailed.ASPIRIN,
				TestFactory.Products.Detailed.ANAPEN,
				TestFactory.Products.Detailed.DORMICUM_5,
				TestFactory.Products.Detailed.PREDNISOLUT
		), resultSet);
	}

	static Stream<Arguments> detailedProductSamplesProvider() {
		return Stream.of(
				arguments(named("Aspirin", TestFactory.Products.Detailed.ASPIRIN)),
				arguments(named("Dormicum 5", TestFactory.Products.Detailed.DORMICUM_5)),
				arguments(named("Dormicum 15", TestFactory.Products.Detailed.DORMICUM_15)),
				arguments(named("Anapen", TestFactory.Products.Detailed.ANAPEN)),
				arguments(named("Prednisolut", TestFactory.Products.Detailed.PREDNISOLUT)),
				arguments(named("Aseptoderm", TestFactory.Products.Detailed.ASEPTODERM))
		);
	}


}
