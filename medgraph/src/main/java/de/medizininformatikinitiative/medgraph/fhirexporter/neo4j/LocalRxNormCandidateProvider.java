package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Local SQLite-backed provider for RxNorm candidates.
 * <p>
 * Provides SCD (Semantic Clinical Drug) candidates by querying a local RxNorm SQLite database dump.
 * Handles PIN/IN compatibility by resolving Precise Ingredient Names to Ingredient Names via
 * 'has_form' relationships.
 * <p>
 * Note: SBD (Semantic Branded Drug) candidate lookup is not implemented - the method
 * {@link #findSbdCandidates(GraphDrug, RxNormProductMatcher.MatchResult)} returns an empty list
 *
 * @author Lucy Strüfing:)
 */
public final class LocalRxNormCandidateProvider implements RxNormProductMatcher.RxNormCandidateProvider {

	private final String jdbcUrl;
	private final Connection conn;
	// Simple in-memory caches to avoid repeated DB lookups per run
	private final Map<String, String> scdToDoseForm = new HashMap<>();
	private final Map<String, StrengthCacheEntry> scdToStrengths = new HashMap<>();

	/**
	 * Creates a new provider that connects to the specified RxNorm SQLite database.
	 *
	 * @param sqliteDbPath path to the SQLite database file
	 * @throws RuntimeException if the database connection fails
	 */
	public LocalRxNormCandidateProvider(@NotNull String sqliteDbPath) {
		this.jdbcUrl = "jdbc:sqlite:" + sqliteDbPath;
		try {
			this.conn = DriverManager.getConnection(jdbcUrl);
		} catch (SQLException e) {
			throw new RuntimeException("[LocalRxNormCandidateProvider] Failed to open SQLite connection: " + e.getMessage(), e);
		}
	}

	/**
	 * Finds SCD (Semantic Clinical Drug) candidates that match the given ingredients and dose form.
	 * Resolves PINs to INs, queries the database for matching SCDs, filters by dose form,
	 * and extracts strengths and ingredients for each candidate.
	 *
	 * @param ingredients list of ingredient matches with RxCUIs and normalized strengths
	 * @param doseForm expected dose form (e.g., "oral tablet", "injection")
	 * @return list of SCD candidates, empty if none found
	 */
	@Override
	public @NotNull List<RxNormProductMatcher.RxNormCandidate> findScdCandidates(
			@NotNull List<RxNormProductMatcher.IngredientMatch> ingredients,
			@NotNull String doseForm) {
		if (ingredients.isEmpty()) {
			System.out.println("[LocalRxNormCandidateProvider] DEBUG: No ingredients provided");
			return Collections.emptyList();
		}
		Set<String> ingredientRxcuis = ingredients.stream().map(im -> im.rxcui).collect(Collectors.toSet());
		if (ingredientRxcuis.isEmpty()) {
			System.out.println("[LocalRxNormCandidateProvider] DEBUG: No RxCUIs in ingredients");
			return Collections.emptyList();
		}

		System.out.println("[LocalRxNormCandidateProvider] DEBUG: Searching SCDs for RxCUIs: " + ingredientRxcuis + 
				" (ingredients: " + ingredients.stream().map(im -> im.substanceName).collect(Collectors.joining(", ")) + ")");

		final String placeholders = ingredientRxcuis.stream().map(x -> "?").collect(Collectors.joining(","));
		
		// Query resolves PINs to INs via 'has_form' relationships, then finds SCDs containing all ingredients.
		// The RxNorm hierarchy is: IN -> SCDC -> SCD. Exact ingredient matching is validated later.
		final String sql = """
				WITH resolved_ingredients AS (
					-- Direct INs (already ingredient names)
					SELECT DISTINCT rxcui AS ing_rxcui
					FROM RXNCONSO
					WHERE RXCUI IN (%s)
					  AND SAB = 'RXNORM'
					  AND TTY = 'IN'
					
					UNION
					
					-- PINs converted to INs via 'has_form' relationship
					SELECT DISTINCT form_rel.RXCUI2 AS ing_rxcui
					FROM RXNREL form_rel
					JOIN RXNCONSO pin_conso ON pin_conso.RXCUI = form_rel.RXCUI1
					WHERE form_rel.SAB = 'RXNORM'
					  AND COALESCE(form_rel.RELA, form_rel.REL) = 'has_form'
					  AND form_rel.RXCUI1 IN (%s)
					  AND pin_conso.SAB = 'RXNORM'
					  AND pin_conso.TTY = 'PIN'
					
					UNION
					
					-- Also include PINs directly (in case they're used as ingredients in some contexts)
					SELECT DISTINCT rxcui AS ing_rxcui
					FROM RXNCONSO
					WHERE RXCUI IN (%s)
					  AND SAB = 'RXNORM'
					  AND TTY = 'PIN'
				)
				SELECT DISTINCT scd_rel.RXCUI1 AS scd_rxcui
				FROM resolved_ingredients res_ing
				JOIN RXNREL ing_rel ON ing_rel.RXCUI2 = res_ing.ing_rxcui
				  AND ing_rel.SAB = 'RXNORM'
				  AND COALESCE(ing_rel.RELA, ing_rel.REL) IN ('has_ingredient','ingredient_of')
				JOIN RXNREL scd_rel ON scd_rel.RXCUI2 = ing_rel.RXCUI1
				  AND scd_rel.SAB = 'RXNORM'
				  AND COALESCE(scd_rel.RELA, scd_rel.REL) = 'constitutes'
				JOIN RXNCONSO scd_conso ON scd_conso.RXCUI = scd_rel.RXCUI1
				  AND scd_conso.SAB = 'RXNORM'
				  AND scd_conso.TTY = 'SCD'
				GROUP BY scd_rel.RXCUI1
				HAVING COUNT(DISTINCT res_ing.ing_rxcui) >= ?
				LIMIT 200
				""".formatted(placeholders, placeholders, placeholders);

		List<String> scdRxcuis = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			int idx = 1;
			for (String r : ingredientRxcuis) ps.setString(idx++, r);
			for (String r : ingredientRxcuis) ps.setString(idx++, r);
			for (String r : ingredientRxcuis) ps.setString(idx++, r);
			ps.setInt(idx, ingredientRxcuis.size());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					scdRxcuis.add(rs.getString("scd_rxcui"));
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (SCD scan): " + e.getMessage());
			e.printStackTrace();
			return Collections.emptyList();
		}
		
