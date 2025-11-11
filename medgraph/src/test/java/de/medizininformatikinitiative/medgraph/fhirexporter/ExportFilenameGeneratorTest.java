package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.IdProvider;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Substance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportFilenameGeneratorTest {

	private ExportFilenameGenerator sut;

	@BeforeEach
	void setUp() {
		sut = new ExportFilenameGenerator();
	}

	@Test
	void generateSubstanceFilename() {
		Substance substance = new Substance();
		substance.setId(IdProvider.fromSubstanceMmiId(107L));
		substance.getCode().setText("Amazing medication");
		assertEquals("107 Amazing medication", sut.constructFilename(substance));
	}

	@Test
	void generateMedicationFilename() {
		Medication medication = new Medication();
		medication.setId(IdProvider.fromProductMmiId(22484L));
		medication.getCode().setText("Atrovent");
		assertEquals("22484 Atrovent", sut.constructFilename(medication));
	}

	@Test
	void generateMedicationFilename2() {
		Medication medication = new Medication();
		medication.setId(IdProvider.fromProductMmiId(22484L));
		assertEquals("22484 unnamed", sut.constructFilename(medication));
	}

	@Test
	void generateMedicationFilename3() {
		Medication medication = new Medication();
		medication.setId(IdProvider.combinedMedicalProductSubproductIdentifier(1178L, 3));
		medication.getCode().setText("Flumazenil");
		assertEquals("1178-3 Flumazenil", sut.constructFilename(medication));
	}

	@Test
	void generateOrganizationFilename() {
		Organization organization = new Organization();
		organization.setId(IdProvider.fromOrganizationMmiId(1786L));
		organization.setName("Herberts Medikamänte Inc.");
		assertEquals("1786 Herberts Medikamaente Inc.", sut.constructFilename(organization));
	}

	@Test
	void generateOrganizationFilenameWithAlias() {
		Organization organization = new Organization();
		organization.setId(IdProvider.fromOrganizationMmiId(1786L));
		organization.setName("Herberts Medikamente Inc.");
		organization.addAlias("Herbert-Meds");
		assertEquals("1786 Herbert-Meds", sut.constructFilename(organization));
	}

	@Test
	void generateFilenameWithIllegalCharacters() {
		Organization organization = new Organization();
		organization.setId(IdProvider.fromOrganizationMmiId(999L));
		organization.setName("Essen/Bauer Pharma GmbH");
		assertEquals("999 Essen-Bauer Pharma GmbH", sut.constructFilename(organization));
	}

	@Test
	void generateFilenameWithIllegalCharacters2() {
		Medication medication = new Medication();
		medication.setId(IdProvider.fromProductMmiId(9999L));
		medication.getCode().setText("Illinoi Kopf-/Halsschmerztabletten <10X>");
		assertEquals("9999 Illinoi Kopf--Halsschmerztabletten -10X-", sut.constructFilename(medication));
	}

	@Test
	void generateFilenameWithIllegalCharacters3() {
		Medication medication = new Medication();
		medication.setId(IdProvider.fromProductMmiId(123L));
		medication.getCode().setText("Hypromellose (5 mPa∙s)");
		assertEquals("123 Hypromellose (5 mPa-s)", sut.constructFilename(medication));
	}

	@Test
	void limitFilenameLength() {
		Medication medication = new Medication();
		medication.setId(IdProvider.fromProductMmiId(55342L));
		medication.getCode().setText("Misch-Extrakt aus Apfelfrüchte, Brennnesselblätter, Curcumawurzel, Haferkraut, Hagebuttenschale, Hibiscusblüte, Ingwerwurzel, Lemongras, Pfefferminzblätter, Spinatblätter, Teufelskrallenwurzel und Zitronenverbenenblätter");
		assertEquals("55342 Misch-Extrakt aus Apfelfruechte, Brennnessel", sut.constructFilename(medication));
	}
}