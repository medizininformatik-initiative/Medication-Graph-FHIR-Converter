package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.Amount;
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage;
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class SimpleQueryParserTest {

	private SimpleQueryParser sut;

	@BeforeEach
	void setUp() {
		sut = new SimpleQueryParser();
	}

	@Test
	public void parseProducts() {
		SearchQuery query = sut.parse(new RawQuery("ProductA ProductB", "Aspirin", ""));
		assertNotNull(query.getProductName());
		assertTrue(query.getProductName().contains("ProductA ProductB"));
		assertTrue(query.getProductName().contains("Aspirin"));
		assertFalse(query.getProductName().contains("AspirinProductA"));
		assertFalse(query.getProductName().contains("ProductBAspirin"));
	}

	@Test
	public void parseSubstances() {
		SearchQuery query = sut.parse(new RawQuery("SubstanceA", "", "Acetylsalicylic acid"));
		assertNotNull(query.getSubstanceName());
		assertTrue(query.getSubstanceName().contains("SubstanceA"));
		assertTrue(query.getSubstanceName().contains("Acetylsalicylic acid"));
		assertFalse(query.getSubstanceName().contains("SubstanceAAcetylsalicylic"));
		assertFalse(query.getSubstanceName().contains("acidSubstance"));
	}

	@Test
	public void substanceAndProductDoNotSpill() {
		SearchQuery query = sut.parse(new RawQuery("Aspirin", "Bayer", "Acetylsalicylic acid"));

		assertNotNull(query.getProductName());
		assertNotNull(query.getSubstanceName());
		assertFalse(query.getProductName().contains("Acetylsalicylic acid"));
		assertFalse(query.getSubstanceName().contains("Bayer"));
	}

	@Test
	public void noProductInfo() {
		SearchQuery query = sut.parse(new RawQuery("", "", "Acetylsalicylic acid"));

		assertNull(query.getProductName());
		assertEquals("Acetylsalicylic acid", query.getSubstanceName());
	}

	@Test
	public void noSubstanceInfo() {
		SearchQuery query = sut.parse(new RawQuery("", "Aspirin Bayer", ""));

		assertNull(query.getSubstanceName());
		assertEquals("Aspirin Bayer", query.getProductName());
	}

	@Test
	public void dosageInfoFiltered() {
		SearchQuery query = sut.parse(new RawQuery("Aspirin 500 mg", "", ""));

		assertNotNull(query.getProductName());
		assertTrue(query.getProductName().contains("Aspirin"));
		assertFalse(query.getProductName().contains("500"));
		assertFalse(query.getProductName().contains("mg"));
		assertEquals(List.of(Dosage.of(500, "mg")), query.getActiveIngredientDosages());
	}

	@Test
	public void amountInfoFiltered() {
		SearchQuery query = sut.parse(new RawQuery("Tranexamsäure 1 ml", "", ""));

		assertNotNull(query.getSubstanceName());
		assertTrue(query.getSubstanceName().contains("Tranexamsäure"));
		assertFalse(query.getSubstanceName().contains("1"));
		assertFalse(query.getSubstanceName().contains("ml"));
		assertEquals(List.of(new Amount(BigDecimal.ONE, "ml")), query.getDrugAmounts());
	}

}