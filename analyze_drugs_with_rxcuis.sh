#!/bin/bash

# Script zum Analysieren: Von allen Drugs mit bekannten aktiven Ingredients (RxCUIs),
# wie viele erhalten mindestens einen passenden RxNorm-SCD?
# 
# Verwendung:
#   ./analyze_drugs_with_rxcuis.sh [database-uri] [database-user] [database-password] [suffix]
#
# Parameter:
#   database-uri      - Neo4j Datenbank URI (optional, Standard: bolt://localhost:7687)
#   database-user     - Neo4j Benutzername (optional, Standard: neo4j)
#   database-password - Neo4j Passwort (optional, Standard: 7o7MP~8_)h~0)
#   suffix            - Optionaler Suffix für Ausgabedateien (z.B. "paper_mapping", "custom_mapping")
#                       Wenn nicht angegeben, werden Standard-Namen verwendet
#
# Beispiele:
#   ./analyze_drugs_with_rxcuis.sh                                    # Standard-Datenbankverbindung, Standard-Namen
#   ./analyze_drugs_with_rxcuis.sh "" "" "" "paper_mapping"          # Standard-DB, aber mit Suffix "paper_mapping"
#   ./analyze_drugs_with_rxcuis.sh bolt://localhost:7687 neo4j "pass" "custom"  # Alle Parameter

# Wechsle zum Projekt-Root (wo das Skript liegt)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Setze Java 21 für Gradle
export JAVA_HOME="/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home"

# Parameter mit Standardwerten
DB_URI="${1:-bolt://localhost:7687}"
DB_USER="${2:-neo4j}"
DB_PASSWORD="${3:-7o7MP~8_)h~0}"
SUFFIX="${4:-}"

# JSON Output-Datei (relativ zum medgraph Verzeichnis, da Gradle dort läuft)
if [ -n "$SUFFIX" ]; then
    JSON_OUTPUT="drugs_with_rxcuis_analysis_${SUFFIX}.json"
else
    JSON_OUTPUT="drugs_with_rxcuis_analysis.json"
fi

echo "=== Analyse: Drugs mit RxCUIs und SCD Matching ==="
echo "  Datenbank URI: $DB_URI"
echo "  Benutzer: $DB_USER"
echo "  JSON Output: medgraph/$JSON_OUTPUT"
if [ -n "$SUFFIX" ]; then
    echo "  Suffix: $SUFFIX"
fi
echo ""

# Führe die Analyse direkt mit Gradle aus
./gradlew :medgraph:runAnalyzeDrugsWithRxCUIs --args="$DB_URI $DB_USER $DB_PASSWORD $JSON_OUTPUT"

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✓ Analyse erfolgreich abgeschlossen!"
    echo "  Ergebnisse: medgraph/$JSON_OUTPUT"
else
    echo ""
    echo "✗ Analyse fehlgeschlagen (Exit-Code: $EXIT_CODE)"
    exit $EXIT_CODE
fi

