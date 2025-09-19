package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.GraphFhirExportSource;
import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.Neo4jOrganizationExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.Neo4jProductExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.Neo4jSubstanceExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphOrganization;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphSubstance;
import org.neo4j.driver.Session;

/**
 * Carries the different sources of FHIR resources needed for a full export.
 *
 * @author Markus Budeus
 */
public class FhirExportSources {

	/**
	 * The source of exported FHIR organizations.
	 */
	public final FhirExportSource<Organization> organizationExporter;
	/**
	 * The source of exported FHIR substances.
	 */
	public final FhirExportSource<Substance> substanceExporter;
	/**
	 * The source of exported FHIR medications.
	 */
	public final FhirExportSource<Medication> medicationExporter;

	public FhirExportSources(FhirExportSource<Organization> organizationExporter,
	                         FhirExportSource<Substance> substanceExporter,
	                         FhirExportSource<Medication> medicationExporter) {
		this.organizationExporter = organizationExporter;
		this.substanceExporter = substanceExporter;
		this.medicationExporter = medicationExporter;
	}

	/**
	 * Constructs the export sources by utilizing a connection to a Neo4j database.
	 * @param session The Neo4j session to use for querying resources for the export.
	 * @return A {@link FhirExportSources} instance carrying sources for the different types of exportable resources.
	 */
	public static FhirExportSources forNeo4jSession(Session session) {
		FhirExportSource<Organization> organizationExporter = new GraphFhirExportSource<>(
				new Neo4jOrganizationExporter(session),
				s -> s.map(GraphOrganization::toLegacyFhirOrganization));
		FhirExportSource<Substance> substanceExporter = new GraphFhirExportSource<>(new Neo4jSubstanceExporter(session),
				s -> s.map(GraphSubstance::toFhirSubstance));
		FhirExportSource<Medication> medicationExporter = new GraphFhirExportSource<>(
				new Neo4jProductExporter(session, false),
				s -> s.flatMap(p -> p.toFhirMedications().stream()));

		return new FhirExportSources(
				organizationExporter,
				substanceExporter,
				medicationExporter
		);
	}

}
