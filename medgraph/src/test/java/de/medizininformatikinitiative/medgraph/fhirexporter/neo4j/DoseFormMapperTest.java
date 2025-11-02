package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import java.time.LocalDate;
import java.util.Map;
import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for the enhanced DoseFormMapper with database support.
 */
@ExtendWith(MockitoExtension.class)
class DoseFormMapperTest {

    @Mock
    private Neo4jCypherDatabase database;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    private GraphEdqmPharmaceuticalDoseForm testEdqmForm;

    @BeforeEach
    void setUp() {
        DoseFormMapper.initialize(database);
        testEdqmForm = new GraphEdqmPharmaceuticalDoseForm(
            "PDF-10219000",
            "http://standardterms.edqm.eu",
            LocalDate.now(),
            "1.0",
            "Ear drops, solution"
        );
    }

    @Test
    void testMapEdqmWithDatabase() {
        // Mock database response
        when(database.getSession()).thenReturn(session);
        when(session.run(anyString(), ArgumentMatchers.<Map<String, Object>>any())).thenReturn(mock(org.neo4j.driver.Result.class));

        // Test with database
        String result = DoseFormMapper.mapEdqm(testEdqmForm);
        
        // Should attempt database query
        verify(database).getSession();
        verify(session).run(anyString(), ArgumentMatchers.<Map<String, Object>>any());
    }

    @Test
    void testGetEdqmMapping() {
        // Mock database response for comprehensive mapping
        when(database.getSession()).thenReturn(session);
        when(session.run(anyString(), ArgumentMatchers.<Map<String, Object>>any())).thenReturn(mock(org.neo4j.driver.Result.class));

        EdqmRxNormDoseFormMapping result = DoseFormMapper.getEdqmMapping(testEdqmForm);
        
        // Should attempt database query
        verify(database).getSession();
        verify(session).run(anyString(), ArgumentMatchers.<Map<String, Object>>any());
    }

    @Test
    void testEdqmRxNormDoseFormMapping() {
        // Test the mapping data model
        EdqmRxNormDoseFormMapping mapping = new EdqmRxNormDoseFormMapping(
            "Ear drops, solution",
            "Otic solution",
            "1",
            "no transformation",
            "1",
            "application",
            "1",
            "local exposure",
            "1",
            "ear"
        );

        assertEquals("Ear drops, solution", mapping.getEdqmDoseForm());
        assertEquals("Otic solution", mapping.getRxnormDoseForm());
        assertEquals("1", mapping.getTracCode());
        assertEquals("no transformation", mapping.getTracTerm());
        assertTrue(mapping.hasRxNormMapping());
        assertTrue(mapping.hasTracInfo());
        assertTrue(mapping.hasRcaInfo());
        assertTrue(mapping.hasAmecInfo());
        assertTrue(mapping.hasIsicInfo());
    }

    // Removed null-from test: EdqmRxNormDoseFormMapping.from expects a non-null Value
}
