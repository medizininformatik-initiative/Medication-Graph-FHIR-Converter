package de.tum.med.aiim.markusbudeus.fhirexporter.neo4j;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Coding;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Uri;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.CodingSystem;
import io.netty.handler.codec.DateFormatter;
import org.neo4j.driver.Session;

import java.text.DateFormat;
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
		Coding coding = new Coding();
		coding.system = new Uri(codingSystem.uri);
		coding.version = Objects.requireNonNullElseGet(codingSystem.version,
				() -> FHIR_DATE_FORMATTER.format(codingSystem.dateOfRetrieval));
		coding.userSelected = false;
		return coding;
	}

}
