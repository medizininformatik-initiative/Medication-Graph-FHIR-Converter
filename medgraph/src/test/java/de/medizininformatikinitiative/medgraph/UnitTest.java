package de.medizininformatikinitiative.medgraph;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import java.io.Closeable;

/**
 * @author Markus Budeus
 */
public class UnitTest {

	private AutoCloseable mocks;

	@BeforeEach
	public void setUpMocks() {
		mocks = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	public void closeMocks() throws Exception {
		mocks.close();
	}

}
