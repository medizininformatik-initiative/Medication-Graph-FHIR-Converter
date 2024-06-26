package de.medizininformatikinitiative.medgraph.graphdbpopulator;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class CodingSystemTest {

	@Test
	public void edqmCodingsDateIsValid() throws IOException {
		LocalDate codingSystemDate = CodingSystem.EDQM.dateOfRetrieval;
		LocalDate edqmObjectsDate = parseLocalDateFromCsvHeader("edqm_objects.csv");
		LocalDate pdfRelationsFileDate = parseLocalDateFromCsvHeader("pdf_relations.csv");

		assertEquals(edqmObjectsDate, pdfRelationsFileDate,
				"The files edqm_objects.csv and pdf_relations.csv specify different retrieval dates! Why? Please generate them anew.");
		assertEquals(codingSystemDate, edqmObjectsDate,
		"The resource files specify that the data is from "+DateTimeFormatter.ISO_LOCAL_DATE.format(edqmObjectsDate)
				+", but the CodingSystem enum specifies the data is from "+DateTimeFormatter.ISO_LOCAL_DATE.format(codingSystemDate)+
				". Please correct the date specified by the CodingSystem.");
	}

	private LocalDate parseLocalDateFromCsvHeader(String resourceFile) throws IOException {
		try (InputStream inputStream = CodingSystem.class.getClassLoader().getResourceAsStream(resourceFile)) {
			assertNotNull(inputStream);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
				return LocalDate.parse(reader.readLine().substring(2));
			}
		}
	}

}