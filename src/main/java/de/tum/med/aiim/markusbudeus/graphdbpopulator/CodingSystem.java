package de.tum.med.aiim.markusbudeus.graphdbpopulator;

import java.time.LocalDate;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public enum CodingSystem {

	EDQM(
			"https://standardterms.edqm.eu",
			"EDQM Standard Terms database",
			LocalDate.of(2023, 10, 16),
			null,
			"Data is taken from the EDQM Standard Terms database and is reproduced with permission " +
					"of the European Directorate for the Quality of Medicines & HealthCare, Council of Europe (EDQM). " +
					"The data has been retrieved at the date given by the date property. Since the EDQM Standard " +
					"Terms database is not a static list, this data may not be up to date.",
			EDQM_LABEL
	),
	ATC(
			"http://fhir.de/CodeSystem/bfarm/atc",
			"Anatomisch-Therapeutisch-Chemische Klassifikation",
			LocalDate.now(),
			null,
			"Data of the ATC classification has been retrieved from the MMI PharmIndex raw data on the " +
					"date specified by this node. Be aware that the raw data provided to create this database " +
					"may already have been outdated when this database was created. As such, the data may be " +
					"older than what the date suggests.",
			ATC_LABEL
	),
	PZN(
			"http://fhir.de/CodeSystem/ifa/pzn",
			"Pharmazentralnummer",
			LocalDate.now(),
			null,
			"The PZN data has been retrieved from raw MMI PharmIndex data when this database was created. " +
					"The database creation date is specified as date property on this node. However, the raw " +
					"data used to instantiate this database may have been older than that.",
			PZN_LABEL
	),
	ASK(
			"http://fhir.de/CodeSystem/ask",
			"Arzneistoffkatalog",
			LocalDate.now(),
			null,
			"The ASK data has been retrieved from raw MMI PharmIndex data when this database was created. " +
					"The database creation date is specified as date property on this node. However, the raw " +
					"data used to instantiate this database may have been older than that.",
			ASK_LABEL
	),
	//		INN(
//			"https://www.who.int/teams/health-product-and-policy-standards/inn",
//			"International Nonproprietary Name",
//			LocalDate.of(2023, 8, 28),
//			null,
//			"The INN data has been retrieved from " +
//					"https://www.wcoomd.org/en/topics/nomenclature/instrument-and-tools/tools-to-assist-with-the-classification-in-the-hs/hs_classification-decisions/inn-table.aspx " +
//					"on the specified date.",
//			INN_LABEL
//	);
	INN(
			"https://www.who.int/teams/health-product-and-policy-standards/inn",
			"International Nonproprietary Name",
			LocalDate.of(2023, 1, 17),
			null,
			"The INN data has been retrieved from " +
					"https://www.bfarm.de/DE/Arzneimittel/Arzneimittelinformationen/Arzneimittel-recherchieren/Stoffbezeichnungen/_node.html " +
					"and has the version of the specified date.",
			INN_LABEL
	),
	CAS(
			"https://www.cas.org/cas-data/cas-registry",
			"Chemical Abstracts Service Registry Number®",
			LocalDate.of(2023, 1, 17),
			null,
			"The CAS data has been retrieved from " +
					"https://www.bfarm.de/DE/Arzneimittel/Arzneimittelinformationen/Arzneimittel-recherchieren/Stoffbezeichnungen/_node.html " +
					"and has the version of the specified date.",
			CAS_LABEL
	);

	/**
	 * The URI referencing the coding system.
	 */
	public final String uri;
	/**
	 * The coding system's name.
	 */
	public final String name;
	/**
	 * The date at which the coding system was retrieved.
	 */
	public final LocalDate dateOfRetrieval;
	/**
	 * The retrieved version of the coding system or null if unknown or unspecified.
	 */
	public final String version;
	/**
	 * Additional information to be provided alongside the coding system.
	 */
	public final String notice;
	/**
	 * The label to assign to nodes belonging to this coding system.
	 */
	public final String assignedNodesLabel;

	CodingSystem(String uri, String name, LocalDate dateOfRetrieval, String version, String notice,
	             String assignedNodesLabel) {
		this.uri = uri;
		this.name = name;
		this.dateOfRetrieval = dateOfRetrieval;
		this.version = version;
		this.notice = notice;
		this.assignedNodesLabel = assignedNodesLabel;
	}
}
