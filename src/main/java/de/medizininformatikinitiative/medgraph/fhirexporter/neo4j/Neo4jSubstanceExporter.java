package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.resource.CodeableConcept;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Coding;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.substance.Substance;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.CodingSystem;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jMedicationExporter.*;
import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.*;

/**
 * This class generates {@link Substance}-objects using the Neo4j knowledge graph.
 *
 * @author Markus Budeus
 */
public class Neo4jSubstanceExporter extends Neo4jExporter<Substance> {

	protected final boolean collectStatistics;
	private Statistics statistics;

	/**
	 * Instantiates a new exporter.
	 *
	 * @param session                            the database session to use for querying the knowledge graph
	 * @param collectStatistics                  if true, statistics are collected during the export, which can be
	 *                                           printed after {@link #exportObjects()} has been called via
	 *                                           {@link #printStatistics()}.
	 */
	public Neo4jSubstanceExporter(Session session, boolean collectStatistics) {
		super(session);
		this.collectStatistics = collectStatistics;
	}

	/**
	 * Reads all substances with their assigned codes and coding systems from the database and returns them as a stream
	 * of {@link Substance Substances}.
	 */
	@Override
	public Stream<Substance> exportObjects() {
		if (collectStatistics) {
			this.statistics = new Statistics();
		} else {
			this.statistics = null;
		}

		Result result = session.run(new Query(
				"MATCH (s:" + SUBSTANCE_LABEL + ") " +
						"OPTIONAL MATCH (cs:" + CODING_SYSTEM_LABEL + ")<-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]-(c:" + CODE_LABEL + ")-->(s) " +
						"WITH s, collect("+Neo4jMedicationExporter.groupCodingSystem("c", "cs")+") AS codes " +
						"RETURN s.name, s.mmiId, codes"
		));

		Stream<Substance> stream = result.stream().map(Neo4jSubstanceExporter::toSubstance);
		if (collectStatistics) {
			stream = stream.map(this::addToStatistics);
		}
		return stream;
	}

	private Substance addToStatistics(Substance substance) {
		statistics.add(substance);
		return substance;
	}

	private static Substance toSubstance(Record record) {
		Substance substance = new Substance();
		String substanceName = record.get(0).asString();

		substance.identifier = new Identifier[] { Identifier.fromSubstanceMmiId(record.get(1).asLong()) };
		substance.description = substanceName;

		CodeableConcept codeableConcept = new CodeableConcept();
		List<Coding> codings = record.get(2).asList(code -> CodingProvider.createCoding(
						code.get(CODE).asString(),
						(String) code.get(SYSTEM_URI).asObject(),
						(String) code.get(SYSTEM_VERSION).asObject(),
						(LocalDate) code.get(SYSTEM_DATE).asObject()
				)
		);
		codeableConcept.coding = codings.toArray(new Coding[0]);

		for (Coding coding : codings) {
			if (CodingSystem.INN.uri.equals(coding.system)) {
				codeableConcept.text = coding.code;
				break;
			}
		}

		if (codeableConcept.text == null)
			codeableConcept.text = substanceName;

		substance.code = codeableConcept;

		return substance;
	}

	public void printStatistics() {
		System.out.println(statistics);
	}

	private static class Statistics {

		private final AtomicInteger substancesTotal = new AtomicInteger();
		private final AtomicInteger substancesWithAtLeastOneCode = new AtomicInteger();
		private final AtomicInteger uniiOccurrences = new AtomicInteger();
		private final AtomicInteger objectsWithUnii = new AtomicInteger();
		private final AtomicInteger casOccurrences = new AtomicInteger();
		private final AtomicInteger objectsWithCas = new AtomicInteger();
		private final AtomicInteger rxcuiOccurrences = new AtomicInteger();
		private final AtomicInteger objectsWithRxcui = new AtomicInteger();
		private final AtomicInteger askOccurrences = new AtomicInteger();
		private final AtomicInteger objectsWithAsk = new AtomicInteger();
		private final AtomicInteger innOccurrences = new AtomicInteger();
		private final AtomicInteger objectsWithInn = new AtomicInteger();

		public void add(Substance substance) {
			if (substance == null) return;
			substancesTotal.incrementAndGet();
			if (substance.code == null) return;
			boolean anyCode = addCodes(substance, CodingSystem.UNII.uri, uniiOccurrences, objectsWithUnii);
			anyCode = addCodes(substance, CodingSystem.CAS.uri, casOccurrences, objectsWithCas) | anyCode;
			anyCode = addCodes(substance, CodingSystem.RXCUI.uri, rxcuiOccurrences, objectsWithRxcui) | anyCode;
			anyCode = addCodes(substance, CodingSystem.ASK.uri, askOccurrences, objectsWithAsk) | anyCode;
			anyCode = addCodes(substance, CodingSystem.INN.uri, innOccurrences, objectsWithInn) | anyCode;
			if (anyCode) {
				substancesWithAtLeastOneCode.incrementAndGet();
			}
		}

		private boolean addCodes(Substance substance, String codeSystem, AtomicInteger occurrences, AtomicInteger objects) {
			boolean anyMatch = false;
			for (Coding c: substance.code.coding) {
				if (codeSystem.equals(c.system)) {
					occurrences.getAndIncrement();
					if (!anyMatch) {
						objects.getAndIncrement();
						anyMatch = true;
					}
				}
			}
			return anyMatch;
		}

		@Override
		public String toString() {
			return "Statistics{" +
					"substancesTotal=" + substancesTotal +
					", substancesWithAtLeastOneCode=" + substancesWithAtLeastOneCode +
					", uniiOccurrences=" + uniiOccurrences +
					", objectsWithUnii=" + objectsWithUnii +
					", casOccurrences=" + casOccurrences +
					", objectsWithCas=" + objectsWithCas +
					", rxcuiOccurrences=" + rxcuiOccurrences +
					", objectsWithRxcui=" + objectsWithRxcui +
					", askOccurrences=" + askOccurrences +
					", objectsWithAsk=" + objectsWithAsk +
					", innOccurrences=" + innOccurrences +
					", objectsWithInn=" + objectsWithInn +
					'}';
		}
	}

}
