package de.tum.med.aiim.markusbudeus.gsrsextractor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class GsrsParser {

	public static GsrsObject constructFromQueryResponse(JsonElement queryResponse) {

			JsonObject root = queryResponse.getAsJsonObject();
			JsonArray content = root.getAsJsonArray("content");
			if (content.isEmpty()) return null;
			if (content.size() > 1) {
				throw new IllegalArgumentException("Found multiple results in the given response!");
			}
			JsonObject contentObject = content.get(0).getAsJsonObject();
			String unii = contentObject.get("approvalID").getAsString();

			List<String> rxcui = findFacetLabelsByName("RXCUI", root.getAsJsonArray("facets"));

			String primaryRxcui = rxcui == null || rxcui.isEmpty() ?
					null : rxcui.get(0);
			String[] alternativeRxcui = rxcui != null && rxcui.size() > 1 ?
					rxcui.subList(1, rxcui.size()).toArray(new String[0]) : new String[0];

			return new GsrsObject(unii, primaryRxcui, alternativeRxcui);
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
