package de.medizininformatikinitiative.medgraph.fhirexporter;

/**
 * Factory for {@link FhirServerExportSink}-objects.
 *
 * @author Markus Budeus
 */
public interface FhirServerExportSinkFactory {

	/**
	 * Prepares a {@link FhirServerExportSink} instance which can be used to orchestrate a single FHIR export.
	 * Used when the target FHIR server uses no authentication.
	 * @param url The base URL of the FHIR server to which to upload.
	 * @return a ready-for-use {@link FhirServerExportSink}-instance
	 */
	FhirServerExportSink prepareExportWithoutAuth(String url);

	/**
	 * Prepares a {@link FhirServerExportSink} instance which can be used to orchestrate a single FHIR export.
	 * Used when the target FHIR server uses HTTP Basic Auth.
	 * @param url The base URL of the FHIR server to which to upload.
	 * @param user The HTTP Basic Auth username to supply.
	 * @param password The HTTP Basic Auth password to supply.
	 * @return a ready-for-use {@link FhirServerExportSink}-instance.
	 */
	FhirServerExportSink prepareExportWithHttpBasicAuth(String url, String user, char[] password);

	/**
	 * Prepares a {@link FhirServerExportSink} instance which can be used to orchestrate a single FHIR export.
	 * Used when the target FHIR server uses bearer token authentication.
	 * @param url The base URL of the FHIR server to which to upload.
	 * @param token The bearer token to use for the upload.
	 * @return a ready-for-use {@link FhirServerExportSink}-instance.
	 */
	FhirServerExportSink prepareExportWithTokenAuth(String url, String token);

}
