package de.medizininformatikinitiative.medgraph.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

/**
 * @author Markus Budeus
 */
public class UniquenessEnsuringCsvWriterTest extends UnitTest {

	@Mock
	private CSVWriter writer;

	private UniquenessEnsuringCsvWriter sut;

	@BeforeEach
	void setUp() {
		sut = new UniquenessEnsuringCsvWriter(writer);
	}

	@Test
	void noDuplicates() {
		sut.write("Hello", "World");
		sut.write("This", "is", "my", "favorite");

		verify(writer).write("Hello", "World");
		verify(writer).write("This", "is", "my", "favorite");
	}

	@Test
	void withDuplicates() {
		sut.write("Hello", "World");
		sut.write("This", "is", "my", "favorite");
		sut.write("Hello", "World");
		sut.write("World", "Hello");
		sut.write("Hello", "World", "hello");

		verify(writer).write("Hello", "World");
		verify(writer).write("This", "is", "my", "favorite");
		verify(writer).write("World", "Hello");
		verify(writer).write("Hello", "World", "hello");
	}
}