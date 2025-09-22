package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Markus Budeus
 */
public class GraphAddressConversionTest extends UnitTest {

	@Test
	void sample1() {
		GraphAddress address = new GraphAddress(
				"Buchenstraße",
				"12",
				"81104",
				"Karlsruhe",
				"Deutschland",
				"DE"
		);

		Address fhirAddress = address.toFhirAddress();

		assertEquals(List.of("Buchenstraße 12"), fhirAddress.getLine().stream().map(PrimitiveType::getValueAsString).toList());
		assertEquals("81104", fhirAddress.getPostalCode());
		assertEquals("Karlsruhe", fhirAddress.getCity());
		assertEquals("Deutschland", fhirAddress.getCountry());
		assertEquals("Buchenstraße 12\n81104 Karlsruhe\nDeutschland", fhirAddress.getText());
	}

	@Test
	void sample2() {
		GraphAddress address = new GraphAddress(
				"Gärtnerplatz",
				"1",
				"80469",
				"München",
				null,
				null
		);

		Address fhirAddress = address.toFhirAddress();

		assertEquals(List.of("Gärtnerplatz 1"), fhirAddress.getLine().stream().map(PrimitiveType::getValueAsString).toList());
		assertEquals("80469", fhirAddress.getPostalCode());
		assertEquals("München", fhirAddress.getCity());
		assertNull(fhirAddress.getCountry());
		assertEquals("Gärtnerplatz 1\n80469 München", fhirAddress.getText());
	}

	@Test
	void sample3() {
		GraphAddress address = new GraphAddress(
				"Gärtnerplatz",
				"1",
				"80469",
				"München",
				null,
				"DE"
		);

		Address fhirAddress = address.toFhirAddress();

		assertEquals(List.of("Gärtnerplatz 1"), fhirAddress.getLine().stream().map(PrimitiveType::getValueAsString).toList());
		assertEquals("80469", fhirAddress.getPostalCode());
		assertEquals("München", fhirAddress.getCity());
		assertEquals("DE", fhirAddress.getCountry());
		assertEquals("Gärtnerplatz 1\n80469 München\nDE", fhirAddress.getText());
	}

	@Test
	void sample4() {
		GraphAddress address = new GraphAddress(
				"Oliverstreet",
				"27a",
				"12401",
				"New York",
				"United States",
				"US"
		);

		Address fhirAddress = address.toFhirAddress();

		assertEquals(List.of("Oliverstreet 27a"), fhirAddress.getLine().stream().map(PrimitiveType::getValueAsString).toList());
		assertEquals("12401", fhirAddress.getPostalCode());
		assertEquals("New York", fhirAddress.getCity());
		assertEquals("United States", fhirAddress.getCountry());
		assertEquals("Oliverstreet 27a\n12401 New York\nUnited States", fhirAddress.getText());
	}

	@Test
	void sample5() {
		GraphAddress address = new GraphAddress(
				null,
				null,
				"12401",
				"New York",
				"United States",
				"US"
		);

		Address fhirAddress = address.toFhirAddress();

		assertEquals(0, fhirAddress.getLine().size());
		assertEquals("12401", fhirAddress.getPostalCode());
		assertEquals("New York", fhirAddress.getCity());
		assertEquals("United States", fhirAddress.getCountry());
		assertEquals("12401 New York\nUnited States", fhirAddress.getText());
	}

}