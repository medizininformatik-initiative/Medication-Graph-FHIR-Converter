

// ============================================
// Query 1: Alle UCUM-Codes mit Verwendungshäufigkeit
// ============================================
// Zeigt alle UCUM-Codes, die tatsächlich in Ingredients verwendet werden
MATCH (i:MMIIngredient)-[:HAS_UNIT]->(u:Unit)
WHERE u.ucumCs IS NOT NULL AND u.ucumCs <> ""
RETURN u.ucumCs AS ucumCode, 
       u.mmiCode AS mmiCode,
       u.mmiName AS mmiName,
       count(i) AS anzahlVerwendungen
ORDER BY anzahlVerwendungen DESC;





