package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.DoseFormMapper;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphEdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import org.neo4j.driver.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Minimal CLI to quickly verify EDQM → RxNorm dose form mapping.
 * Usage (defaults shown):
 *   env NEO4J_URI=bolt://localhost:7687 NEO4J_USER=neo4j NEO4J_PASSWORD=neo4j \
 *   run this main; optionally pass EDQM name as first arg (e.g., "ear ointment").
 */
public final class QuickDoseFormMappingCheck {

    public static void main(String[] args) {
        String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost:7687");
        String user = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String password = System.getenv("NEO4J_PASSWORD");
        if (password == null || password.isEmpty()) {
            System.err.println("WARNING: No Neo4j password provided!");
            System.err.println("Please set NEO4J_PASSWORD environment variable.");
            System.exit(1);
        }
        String edqmName = args != null && args.length > 0 ? args[0] : "ear ointment";

        System.out.println("Connecting to Neo4j: " + uri + " as " + user);
        AuthToken token = AuthTokens.basic(user, password);
        try (Driver driver = GraphDatabase.driver(uri, token, Config.defaultConfig());
             Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {

            // Initialize DB-backed mapper
            DoseFormMapper.initialize(new Neo4jCypherDatabase(session));

            // Lookup EDQM code by name
            String code = session.run(
                    "MATCH (e:EDQM {name: $name}) RETURN e.code AS code LIMIT 1",
                    Map.of("name", edqmName)
            ).single().get("code").asString();

            GraphEdqmPharmaceuticalDoseForm edqm = new GraphEdqmPharmaceuticalDoseForm(
                    code,
                    "http://standardterms.edqm.eu",
                    LocalDate.now(),
                    "1.0",
                    edqmName
            );

            String rx = DoseFormMapper.mapEdqm(edqm);
            System.out.println("EDQM: " + edqmName + " (" + code + ") → RxNorm dose form: " + rx);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}


