package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.Neo4jTest;
import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.TestFactory.Substances;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.*;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.CodingSystem;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.IdMatchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.medizininformatikinitiative.medgraph.FhirExportTestFactory.GraphEdqmPharmaceuticalDoseForms;
import static de.medizininformatikinitiative.medgraph.FhirExportTestFactory.GraphUnits;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Markus Budeus
 */
public class Neo4jProductExporterTest extends Neo4jTest {

	private Set<GraphProduct> products;
	private Set<GraphProduct> productsNoIngredients;

	@BeforeAll
	void doExport() {
		Neo4jProductExporter sut1 = new Neo4jProductExporter(session, false);
		products = sut1.exportObjects().collect(Collectors.toSet());

		Neo4jProductExporter sut2 = new Neo4jProductExporter(session, true);
		productsNoIngredients = sut2.exportObjects().collect(Collectors.toSet());
	}

	@ParameterizedTest(name = "allowNoIngredients: {0}")
	@ValueSource(booleans = {false, true})
	void exportComplete(boolean allowNoIngredients) {
		Set<GraphProduct> products = allowNoIngredients ? this.productsNoIngredients : this.products;

		List<Product> allProducts = Catalogue.getAllFields(TestFactory.Products.class, false);
		// Aseptoderm not included! Not even if zero ingredients are allowed, because drugs are still needed
		allProducts.remove(TestFactory.Products.ASEPTODERM);
		List<String> allProductNames = allProducts.stream().map(IdMatchable::getName).toList();
		List<String> receivedProductNames = products.stream().map(GraphProduct::name).toList();
		assertEqualsIgnoreOrder(allProductNames, receivedProductNames);
	}

	@ParameterizedTest(name = "allowNoIngredients: {0}")
	@ValueSource(booleans = {false, true})
	void sampleDolomo(boolean allowNoIngredients) {
		Set<GraphProduct> products = allowNoIngredients ? this.productsNoIngredients : this.products;
		Optional<GraphProduct> sampleOpt = products.stream()
		                                           .filter(p -> p.mmiId() == TestFactory.Products.DOLOMO.getId())
		                                           .findFirst();
		assertTrue(sampleOpt.isPresent());
		GraphProduct sample = sampleOpt.get();

		GraphIngredient ingredient1 = new GraphIngredient(
				1,
				Substances.ACETYLSALICYLIC_ACID.getName(),
				true,
				BigDecimal.valueOf(250),
				null,
				GraphUnits.MG
		);
		GraphIngredient ingredient2 = new GraphIngredient(
				8,
				Substances.PARACETAMOL.getName(),
				true,
				BigDecimal.valueOf(250),
				null,
				GraphUnits.MG
		);

		GraphDrug drug = new GraphDrug(
				List.of(ingredient1, ingredient2),
				List.of(),
				"Zum Einnehmen",
				GraphEdqmPharmaceuticalDoseForms.TABLET,
				BigDecimal.ONE,
				null
		);

		GraphProduct expected = new GraphProduct(
				TestFactory.Products.DOLOMO.getName(),
				TestFactory.Products.DOLOMO.getId(),
				1L,
				"Bayer Vital GmbH",
				List.of(),
				List.of(drug, drug),
				List.of(
						new GraphPackage("dolomo® TN 10 Tbl. N1", BigDecimal.TEN, LocalDate.of(2004, 1, 1), List.of(
								new GraphCode("00778219", CodingSystem.PZN.uri, LocalDate.of(2024, 8, 8), null)
						))
				)
		);

		assertEquals(expected, sample);
	}

	@ParameterizedTest(name = "allowNoIngredients: {0}")
	@ValueSource(booleans = {false, true})
	void samplePrednisolut(boolean allowNoIngredients) {
		Set<GraphProduct> products = allowNoIngredients ? this.productsNoIngredients : this.products;
		Optional<GraphProduct> sampleOpt = products.stream()
		                                           .filter(p -> p.mmiId() == TestFactory.Products.PREDNISOLUT.getId())
		                                           .findFirst();
		assertTrue(sampleOpt.isPresent());
		GraphProduct sample = sampleOpt.get();

		GraphIngredient ingredient1 = new GraphIngredient(
				Substances.PREDNISOLONE_HYDROGENSUCCINATE.getId(),
				Substances.PREDNISOLONE_HYDROGENSUCCINATE.getName(),
				true,
				new BigDecimal("10.48"),
				null,
				GraphUnits.MG,
				List.of(
						new SimpleGraphIngredient(Substances.PREDNISOLONE.getId(), Substances.PREDNISOLONE.getName(),
								new BigDecimal("7.83"), null, GraphUnits.MG),
						new SimpleGraphIngredient(Substances.SODIUM.getId(), Substances.SODIUM.getName(),
								new BigDecimal("1"), null, GraphUnits.MG
						)
				)
		);

		GraphDrug drug = new GraphDrug(
				List.of(ingredient1),
				List.of(),
				"Pulver zur Herst. e. Inj.-Lsg.",
				GraphEdqmPharmaceuticalDoseForms.POWDER_FOR_SOLUTION_FOR_INJECTION,
				BigDecimal.ONE,
				null
		);

		GraphIngredient solvent = new GraphIngredient(
				Substances.WATER.getId(),
				Substances.WATER.getName(),
				false,
				null,
				null,
				null
		);

		GraphDrug solventDrug = new GraphDrug(
				List.of(solvent),
				List.of(),
				"Lösungsmittel",
				null,
				BigDecimal.valueOf(2),
				GraphUnits.ML
		);

		GraphProduct expected = new GraphProduct(
				TestFactory.Products.PREDNISOLUT.getName(),
				TestFactory.Products.PREDNISOLUT.getId(),
				null,
				null,
				List.of(),
				List.of(drug, solventDrug),
				List.of(
						new GraphPackage("Prednisolut 10mg L 3 Amp. m. Pulv. + 3 Amp. m. Lsgm. N2",
								BigDecimal.valueOf(3), LocalDate.of(1998, 12, 15), List.of(
								new GraphCode("01343446", CodingSystem.PZN.uri, LocalDate.of(2024, 8, 8), null)
						))
				)
		);

		assertEquals(expected, sample);
	}

}
