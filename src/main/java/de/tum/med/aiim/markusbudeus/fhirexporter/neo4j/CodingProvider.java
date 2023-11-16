package de.tum.med.aiim.markusbudeus.fhirexporter.neo4j;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Coding;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.CodingSystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Support class for creating {@link Coding} instances for different coding systems.
 */
public class CodingProvider {

	private static DateTimeFormatter FHIR_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	/**
	 * Creates and returns a pre-filled {@link Coding} instance with the {@link Coding#system}, {@link Coding#version}
	 * and {@link Coding#userSelected} already specified through the given {@link CodingSystem}.
	 */
	public static Coding createCodingTemplate(CodingSystem codingSystem) {
		return createCodingTemplate(codingSystem.uri, codingSystem.version, codingSystem.dateOfRetrieval);
	}

	public static Coding createCodingTemplate(String systemUri, String systemVersion, LocalDate systemDate) {
		Coding coding = new Coding();
		coding.system = systemUri;

		if (systemDate == null) {
			coding.version = systemVersion;
		} else {
			coding.version = Objects.requireNonNullElseGet(systemVersion,
					() -> FHIR_DATE_FORMATTER.format(systemDate));
		}
		coding.userSelected = false;
		return coding;
	}

	public static Coding createCoding(String value, String systemUri, String systemVersion, LocalDate systemDate) {
		Coding coding = createCodingTemplate(systemUri, systemVersion, systemDate);
		coding.code = value;
		coding.display = value;
		return coding;
	}

}
