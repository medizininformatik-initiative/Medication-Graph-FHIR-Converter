package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.*;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.CodingSystem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Provides some sample object instances used by the fhir exporter. Note that these instances may not be correct in the
 * real data sense or may not even make sense.
 *
 * @author Markus Budeus
 */
public class FhirExportTestFactory {

	public static final class GraphCodes implements Catalogue {
		public static final class Atc implements Catalogue {
			public static final GraphAtc B01AC06 = new GraphAtc("B01AC06", CodingSystem.ATC.uri,
					LocalDate.of(2023, 8, 19), null, "Acetylsalicylsäure");
			public static final GraphAtc N02BA01 = new GraphAtc("N02BA01", CodingSystem.ATC.uri,
					LocalDate.of(2023, 8, 19), "1", "Acetylsalicylsäure");
			public static final GraphAtc N05CD08 = new GraphAtc("N05CD08", CodingSystem.ATC.uri,
					LocalDate.of(2024, 2, 27), null, "Midazolam");
		}

		public static final class Pzn implements Catalogue {
			public static final GraphCode SAMPLE_1 = new GraphCode("10203595", CodingSystem.PZN.uri,
					LocalDate.of(2024, 1, 1), "1.7");
			public static final GraphCode SAMPLE_2 = new GraphCode("01689009", CodingSystem.PZN.uri,
					LocalDate.of(2024, 1, 17), null);
			public static final GraphCode SAMPLE_3 = new GraphCode("00561867", CodingSystem.PZN.uri,
					LocalDate.of(2024, 1, 17), null);
		}

		public static final class Ask implements Catalogue {
			public static final GraphCode SAMPLE_1 = new GraphCode("00001", CodingSystem.ASK.uri,
					LocalDate.of(2022, 6, 14), null);
			public static final GraphCode SAMPLE_2 = new GraphCode("00002", CodingSystem.ASK.uri,
					LocalDate.of(2022, 7, 14), null);
		}

		public static final class Cas implements Catalogue {
			public static final GraphCode ASS = new GraphCode("50-78-2", CodingSystem.CAS.uri,
					LocalDate.of(2024, 6, 6), "3.141592653589793");
		}
	}

	public static final class GraphUnits implements Catalogue {
		/**
		 * The unit milligrams, expressed as {@link GraphUnit}.
		 */
		public static final GraphUnit MG = new GraphUnit("mg", "MG", "mg", "mg", "MG", "mg");
		/**
		 * The unit millilitres, expressed as {@link GraphUnit}.
		 */
		public static final GraphUnit ML = new GraphUnit("ml", "ML", "ml", "ml", "ML", "ml");
		/**
		 * "FCC-Einheiten", a unit with no known UCUM equivalent.
		 */
		public static final GraphUnit FCC_UNITS = new GraphUnit("FCC-Einheiten", "FCC", "FCC-Einheiten", null, null,
				"FCC-Einheiten");
	}

	public static final class GraphIngredients implements Catalogue {
		public static final GraphIngredient ASS = new GraphIngredient(
				2L,
				"Acetylsalicylsäure",
				true,
				new BigDecimal("100"),
				null,
				GraphUnits.MG
		);
		public static final GraphIngredient PREDNISOLON = new GraphIngredient(
				17L,
				"Prednisolon",
				true,
				new BigDecimal("250"),
				null,
				GraphUnits.MG
		);
		public static final GraphIngredient PREDNISOLONE_HYDROGENSUCCINATE = new GraphIngredient(
				42L,
				"Prednisolon 21-hydrogensuccinat, Natriumsalz",
				true,
				new BigDecimal("10.48"),
				null,
				GraphUnits.MG,
				List.of(
						new SimpleGraphIngredient(17L, "Prednisolon", new BigDecimal("7.83"), null, GraphUnits.MG),
						new SimpleGraphIngredient(142L, "Natrium", new BigDecimal("1"), null, GraphUnits.MG)
				)
		);
		public static final GraphIngredient WATER = new GraphIngredient(
				149L,
				"Water",
				false,
				new BigDecimal("10"),
				new BigDecimal("20"),
				GraphUnits.ML
		);
		public static final GraphIngredient MIDAZOLAM_HYDROCHLORIDE = new GraphIngredient(
				148L,
				"Midazolam hydrochlorid",
				false,
				new BigDecimal("5.56"),
				null,
				GraphUnits.MG,
				List.of(new SimpleGraphIngredient(
						150L,
						"Midazolam hydrochlorid",
						new BigDecimal("5"),
						null,
						GraphUnits.MG
				))
		);
	}

