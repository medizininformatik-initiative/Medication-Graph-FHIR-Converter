package de.medizininformatikinitiative.medgraph.rxnorm_matching.db;

import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.model.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides utility functions to read information from an RxNorm database
 *
 * @author Markus Budeus
 */
public class RxNormDatabaseImpl implements AutoCloseable, RxNormDatabase {

	private final Logger logger = LogManager.getLogger(RxNormDatabaseImpl.class);
	private final PreparedStatementCache queryCache;

	public RxNormDatabaseImpl(Connection connection) {
		queryCache = new PreparedStatementCache(connection);
	}

	/**
	 * Assumes the given ingredientRxCUIs are IN/PIN RxCUIs, resolves all connected SCDs and returns their RxCUIs.
	 */
	@Override
	public synchronized Set<String> getSCDRxCUIsForIngredientRxCUIs(Set<String> ingredientRxCUIs) {
		final String sql = """
				SELECT scd.RXCUI
				FROM RXNCONSO ing
				JOIN RXNREL scdc_in ON scdc_in.RXCUI1 = ing.RXCUI AND
				    (scdc_in.RELA = 'has_ingredient' OR scdc_in.RELA = 'has_precise_ingredient')
				JOIN RXNCONSO scdc ON scdc.RXCUI = scdc_in.RXCUI2 AND scdc.SAB = 'RXNORM' AND scdc.TTY = 'SCDC' AND scdc.SUPPRESS <> 'O'
				JOIN RXNREL scd_scdc ON scd_scdc.RXCUI1 = scdc.RXCUI AND scd_scdc.RELA = 'consists_of'
				JOIN RXNCONSO scd ON scd_scdc.RXCUI2 = scd.RXCUI AND scd.SAB = 'RXNORM' AND scd.TTY = 'SCD' AND scd.SUPPRESS <> 'O'
				WHERE ing.SAB = 'RXNORM' AND (ing.TTY = 'IN' OR ing.TTY = 'PIN') AND ing.RXCUI IN ?
				""";

		try(ResultSet resultSet = queryCache.executeQueryWithOneTupleVar(sql, ingredientRxCUIs);) {
			Set<String> results = new HashSet<>();
			while (resultSet.next()) {
				results.add(resultSet.getString(1));
			}
			return results;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Acquires additional information on the SCDs identified by the given RXCUIs. The information is returned as
	 * {@link DetailedRxNormSCD} objects.
	 */
	@Override
	public synchronized Set<DetailedRxNormSCD> resolveDetails(Set<String> scdRxCUIs) {
		final String sql = """
				SELECT scd.RXCUI AS scd, scd.STR as scdName, scd_df.RXCUI1 AS df, scd_scdc.RXCUI1 AS scdc, ing.RXCUI AS ing
				FROM RXNCONSO scd
				JOIN RXNREL scd_df ON (scd_df.RXCUI2 = scd.RXCUI AND scd_df.RELA = 'has_dose_form')
				JOIN RXNREL scd_scdc ON scd_scdc.RXCUI2 = scd.RXCUI AND scd_scdc.RELA = 'consists_of'
				JOIN RXNCONSO scdc ON scd_scdc.RXCUI1 = scdc.RXCUI AND scdc.SAB = 'RXNORM' AND scdc.TTY = 'SCDC' AND scdc.SUPPRESS <> 'O'
				JOIN RXNREL scdc_in ON scdc.RXCUI = scdc_in.RXCUI2 AND
				    (scdc_in.RELA = 'has_ingredient' OR scdc_in.RELA = 'has_precise_ingredient')
				JOIN RXNCONSO ing ON scdc_in.RXCUI1 = ing.RXCUI AND ing.SAB = 'RXNORM' AND
				    (ing.TTY = 'IN' OR ing.TTY = 'PIN') AND ing.SUPPRESS <> 'O'
				WHERE scd.SAB = 'RXNORM' AND scd.TTY = 'SCD' AND scd.RXCUI IN ?
				""";

		try (ResultSet resultSet = queryCache.executeQueryWithOneTupleVar(sql, scdRxCUIs)) {

			Map<String, RxNormSCDWithAssignedSCDCs> ingredientRxcuisByScdcByScd = new HashMap<>();
			while (resultSet.next()) {
				String scd = resultSet.getString(1);
				String scdName = resultSet.getString(2);
				String df = resultSet.getString(3);
				String scdc = resultSet.getString(4);
				String in = resultSet.getString(5);

				RxNormSCDWithAssignedSCDCs carrier = ingredientRxcuisByScdcByScd.compute(scd,
						(k, v) -> v == null ?
								new RxNormSCDWithAssignedSCDCs(scd, scdName, df) : v);
				List<String> ingredientRxcuis = carrier.ingredientsByScdcRxcui.compute(scdc,
						(k, v) -> v == null ? new ArrayList<>() : v);
				ingredientRxcuis.add(in);
			}

			Set<String> allDfRxcuis = ingredientRxcuisByScdcByScd.values().stream()
			                                                     .map(m -> m.doseForm)
			                                                     .collect(Collectors.toSet());
			Set<String> allScdcRxcuis = ingredientRxcuisByScdcByScd.values().stream()
			                                                       .map(m -> m.ingredientsByScdcRxcui)
			                                                       .flatMap(m -> m.keySet().stream())
			                                                       .collect(Collectors.toSet());
			Set<String> allInRxcuis = ingredientRxcuisByScdcByScd.values().stream()
			                                                     .map(m -> m.ingredientsByScdcRxcui)
			                                                     .flatMap(m -> m.values().stream())
			                                                     .flatMap(Collection::stream)
			                                                     .collect(Collectors.toSet());

			List<RxNormDoseForm> doseForms = resolveDoseForms(allDfRxcuis);
			List<RxNormSCDC> scdcs = resolveSemanticClinicalDrugConcepts(allScdcRxcuis);
			List<RxNormIngredient> ingredients = resolveIngredients(allInRxcuis);

			if (scdcs.size() < allScdcRxcuis.size()) {
				logger.log(Level.ERROR, "Found " + allScdcRxcuis.size()
						+ " SCDC RXCUIs, but could only resolve " + scdcs.size() + ". Please investigate.");
			}
			if (ingredients.size() < allInRxcuis.size()) {
				logger.log(Level.ERROR, "Found " + allInRxcuis.size()
						+ " INs/PIN RXCUIs, but could only resolve " + ingredients.size() + ". Please investigate.");
			}

			Map<String, RxNormDoseForm> dfByRxcui = associateBy(doseForms, RxNormDoseForm::getRxcui);
			Map<String, RxNormSCDC> scdcByRxcui = associateBy(scdcs, RxNormSCDC::getRxcui);
			Map<String, RxNormIngredient> ingredientByRxcui = associateBy(ingredients, RxNormIngredient::getRxcui);

			Set<DetailedRxNormSCD> results = new HashSet<>();
			for (Map.Entry<String, RxNormSCDWithAssignedSCDCs> scdCandidate : ingredientRxcuisByScdcByScd.entrySet()) {
				List<RxNormSCDCWithIngredients> scdcsWithIngredients = new ArrayList<>();
				for (Map.Entry<String, List<String>> scdcCandidate : scdCandidate.getValue().ingredientsByScdcRxcui.entrySet()) {
					List<RxNormIngredient> localIngredients = new ArrayList<>(scdcCandidate.getValue().size());
					for (String ingredientCandidate : scdcCandidate.getValue()) {
						localIngredients.add(ingredientByRxcui.get(ingredientCandidate));
					}
					RxNormSCDCWithIngredients scdcWithIngredients = new RxNormSCDCWithIngredients(
							scdcByRxcui.get(scdcCandidate.getKey()),
							localIngredients
					);
					scdcsWithIngredients.add(scdcWithIngredients);
				}
				RxNormSCDWithAssignedSCDCs carrier = scdCandidate.getValue();
				DetailedRxNormSCD detailedRxNormSCD = new DetailedRxNormSCD(
						carrier.rxcui,
						carrier.name,
						dfByRxcui.get(carrier.doseForm),
						scdcsWithIngredients
				);
				results.add(detailedRxNormSCD);
			}

			return results;

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public List<RxNormDoseForm> resolveDoseForms(Set<String> rxcuis) {
		return resolveSimpleConcept(rxcuis,
				(rxcui, name, tty) -> new RxNormDoseForm(rxcui, name),
				List.of(RxNormTermType.DF));
	}

	public List<RxNormSCDC> resolveSemanticClinicalDrugConcepts(Set<String> rxcuis) {
		return resolveSimpleConcept(rxcuis,
				(rxcui, name, tty) -> new RxNormSCDC(rxcui, name),
				List.of(RxNormTermType.SCDC));
	}

	public List<RxNormIngredient> resolveIngredients(Set<String> rxcuis) {
		return resolveSimpleConcept(rxcuis, RxNormIngredient::new, List.of(RxNormTermType.IN, RxNormTermType.PIN));
	}

	private <T extends RxNormConcept> List<T> resolveSimpleConcept(Set<String> rxcuis,
	                                                               TriFunction<String, String, RxNormTermType, T> constructor,
	                                                               List<RxNormTermType> termTypes) {

		String termtypeSearch = termTypes.stream()
		                                 .map(RxNormTermType::name)
		                                 .map(name -> "TTY = '" + name + "'")
		                                 .collect(Collectors.joining(" OR "));

		final String sql = """
					SELECT RXCUI, STR, TTY
						FROM RXNCONSO
						WHERE RXCUI IN ?
						  AND SAB = 'RXNORM'
						  AND (%s)
					""";

		try (ResultSet resultSet = queryCache.executeQueryWithOneTupleVar(sql.formatted(termtypeSearch), rxcuis)) {
			List<T> results = new ArrayList<>(rxcuis.size());
			while (resultSet.next()) {
				results.add(constructor.apply(
						resultSet.getString(1),
						resultSet.getString(2),
						RxNormTermType.valueOf(resultSet.getString(3))
				));
			}
			return results;

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private String toSqlTuple(Collection<String> elements) {
		return elements.stream().map(x -> "'" + x + "'").collect(Collectors.joining(","));
	}

	private <K, V> Map<K, V> associateBy(Collection<V> values, Function<V, K> keyFunction) {
		Map<K, V> resultMap = new HashMap<>();
		for (V value : values) {
			resultMap.put(keyFunction.apply(value), value);
		}
		return resultMap;
	}

	/**
	 * Temporary carrier object. Contains the RXCUI of an RxNorm SCD, its name, the RXCUI of its dose form and RXCUIs of
	 * assigned SCDCs and their INs/PINs.
	 */
	private static class RxNormSCDWithAssignedSCDCs {
		final String rxcui;
		final String name;
		final String doseForm;
		final Map<String, List<String>> ingredientsByScdcRxcui = new HashMap<>();

		public RxNormSCDWithAssignedSCDCs(String rxcui, String name, String doseForm) {
			this.rxcui = rxcui;
			this.name = name;
			this.doseForm = doseForm;
		}
	}

	@FunctionalInterface
	private interface TriFunction<T, U, V, R> {

		R apply(T t, U u, V v);

		default <K> TriFunction<T, U, V, K> andThen(Function<? super R, ? extends K> after) {
			Objects.requireNonNull(after);
			return (T t, U u, V v) -> after.apply(apply(t, u, v));
		}
	}

	@Override
	public void close() throws Exception {
		queryCache.close();
	}
}
