package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.CodeableConcept;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExportFilenameGeneratorTest {

	private ExportFilenameGenerator sut;

	@BeforeEach
	void setUp() {
		sut = new ExportFilenameGenerator();
	}

	@Test
	void generateSubstanceFilename() {
		Substance substance = new Substance();
		substance.identifier = new Identifier[] { Identifier.fromSubstanceMmiId(107L) };
		substance.code.text = "Amazing medication";
		assertEquals("107 Amazing medication", sut.constructFilename(substance));
	}

	@Test
	void generateMedicationFilename() {
		Medication medication = new Medication();
		medication.identifier = new Identifier[] { Identifier.fromProductAndOrganizationMmiId(22484L, 10L) };
		medication.code = new CodeableConcept();
		medication.code.text = "Atrovent";
		assertEquals("22484-10 Atrovent", sut.constructFilename(medication));
	}

	@Test
	void generateMedicationFilename2() {
		Medication medication = new Medication();
		medication.identifier = new Identifier[] { Identifier.fromProductAndOrganizationMmiId(22484L, 10L) };
		assertEquals("22484-10 unnamed", sut.constructFilename(medication));
	}

	@Test
	void generateOrganizationFilename() {
		Organization organization = new Organization();
		organization.identifier = new Identifier[] { Identifier.fromOrganizationMmiId(1786L) };
		organization.name = "Herberts Medikamänte Inc.";
		assertEquals("1786 Herberts Medikamaente Inc.", sut.constructFilename(organization));
	}

	@Test
	void generateOrganizationFilenameWithAlias() {
		Organization organization = new Organization();
		organization.identifier = new Identifier[] { Identifier.fromOrganizationMmiId(1786L) };
		organization.name = "Herberts Medikamente Inc.";
		organization.alias = new String[] { "Herbert-Meds" };
		assertEquals("1786 Herbert-Meds", sut.constructFilename(organization));
	}

	@Test
	void generateFilenameWithIllegalCharacters() {
		Organization organization = new Organization();
		organization.identifier = new Identifier[] { Identifier.fromOrganizationMmiId(999L) };
		organization.name = "Essen/Bauer Pharma GmbH";
		assertEquals("999 Essen-Bauer Pharma GmbH", sut.constructFilename(organization));
	}

	@Test
	void generateFilenameWithIllegalCharacters2() {
		Medication medication = new Medication();
		medication.identifier = new Identifier[] { Identifier.fromProductAndOrganizationMmiId(9999L, null) };
		medication.code = new CodeableConcept();
		medication.code.text = "Illinoi Kopf-/Halsschmerztabletten <10X>";
		assertEquals("9999 Illinoi Kopf--Halsschmerztabletten -10X-", sut.constructFilename(medication));
	}

	@Test
	void generateFilenameWithIllegalCharacters3() {
		Medication medication = new Medication();
		medication.identifier = new Identifier[] { Identifier.fromProductAndOrganizationMmiId(123L, null) };
		medication.code = new CodeableConcept();
		medication.code.text = "Hypromellose (5 mPa∙s)";
		assertEquals("123 Hypromellose (5 mPa-s)", sut.constructFilename(medication));
	}

	@Test
	void limitFilenameLength() {
		Medication medication = new Medication();
		medication.identifier = new Identifier[] { Identifier.fromProductAndOrganizationMmiId(55342L, null) };
		medication.code = new CodeableConcept();
		medication.code.text = "Misch-Extrakt aus Apfelfrüchte, Brennnesselblätter, Curcumawurzel, Haferkraut, Hagebuttenschale, Hibiscusblüte, Ingwerwurzel, Lemongras, Pfefferminzblätter, Spinatblätter, Teufelskrallenwurzel und Zitronenverbenenblätter";
		assertEquals("55342 Misch-Extrakt aus Apfelfruechte, Brennnessel", sut.constructFilename(medication));
	}
}