package de.medizininformatikinitiative.medgraph.tools.edqmscraper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Client supporting the creation of HTTP requests for the EDQM Standard Terms Database API.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsApiRequestBuilder {

	private static final String HMAC_ALGORITHM = "HmacSHA512";
	private static final String HOST = "standardterms.edqm.eu";
	private static final String BASE_URI = "https://" + HOST;
	private static final String API_PATH = "/standardterms/api/v1";

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

	private static final String DATE_HEADER = "Date";
	private static final String AUTH_HEADER = "X-STAPI-KEY";

	/**
	 * The login email of the standard terms database user.
	 */
	private final String email;
	/**
	 * The access key to the standard terms database for the given user.
	 */
	private final char[] secretKey;

	/**
	 * Creates a new API request builder.
	 * <p>
	 * The email you need to provide is your regular login email for the Standard Terms database. The secret key is
	 * <b>not</b> the password you use to log into the standard terms web interface. Instead, it's a separate
	 * 8-character code which is sent to you when you enable the web services on the Profile screen on the EDQM Standard
	 * Terms database web interface.
	 *
	 * @param email     the email used to log into the standard terms database
	 * @param secretKey the secret key used by this user to access the standard terms database
	 */
	public EdqmStandardTermsApiRequestBuilder(String email, char[] secretKey) {
		this.email = email;
		this.secretKey = secretKey;
	}

	/**
	 * Creates a HTTP GET Request to access the given URI within the standard terms database API. Please note the
	 * URI <b>must only contain the local path below the API endpoint</b>, e.g. "/regions" or, "/terms/en/1/1/1?min=1"
	 *
	 * @param uri the URI to access
	 * @return a properly configured http request
	 */
	public HttpRequest createRequest(String uri) throws URISyntaxException {
		return prepareRequest(uri).build();
	}

	/**
	 * Configures a HTTP Request Builder to access the given URI within the standard terms database API. Please note the
	 * URI <b>must only contain the local path below the API endpoint</b>, e.g. "/regions" or, "/terms/en/1/1/1?min=1"
	 * <p>
	 * Please note this only works for GET requests!
	 *
	 * @param uri the URI to access
	 * @return a properly configured http request builder
	 */
	public HttpRequest.Builder prepareRequest(String uri) throws URISyntaxException {
		if (!uri.startsWith("/")) uri = "/" + uri;

		String fullApiPath = API_PATH + uri;
		HttpRequest.Builder builder = HttpRequest.newBuilder(new URI(
				BASE_URI + fullApiPath
		));
		addSecurityHeaders(builder, "GET", fullApiPath);
		return builder;
	}

	/**
	 * Adds the two required headers for the server to accept the request to the given request builder.
	 *
	 * @param requestBuilder the request builder to which to add the headers
	 * @param requestMethod  the HTTP method used for the call, like "GET" or "POST"
	 * @param requestUri     the request URI, e.g. "/standardterms/api/v1/domains"
	 */
	private void addSecurityHeaders(
			HttpRequest.Builder requestBuilder,
			String requestMethod,
			String requestUri) {
		String rfcTime = FORMATTER.format(Instant.now().atOffset(ZoneOffset.UTC));

		requestBuilder
				.header(DATE_HEADER, rfcTime)
				.header(AUTH_HEADER, constructSecurityHeader(requestMethod.toUpperCase(), requestUri, rfcTime));
	}

	/**
	 * Constructs the security header used by the server to validate the request
	 *
	 * @param httpVerb        the HTTP method used for the call, like "GET" or "POST"
	 * @param requestUri      the request URI, e.g. "/standardterms/api/v1/domains"
	 * @param requestDateTime the value of the date time header provided with the request
	 * @return the value of the security header
	 */
	private String constructSecurityHeader(String httpVerb, String requestUri, String requestDateTime) {
		String stringToSign = httpVerb + "&" + requestUri + "&" + HOST + "&" + requestDateTime;
		String fullSignedMessage = Base64.getEncoder().encodeToString(sign(stringToSign));
		String signature = fullSignedMessage.substring(fullSignedMessage.length() - 22); // Only last 22 characters
		return email + "|" + signature;
	}

	/**
	 * Signs the given message using the secret key.
	 */
	private byte[] sign(String message) {
		try {
			Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
			SecretKeySpec secret_key = new SecretKeySpec(new String(secretKey).getBytes(), HMAC_ALGORITHM);
			hmac.init(secret_key);

			return hmac.doFinal(message.getBytes());
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}

}
