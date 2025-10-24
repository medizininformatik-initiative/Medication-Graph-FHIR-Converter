package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

/**
 * Central provider for FHIR medication resource IDs.
 *
 * @author Markus Budeus
 */
public class IdProvider {

	private static final String PREFIX = "MII-Medgraph-MMI-";
	private static final String PRODUCT_PREFIX = PREFIX + "P-";
	private static final String SUBSTANCE_PREFIX = PREFIX + "S-";
	private static final String ORGANIZATION_PREFIX = PREFIX + "O-";

	public static String combinedMedicalProductSubproductIdentifier(long parentMmiId, int childNo) {
		return fromProductMmiId(parentMmiId) + "-" + childNo;
	}

	public static String fromOrganizationMmiId(long mmiId) {
		return ORGANIZATION_PREFIX + mmiId;
	}

	public static String fromSubstanceMmiId(long mmiId) {
		return SUBSTANCE_PREFIX + mmiId;
	}

	public static String fromProductMmiId(long productMmiId) {
		return PRODUCT_PREFIX + productMmiId;
	}

}
