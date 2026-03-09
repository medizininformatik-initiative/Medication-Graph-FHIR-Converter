package de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j;

import de.medizininformatikinitiative.medgraph.rxnorm_matching.model.RxNormSCDC;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class RxNormSCDCTest {


	@Test
	public void polyethyleneGlycolSucks() {
		RxNormSCDC scdc = new RxNormSCDC("336921", "polyethylene glycol 400 10 MG/ML");

		assertEquals("polyethylene glycol 400", scdc.getIngredientName());
		assertEquals(BigDecimal.TEN, scdc.getAmount());
		assertEquals("MG/ML", scdc.getUnit());
	}

}