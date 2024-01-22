package de.tum.med.aiim.markusbudeus.fhirvalidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.tum.med.aiim.markusbudeus.fhirexporter.Main.*;

public class Main {

	private static final String USERNAME = "markus.budeus@tum.de";
	private static final char[] PASSWORD = "#Ak61Mb)RZuG^^#~5b".toCharArray();

	private static final Path BASE_PATH = OUT_PATH;

	private static final File MEDICATION_DIR = BASE_PATH.resolve(MEDICATION_OUT_PATH).toFile();
	private static final File SUBSTANCE_DIR = BASE_PATH.resolve(SUBSTANCE_OUT_PATH).toFile();
	private static final File ORGANIZATION_DIR = BASE_PATH.resolve(ORGANIZATION_OUT_PATH).toFile();

	private static final Pattern PRODUCT_IDENTIFIER_PATTERN = Pattern.compile("^[0-9]+-[0-9]+");
	private static final Pattern COMPOSITE_CHILD_PATTERN = Pattern.compile("^([0-9]+-[0-9]+)-[0-9]+");

	public static void main(String[] args) throws IOException, InterruptedException {
		SimplifierClient client = new SimplifierClient();
		client.login(USERNAME, PASSWORD);

		Random random = new Random(-78512384677349L);
		Set<String> compositeIdentifiers = getCompositeFileIdentifiers();
		RandomOrderFileSet simpleObjects = new RandomOrderFileSet(MEDICATION_DIR,
				(dir, name) -> !compositeIdentifiers.contains(getProductIdentifierFromFilename(name)), random);
		RandomOrderFileSet compositeParentObjects = new RandomOrderFileSet(MEDICATION_DIR,
				(dir, name) -> compositeIdentifiers.contains(getProductIdentifierFromFilename(name)) &&
						!COMPOSITE_CHILD_PATTERN.matcher(name).find(), random);
		RandomOrderFileSet compositeChildObjects = new RandomOrderFileSet(MEDICATION_DIR,
				(dir, name) -> compositeIdentifiers.contains(getProductIdentifierFromFilename(name)) &&
						COMPOSITE_CHILD_PATTERN.matcher(name).find(), random);
		RandomOrderFileSet substances = new RandomOrderFileSet(SUBSTANCE_DIR, random);
		RandomOrderFileSet organizations = new RandomOrderFileSet(ORGANIZATION_DIR, random);

		System.out.println("Simple objects found: "+simpleObjects.size());
		System.out.println("Composite parents found: "+compositeParentObjects.size());
		System.out.println("Composite children found: "+compositeChildObjects.size());
		System.out.println("Substances found: "+substances.size());
		System.out.println("Organizations found: "+organizations.size());

		validateFiles(client, simpleObjects, 30);
		validateFiles(client, compositeParentObjects, 20);
		validateFiles(client, compositeChildObjects, 20);
		validateFiles(client, substances, 20);
		validateFiles(client, organizations, 20);
	}

	public static Set<String> getCompositeFileIdentifiers() {
		File[] allFiles = Objects.requireNonNull(MEDICATION_DIR.listFiles());
		Set<String> resultSet = new HashSet<>();
		for (File f: allFiles) {
			String name = f.getName();
			Matcher matcher = COMPOSITE_CHILD_PATTERN.matcher(name);
			if (matcher.find()) {
				resultSet.add(matcher.group(1));
			}
		}

		return resultSet;
	}

	public static String getProductIdentifierFromFilename(String filename) {
		Matcher matcher = PRODUCT_IDENTIFIER_PATTERN.matcher(filename);
		if (!matcher.find())
			throw new IllegalArgumentException("Product identifier not found!");
		return matcher.group();
	}


	public static void validateFiles(SimplifierClient client, Supplier<File> fileSelector, int iterations)
	throws IOException, InterruptedException {
		for (int i = 0; i < iterations; i++) {
			File f = fileSelector.get();
			System.out.println("Validating "+f.getName());

			if (!client.validate(Files.readString(f.toPath()))) {
				throw new IllegalStateException("Validation of "+f.getName()+" unsuccessful!");
			}
		}
	}

}
