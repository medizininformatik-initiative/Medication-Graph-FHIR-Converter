package de.medizininformatikinitiative.medgraph.tools.fhirvalidator;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client which can access the Simplifier validator.
 *
 * @author Markus Budeus
 */
public class SimplifierClient {

	private static final long REQUEST_INTERVAL = 800;

	private static final Pattern RVT_PATTERN = Pattern.compile(
			"<input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"([a-zA-Z0-9-_/+=]*)\" />"
	);
	private static final Pattern VALIDATION_PATTERN = Pattern.compile(
			"<span>Validation:? <b>([A-Z]*)</b></span>"
	);

	private static final URI SIMPLIFIER_LOGIN;
	private static final URI SIMPLIFIER_VALIDATE;

	private final HttpClient client = HttpClient.newBuilder()
	                                            .cookieHandler(new CookieManager())
	                                            .build();
	private long lastRequest = 0;


	static {
		try {
			SIMPLIFIER_LOGIN = new URI("https://simplifier.net/login?returnurl=/");
			SIMPLIFIER_VALIDATE = new URI("https://simplifier.net/validate");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public void login(String username, char[] password) throws IOException, InterruptedException {
		String token = acquireVerificationToken(SIMPLIFIER_LOGIN);
		HttpRequest loginRequest = buildLoginRequest(username, password, token);
		HttpResponse<String> response = send(loginRequest);
		if (response.statusCode() != 302) {
			throw new IllegalStateException("Login failed! Status: " + response.statusCode());
		}
	}

	public boolean validate(String resource) throws IOException, InterruptedException {
		String token = acquireVerificationToken(SIMPLIFIER_VALIDATE);
		HttpRequest validationRequest = buildValidationRequest(resource, token);
		HttpResponse<String> response = send(validationRequest);

		if (response.statusCode() != 200) {
			throw new IllegalStateException("Validation attempt failed! Status: " + response.statusCode());
		}

		Matcher matcher = VALIDATION_PATTERN.matcher(response.body());
		if (!matcher.find()) {
			throw new IllegalStateException("Failed to identify validation result!");
		}

		return matcher.group(1).equals("SUCCESS");
	}

	private HttpRequest buildLoginRequest(String username, char[] password, String verificationToken) {
		String request =
				"UsernameEmail=" + urlEncode(username) +
						"&Password=" + urlEncode(new String(password)) +
						"&__RequestVerificationToken=" + urlEncode(verificationToken) +
						"&RememberMe=false";

		return HttpRequest.newBuilder(SIMPLIFIER_LOGIN)
		                  .header("Content-Type", "application/x-www-form-urlencoded")
		                  .POST(HttpRequest.BodyPublishers.ofString(request))
		                  .build();
	}

	private HttpRequest buildValidationRequest(String content, String verificationToken) {
		String request =
				"Scope.Moniker=MedizininformatikInitiative-ModulMedikation%40current" +
						"&Scope.DisplayName=MII+-+Basismodul+Medikation+%282023%29" +
						"" +
						"&FhirVersions=R4" +
						"&Content=" + urlEncode(content) +
						"&Validate=Validate" +
						"&__RequestVerificationToken=" + urlEncode(verificationToken) +
						"&SaveAsSnippet=false";
		return HttpRequest.newBuilder(SIMPLIFIER_VALIDATE)
		                  .header("Content-Type", "application/x-www-form-urlencoded")
		                  .POST(HttpRequest.BodyPublishers.ofString(request))
		                  .build();
	}

	private String acquireVerificationToken(URI uri) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(uri)
		                                 .build();
		String body = sendRequestAndReturnBody(request);
		return extractVerificationToken(body);
	}

	private static String extractVerificationToken(String pageContent) {
		Matcher matcher = RVT_PATTERN.matcher(pageContent);
		if (!matcher.find()) {
			throw new IllegalArgumentException("No Request Verification Token present!");
		}

		return matcher.group(1);
	}

	private String sendRequestAndReturnBody(HttpRequest request) throws IOException, InterruptedException {
		return send(request).body();
	}

	private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
		long time = System.currentTimeMillis();
		if (time - REQUEST_INTERVAL < lastRequest) {
			Thread.sleep(REQUEST_INTERVAL - time + lastRequest);
		}
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		lastRequest = time;
		return response;
	}

	private static String urlEncode(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}
}
