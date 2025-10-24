package de.medizininformatikinitiative.medgraph.fhirexporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import de.medizininformatikinitiative.medgraph.DI;
import org.hl7.fhir.r4.model.*;

import java.io.IOException;
import java.util.List;

/**
 * {@link FhirExportSink}-implementation that targets a FHIR server as sink.
 *
 * @author Markus Budeus
 */
public class FhirServerExportSink extends FhirExportSink {

	private final FhirContext context = DI.get(FhirContext.class);
	private final IGenericClient hapiClient;

	/**
	 * Sets an export sink targetting the FHIR server with the given base url (e.g. http://localhost:8080/fhir)
	 * @param baseUrl The base url of the FHIR server to target.
	 */
	public FhirServerExportSink(String baseUrl) {
		hapiClient = buildHapiClient(baseUrl);
	}

	/**
	 * Sets an export sink targetting the FHIR server with the given base url (e.g. http://localhost:8080/fhir).
	 * The export will authenticate itself to the FHIR server using HTTP Basic Auth.
	 *
	 * @param baseUrl The base url of the FHIR server to target.
	 * @param basicAuthUser The basic auth user to use for authentication.
	 * @param basicAuthPassword The basic auth password to use for authentication.
	 */
	public FhirServerExportSink(String baseUrl, String basicAuthUser, char[] basicAuthPassword) {
		this(baseUrl);
		hapiClient.registerInterceptor(new BasicAuthInterceptor(basicAuthUser, new String(basicAuthPassword)));
	}

	/**
	 * Sets an export sink targetting the FHIR server with the given base url (e.g. http://localhost:8080/fhir).
	 * The export will authenticate itself to the FHIR server using a bearer token.
	 *
	 * @param baseUrl The base url of the FHIR server to target.
	 * @param bearerToken The bearer token to use for authentication.
	 */
	public FhirServerExportSink(String baseUrl, String bearerToken) {
		this(baseUrl);
		hapiClient.registerInterceptor(new BearerTokenAuthInterceptor(bearerToken));
	}


	@Override
	public void doExport(FhirExportSources sources) throws IOException {
		setProgress(0, 6);

		setTaskStack("Downloading FHIR Organizations from Neo4j...");
		List<Organization> organizations = sources.organizationExporter.export().toList();
		setProgress(1);

		setTaskStack("Uploading FHIR Organizations to FHIR server...");
		uploadAll(organizations);
		setProgress(2);

		setTaskStack("Downloading FHIR Substances from Neo4j...");
		List<Substance> substances = sources.substanceExporter.export().toList();
		setProgress(3);

		setTaskStack("Uploading FHIR Substances to FHIR server...");
		uploadAll(substances);
		setProgress(4);

		setTaskStack("Downloading FHIR Medications from Neo4j...");
		List<Medication> medications = sources.medicationExporter.export().toList();
		setProgress(5);

		setTaskStack("Uploading FHIR Medications to FHIR server...");
		uploadAll(medications);
		setProgress(6);
	}

	private void uploadAll(List<? extends Resource> resources) throws IOException {
		Bundle bundle = new Bundle();

		bundle.setType(Bundle.BundleType.TRANSACTION);

		for (Resource resource : resources) {
			Bundle.BundleEntryComponent entry = bundle.addEntry();
			entry.setResource(resource);
			entry.getRequest()
			     .setUrl(resource.getClass().getSimpleName() + "/" + resource.getIdPart())
			     .setMethod(Bundle.HTTPVerb.PUT);
		}

		// Execute the transaction
		Bundle response = hapiClient.transaction().withBundle(bundle).execute();

		for (Bundle.BundleEntryComponent component : response.getEntry()) {
			if (component.hasResponse() && !component.getResponse().getStatus().startsWith("2")) {
				throw new IOException("The FHIR Server rejected a resource: "+component.getResponse().getStatus());
			}
		}
	}

	private IGenericClient buildHapiClient(String baseUrl) {
		context.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		return context.newRestfulGenericClient(baseUrl);
	}

}
