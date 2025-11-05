package de.medizininformatikinitiative.medgraph.fhirexporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import de.medizininformatikinitiative.medgraph.DI;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.function.TriFunction;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link FhirExportSink}-implementation that targets a FHIR server as sink.
 *
 * @author Markus Budeus
 */
public class FhirServerExportSink extends FhirExportSink {

	/**
	 * Logical ID parts of all medications that have thus far been processed or staged. Needed to ensure interdependent
	 * medications are staged together or in the correct order.
	 */
	private final Set<String> processedMedicationIds = new HashSet<>();
	private final FhirContext context = DI.get(FhirContext.class);
	private final IGenericClient hapiClient;

	/**
	 * Sets an export sink targetting the FHIR server with the given base url (e.g. http://localhost:8080/fhir)
	 *
	 * @param baseUrl The base url of the FHIR server to target.
	 */
	public FhirServerExportSink(String baseUrl) {
		hapiClient = buildHapiClient(baseUrl);
	}

	/**
	 * Sets an export sink targetting the FHIR server with the given base url (e.g. http://localhost:8080/fhir). The
	 * export will authenticate itself to the FHIR server using HTTP Basic Auth.
	 *
	 * @param baseUrl           The base url of the FHIR server to target.
	 * @param basicAuthUser     The basic auth user to use for authentication.
	 * @param basicAuthPassword The basic auth password to use for authentication.
	 */
	public FhirServerExportSink(String baseUrl, String basicAuthUser, char[] basicAuthPassword) {
		this(baseUrl);
		hapiClient.registerInterceptor(new BasicAuthInterceptor(basicAuthUser, new String(basicAuthPassword)));
	}

	/**
	 * Sets an export sink targetting the FHIR server with the given base url (e.g. http://localhost:8080/fhir). The
	 * export will authenticate itself to the FHIR server using a bearer token.
	 *
	 * @param baseUrl     The base url of the FHIR server to target.
	 * @param bearerToken The bearer token to use for authentication.
	 */
	public FhirServerExportSink(String baseUrl, String bearerToken) {
		this(baseUrl);
		hapiClient.registerInterceptor(new BearerTokenAuthInterceptor(bearerToken));
	}


	@Override
	public void doExport(FhirExportSources sources) throws IOException {
		int majorSteps = 6;
		setProgress(0, majorSteps);
		processedMedicationIds.clear();

		setTaskStack("Downloading FHIR Organizations from Neo4j...");
		List<Organization> organizations = sources.organizationExporter.export().toList();
		setProgress(1);

		setTaskStack("Uploading FHIR Organizations to FHIR server...");
		uploadAllBatches(toBatches(organizations, 1000));

		setTaskStack("Downloading FHIR Substances from Neo4j...");
		List<Substance> substances = sources.substanceExporter.export().toList();
		setProgress(3);

		setTaskStack("Uploading FHIR Substances to FHIR server...");
		uploadAllBatches(toBatches(substances, 1000));

		setTaskStack("Downloading FHIR Medications from Neo4j...");
		List<Medication> medications = sources.medicationExporter.export().toList();
		setProgress(5);

		setTaskStack("Uploading FHIR Medications to FHIR server...");
		uploadAllBatches(toBatches(medications, 1000, this::pullNextMedicationBatch));
		setProgress(majorSteps, majorSteps);
	}

	/**
	 * Splits the given list of resources into batches.
	 *
	 * @param resources       The list to split.
	 * @param targetBatchSize The target batch size to aim for.
	 */
	private <T extends Resource> List<List<T>> toBatches(
			List<T> resources,
			int targetBatchSize
	) {
		return toBatches(resources, targetBatchSize, (list, startIndex, targetSize) ->
				list.subList(startIndex, Math.min(startIndex + targetSize, list.size())));
	}

	/**
	 * Splits the given list of resources into batches.
	 *
	 * @param resources       The list to split.
	 * @param targetBatchSize The target batch size to aim for.
	 * @param pullFunction    A function which takes a list, a start index, and a target size and extracts the most
	 *                        appropriate sublist starting at the requested index and being at least (and as close as
	 *                        possible to) the target size. This is typically just a sublist function, but a more
	 *                        sophisticated extraction function can be provided if there are special constraints
	 *                        requiring certain elements to be in the same batch.
	 */
	private <T extends Resource> List<List<T>> toBatches(
			List<T> resources,
			int targetBatchSize,
			TriFunction<List<T>, Integer, Integer, List<T>> pullFunction
	) {
		List<List<T>> batches = new ArrayList<>(1 + (resources.size() / targetBatchSize));
		int index = 0;
		while (index < resources.size()) {
			List<T> batch = pullFunction.apply(resources, index, targetBatchSize);
			batches.add(batch);
			index += batch.size();
		}
		return batches;
	}

	/**
	 * Attempts to pull the next targetSize elements from the given list. But if this leaves references to other
	 * medications which have not yet been pulled, this function pulls additional elements until referential integrity
	 * requirements are resolved.
	 */
	private List<Medication> pullNextMedicationBatch(List<Medication> from, int startIndex, int targetSize) {
		List<Medication> result = new ArrayList<>(targetSize + 10);
		Set<String> unresolvedReferences = new HashSet<>();

		int i = 0;
		while ((i < targetSize || !unresolvedReferences.isEmpty()) && (i + startIndex) < from.size()) {
			Medication medication = from.get(startIndex + i);
			unresolvedReferences.addAll(getMedicationReferences(medication));
			processedMedicationIds.add(medication.getIdPart());
			result.add(medication);
			i++;
			if (i >= targetSize) {
				unresolvedReferences.removeAll(processedMedicationIds);
			}
		}
		return result;
	}

	/**
	 * Returns all logical ids of FHIR Medication resources referenced as ingredients by the given resource.
	 */
	private Set<String> getMedicationReferences(Medication medication) {
		return medication.getIngredient()
		                 .stream()
		                 .filter(Medication.MedicationIngredientComponent::hasItemReference)
		                 .map(Medication.MedicationIngredientComponent::getItemReference)
		                 .filter(reference -> reference.getType().equals("Medication"))
		                 .map(Reference::getReferenceElement)
		                 .map(IIdType::getIdPart)
		                 .collect(Collectors.toSet());
	}

	/**
	 * Uploads all given batches. Increments the progress for each batch uploaded.  This function assumes that the whole
	 * batch upload is a single major step and the current maxProgress indicates the number of major steps. When complete,
	 * this function will have returned the maxProgress to its original state and incremented the progress by one.
	 * It may however modify the progress and max progress in between for a smoother progress representation.
	 *
	 * @param batches    The batches of resources to upload.
	 */
	private void uploadAllBatches(List<? extends List<? extends Resource>> batches) throws IOException {
		int maxSteps = this.getMaxProgress();
		int majorStepsDone = this.getProgress();

		int subSteps = batches.size();
		setProgress(majorStepsDone * subSteps, maxSteps * subSteps);

		for (List<? extends Resource> batch : batches) {
			this.uploadAll(batch);
			incrementProgress();
		}
		setProgress(majorStepsDone + 1, maxSteps);
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
				throw new IOException("The FHIR Server rejected a resource: " + component.getResponse().getStatus());
			}
		}
	}

	private IGenericClient buildHapiClient(String baseUrl) {
		context.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		return context.newRestfulGenericClient(baseUrl);
	}

}
