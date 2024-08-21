package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.Neo4jTest;
import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.*;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.CodingSystem;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.IdMatchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Markus Budeus
 */
public class Neo4jMedicationExporterTest extends Neo4jTest {

	@ParameterizedTest(name = "allowNoIngredients: {0}")
	@ValueSource(booleans = {false, true})
	void export(boolean allowNoIngredients) {
		Neo4jProductExporter sut = new Neo4jProductExporter(session, allowNoIngredients);
		Set<GraphProduct> products = sut.exportObjects().collect(Collectors.toSet());

		List<Product> allProducts = Catalogue.getAllFields(TestFactory.Products.class, false);
		// Aseptoderm not included! Not even if zero ingredients are allowed, because drugs are still needed
		allProducts.remove(TestFactory.Products.ASEPTODERM);
		List<String> allProductNames = allProducts.stream().map(IdMatchable::getName).toList();
		List<String> receivedProductNames = products.stream().map(GraphProduct::name).toList();
		assertEqualsIgnoreOrder(allProductNames, receivedProductNames);

		Optional<GraphProduct> sampleOpt = products.stream().filter(p -> p.mmiId() == 7L).findFirst();
		assertTrue(sampleOpt.isPresent());
		GraphProduct sample = sampleOpt.get();

		GraphUnit mg = new GraphUnit(
				"mg",
				"MG",
				"mg",
				"mg",
				"MG",
				"mg"
		);
		GraphIngredient ingredient1 = new GraphIngredient(
				1,
				TestFactory.Substances.ACETYLSALICYLIC_ACID.getName(),
				true,
				BigDecimal.valueOf(250),
				null,
				mg,
				null
		);
		GraphIngredient ingredient2 = new GraphIngredient(
				8,
				TestFactory.Substances.PARACETAMOL.getName(),
				true,
				BigDecimal.valueOf(250),
				null,
				mg,
				null
		);

		GraphEdqmPharmaceuticalDoseForm doseForm = new GraphEdqmPharmaceuticalDoseForm(
				"PDF-10219000",
				CodingSystem.EDQM.uri,
				LocalDate.of(2024, 5, 29),
				null,
				"Tablet"
		);

		GraphDrug drug = new GraphDrug(
				List.of(ingredient1, ingredient2),
				List.of(),
				"Zum Einnehmen",
				doseForm,
				BigDecimal.ONE,
				null
		);

		GraphProduct expected = new GraphProduct(
				TestFactory.Products.DOLOMO.getName(),
				7L,
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

}
