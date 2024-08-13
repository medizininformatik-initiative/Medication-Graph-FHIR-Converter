package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.FhirAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

		FhirAddress fhirAddress = address.toFhirAddress();

		assertArrayEquals(new String[]{"Buchenstraße 12"}, fhirAddress.getLine());
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

		FhirAddress fhirAddress = address.toFhirAddress();

		assertArrayEquals(new String[]{"Gärtnerplatz 1"}, fhirAddress.getLine());
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

		FhirAddress fhirAddress = address.toFhirAddress();

		assertArrayEquals(new String[]{"Gärtnerplatz 1"}, fhirAddress.getLine());
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

		FhirAddress fhirAddress = address.toFhirAddress();

		assertArrayEquals(new String[]{"Oliverstreet 27a"}, fhirAddress.getLine());
		assertEquals("12401", fhirAddress.getPostalCode());
		assertEquals("New York", fhirAddress.getCity());
		assertEquals("United States", fhirAddress.getCountry());
		assertEquals("Oliverstreet 27a\n12401 New York\nUnited States", fhirAddress.getText());
	}

}