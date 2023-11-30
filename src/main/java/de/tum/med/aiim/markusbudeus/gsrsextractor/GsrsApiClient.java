package de.tum.med.aiim.markusbudeus.gsrsextractor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GsrsApiClient {

	private static final String BASE_URL = "https://gsrs.ncats.nih.gov/ginas/app/api/v1/";

	private final HttpClient client;

	public GsrsApiClient() {
		client = HttpClient.newHttpClient();
	}

	public HttpResponse<String> makeRequest(String apiRequestUrl) throws IOException, InterruptedException {
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

			return GsrsParser.constructFromQueryResponse(cas, response);

		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Failed to parse JSON object for CAS number " + cas + ": " + e.getMessage());
			return null;
		}
	}

}
