package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import de.tum.med.aiim.markusbudeus.matcher.Amount;
import de.tum.med.aiim.markusbudeus.matcher.Dosage;
import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Session;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class DosageFilterTest {

	private static DatabaseConnection connection;
	private static Session session;
	private static BaseProvider provider;

	private static DosageFilter sut;

	@BeforeAll
	public static void setupAll() {
		connection = new DatabaseConnection();
		session = connection.createSession();
		provider = BaseProvider.ofDatabaseSynonymes(session);
		sut = new DosageFilter(session);
	}

	@Test
	public void testAbsolute() {
		IdentifierTarget target = getProductByName("Methylprednisolut® 1000 mg, Pulver und Lösungsmittel zur Herstellung einer Injektions-/Infusionslösung");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal(1000), "mg"), null, null));
		assertTrue(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testAbsolute2() {
		IdentifierTarget target = getProductByName("Methylprednisolut® 1000 mg, Pulver und Lösungsmittel zur Herstellung einer Injektions-/Infusionslösung");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal(900), "mg"), null, null));
		assertFalse(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testAbsolute3() {
		IdentifierTarget target = getProductByName("Methylprednisolut® 1000 mg, Pulver und Lösungsmittel zur Herstellung einer Injektions-/Infusionslösung");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal(1000), "ml"), null, null));
		assertFalse(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testRelative() {
		IdentifierTarget target = getProductByName("Berberil® N Augentropfen, 0,5 mg/ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "ml")));
		assertTrue(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testRelative2() {
		IdentifierTarget target = getProductByName("Berberil® N Augentropfen, 0,5 mg/ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.TEN, "ml")));
		assertFalse(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testRelative3() {
		IdentifierTarget target = getProductByName("Berberil® N Augentropfen, 0,5 mg/ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "mg")));
		assertFalse(sut.passesFilter(target, sampleEntry));
	}

	@AfterAll
	public static void tearDownAll() {
		session.close();
		connection.close();
	}

	private IdentifierTarget getProductByName(String name) {
		MappedIdentifier<String> identifier = provider.identifiers.get(name);
		if (identifier == null) throw new NoSuchElementException();
		if (identifier.targets.size() != 1) throw new NoSuchElementException("Product name is not unique!");
		return identifier.targets.iterator().next();
	}

}