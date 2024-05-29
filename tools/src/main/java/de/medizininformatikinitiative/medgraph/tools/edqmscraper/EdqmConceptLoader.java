package de.medizininformatikinitiative.medgraph.tools.edqmscraper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.medizininformatikinitiative.medgraph.tools.CSVWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads {@link EdqmConcept}s from JSON and can export them to CSV.
 *
 * @author Markus Budeus
 */
public class EdqmConceptLoader {

	protected final Gson gson = new Gson();
	protected final List<EdqmConcept> loadedObjects = new ArrayList<>();

	public void load(String apiResponseJson) {
		loadedObjects.clear();
		JsonObject object = gson.fromJson(apiResponseJson, JsonObject.class);
		JsonArray content = object.get("content").getAsJsonArray();

		content.forEach(jsonElement -> {
			EdqmConcept concept = load(jsonElement);
			if (isApplicable(concept)) {
				loadedObjects.add(concept);
			}
		});
	}

	protected EdqmConcept load(JsonElement element) {
		return gson.fromJson(element, EdqmConcept.class);
	}

	/**
	 * Writes the currently loaded objects to the given CSV writer. Only objects which are meant for human use are
	 * included. The CSV file is structured as follows:<br> CLASS,CODE,NAME,STATUS
	 */
	public void writeObjectsToCsv(CSVWriter writer) {
		loadedObjects.forEach(edqmConcept -> {
			writer.write(
					edqmConcept.getObjectClass(),
					edqmConcept.getCode(),
					edqmConcept.getName(),
					edqmConcept.getStatus()
			);
		});
	}

	/**
	 * Writes the relationships of the loaded objects to objects of the given target class to the given CSV writer.  The
	 * CSV file is structured as follows:<br> SOURCECLASS,SOURCECODE,TARGETCLASS,TARGETCODE
	 *
	 * @param writer      the csv writer to write the relationships to
	 * @param targetClass the class of target objects for which the relationships shall be written
	 */
	public void writeRelationsToCsv(CSVWriter writer, String targetClass) {
		loadedObjects.forEach(concept -> {
			Map<String, List<Link>> linkMap = concept.getLinksByClassName();
			if (linkMap != null) {
				List<Link> links = linkMap.get(targetClass);
				if (links != null) {
					links.forEach(link -> writer.write(
							concept.getObjectClass(), concept.getCode(),
							targetClass, link.getCode())
					);
				}
			}
		});
	}

	/**
	 * Writes the translation of the given language for each loaded object into the given CSV writer, if such a
	 * translation exists. The CSV format is:<br>CLASS,CODE,TRANSLATION
	 * @param writer the writer to write the translations to
	 * @param language the language of which to write the translations
	 */
	public void writeTranslationsToCsv(CSVWriter writer, String language) {
		loadedObjects.forEach(concept -> {
			List<Translation> translations = concept.getTranslations();
			if (translations != null) {
				translations.forEach(translation -> {
					if (translation.getLanguage().equals(language)) {
						writer.write(concept.getObjectClass(), concept.getCode(), translation.getTerm());
					}
				});
			}
		});
	}

	private boolean isApplicable(EdqmConcept concept) {
		return concept.getDomain().contains("Human");
	}
}
