package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.Neo4jTest;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static de.medizininformatikinitiative.medgraph.TestFactory.DoseForms.*;
import static de.medizininformatikinitiative.medgraph.TestFactory.Products.*;
import static de.medizininformatikinitiative.medgraph.TestFactory.Substances.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Markus Budeus
 */
public class ProvidersTest extends Neo4jTest {

	@Test
	void productsProvider() {
		assertEquals(Set.of(
						new MappedIdentifier<>("Dormicum", DORMICUM_5),
						new MappedIdentifier<>("Dormicum", DORMICUM_15),
						new MappedIdentifier<>("Aspirin Complex Granulat-Sticks", ASPIRIN),
						new MappedIdentifier<>("Aspirin Sticks", ASPIRIN)),
				Providers.getProductSynonyms(session).getIdentifiers().collect(Collectors.toSet())
		);
	}

	@Test
	void substancesProvider() {
		assertEquals(Set.of(
						new MappedIdentifier<>("Paracetamol", PARACETAMOL),
						new MappedIdentifier<>("Aspirin", ACETYLSALICYLIC_ACID),
						new MappedIdentifier<>("Midazolam", MIDAZOLAM),
						new MappedIdentifier<>("Wasser", WATER),
						new MappedIdentifier<>("Adrenalin", EPINEPHRINE)),
				Providers.getSubstanceSynonyms(session).getIdentifiers().collect(Collectors.toSet())
		);
	}

	@Test
	void edqmConceptsProvider() {
		assertEquals(Set.of(
						new MappedIdentifier<>("Injektionslsg.", SOLUTION_FOR_INJECTION),
						new MappedIdentifier<>("Inj.-Lsg.", SOLUTION_FOR_INJECTION),
						new MappedIdentifier<>("Parenteral", Characteristics.PARENTERAL),
						new MappedIdentifier<>("Granules", GRANULES),
						new MappedIdentifier<>("Granules", Characteristics.GRANULES),
						new MappedIdentifier<>("Oral", Characteristics.ORAL),
						new MappedIdentifier<>("Tablet", Characteristics.TABLET),
						new MappedIdentifier<>("Tablet", TABLET),
						new MappedIdentifier<>("Tbl.", Characteristics.TABLET),
						new MappedIdentifier<>("Tbl.", TABLET)),
				Providers.getEdqmConceptIdentifiers(session).getIdentifiers().collect(Collectors.toSet())
		);
	}


}