	public static final class GraphPackages implements Catalogue {
		public static final GraphPackage ASPIRIN_TABLETS_1 = new GraphPackage(
				"Aspirin 500mg Tabletten", BigDecimal.TEN, LocalDate.of(2014, 1, 1),
				List.of(GraphCodes.Pzn.SAMPLE_1)
		);
		public static final GraphPackage ASPIRIN_TABLETS_2 = new GraphPackage(
				"Aspirin 500mg Tabletten", new BigDecimal(20), LocalDate.of(2014, 1, 1),
				List.of(GraphCodes.Pzn.SAMPLE_2)
		);
	}

	public static final class GraphEdqmPharmaceuticalDoseForms implements Catalogue {
		public static final GraphEdqmPharmaceuticalDoseForm TABLET = new GraphEdqmPharmaceuticalDoseForm(
				"PDF-10219000", CodingSystem.EDQM.uri, LocalDate.of(2024, 5, 29), null, "Tablet"
		);
		public static final GraphEdqmPharmaceuticalDoseForm INJECTION = new GraphEdqmPharmaceuticalDoseForm(
				"PDF-11201000", CodingSystem.EDQM.uri, LocalDate.of(2024, 5, 29), null, "Solution for injection"
		);
		public static final GraphEdqmPharmaceuticalDoseForm POWDER_FOR_SOLUTION_FOR_INJECTION = new GraphEdqmPharmaceuticalDoseForm(
				"PDF-11205000", CodingSystem.EDQM.uri, LocalDate.of(2024, 5, 29), null, "Powder for solution for injection"
		);
	}

	public static final class GraphDrugs implements Catalogue {
		public static final GraphDrug ASPIRIN = new GraphDrug(
				List.of(GraphIngredients.ASS),
				List.of(GraphCodes.Atc.B01AC06),
				"Zum Einnehmen",
				GraphEdqmPharmaceuticalDoseForms.TABLET,
				BigDecimal.ONE,
				null
		);
		public static final GraphDrug PREDNISOLON = new GraphDrug(
				List.of(GraphIngredients.WATER, GraphIngredients.PREDNISOLON),
				List.of(GraphCodes.Atc.N02BA01),
				"Injektionslösung",
				GraphEdqmPharmaceuticalDoseForms.INJECTION,
				BigDecimal.TEN,
				GraphUnits.ML
		);
		public static final GraphDrug SOME_HOMEOPATHIC_STUFF = new GraphDrug(
				List.of(GraphIngredients.WATER),
				List.of(),
				"Dil.",
				null,
				null,
				null
		);
		public static final GraphDrug DORMICUM = new GraphDrug(
				List.of(GraphIngredients.MIDAZOLAM_HYDROCHLORIDE),
				List.of(),
				"Injektionslösung",
				GraphEdqmPharmaceuticalDoseForms.INJECTION,
				null,
				null
		);
	}

	public static final class GraphProducts implements Catalogue {
		public static final GraphProduct ASPIRIN = new GraphProduct(
				"Aspirin 500mg Tabletten",
				1L,
				128L,
				"Great Pharma Inc.",
				List.of(),
				List.of(GraphDrugs.ASPIRIN),
				List.of(GraphPackages.ASPIRIN_TABLETS_1, GraphPackages.ASPIRIN_TABLETS_2)
		);

		public static final GraphProduct HOMEOPATHIC_STUFF = new GraphProduct(
				"This amazing product will cure all of your ailments",
				2L,
				124L,
				"Ripoff Inc.",
				List.of(),
				List.of(GraphDrugs.SOME_HOMEOPATHIC_STUFF, GraphDrugs.SOME_HOMEOPATHIC_STUFF),
				List.of()
		);

		public static final GraphProduct COMPLETE_BS = new GraphProduct(
				"This product does not exist. Hopefully",
				6874845L,
				null,
				null,
				List.of(GraphCodes.Atc.N02BA01, GraphCodes.Ask.SAMPLE_1),
				List.of(GraphDrugs.SOME_HOMEOPATHIC_STUFF, GraphDrugs.PREDNISOLON, GraphDrugs.ASPIRIN),
				List.of(GraphPackages.ASPIRIN_TABLETS_1)
		);
	}

}
