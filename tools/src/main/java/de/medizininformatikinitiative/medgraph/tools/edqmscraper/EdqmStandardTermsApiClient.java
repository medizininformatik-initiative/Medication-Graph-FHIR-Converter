package de.medizininformatikinitiative.medgraph.tools.edqmscraper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Client which supports the execution of API calls against the EDQM standard terms database.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsApiClient implements AutoCloseable {

	// This class is intentionally left barebone! I do not need all of the endpoints, but it should be obvious
	// enough how to add methods for othe relevant endpoints.

	private final HttpClient client;
	private final EdqmStandardTermsApiRequestBuilder requestBuilder;

	public EdqmStandardTermsApiClient(String email, char[] secretKey) {
		this(HttpClient.newHttpClient(), email, secretKey);
	}

	public EdqmStandardTermsApiClient(HttpClient client, String email, char[] secretKey) {
		this.client = client;
		requestBuilder = new EdqmStandardTermsApiRequestBuilder(email, secretKey);
	}

	/**
	 * Returns the full list of traditional standard terms with the given concept name.
	 */
	public String getFullDataByClass(String conceptName) throws IOException, InterruptedException, URISyntaxException {
		return executeRequest("/full_data_by_class/" + conceptName + "/1/1");
	}

	/**
	 * Executes an API request against the given uri and returns the response as string.
	 *
	 * @throws IllegalStateException if the request failed due to an authentication failure
	 * @throws IOException           if the http request failed for other reasons
	 * @throws InterruptedException  if the thread was interrupted while waiting for the response
	 */
	private String executeRequest(String uri) throws IOException, InterruptedException, URISyntaxException {
		HttpRequest request = requestBuilder.createRequest(uri);
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 403)
			throw new IllegalStateException(
					"Authentication failed (Status 403)! Please ensure the email and secret key passed to the constructor are correct."
			);
		return response.body();
	}

	@Override
	public void close() {
		client.close();
	}
}
