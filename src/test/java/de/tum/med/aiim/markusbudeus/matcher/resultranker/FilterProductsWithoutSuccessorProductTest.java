package de.tum.med.aiim.markusbudeus.matcher.resultranker;

import de.tum.med.aiim.markusbudeus.matcher.TestWithSession;
import de.tum.med.aiim.markusbudeus.matcher.model.Product;
import de.tum.med.aiim.markusbudeus.matcher.model.Substance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterProductsWithoutSuccessorProductTest extends TestWithSession {

	private static FilterProductsWithoutSuccessorProduct sut;

	@BeforeAll
	public static void setUpAll() {
		sut = new FilterProductsWithoutSuccessorProduct(session);
	}

	@Test
	public void testWithoutSuccessor() {
		assertTrue(sut.passesFilter(new Product(364874L, ""), null));
	}

	@Test
	public void testWithSuccessor() {
		assertFalse(sut.passesFilter(new Product(354453L, ""), null));
	}

	@Test
	public void testWithCircularSuccession() {
		assertTrue(sut.passesFilter(new Product(444968L, ""), null));
	}

	@Test
	public void testNonProduct() {
		assertEquals(FilterProductsWithoutSuccessorProduct.ALLOW_NON_PRODUCTS,
				sut.passesFilter(new Substance(1L, ""), null));
	}

	@Test
	public void multiTest() {
		List<Boolean> results = sut.batchPassesFilter(List.of(
				new Product(364874L, ""),
				new Product(354453L, ""),
				new Product(444968L, ""),
				new Substance(1L, "")
		), null);
		assertEquals(List.of(true, false, true, FilterProductsWithoutSuccessorProduct.ALLOW_NON_PRODUCTS), results);
	}

}