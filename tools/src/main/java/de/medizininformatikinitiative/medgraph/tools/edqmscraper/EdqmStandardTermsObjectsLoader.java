package de.medizininformatikinitiative.medgraph.tools.edqmscraper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.medizininformatikinitiative.medgraph.tools.CSVWriter;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads {@link EdqmStandardTermsObject}s from JSON and can export them to CSV.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsObjectsLoader {

	protected final Gson gson = new Gson();
	protected final List<EdqmStandardTermsObject> loadedObjects = new ArrayList<>();

	public void load(String apiResponseJson) {
		loadedObjects.clear();
		JsonObject object = gson.fromJson(apiResponseJson, JsonObject.class);
		JsonArray content = object.get("content").getAsJsonArray();

		content.forEach(jsonElement -> {
			loadedObjects.add(load(jsonElement));
		});
	}

	protected EdqmStandardTermsObject load(JsonElement element) {
		return gson.fromJson(element, EdqmStandardTermsObject.class);
	}

	/**
	 * Writes the currently loaded objects to the given CSV writer. Only objects which are meant for human use are
	 * included. The CSV file is structured as follows:<br> CLASS,CODE,NAME,STATUS
	 */
	public void writeToCsv(CSVWriter writer) {
		loadedObjects.forEach(edqmStandardTermsObject -> {
			if (edqmStandardTermsObject.getDomain().contains("Human")) {
				writer.write(
						edqmStandardTermsObject.getObjectClass(),
						edqmStandardTermsObject.getCode(),
						edqmStandardTermsObject.getName(),
						edqmStandardTermsObject.getStatus()
				);
			}
		});
	}

}
