package de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Markus Budeus
 */
public class FlexibleLevenshteinDistanceServiceTest extends UnitTest {

	@Test
	public void fixedLength() {
		FlexibleLevenshteinDistanceService sut = new FlexibleLevenshteinDistanceService(l -> 2);

		assertEquals(OptionalInt.of(0), sut.apply("Warm", "Warm"));
		assertEquals(OptionalInt.of(1), sut.apply("Warm", "Worm"));
		assertEquals(OptionalInt.of(2), sut.apply("Warm", "Warmth"));
		assertEquals(OptionalInt.empty(), sut.apply("Warm", "Wild"));
	}

	@Test
	public void correctValuesPassed() {
		BiFunction<String, String, Integer> function = mock();
		when(function.apply(anyString(), anyString())).thenReturn(1);
		FlexibleLevenshteinDistanceService sut = new FlexibleLevenshteinDistanceService(function);

		sut.apply("Floor", "House");
		verify(function).apply("Floor", "House");

		sut.apply("", "");
		verify(function).apply("", "");

		sut.apply("Nice!", "Noice!");
		verify(function).apply("Nice!", "Noice!");
	}

	@Test
	public void flexibleLength() {
		FlexibleLevenshteinDistanceService sut = new FlexibleLevenshteinDistanceService(l -> Math.min((l-1) / 3, 3));

		assertEquals(OptionalInt.empty(),  sut.apply("zum", "gum"));
		assertEquals(OptionalInt.of(0), sut.apply( "", ""));
		assertEquals(OptionalInt.of(0), sut.apply( "AA", "AA"));
		assertEquals(OptionalInt.of(1), sut.apply("form", "firm"));
		assertEquals(OptionalInt.empty(), sut.apply("format", "first"));
		assertEquals(OptionalInt.empty(), sut.apply("Flames", "Falmes"));
		assertEquals(OptionalInt.of(2), sut.apply("Foundry", "Fuondry"));
		assertEquals(OptionalInt.of(3), sut.apply("AAAAAAAAAA", "AAAAAAABBB"));
		assertEquals(OptionalInt.empty(), sut.apply("AAAAAAAAAAAAA", "AAAAAAABBBB"));

	}

}