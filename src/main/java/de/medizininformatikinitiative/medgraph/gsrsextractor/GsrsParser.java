package de.medizininformatikinitiative.medgraph.gsrsextractor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.medizininformatikinitiative.medgraph.gsrsextractor.extractor.GsrsSingleMatch;
import de.medizininformatikinitiative.medgraph.gsrsextractor.extractor.GsrsMultiMatch;
import de.medizininformatikinitiative.medgraph.gsrsextractor.extractor.GsrsSearchResult;
import de.medizininformatikinitiative.medgraph.gsrsextractor.refiner.GsrsObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser which can interpret responses from the GSRS API.
 *
 * @author Markus Budeus
 */
public class GsrsParser {

	public static GsrsObject constructFromInternalSubstanceRequestResponse(JsonElement queryResponse) {
		JsonObject root = queryResponse.getAsJsonObject();

		String uuid = getString(root, "uuid");
		String name = getString(root, "_name");
		String unii = getString(root, "approvalId");
		String substanceClass = getString(root, "substanceClass");
		String status = getString(root, "status");
		if (unii == null) unii = getString(root, "_approvalIDDisplay");

		List<String> primaryRxcui = new ArrayList<>();
		List<String> altRxcui = new ArrayList<>();
		List<String> primaryCas = new ArrayList<>();
		List<String> altCas = new ArrayList<>();

		JsonArray array = root.get("codes").getAsJsonArray();

		for (JsonElement element : array) {
			JsonObject object = element.getAsJsonObject();
			String codeSystem = getString(object, "codeSystem");
			boolean primary = "PRIMARY".equals(getString(object, "type"));
			String code = getString(object, "code");
			if (codeSystem == null || code == null) continue;
			switch (codeSystem) {
				case "CAS":
					if (primary) primaryCas.add(code); else altCas.add(code);
					break;
				case "RXCUI":
					if (primary) primaryRxcui.add(code); else altRxcui.add(code);
					break;
				default:
			}
		}

		return new GsrsObject(uuid, name, unii,
				primaryRxcui.toArray(new String[0]),
				altRxcui.toArray(new String[0]),
				assertAndGetAtMostOne(primaryCas),
				altCas.toArray(new String[0]),
				substanceClass,
				status);
	}

	private static String assertAndGetAtMostOne(List<String> list) {
		if (list.isEmpty()) return null;
		if (list.size() > 1) {
			// Check edge case: Botulinum toxin has the primary CAS number given twice, but its the same value
			// Therefore, we only throw if different values exist in the list
			String value = list.get(0);
			for (String entry : list) {
				if (!entry.equals(value)) {
					throw new IllegalStateException("Object contains more than one primary code of the same code system!");
				}
			}
		}
		return list.get(0);
	}

	public static GsrsSearchResult constructFromCasQueryResponse(String cas, JsonElement queryResponse) {
		JsonObject root = queryResponse.getAsJsonObject();
		JsonArray content = root.getAsJsonArray("content");
		if (content.isEmpty()) return null;
		if (content.size() > 1) {
			return parseToMultiMatch(cas, content);
		} else {
			return parseToSingleMatch(cas, root, content);
		}
	}

	private static GsrsMultiMatch parseToMultiMatch(String cas, JsonArray content) {
		List<String> uuids = new ArrayList<>();
		for (JsonElement element : content) {
			uuids.add(element.getAsJsonObject().get("uuid").getAsString());
		}
		return new GsrsMultiMatch(cas, uuids.toArray(new String[0]));
	}

	private static GsrsSingleMatch parseToSingleMatch(String cas, JsonObject root, JsonArray content) {
		JsonObject contentObject = content.get(0).getAsJsonObject();
		String uuid = getString(contentObject, "uuid");
		String name = getString(contentObject, "_name");
		String unii = getString(contentObject, "approvalID");
		if (unii == null) unii = getString(root, "_approvalIDDisplay");

		List<String> rxcui = findFacetLabelsByName("RXCUI", root.getAsJsonArray("facets"));

		String[] alternativeRxcui = rxcui != null ? rxcui.toArray(new String[0]) : new String[0];

		return new GsrsSingleMatch(uuid, name, cas, unii, alternativeRxcui);
	}

	private static String getString(JsonObject object, String member) {
		JsonElement memberObj = object.get(member);
		if (memberObj == null) return null;
		return memberObj.getAsString();
	}

	private static List<String> findFacetLabelsByName(String facetName, JsonArray facets) {
		for (JsonElement element : facets) {
			if (element.isJsonObject()) {
				JsonObject object = element.getAsJsonObject();
				if ("RXCUI".equals(object.get("name").getAsString()))
					return extracsFacetLabels(object);
			}
		}
		return null;
	}

	private static List<String> extracsFacetLabels(JsonObject facet) {
		try {
			JsonArray array = facet.getAsJsonArray("values");
			List<String> result = new ArrayList<>(array.size());
			for (JsonElement element : array) {
				result.add(element.getAsJsonObject().get("label").getAsString());
			}
			return result;
		} catch (IllegalStateException e) {
			throw new IllegalStateException("Failed to extract facet labels!", e);
		}
	}

}