		if (scdRxcuis.isEmpty()) {
			System.out.println("[LocalRxNormCandidateProvider] DEBUG: No SCD RxCUIs found in RxNorm dump for these ingredients");
			return Collections.emptyList();
		}

		System.out.println("[LocalRxNormCandidateProvider] DEBUG: Found " + scdRxcuis.size() + " SCD RxCUIs");

		// Filter candidates by dose form early to reduce processing overhead
		List<String> filteredScdRxcuis = new ArrayList<>();
		for (String scd : scdRxcuis) {
			String candidateDoseForm = resolveDoseFormFromDb(scd);
			if (candidateDoseForm == null || candidateDoseForm.isBlank() || doseForm.equalsIgnoreCase(candidateDoseForm)) {
				filteredScdRxcuis.add(scd);
			}
		}

		if (filteredScdRxcuis.isEmpty()) {
			System.out.println("[LocalRxNormCandidateProvider] DEBUG: All SCDs filtered out by early doseForm check");
			return Collections.emptyList();
		}

		// Batch-fetch properties and ingredients for remaining SCDs
		Map<String, Properties> propertiesMap = batchGetProperties(filteredScdRxcuis);
		Map<String, List<String>> ingredientsMap = batchGetIngredientsOfScds(filteredScdRxcuis);
		System.out.println("[LocalRxNormCandidateProvider] DEBUG: Retrieved properties for " + propertiesMap.size() + 
				" SCDs, ingredients for " + ingredientsMap.size() + " SCDs");

		// Build candidate objects with name, TTY, ingredients, strengths, units, and dose form
		List<RxNormProductMatcher.RxNormCandidate> result = new ArrayList<>();
		int skippedNoProps = 0;
		int skippedWrongTty = 0;
		for (String scd : filteredScdRxcuis) {
			Properties props = propertiesMap.get(scd);
			if (props == null) {
				skippedNoProps++;
				continue;
			}
			String tty = props.getProperty("TTY", "");
			String name = props.getProperty("STR", "");
			if (!"SCD".equalsIgnoreCase(tty)) {
				skippedWrongTty++;
				continue;
			}

			List<String> ingList = ingredientsMap.getOrDefault(scd, Collections.emptyList());
			Map<String, BigDecimal> strengths = new HashMap<>();
			Map<String, String> numeratorUnits = new HashMap<>();
			Map<String, String> denominatorUnits = new HashMap<>();

			// Extract strengths from SCDC component names, fallback to full SCD name parsing
			fillStrengthsFromComponents(scd, ingList, strengths, numeratorUnits, denominatorUnits);
			if (strengths.isEmpty()) {
				fillStrengthsFromName(name, ingList, strengths, numeratorUnits, denominatorUnits);
			}

			String candidateDoseForm = resolveDoseFormFromDb(scd);

			result.add(new RxNormProductMatcher.RxNormCandidate(
					scd, name, tty, ingList, strengths, numeratorUnits, denominatorUnits, candidateDoseForm
			));
		}
		
		if (result.isEmpty()) {
			System.out.println("[LocalRxNormCandidateProvider] DEBUG: No valid candidates built");
			System.out.println("[LocalRxNormCandidateProvider] DEBUG:   - SCD RxCUIs found: " + scdRxcuis.size());
			System.out.println("[LocalRxNormCandidateProvider] DEBUG:   - Skipped (no props): " + skippedNoProps);
			System.out.println("[LocalRxNormCandidateProvider] DEBUG:   - Skipped (wrong TTY): " + skippedWrongTty);
		} else {
			System.out.println("[LocalRxNormCandidateProvider] DEBUG: Built " + result.size() + " valid candidates");
		}
		
