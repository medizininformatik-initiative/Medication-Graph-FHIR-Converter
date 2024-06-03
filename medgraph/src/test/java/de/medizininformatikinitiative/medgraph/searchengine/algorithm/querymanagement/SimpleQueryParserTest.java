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
		assertNotNull(query.getProductNameKeywords());
		assertTrue(query.getProductNameKeywords().contains("ProductA"));
		assertTrue(query.getProductNameKeywords().contains("ProductB"));
		assertTrue(query.getProductNameKeywords().contains("Aspirin"));
		assertFalse(query.getProductNameKeywords().contains("AspirinProductA"));
		assertFalse(query.getProductNameKeywords().contains("ProductBAspirin"));
	}

//	@Test
//	public void parseSubstances() {
//		SearchQuery query = sut.parse(new RawQuery("SubstanceA", "", "Acetylsalicylic-acid"));
//		assertNotNull(query.getSubstanceNameKeywords());
//		assertTrue(query.getSubstanceNameKeywords().contains("SubstanceA"));
//		assertTrue(query.getSubstanceNameKeywords().contains("Acetylsalicylic-acid"));
//		assertFalse(query.getSubstanceNameKeywords().contains("SubstanceAAcetylsalicylic"));
//		assertFalse(query.getSubstanceNameKeywords().contains("acidSubstance"));
//	}
//
//	@Test
//	public void substanceAndProductDoNotSpill() {
//		SearchQuery query = sut.parse(new RawQuery("Aspirin", "Bayer", "Acetylsalicylic_acid"));
//
//		assertNotNull(query.getProductNameKeywords());
//		assertNotNull(query.getSubstanceNameKeywords());
//		assertFalse(query.getProductNameKeywords().contains("Acetylsalicylic_acid"));
//		assertFalse(query.getSubstanceNameKeywords().contains("Bayer"));
//	}
//
//	@Test
//	public void noProductInfo() {
//		SearchQuery query = sut.parse(new RawQuery("", "", "Acetylsalicylic_acid"));
//
//		assertTrue(query.getProductNameKeywords().isEmpty());
//		assertEquals(List.of("Acetylsalicylic_acid"), query.getSubstanceNameKeywords());
//	}
//
//	@Test
//	public void noSubstanceInfo() {
//		SearchQuery query = sut.parse(new RawQuery("", "Aspirin-Bayer", ""));
//
//		assertTrue(query.getSubstanceNameKeywords().isEmpty());
//		assertEquals(List.of("Aspirin-Bayer"), query.getProductNameKeywords());
//	}

	@Test
	public void keywordSeparation() {
		SearchQuery query = sut.parse(new RawQuery("Tranexamsäure PUREN", "Bayer", ""));

		assertTrue(query.getProductNameKeywords().contains("Tranexamsäure"));
		assertTrue(query.getProductNameKeywords().contains("PUREN"));
		assertTrue(query.getProductNameKeywords().contains("Bayer"));
		assertEquals(3, query.getProductNameKeywords().size());
	}

	@Test
	public void keywordQuoting() {
		SearchQuery query = sut.parse(new RawQuery("\"Tranexamsäure PUREN\"", "", ""));

		assertEquals(List.of("Tranexamsäure PUREN"), query.getProductNameKeywords());
	}

}