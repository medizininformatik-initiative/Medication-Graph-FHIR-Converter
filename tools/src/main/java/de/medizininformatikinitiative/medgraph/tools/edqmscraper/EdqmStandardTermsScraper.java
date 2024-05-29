package de.medizininformatikinitiative.medgraph.tools.edqmscraper;

import de.medizininformatikinitiative.medgraph.tools.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static de.medizininformatikinitiative.medgraph.tools.edqmscraper.EdqmConcept.*;

/**
 * Accesses the EDQM standard terms API to download information about all pharmaceutical dose forms and writes them to a
 * CSV file.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsScraper {

	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
		downloadData(new File("."), "markus.budeus@tum.de", "REDACTED".toCharArray());
	}

	/**
	 * Accesses the EDQM Standard Terms API, downloads information about the pharmaceutical dose forms and other objects
	 * of interest and stores them in two files in the target directory.
	 * <p>
	 * 1. A file edqmObjects.csv, which contains all the downloaded objects of interest<br> 2. A file pdfRelations.csv,
	 * which contains relationships between the pharmaceutical dose forms and the other objects of interest
	 *
	 * @param targetDir    the target directory into which to write the results
	 * @param apiEmail     the email address to access the API
	 * @param apiSecretKey the secret key to access the API
	 */
	public static void downloadData(File targetDir, String apiEmail, char[] apiSecretKey)
	throws IOException, URISyntaxException, InterruptedException {
		File objectsOutFile = new File(targetDir + File.separator + "edqm_objects.csv");
		objectsOutFile.createNewFile();
		File relationsOutFile = new File(targetDir + File.separator + "pdf_relations.csv");
		relationsOutFile.createNewFile();
		File translationsOutFile = new File(targetDir + File.separator + "edqm_translations.csv");
		relationsOutFile.createNewFile();

		EdqmConceptLoader objectsLoader = new EdqmConceptLoader();
		try (EdqmStandardTermsApiClient apiClient = new EdqmStandardTermsApiClient(apiEmail, apiSecretKey);
		     FileWriter objectsOutFileWriter = new FileWriter(objectsOutFile);
		     FileWriter relationsOutFileWriter = new FileWriter(relationsOutFile);
		     FileWriter translationsOutFileWriter = new FileWriter(translationsOutFile);
		     CSVWriter objectsWriter = CSVWriter.open(objectsOutFileWriter);
		     CSVWriter relationsWriter = CSVWriter.open(relationsOutFileWriter);
		     CSVWriter translationsWriter = CSVWriter.open(translationsOutFileWriter)
		) {
			String notice = generateNotice();
			objectsOutFileWriter.write(notice);
			objectsWriter.write("CLASS", "CODE", "NAME", "STATUS");
			relationsOutFileWriter.write(notice);
			relationsWriter.write("SOURCECLASS", "SOURCECODE", "TARGETCLASS", "TARGETCODE");
			translationsOutFileWriter.write(notice);
			translationsWriter.write("CLASS", "CODE", "SYNONYME");

			loadClassAndWriteObjectsAndTranslations(objectsLoader, PHARMACEUTICAL_DOSE_FORM_CLASS, apiClient,
					objectsWriter, translationsWriter);
			objectsLoader.writeRelationsToCsv(relationsWriter, BASIC_DOSE_FORM_CLASS);
			objectsLoader.writeRelationsToCsv(relationsWriter, INTENDED_SITE_CLASS);
			objectsLoader.writeRelationsToCsv(relationsWriter, RELEASE_CHARACTERISTICS_CLASS);

			loadClassAndWriteObjectsAndTranslations(objectsLoader, BASIC_DOSE_FORM_CLASS, apiClient,
					objectsWriter, translationsWriter);
			loadClassAndWriteObjectsAndTranslations(objectsLoader, INTENDED_SITE_CLASS, apiClient,
					objectsWriter, translationsWriter);
			loadClassAndWriteObjectsAndTranslations(objectsLoader, RELEASE_CHARACTERISTICS_CLASS, apiClient,
					objectsWriter, translationsWriter);
		}
	}

	private static void loadClassAndWriteObjectsAndTranslations(EdqmConceptLoader loader, String objectClass,
														 EdqmStandardTermsApiClient client,
	                                                     CSVWriter objectsWriter, CSVWriter translationsWriter)
	throws IOException, URISyntaxException, InterruptedException {

		loader.load(client.getFullDataByClass(objectClass));
		loader.writeObjectsToCsv(objectsWriter);
		loader.writeTranslationsToCsv(translationsWriter, "de");
	}

	private static String generateNotice() {
		return "# " + DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDateTime.now()) + "\n" +
				"# Data taken from the EDQM Standard Terms database (http://standardterms.edqm.eu) " +
				"with the permission of the European Directorate for the Quality of Medicines & HealthCare, " +
				"Council of Europe (EDQM).\n";
	}

}
