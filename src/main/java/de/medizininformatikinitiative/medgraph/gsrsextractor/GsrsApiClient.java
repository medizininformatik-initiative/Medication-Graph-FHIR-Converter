package de.medizininformatikinitiative.medgraph.gsrsextractor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.medizininformatikinitiative.medgraph.gsrsextractor.extractor.GsrsSearchResult;
import de.medizininformatikinitiative.medgraph.gsrsextractor.refiner.GsrsObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Client which can be used to access the GSRS api.
 *
 * @author Markus Budeus
 */
public class GsrsApiClient {

	private static final String BASE_URL = "https://gsrs.ncats.nih.gov/ginas/app/api/v1/";

	private static final int REQUEST_INTERVAL = 300;

	private final HttpClient client;
	private long lastRequestTime = 0;

	public GsrsApiClient() {
		client = HttpClient.newHttpClient();
	}

	public HttpResponse<String> makeRequest(String apiRequestUrl) throws IOException, InterruptedException {
		long waitTime = REQUEST_INTERVAL - (System.currentTimeMillis() - lastRequestTime);
		if (waitTime > 0) {
			Thread.sleep(waitTime);
		}
		lastRequestTime = System.currentTimeMillis();

		HttpRequest request = HttpRequest
				.newBuilder(URI.create(BASE_URL + apiRequestUrl))
				.header("Accept", "application/json")
				.build();
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}

	public JsonElement makeRequestAndParse(String apiRequestUrl) throws IOException, InterruptedException {
		HttpResponse<String> response = makeRequest(apiRequestUrl);
		String content = response.body();
		return JsonParser.parseString(content);
	}

	public GsrsSearchResult findSubstanceByCas(String cas) throws IOException, InterruptedException {
		JsonElement response = makeRequestAndParse("substances/search?q=root_codes_CAS%3A%22" + cas + "%22");
		try {

			return GsrsParser.constructFromCasQueryResponse(cas, response);

		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Failed to parse JSON object for CAS number " + cas + ": " + e.getMessage());
			return null;
		}
	}

	public GsrsObject getObject(String uuid) throws IOException, InterruptedException {
		HttpResponse<String> response = makeRequest("substances("+uuid+")?view=internal");
		if (response.statusCode() == 404) return null;
		return GsrsParser.constructFromInternalSubstanceRequestResponse(JsonParser.parseString(response.body()));
	}

}
