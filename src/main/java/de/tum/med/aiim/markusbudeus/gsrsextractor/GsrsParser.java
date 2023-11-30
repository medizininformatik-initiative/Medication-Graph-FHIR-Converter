package de.tum.med.aiim.markusbudeus.gsrsextractor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class GsrsParser {

	public static GsrsSearchResult constructFromQueryResponse(String cas, JsonElement queryResponse) {
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

	private static GsrsObject parseToSingleMatch(String cas, JsonObject root, JsonArray content) {
		JsonObject contentObject = content.get(0).getAsJsonObject();
		String uuid = getString(contentObject, "uuid");
		String name = getString(contentObject, "_name");
		String unii = getString(contentObject, "approvalID");

		List<String> rxcui = findFacetLabelsByName("RXCUI", root.getAsJsonArray("facets"));

		String[] alternativeRxcui = rxcui != null ? rxcui.toArray(new String[0]) : new String[0];

		return new GsrsObject(uuid, name, cas, unii, alternativeRxcui);
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
