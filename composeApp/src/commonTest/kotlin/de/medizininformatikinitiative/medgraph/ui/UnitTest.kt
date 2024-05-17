package de.medizininformatikinitiative.medgraph.ui

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.MockitoAnnotations

/**
 * Superclass for unit tests, takes care of mockito annotations.
 *
 * @author Markus Budeus
 */
open class UnitTest {

    private lateinit var mocksCloseable: AutoCloseable

    @BeforeEach
    fun setupMocks() {
        mocksCloseable = MockitoAnnotations.openMocks(this)
    }

    @AfterEach
    fun tearDownMocks() {
        mocksCloseable.close()
    }

}