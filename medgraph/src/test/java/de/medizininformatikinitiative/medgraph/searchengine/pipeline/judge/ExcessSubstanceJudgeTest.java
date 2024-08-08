package de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge;

import de.medizininformatikinitiative.medgraph.TestFactory;
import de.medizininformatikinitiative.medgraph.UnitTest;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static de.medizininformatikinitiative.medgraph.TestFactory.Products.Detailed.*;
import static de.medizininformatikinitiative.medgraph.TestFactory.Substances.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ExcessSubstanceJudgeTest extends UnitTest {

	private ExcessSubstanceJudge sut;

	@BeforeEach
	void setUp() {
		sut = new ExcessSubstanceJudge();
	}

	@Test
	void exactMatch() {
		double score = sut.judgeInternal(ASPIRIN, queryWithSubstances(ACETYLSALICYLIC_ACID));
		assertEquals(0, score);
	}

	@Test
	void exactMatch2() {
		double score = sut.judgeInternal(DOLOMO, queryWithSubstances(ACETYLSALICYLIC_ACID, PARACETAMOL));
		assertEquals(0, score);
	}

	@Test
	void additionalIngredientsPresent() {
		double score = sut.judgeInternal(DOLOMO, queryWithSubstances(PARACETAMOL));
		assertEquals(-1, score, 0.01);
	}

	@Test
	void noSubstancesQueried() {
		double score = sut.judgeInternal(ANAPEN, queryWithSubstances());
		assertEquals(-1, score, 0.01);
	}

	@Test
	void ingredientsMissing() {
		double score = sut.judgeInternal(ASPIRIN, queryWithSubstances(ACETYLSALICYLIC_ACID, PARACETAMOL));
		assertEquals(0, score); // It is not the job of this judge to check if all searched substances are present!
	}

	@Test
	void additionalIngredientsPresentAndIngredientsMissing() {
		double score = sut.judgeInternal(DOLOMO, queryWithSubstances(ACETYLSALICYLIC_ACID, PREDNISOLONE));
		assertEquals(-1, score);
	}


	@Test
	void productContainsNoDrugs() {
		double score = sut.judgeInternal(ASEPTODERM, queryWithSubstances(ACETYLSALICYLIC_ACID));
		assertEquals(0, score);
	}

	private SearchQuery queryWithSubstances(Substance... substances) {
		SearchQuery.Builder query = new SearchQuery.Builder();
		query.withSubstances(Arrays.asList(substances));
		return query.build();
	}
}