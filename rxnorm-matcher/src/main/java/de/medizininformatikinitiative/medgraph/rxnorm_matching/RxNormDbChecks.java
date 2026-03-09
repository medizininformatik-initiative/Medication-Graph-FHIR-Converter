package de.medizininformatikinitiative.medgraph.rxnorm_matching;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static de.medizininformatikinitiative.medgraph.rxnorm_matching.RxNormMatcher.getRxNormDbCon;

/**
 * @author Markus Budeus
 */
public class RxNormDbChecks {


	public static void main(String[] args) throws SQLException {
		Connection con = getRxNormDbCon();
//		con.createStatement().execute("CREATE INDEX MYINDEX_RXNCONSO_SAB_TTY_RXCUI ON RXNCONSO(SAB, TTY, RXCUI)");
//		con.createStatement().execute("CREATE INDEX MYINDEX_RXNREL_RXCUI2_RELA ON RXNREL(RXCUI2, RELA)");
//		con.createStatement().execute("CREATE INDEX MYINDEX_RXNREL_RXCUI1_RELA ON RXNREL(RXCUI1, RELA)");
//		con.createStatement().execute("ANALYZE");

		ResultSet resultSet = con.createStatement().executeQuery("""
				SELECT name, tbl_name, sql
				FROM sqlite_master
				WHERE type = 'index';
				""");
		while (resultSet.next()) {
			for (int i = 1; i < 4; i++) {
				System.out.print(resultSet.getString(i) +"\t");
			}
			System.out.println();
		}

		resultSet = con.createStatement().executeQuery("""
				EXPLAIN QUERY PLAN SELECT scd.RXCUI AS scd, scd.STR as scdName, scd_df.RXCUI1 AS df, scd_scdc.RXCUI1 AS scdc, ing.RXCUI AS ing
				FROM RXNCONSO scd
				JOIN RXNREL scd_df ON (scd_df.RXCUI2 = scd.RXCUI AND scd_df.RELA = 'has_dose_form')
				JOIN RXNREL scd_scdc ON scd_scdc.RXCUI2 = scd.RXCUI AND scd_scdc.RELA = 'consists_of'
				JOIN RXNCONSO scdc ON scd_scdc.RXCUI1 = scdc.RXCUI AND scdc.SAB = 'RXNORM' AND scdc.TTY = 'SCDC' AND scdc.SUPPRESS <> 'O'
				JOIN RXNREL scdc_in ON scdc.RXCUI = scdc_in.RXCUI2 AND
				    (scdc_in.RELA = 'has_ingredient' OR scdc_in.RELA = 'has_precise_ingredient')
				JOIN RXNCONSO ing ON scdc_in.RXCUI1 = ing.RXCUI AND ing.SAB = 'RXNORM' AND
				    (ing.TTY = 'IN' OR ing.TTY = 'PIN') AND ing.SUPPRESS <> 'O'
				WHERE scd.SAB = 'RXNORM' AND scd.TTY = 'SCD' AND scd.RXCUI IN ('1234', '123', '12345', '1233')
				""");
		while (resultSet.next()) {
			for (int i = 1; i <= 4; i++) {
				System.out.print(resultSet.getString(i) +"\t");
			}
			System.out.println();
		}
	}
}
