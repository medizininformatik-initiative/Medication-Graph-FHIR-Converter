package de.medizininformatikinitiative.medgraph.tools;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

/**
 * @author Markus Budeus
 */
public class UnitTest {

	private AutoCloseable mocks;

	@BeforeEach
	void setUpMocks() {
		mocks = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void closeMocks() throws Exception {
		mocks.close();
	}

}
