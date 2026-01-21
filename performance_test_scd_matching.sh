#!/bin/bash

# Script zum Durchführen eines Performance-Tests für den SCD-Matching-Algorithmus
# 
# Verwendung:
#   ./performance_test_scd_matching.sh [database-uri] [database-user] [database-password] [iterations] [suffix]
#
# Parameter:
#   database-uri      - Neo4j Datenbank URI (optional, Standard: bolt://localhost:7687)
#   database-user     - Neo4j Benutzername (optional, Standard: neo4j)
#   database-password - Neo4j Passwort (optional, Standard: 7o7MP~8_)h~0)
#   iterations        - Anzahl der Durchläufe (optional, Standard: 10)
#   suffix            - Optionaler Suffix für Ausgabedateien (z.B. "paper_mapping", "custom_mapping")
#
# Beispiele:
#   ./performance_test_scd_matching.sh                                    # Standard: 10 Durchläufe
#   ./performance_test_scd_matching.sh "" "" "" 10 "paper_mapping"       # 10 Durchläufe mit Suffix
#   ./performance_test_scd_matching.sh "" "" "" 5                        # 5 Durchläufe

# Wechsle zum Projekt-Root (wo das Skript liegt)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Setze Java 21 für Gradle
export JAVA_HOME="/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home"

# Parameter mit Standardwerten
DB_URI="${1:-bolt://localhost:7687}"
DB_USER="${2:-neo4j}"
DB_PASSWORD="${3:-7o7MP~8_)h~0}"
ITERATIONS="${4:-10}"
SUFFIX="${5:-}"

# JSON Output-Datei (relativ zum medgraph Verzeichnis, da Gradle dort läuft)
if [ -n "$SUFFIX" ]; then
    JSON_OUTPUT="scd_matching_performance_${SUFFIX}.json"
else
    JSON_OUTPUT="scd_matching_performance.json"
fi

echo "=== Performance-Test: SCD Matching ==="
echo "  Datenbank URI: $DB_URI"
echo "  Benutzer: $DB_USER"
echo "  Anzahl Durchläufe: $ITERATIONS"
echo "  JSON Output: medgraph/$JSON_OUTPUT"
if [ -n "$SUFFIX" ]; then
    echo "  Suffix: $SUFFIX"
fi
echo ""

# Führe den Performance-Test mit Gradle aus
./gradlew :medgraph:runPerformanceTestSCDMatching --args="$DB_URI $DB_USER $DB_PASSWORD $ITERATIONS $JSON_OUTPUT"

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✓ Performance-Test erfolgreich abgeschlossen!"
    echo "  Ergebnisse: medgraph/$JSON_OUTPUT"
else
    echo ""
    echo "✗ Performance-Test fehlgeschlagen (Exit-Code: $EXIT_CODE)"
    exit $EXIT_CODE
fi

