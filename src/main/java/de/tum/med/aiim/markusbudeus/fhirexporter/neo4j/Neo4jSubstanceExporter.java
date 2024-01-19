package de.tum.med.aiim.markusbudeus.fhirexporter.neo4j;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Coding;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance.Substance;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.CodingSystem;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class Neo4jSubstanceExporter extends Neo4jExporter<Substance> {

	private static final String CODE = "code";
	private static final String SYSTEM_URI = "uri";
	private static final String SYSTEM_DATE = "date";
	private static final String SYSTEM_VERSION = "version";

	protected final boolean collectStatistics;
	private Statistics statistics;

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
						"MATCH (cs:" + CODING_SYSTEM_LABEL + ")<-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]-(c:" + CODE_LABEL + ")-->(s) " +
						"WITH s, collect({" +
						CODE + ":c.code," +
						SYSTEM_URI + ":cs.uri," +
						SYSTEM_DATE + ":cs.date," +
						SYSTEM_VERSION + ":cs.version" +
						"}) AS codes " +
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

		substance.identifier = new Identifier[] { Identifier.fromSubstanceMmiId(record.get(1).asLong()) };
		substance.description = record.get(0).asString(); // Substance name

		CodeableConcept codeableConcept = new CodeableConcept();
		List<Coding> codings = record.get(2).asList(code -> CodingProvider.createCoding(
						code.get(CODE).asString(),
						(String) code.get(SYSTEM_URI).asObject(),
						(String) code.get(SYSTEM_VERSION).asObject(),
						(LocalDate) code.get(SYSTEM_DATE).asObject()
				)
		);
		codeableConcept.coding = codings.toArray(new Coding[0]);

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

		private boolean addCodes(Substance substance, String codeSystem, AtomicInteger objects, AtomicInteger occurrences) {
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
