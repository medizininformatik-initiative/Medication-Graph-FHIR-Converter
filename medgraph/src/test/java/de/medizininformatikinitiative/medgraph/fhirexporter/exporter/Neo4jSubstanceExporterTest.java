package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.Catalogue;
import de.medizininformatikinitiative.medgraph.Neo4jTest;
import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphCode;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphSubstance;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.CodingSystem;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.IdMatchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class Neo4jSubstanceExporterTest extends Neo4jTest {

	private Neo4jSubstanceExporter sut;

	@BeforeEach
	void setUp() {
		sut = new Neo4jSubstanceExporter(session);
	}

	@Test
	void export() {
		Set<GraphSubstance> substances = sut.exportObjects().collect(Collectors.toSet());

		List<Substance> allSubstances = Catalogue.getAllFields(TestFactory.Substances.class, false);
		List<String> allSubstanceNames = allSubstances.stream().map(IdMatchable::getName).toList();
		List<String> receivedSubstanceNames = substances.stream().map(GraphSubstance::name).toList();
		assertEqualsIgnoreOrder(allSubstanceNames, receivedSubstanceNames);


		Map<String, GraphSubstance> substanceByName = substances.stream().collect(
				Collectors.toMap(GraphSubstance::name, s -> s));


		GraphSubstance ass = substanceByName.get(TestFactory.Substances.ACETYLSALICYLIC_ACID.getName());
		assertNotNull(ass);
		assertContainsCode(ass, CodingSystem.ASK, "00002");
		assertContainsCode(ass, CodingSystem.CAS, "2349-94-2");
		assertContainsCode(ass, CodingSystem.CAS, "50-78-2");
		assertContainsCode(ass, CodingSystem.UNII, "R16CO5Y76E");

		GraphSubstance paracetamol = substanceByName.get(TestFactory.Substances.PARACETAMOL.getName());
		assertNotNull(paracetamol);
		assertContainsCode(paracetamol, CodingSystem.ASK, "01212");
	}

	private static void assertContainsCode(GraphSubstance substance, CodingSystem codingSystem, String code) {
		assertContainsCode(substance.codes(), codingSystem, code);
	}

	/**
	 * Fails if no code belonging to the given coding system is found in the given list of codes.
	 *
	 * @return the {@link GraphCode} representing the corresponding code instance
	 */
	public static GraphCode assertContainsCode(List<GraphCode> codes, CodingSystem codingSystem, String code) {
		for (GraphCode code1 : codes) {
			if (code1.getSystemUri().equals(codingSystem.uri)) {
				if (Objects.equals(code, code1.getCode())) return code1;
			}
		}
		fail("The code " + code + " of Coding System " + codingSystem.name + " was not found in the substance! Note that this " +
				"may happen if the URI of the Coding System was changed without the test dataset being updated.");
		return null;
	}
}
