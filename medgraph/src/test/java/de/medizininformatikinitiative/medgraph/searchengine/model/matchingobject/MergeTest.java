package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class MergeTest {

	@Test
	public void createSuccessfully() {
		new Merge(List.of(
				new OriginalMatch(new Substance(107, "A")),
				new OriginalMatch(new Substance(107, "A")),
				new OriginalMatch(new Substance(107, "A"))
		));
	}

	@Test
	public void differentMatchables() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Merge(List.of(
					new OriginalMatch(new Substance(107, "A")),
					new OriginalMatch(new Product(107, "A")),
					new OriginalMatch(new Substance(107, "A"))
			));
		});
	}

	@Test
	public void differentMatchables2() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Merge(List.of(
					new OriginalMatch(new Substance(107, "A")),
					new OriginalMatch(new Substance(108, "A"))
			));
		});
	}

	@Test
	public void emptyList() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Merge(List.of());
		});
	}

	@Test
	public void nullList() {
		assertThrows(NullPointerException.class, () -> {
			new Merge(null);
		});
	}


}