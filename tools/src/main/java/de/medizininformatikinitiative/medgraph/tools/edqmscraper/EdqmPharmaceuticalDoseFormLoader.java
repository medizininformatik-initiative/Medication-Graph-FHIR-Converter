package de.medizininformatikinitiative.medgraph.tools.edqmscraper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.medizininformatikinitiative.medgraph.tools.CSVWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.tools.edqmscraper.EdqmStandardTermsObject.*;

/**
 * Specialized {@link EdqmStandardTermsObjectsLoader} which only supports pharmaceutical dose forms to be loaded, but
 * also loads information about relations.
 *
 * @author Markus Budeus
 */
public class EdqmPharmaceuticalDoseFormLoader extends EdqmStandardTermsObjectsLoader {

	@Override
	protected EdqmPharmaceuticalDoseForm load(JsonElement element) {
		EdqmStandardTermsObject object = super.load(element);
		JsonObject links = element.getAsJsonObject().get("links").getAsJsonObject();

		List<String> bdf = getLinks(links, BASIC_DOSE_FORM_CLASS);
		List<String> isi = getLinks(links, INTENDED_SITE_CLASS);
		List<String> rca = getLinks(links, RELEASE_CHARACTERISTICS_CLASS);
		return new EdqmPharmaceuticalDoseForm(object, bdf, isi, rca);
	}

	/**
	 * Writes the relations from the loaded pharmaceutical dose forms to a file using the given CSV writer. The CSV file
	 * has the following structure:<br> SOURCECODE,TARGETCLASS,TARGETCODE
	 */
	public void writeRelationsToCsv(CSVWriter writer) {
		loadedObjects.forEach(edqmStandardTermsObject -> {
			EdqmPharmaceuticalDoseForm pdf = (EdqmPharmaceuticalDoseForm) edqmStandardTermsObject;

			if (pdf.getDomain().contains("Human")) {
				writeRelations(pdf.getCode(), BASIC_DOSE_FORM_CLASS, pdf.getBasicDoseFormCodes(), writer);
				writeRelations(pdf.getCode(), INTENDED_SITE_CLASS, pdf.getIntendedSiteCodes(), writer);
				writeRelations(pdf.getCode(), RELEASE_CHARACTERISTICS_CLASS, pdf.getReleaseCharacteristicsCodes(), writer);
			}
		});
	}

	private List<String> getLinks(JsonObject links, String property) {
		JsonElement targetLink = links.get(property);
		if (targetLink == null) {
			return Collections.emptyList();
		}
		JsonArray array = targetLink.getAsJsonArray();

		List<String> resultList = new ArrayList<>();
		array.forEach(element -> resultList.add(element.getAsJsonObject().get("code").getAsString()));
		return resultList;
	}

	private void writeRelations(String sourceCode, String targetClass, List<String> targetCodes, CSVWriter writer) {
		targetCodes.forEach(code -> writer.write(sourceCode, targetClass, code));
	}
}
