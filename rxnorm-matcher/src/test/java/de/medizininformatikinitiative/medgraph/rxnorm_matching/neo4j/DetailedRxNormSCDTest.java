package de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j;

import de.medizininformatikinitiative.medgraph.rxnorm_matching.model.DetailedRxNormSCD;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Amount;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
class DetailedRxNormSCDTest {

	@Test
	void withDrugAmount() {
		DetailedRxNormSCD sut = new DetailedRxNormSCD("2263077", "100 ML mannitol 200 MG/ML Injection", null, List.of());

		assertEquals(
				new Amount(new BigDecimal(100), new Unit("ml", null)),
				sut.getDrugAmount()
		);
	}

	@Test
	void withDrugAmount2() {
		DetailedRxNormSCD sut = new DetailedRxNormSCD("1791408", "500 ML mannitol 200 MG/ML Injection", null, List.of());

		assertEquals(
				new Amount(new BigDecimal(500), new Unit("ml", null)),
				sut.getDrugAmount()
		);
	}
	@Test
	void withoutDrugAmount() {
		DetailedRxNormSCD sut = new DetailedRxNormSCD("1421893", "lidocaine 40 MG/ML Topical Cream", null, List.of());
		assertNull(sut.getDrugAmount());
	}

	@Test
	void aReallyAnnoyingSCD() {
		DetailedRxNormSCD sut = new DetailedRxNormSCD("2619152", "0.25 MG, 0.5 MG Dose 3 ML semaglutide 0.68 MG/ML Pen Injector", null, List.of());
		assertEquals(
				new Amount(new BigDecimal(3), new Unit("ml", null)),
				sut.getDrugAmount()
		);
	}

}