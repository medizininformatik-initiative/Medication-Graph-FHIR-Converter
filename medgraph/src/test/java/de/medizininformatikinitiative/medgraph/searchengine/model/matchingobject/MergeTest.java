package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
@SuppressWarnings("ConstantConditions")
public class MergeTest {

	@Test
	public void createSuccessfully() {
		new Merge<>(List.of(
				new OriginalMatch<>(new Substance(107, "A")),
				new OriginalMatch<>(new Substance(107, "A")),
				new OriginalMatch<>(new Substance(107, "A"))
		), ScoreMergingStrategy.MAX);
	}

	@Test
	public void differentMatchables() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Merge<>(List.of(
					new OriginalMatch<>(new Substance(107, "A")),
					new OriginalMatch<>(new Product(107, "A")),
					new OriginalMatch<>(new Substance(107, "A"))
			), ScoreMergingStrategy.SUM);
		});
	}

	@Test
	public void differentMatchables2() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Merge<>(List.of(
					new OriginalMatch<>(new Substance(107, "A")),
					new OriginalMatch<>(new Substance(108, "A"))
			), ScoreMergingStrategy.MAX);
		});
	}

	@Test
	public void emptyList() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Merge<>(List.of(), ScoreMergingStrategy.MAX);
		});
	}

	@Test
	public void nullList() {
		assertThrows(NullPointerException.class, () -> {
			new Merge<>(null, ScoreMergingStrategy.SUM);
		});
	}

	@Test
	public void nullMergingStrategy() {
		assertThrows(NullPointerException.class, () -> {
			new Merge<>(List.of(
					new OriginalMatch<>(new Substance(107, "A")),
					new OriginalMatch<>(new Substance(107, "A")),
					new OriginalMatch<>(new Substance(107, "A"))
			), null);
		});
	}

	@Test
	void scoreMergingStrategy() {
		Merge<Substance> merge = new Merge<>(List.of(
				new OriginalMatch<>(new Substance(107, "A"), 2.4, Origin.UNKNOWN),
				new OriginalMatch<>(new Substance(107, "A"), 1.3, Origin.UNKNOWN),
				new OriginalMatch<>(new Substance(107, "A"), 0.5, Origin.UNKNOWN)
		), ScoreMergingStrategy.MAX);

		assertEquals(2.4, merge.getScore(), 0.01);

		merge = new Merge<>(List.of(
				new OriginalMatch<>(new Substance(107, "A"), 2.4, Origin.UNKNOWN),
				new OriginalMatch<>(new Substance(107, "A"), 1.3, Origin.UNKNOWN),
				new OriginalMatch<>(new Substance(107, "A"), 0.5, Origin.UNKNOWN)
		), ScoreMergingStrategy.SUM);

		assertEquals(4.2, merge.getScore(), 0.01);

		merge = new Merge<>(List.of(
				new OriginalMatch<>(new Substance(107, "A"), 2.4, Origin.UNKNOWN),
				new OriginalMatch<>(new Substance(107, "A"), 1.3, Origin.UNKNOWN),
				new OriginalMatch<>(new Substance(107, "A"), 0.5, Origin.UNKNOWN)
		), scores -> 1.0);

		assertEquals(1.0, merge.getScore(), 0.01);
	}


}