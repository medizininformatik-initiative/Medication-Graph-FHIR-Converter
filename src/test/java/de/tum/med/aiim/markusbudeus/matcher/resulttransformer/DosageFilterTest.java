package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.TestWithSession;
import de.tum.med.aiim.markusbudeus.matcher.model.Amount;
import de.tum.med.aiim.markusbudeus.matcher.model.Dosage;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.provider.BaseProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DosageFilterTest extends TestWithSession {

	private static BaseProvider<String> provider;

	private static DosageFilter sut;

	@BeforeAll
	public static void setupAll() {
		provider = BaseProvider.ofDatabaseSynonymes(session);
		sut = new DosageFilter(session);
	}

	@Test
	public void testAbsolute() {
		MatchingTarget target = getProductByName(
				"Methylprednisolut® 1000 mg, Pulver und Lösungsmittel zur Herstellung einer Injektions-/Infusionslösung");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal(1000), "mg"), null, null));
		assertTrue(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testAbsolute2() {
		MatchingTarget target = getProductByName(
				"Methylprednisolut® 1000 mg, Pulver und Lösungsmittel zur Herstellung einer Injektions-/Infusionslösung");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal(900), "mg"), null, null));
		assertFalse(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testAbsolute3() {
		MatchingTarget target = getProductByName(
				"Methylprednisolut® 1000 mg, Pulver und Lösungsmittel zur Herstellung einer Injektions-/Infusionslösung");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(new Dosage(new Amount(new BigDecimal(1000), "ml"), null, null));
		assertFalse(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testRelative() {
		MatchingTarget target = getProductByName("Berberil® N Augentropfen, 0,5 mg/ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "ml")));
		assertTrue(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testRelative2() {
		MatchingTarget target = getProductByName("Berberil® N Augentropfen, 0,5 mg/ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.TEN, "ml")));
		assertFalse(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testRelative3() {
		MatchingTarget target = getProductByName("Berberil® N Augentropfen, 0,5 mg/ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("0.5"), "mg"), null, new Amount(BigDecimal.ONE, "mg")));
		assertFalse(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testRelative4() {
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 5 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("500"), "mg"), null, new Amount(new BigDecimal("5"), "ml")));
		assertTrue(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void testRelative5() {
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 5 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("100"), "mg"), null, new Amount(BigDecimal.ONE, "ml")));
		assertTrue(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void matchesDrugAmount() {
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 10 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("10"), "ml"), null, null)
		);
		assertTrue(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void allMustMatchPositive() {
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 10 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("100"), "mg"), null, new Amount(BigDecimal.ONE, "ml")),
				new Dosage(new Amount(new BigDecimal("10"), "ml"), null, null)
		);
		assertTrue(sut.passesFilter(target, sampleEntry));
	}

	@Test
	public void allMustMatchNegative() {
		MatchingTarget target = getProductByName("Tranexamsäure Carinopharm 100 mg/ml Injektionslösung, 5 ml");
		HouselistEntry sampleEntry = new HouselistEntry();
		sampleEntry.activeIngredientDosages = List.of(
				new Dosage(new Amount(new BigDecimal("100"), "mg"), null, new Amount(BigDecimal.ONE, "ml")),
				new Dosage(new Amount(new BigDecimal("10"), "ml"), null, null)
		);
		assertFalse(sut.passesFilter(target, sampleEntry));
	}

	private MatchingTarget getProductByName(String name) {

		List<MappedIdentifier<String>> results = provider.identifiers
				.stream().filter(i -> i.identifier.equals(name)).toList();

		if (results.size() != 1) throw new NoSuchElementException("Product name is not unique!");
		return results.get(0).target;
	}

}