		return result;
	}
	
	/**
	 * Counts how many SCDCs contain the given ingredient RxCUIs.
	 */
	private int countScdcsForIngredients(Set<String> ingredientRxcuis) {
		if (ingredientRxcuis.isEmpty()) return 0;
		String placeholders = ingredientRxcuis.stream().map(x -> "?").collect(Collectors.joining(","));
		final String sql = """
				SELECT COUNT(DISTINCT ing_rel.RXCUI1) AS scdc_count
				FROM RXNREL ing_rel
				WHERE ing_rel.SAB = 'RXNORM'
				  AND (ing_rel.RELA = 'has_ingredient' 
				       OR ing_rel.RELA = 'ingredient_of'
				       OR ing_rel.RELA = 'has_precise_ingredient'
				       OR ing_rel.RELA = 'precise_ingredient_of')
				  AND ing_rel.RXCUI2 IN (%s)
				""".formatted(placeholders);
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			int idx = 1;
			for (String r : ingredientRxcuis) ps.setString(idx++, r);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("scdc_count");
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (SCDC count): " + e.getMessage());
		}
		return 0;
	}
	
	/**
	 * Checks if an ingredient RxCUI has at least one SCD in RxNorm.
	 * Used to prioritize RxCUIs that actually have SCDs available.
	 */
	public boolean hasScdsForIngredient(@NotNull String ingredientRxcui) {
		// Use a simplified query that just checks for existence (LIMIT 1 for performance)
		final String sql = """
				SELECT 1
				FROM RXNREL ing_rel
				JOIN RXNREL scd_rel ON scd_rel.RXCUI2 = ing_rel.RXCUI1
				JOIN RXNCONSO scd_conso ON scd_conso.RXCUI = scd_rel.RXCUI1
				WHERE ing_rel.RXCUI2 = ?
				  AND ing_rel.SAB = 'RXNORM'
				  AND COALESCE(ing_rel.RELA, ing_rel.REL) IN ('has_ingredient','ingredient_of','has_precise_ingredient','precise_ingredient_of')
				  AND scd_rel.SAB = 'RXNORM'
				  AND COALESCE(scd_rel.RELA, scd_rel.REL) = 'constitutes'
				  AND scd_conso.SAB = 'RXNORM'
				  AND scd_conso.TTY = 'SCD'
				LIMIT 1
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, ingredientRxcui);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (has SCDs check): " + e.getMessage());
			return false;
		}
	}

	/**
	 * Counts how many SCDs exist for a single ingredient.
	 * Used for debugging multi-ingredient drugs.
	 */
	private int countScdsForSingleIngredient(String ingredientRxcui) {
		final String sql = """
				SELECT COUNT(DISTINCT scd_rel.RXCUI1) AS scd_count
				FROM RXNREL ing_rel
				JOIN RXNREL scd_rel ON scd_rel.RXCUI2 = ing_rel.RXCUI1
				JOIN RXNCONSO scd_conso ON scd_conso.RXCUI = scd_rel.RXCUI1
				WHERE ing_rel.RXCUI2 = ?
				  AND ing_rel.SAB = 'RXNORM'
				  AND (ing_rel.RELA = 'has_ingredient' 
				       OR ing_rel.RELA = 'ingredient_of'
				       OR ing_rel.RELA = 'has_precise_ingredient'
				       OR ing_rel.RELA = 'precise_ingredient_of')
				  AND scd_rel.SAB = 'RXNORM'
				  AND scd_rel.RELA = 'constitutes'
				  AND scd_conso.SAB = 'RXNORM'
				  AND scd_conso.TTY = 'SCD'
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, ingredientRxcui);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("scd_count");
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (single ingredient SCD count): " + e.getMessage());
		}
		return 0;
	}
	
	/**
	 * Counts how many SCDs exist that contain SOME (but not necessarily all) of the given ingredients.
	 * Used for debugging multi-ingredient drugs.
	 */
	private int countScdsWithPartialIngredients(Set<String> ingredientRxcuis) {
		if (ingredientRxcuis.isEmpty()) return 0;
		String placeholders = ingredientRxcuis.stream().map(x -> "?").collect(Collectors.joining(","));
		final String sql = """
				SELECT COUNT(DISTINCT scd_rel.RXCUI1) AS scd_count
				FROM (
					SELECT DISTINCT ing_rel.RXCUI1 AS scdc_rxcui, ing_rel.RXCUI2 AS ing_rxcui
					FROM RXNREL ing_rel
					WHERE ing_rel.SAB = 'RXNORM'
					  AND (ing_rel.RELA = 'has_ingredient' 
					       OR ing_rel.RELA = 'ingredient_of'
					       OR ing_rel.RELA = 'has_precise_ingredient'
					       OR ing_rel.RELA = 'precise_ingredient_of')
					  AND ing_rel.RXCUI2 IN (%s)
				) scdcs
				JOIN RXNREL scd_rel ON scd_rel.RXCUI2 = scdcs.scdc_rxcui
				  AND scd_rel.SAB = 'RXNORM'
				  AND scd_rel.RELA = 'constitutes'
				JOIN RXNCONSO scd_conso ON scd_conso.RXCUI = scd_rel.RXCUI1
				  AND scd_conso.SAB = 'RXNORM'
				  AND scd_conso.TTY = 'SCD'
				""".formatted(placeholders);
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			int idx = 1;
			for (String r : ingredientRxcuis) ps.setString(idx++, r);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("scd_count");
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (partial SCD count): " + e.getMessage());
		}
		return 0;
	}

	/**
	 * Finds SBD (Semantic Branded Drug) candidates for a drug based on an SCD match.
	 * <p>
	 * Currently not implemented!
	 */
	@Override
	public @NotNull List<RxNormProductMatcher.RxNormCandidate> findSbdCandidates(
			@NotNull GraphDrug drug, @NotNull RxNormProductMatcher.MatchResult scdBaseMatch) {
		return Collections.emptyList();
	}


	/**
	 * Checks if two RxCUIs are compatible by determining if they resolve to the same IN root.
	 * Traverses form_of/has_form relationships to find common ingredient roots.
	 */
	public boolean areRxcuisCompatible(@NotNull String a, @NotNull String b) {
		if (a.equals(b)) return true;
		final String sql = """
				WITH roots AS (
				    SELECT ing.rxcui AS root
				    FROM RXNCONSO ing
				    WHERE ing.sab='RXNORM' AND ing.tty='IN' AND ing.rxcui IN (?, ?)
				    UNION
				    SELECT rel.RXCUI2 AS root
				    FROM RXNREL rel
				    JOIN RXNCONSO c ON c.RXCUI = rel.RXCUI2 AND c.SAB='RXNORM' AND c.TTY='IN'
				    WHERE rel.SAB='RXNORM'
				      AND COALESCE(rel.RELA, rel.REL) IN ('form_of','has_form','precise_ingredient_of','has_precise_ingredient')
				      AND rel.RXCUI1 IN (?, ?)
				),
				a_roots AS (SELECT root FROM roots WHERE root IN (
				    SELECT ing.rxcui FROM RXNCONSO ing WHERE ing.sab='RXNORM' AND ing.tty='IN' AND ing.rxcui = ?
				    UNION
				    SELECT rel.RXCUI2 FROM RXNREL rel
				    JOIN RXNCONSO c ON c.RXCUI = rel.RXCUI2 AND c.SAB='RXNORM' AND c.TTY='IN'
				    WHERE rel.SAB='RXNORM'
				      AND COALESCE(rel.RELA, rel.REL) IN ('form_of','has_form','precise_ingredient_of','has_precise_ingredient')
				      AND rel.RXCUI1 = ?
				)),
				b_roots AS (SELECT root FROM roots WHERE root IN (
				    SELECT ing.rxcui FROM RXNCONSO ing WHERE ing.sab='RXNORM' AND ing.tty='IN' AND ing.rxcui = ?
				    UNION
				    SELECT rel.RXCUI2 FROM RXNREL rel
				    JOIN RXNCONSO c ON c.RXCUI = rel.RXCUI2 AND c.SAB='RXNORM' AND c.TTY='IN'
				    WHERE rel.SAB='RXNORM'
				      AND COALESCE(rel.RELA, rel.REL) IN ('form_of','has_form','precise_ingredient_of','has_precise_ingredient')
				      AND rel.RXCUI1 = ?
				))
				SELECT 1
				WHERE EXISTS (SELECT 1 FROM a_roots INTERSECT SELECT 1 FROM b_roots)
				""";
		try (Connection conn = DriverManager.getConnection(jdbcUrl);
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, a);
			ps.setString(2, b);
			ps.setString(3, a);
			ps.setString(4, b);
			ps.setString(5, a);
			ps.setString(6, a);
			ps.setString(7, b);
			ps.setString(8, b);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (compatibility): " + e.getMessage());
			return false;
		}
	}

	/**
	 * Finds the IN (Ingredient Name) root for a given RxCUI by traversing form_of/has_form relationships.
	 */
	@Nullable
	public String findIngredientRoot(@NotNull String rxcui) {
		final String checkIn = """
				SELECT RXCUI
				FROM RXNCONSO
				WHERE RXCUI = ?
				  AND SAB = 'RXNORM'
				  AND TTY = 'IN'
				LIMIT 1
				""";
		try (PreparedStatement ps = conn.prepareStatement(checkIn)) {
			ps.setString(1, rxcui);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rxcui;
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (check IN): " + e.getMessage());
			return null;
		}

		// Traverse form_of/has_form relationships iteratively (max 5 levels)
		String current = rxcui;
		for (int depth = 0; depth < 5; depth++) {
			final String findNext = """
					SELECT rel.RXCUI2 AS next_rxcui
					FROM RXNREL rel
					JOIN RXNCONSO c ON c.RXCUI = rel.RXCUI2 AND c.SAB='RXNORM' AND c.TTY='IN'
					WHERE rel.RXCUI1 = ?
					  AND rel.SAB='RXNORM'
					  AND COALESCE(rel.RELA, rel.REL) IN ('form_of','has_form','precise_ingredient_of','has_precise_ingredient')
					LIMIT 1
					""";
			try (PreparedStatement ps = conn.prepareStatement(findNext)) {
				ps.setString(1, current);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						String next = rs.getString("next_rxcui");
						if (next != null) {
							current = next;
							// Found an IN, return it
							return current;
						}
					}
				}
			} catch (SQLException e) {
				System.err.println("[LocalRxNormCandidateProvider] SQL error (find root): " + e.getMessage());
				return null;
			}
		}
		
		return null;
	}

	/**
	 * Batch-fetches properties (STR, TTY) for multiple RxCUIs.
	 * Chunks requests to stay within SQLite's parameter limit.
	 */
	private @NotNull Map<String, Properties> batchGetProperties(@NotNull Collection<String> rxcuis) {
		if (rxcuis.isEmpty()) return Collections.emptyMap();
		Map<String, Properties> map = new HashMap<>();
		List<String> list = new ArrayList<>(rxcuis);
		int chunkSize = 500;
		for (int i = 0; i < list.size(); i += chunkSize) {
			List<String> chunk = list.subList(i, Math.min(i + chunkSize, list.size()));
			String placeholders = chunk.stream().map(x -> "?").collect(Collectors.joining(","));
			final String sql = """
					SELECT RXCUI, STR, TTY
					FROM RXNCONSO
					WHERE RXCUI IN (%s)
					  AND SAB = 'RXNORM'
					  AND TTY = 'SCD'
					  AND (SUPPRESS IS NULL OR SUPPRESS <> 'Y')
					""".formatted(placeholders);
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				int idx = 1;
				for (String r : chunk) ps.setString(idx++, r);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String rxcui = rs.getString("RXCUI");
						Properties p = new Properties();
						p.setProperty("STR", rs.getString("STR"));
						p.setProperty("TTY", rs.getString("TTY"));
						map.put(rxcui, p);
					}
				}
			} catch (SQLException e) {
				System.err.println("[LocalRxNormCandidateProvider] SQL error (batch properties): " + e.getMessage());
			}
		}
		return map;
	}

	/**
	 * Batch-fetches ingredient RxCUIs for multiple SCDs.
	 * Chunks requests to stay within SQLite's parameter limit.
	 */
	private @NotNull Map<String, List<String>> batchGetIngredientsOfScds(@NotNull Collection<String> scdRxcuis) {
		if (scdRxcuis.isEmpty()) return Collections.emptyMap();
		Map<String, List<String>> map = new HashMap<>();
		List<String> list = new ArrayList<>(scdRxcuis);
		int chunkSize = 500;
		for (int i = 0; i < list.size(); i += chunkSize) {
			List<String> chunk = list.subList(i, Math.min(i + chunkSize, list.size()));
			String placeholders = chunk.stream().map(x -> "?").collect(Collectors.joining(","));
			final String sql = """
					SELECT DISTINCT scdc_rel.RXCUI1 AS scd_rxcui, ing.RXCUI2 AS ing_rxcui
					FROM RXNREL scdc_rel
					JOIN RXNREL ing ON ing.RXCUI1 = scdc_rel.RXCUI2
					WHERE scdc_rel.RXCUI1 IN (%s)
					  AND scdc_rel.SAB = 'RXNORM'
					  AND COALESCE(scdc_rel.RELA, scdc_rel.REL) = 'constitutes'
					  AND ing.SAB = 'RXNORM'
					  AND COALESCE(ing.RELA, ing.REL) IN (
					       'has_ingredient',
					       'ingredient_of',
					       'has_precise_ingredient',
					       'precise_ingredient_of')
					""".formatted(placeholders);
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				int idx = 1;
				for (String r : chunk) ps.setString(idx++, r);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String scdRxcui = rs.getString("scd_rxcui");
						String ingRxcui = rs.getString("ing_rxcui");
						map.computeIfAbsent(scdRxcui, k -> new ArrayList<>()).add(ingRxcui);
					}
				}
			} catch (SQLException e) {
				System.err.println("[LocalRxNormCandidateProvider] SQL error (batch ingredients): " + e.getMessage());
			}
		}
		return map;
	}

	/**
	 * Fetches basic properties (name/STR and term type/TTY) for a single RxCUI.
	 *
	 * @param rxcui the RxCUI to look up
	 * @return Properties object with STR and TTY, or null if not found
	 */
	private @Nullable Properties getProperties(@NotNull String rxcui) {
		final String sql = """
				SELECT STR, TTY
				FROM RXNCONSO
				WHERE RXCUI = ?
				  AND SAB = 'RXNORM'
				  AND (SUPPRESS IS NULL OR SUPPRESS <> 'Y')
				LIMIT 1
				""";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, rxcui);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Properties p = new Properties();
					p.setProperty("STR", rs.getString("STR"));
					p.setProperty("TTY", rs.getString("TTY"));
					return p;
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (properties): " + e.getMessage());
		}
		return null;
	}

	/**
	 * Gets example SCDs that contain SOME (but not all) of the given ingredients.
	 * Used for debugging multi-ingredient drugs.
	 */
	private List<String> getExamplePartialMatchScds(Set<String> ingredientRxcuis) {
		if (ingredientRxcuis.isEmpty()) return Collections.emptyList();
		String placeholders = ingredientRxcuis.stream().map(x -> "?").collect(Collectors.joining(","));
		final String sql = """
				SELECT DISTINCT scd_rel.RXCUI1 AS scd_rxcui
				FROM (
					SELECT DISTINCT ing_rel.RXCUI1 AS scdc_rxcui, ing_rel.RXCUI2 AS ing_rxcui
					FROM RXNREL ing_rel
					WHERE ing_rel.SAB = 'RXNORM'
					  AND (ing_rel.RELA = 'has_ingredient' 
					       OR ing_rel.RELA = 'ingredient_of'
					       OR ing_rel.RELA = 'has_precise_ingredient'
					       OR ing_rel.RELA = 'precise_ingredient_of')
					  AND ing_rel.RXCUI2 IN (%s)
				) scdcs
				JOIN RXNREL scd_rel ON scd_rel.RXCUI2 = scdcs.scdc_rxcui
				  AND scd_rel.SAB = 'RXNORM'
				  AND scd_rel.RELA = 'constitutes'
				JOIN RXNCONSO scd_conso ON scd_conso.RXCUI = scd_rel.RXCUI1
				  AND scd_conso.SAB = 'RXNORM'
				  AND scd_conso.TTY = 'SCD'
				GROUP BY scd_rel.RXCUI1
				HAVING COUNT(DISTINCT scdcs.ing_rxcui) < ?
				LIMIT 3
				""".formatted(placeholders);
		List<String> result = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			int idx = 1;
			for (String r : ingredientRxcuis) ps.setString(idx++, r);
			ps.setInt(idx, ingredientRxcuis.size());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					result.add(rs.getString("scd_rxcui"));
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (example partial SCDs): " + e.getMessage());
		}
		return result;
	}
	
	/**
	 * Gets the name (STR) of an SCD by its RxCUI.
	 */
	private String getScdName(String scdRxcui) {
		Properties props = getProperties(scdRxcui);
		return props != null ? props.getProperty("STR", "unknown") : "unknown";
	}
	
	/**
	 * Gets ingredient RxCUIs for a single SCD by traversing SCD -> SCDC -> IN hierarchy.
	 */
	private @NotNull List<String> getIngredientsOfScd(@NotNull String scdRxcui) {
		final String sql = """
				SELECT DISTINCT ing.RXCUI2 AS ing_rxcui
				FROM RXNREL scdc_rel
				JOIN RXNREL ing ON ing.RXCUI1 = scdc_rel.RXCUI2
				WHERE scdc_rel.RXCUI1 = ?
				  AND scdc_rel.SAB = 'RXNORM'
				  AND scdc_rel.RELA = 'constitutes'
				  AND ing.SAB = 'RXNORM'
				  AND (ing.RELA = 'has_ingredient' OR ing.RELA = 'ingredient_of')
				LIMIT 50
				""";
		List<String> list = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(jdbcUrl);
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, scdRxcui);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(rs.getString("ing_rxcui"));
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (ingredients): " + e.getMessage());
		}
		return list;
	}

	/**
	 * Resolves the dose form for an SCD by traversing SCD -> SCDF -> DF relationships.
	 * Results are cached to avoid repeated database queries.
	 */
	private @org.jetbrains.annotations.Nullable String resolveDoseFormFromDb(@NotNull String scdRxcui) {
		if (scdToDoseForm.containsKey(scdRxcui)) {
			return scdToDoseForm.get(scdRxcui);
		}
		final String sqlScdf = """
				SELECT DISTINCT rel.RXCUI2 AS scdf_rxcui
				FROM RXNREL rel
				JOIN RXNCONSO c ON c.RXCUI = rel.RXCUI2
				WHERE rel.RXCUI1 = ?
				  AND rel.SAB = 'RXNORM'
				  AND c.SAB = 'RXNORM'
				  AND c.TTY = 'SCDF'
				""";
		java.util.Set<String> scdfRxcuis = new java.util.HashSet<>();
		try (PreparedStatement ps = conn.prepareStatement(sqlScdf)) {
			ps.setString(1, scdRxcui);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					scdfRxcuis.add(rs.getString("scdf_rxcui"));
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (SCDF lookup): " + e.getMessage());
			scdToDoseForm.put(scdRxcui, null);
			return null;
		}
		if (scdfRxcuis.isEmpty()) {
			scdToDoseForm.put(scdRxcui, null);
			return null;
		}

		// Find DF (dose form) concept for each SCDF
		final String sqlDf = """
				SELECT DISTINCT df_conso.STR AS df_name
				FROM RXNREL rel
				JOIN RXNCONSO df_conso ON df_conso.RXCUI = rel.RXCUI2
				WHERE rel.RXCUI1 = ?
				  AND rel.SAB = 'RXNORM'
				  AND df_conso.SAB = 'RXNORM'
				  AND df_conso.TTY = 'DF'
				LIMIT 1
				""";
		for (String scdf : scdfRxcuis) {
			try (PreparedStatement ps = conn.prepareStatement(sqlDf)) {
				ps.setString(1, scdf);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						String dfName = rs.getString("df_name");
						if (dfName != null && !dfName.isBlank()) {
							String normalized = dfName.toLowerCase(Locale.ROOT);
							scdToDoseForm.put(scdRxcui, normalized);
							return normalized;
						}
					}
				}
			} catch (SQLException e) {
				System.err.println("[LocalRxNormCandidateProvider] SQL error (DF lookup): " + e.getMessage());
			}
		}
		scdToDoseForm.put(scdRxcui, null);
		return null;
	}

	/**
	 * Extracts strengths and units for each ingredient by walking the SCD → SCDC → IN
	 * hierarchy and parsing SCDC names. Results are cached to avoid repeated parsing.
	 */
	private void fillStrengthsFromComponents(
			@NotNull String scdRxcui,
			@NotNull List<String> ingredientRxcuis,
			@NotNull Map<String, BigDecimal> strengths,
			@NotNull Map<String, String> numeratorUnits,
			@NotNull Map<String, String> denominatorUnits) {
		if (ingredientRxcuis.isEmpty()) return;

		StrengthCacheEntry cached = scdToStrengths.get(scdRxcui);
		if (cached != null) {
			strengths.putAll(cached.strengths());
			numeratorUnits.putAll(cached.numeratorUnits());
			denominatorUnits.putAll(cached.denominatorUnits());
			return;
		}

		java.util.Set<String> ingredientSet = new java.util.HashSet<>(ingredientRxcuis);
		final String sql = """
				SELECT DISTINCT scdc_rel.RXCUI2 AS scdc_rxcui, ing.RXCUI2 AS ing_rxcui
				FROM RXNREL scdc_rel
				JOIN RXNREL ing ON ing.RXCUI1 = scdc_rel.RXCUI2
				WHERE scdc_rel.RXCUI1 = ?
				  AND scdc_rel.SAB = 'RXNORM'
				  AND scdc_rel.RELA = 'constitutes'
				  AND ing.SAB = 'RXNORM'
				  AND (ing.RELA = 'has_ingredient'
				       OR ing.RELA = 'ingredient_of'
				       OR ing.RELA = 'has_precise_ingredient'
				       OR ing.RELA = 'precise_ingredient_of')
				""";
		Map<String, List<String>> scdcToIngredients = new HashMap<>();
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, scdRxcui);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String scdc = rs.getString("scdc_rxcui");
					String ing = rs.getString("ing_rxcui");
					if (!ingredientSet.contains(ing)) continue;
					scdcToIngredients
							.computeIfAbsent(scdc, k -> new ArrayList<>())
							.add(ing);
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (SCDC components for strengths): " + e.getMessage());
			return;
		}
		if (scdcToIngredients.isEmpty()) {
			return;
		}

		// Load SCDC names and parse strengths from them
		final String sqlScdcName = """
				SELECT STR
				FROM RXNCONSO
				WHERE RXCUI = ?
				  AND SAB = 'RXNORM'
				  AND TTY = 'SCDC'
				LIMIT 1
				""";
		// Temporary containers for this SCD to allow caching
		Map<String, BigDecimal> tmpStrengths = new HashMap<>();
		Map<String, String> tmpNumeratorUnits = new HashMap<>();
		Map<String, String> tmpDenominatorUnits = new HashMap<>();

		try (PreparedStatement ps = conn.prepareStatement(sqlScdcName)) {
			for (Map.Entry<String, List<String>> entry : scdcToIngredients.entrySet()) {
				String scdc = entry.getKey();
				List<String> ingRxcuis = entry.getValue();
				String scdcName = null;
				ps.setString(1, scdc);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						scdcName = rs.getString("STR");
					}
				}
				if (scdcName == null || scdcName.isBlank()) continue;

				fillStrengthsFromName(scdcName, ingRxcuis, tmpStrengths, tmpNumeratorUnits, tmpDenominatorUnits);
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (SCDC names for strengths): " + e.getMessage());
		}

		if (!tmpStrengths.isEmpty()) {
			scdToStrengths.put(scdRxcui, new StrengthCacheEntry(
					tmpStrengths, tmpNumeratorUnits, tmpDenominatorUnits
			));
			strengths.putAll(tmpStrengths);
			numeratorUnits.putAll(tmpNumeratorUnits);
			denominatorUnits.putAll(tmpDenominatorUnits);
		}
	}

	/**
	 * Parses strength values and units from SCD names using heuristic pattern matching.
	 * Handles formats like "325 MG", "5 MG/ML", and multi-ingredient names separated by " / ".
	 */
	private void fillStrengthsFromName(
			@NotNull String scdName,
			@NotNull List<String> ingredientRxcuis,
			@NotNull Map<String, BigDecimal> strengths,
			@NotNull Map<String, String> numeratorUnits,
			@NotNull Map<String, String> denominatorUnits) {
		// Split by " / " to separate ingredients while preserving ratio units like "MG/ML"
		String[] segments = scdName.split("\\s+/\\s+");
		Map<String, String> rxcuiToPrefName = getPreferredNames(ingredientRxcuis);
		
		for (String rxcui : ingredientRxcuis) {
			String pref = rxcuiToPrefName.getOrDefault(rxcui, "");
			if (pref.isEmpty()) continue;
			
			// Find segment containing this ingredient's preferred name
			String matchedSegment = null;
			for (String seg : segments) {
				if (seg.toLowerCase(Locale.ROOT).contains(pref.toLowerCase(Locale.ROOT))) {
					matchedSegment = seg;
					break;
				}
			}
			if (matchedSegment == null && segments.length == 1) {
				matchedSegment = segments[0];
			}
			if (matchedSegment == null) continue;
			
			// Extract strength pattern: number + unit [+ "/" + denominator unit]
			java.util.regex.Pattern pattern = java.util.regex.Pattern
					.compile("(\\d+(?:[\\.,]\\d+)?)\\s+([A-Za-zµμ]{2,})(?:\\s*/\\s*([A-Za-zµμ]+))?");
			java.util.regex.Matcher m = pattern.matcher(matchedSegment);
			boolean found = m.find();
			
			if (!found) {
				// Fallback: try without whitespace (e.g., "400MG" or "400MG/ML")
				pattern = java.util.regex.Pattern
						.compile("(\\d+(?:[\\.,]\\d+)?)([A-Za-zµμ]{2,}?)(?:/\\s*([A-Za-zµμ]+))?");
				m = pattern.matcher(matchedSegment);
				found = m.find();
			}
			
			if (found) {
				String numStr = m.group(1).replace(',', '.');
				String unit = m.group(2);
				String denom = m.group(3);
				
				// Handle edge case where unit contains "/" despite regex
				if (unit != null && unit.contains("/")) {
					int slashIdx = unit.indexOf('/');
					String actualUnit = unit.substring(0, slashIdx);
					String actualDenom = unit.substring(slashIdx + 1);
					unit = actualUnit;
					if (denom == null || denom.isEmpty()) {
						denom = actualDenom;
					}
				}
				
				// Normalize units to uppercase
				if (unit != null && !unit.isEmpty()) {
					unit = unit.toUpperCase(Locale.ROOT);
				}
				if (denom != null && !denom.isEmpty()) {
					denom = denom.toUpperCase(Locale.ROOT);
				}
				
				try {
					BigDecimal val = new BigDecimal(numStr);
					strengths.put(rxcui, val);
					numeratorUnits.put(rxcui, unit);
					if (denom != null && !denom.isEmpty()) {
						denominatorUnits.put(rxcui, denom);
					}
				} catch (NumberFormatException ignore) {
					// Ignore parsing failures
				}
			}
		}
	}

	/**
	 * Retrieves the preferred names (STR) from RxNorm for the given RxCUIs.
	 * Used to match ingredient names in SCD names when parsing strengths.
	 *
	 * @param rxcuis collection of RxCUIs to look up
	 * @return map from RxCUI to preferred name (STR)
	 */
	private Map<String, String> getPreferredNames(@NotNull Collection<String> rxcuis) {
		if (rxcuis.isEmpty()) return Collections.emptyMap();
		String placeholders = rxcuis.stream().map(x -> "?").collect(Collectors.joining(","));
		final String sql = """
				SELECT RXCUI, STR
				FROM RXNCONSO
				WHERE RXCUI IN (%s)
				  AND SAB = 'RXNORM'
				  AND (SUPPRESS IS NULL OR SUPPRESS <> 'Y')
				GROUP BY RXCUI
				""".formatted(placeholders);
		Map<String, String> map = new HashMap<>();
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			int i = 1;
			for (String r : rxcuis) ps.setString(i++, r);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					map.put(rs.getString("RXCUI"), rs.getString("STR"));
				}
			}
		} catch (SQLException e) {
			System.err.println("[LocalRxNormCandidateProvider] SQL error (preferred names): " + e.getMessage());
		}
		return map;
	}

	/**
	 * Simple container for cached strength data per SCD RXCUI.
	 */
	private record StrengthCacheEntry(
			Map<String, BigDecimal> strengths,
			Map<String, String> numeratorUnits,
			Map<String, String> denominatorUnits) {}
}


