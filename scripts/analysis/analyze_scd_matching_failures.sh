#!/bin/bash

# Script zum Ausführen der SCD Matching Failure Analyse
# 
# Verwendung:
#   ./analyze_scd_matching_failures.sh [database-uri] [database-user] [database-password] [suffix]
#
# Parameter:
#   database-uri      - Neo4j Datenbank URI (optional, Standard: bolt://localhost:7687)
#   database-user     - Neo4j Benutzername (optional, Standard: neo4j)
#   database-password - Neo4j Passwort (optional, Standard: 7o7MP~8_)h~0)
#   suffix            - Optionaler Suffix für Ausgabedateien (z.B. "paper_mapping", "custom_mapping")
#                       Wenn nicht angegeben, wird "paper_mapping" verwendet
#
# Beispiele:
#   ./analyze_scd_matching_failures.sh                                    # Standard-Datenbankverbindung, Standard-Suffix
#   ./analyze_scd_matching_failures.sh "" "" "" "paper_mapping"          # Standard-DB, expliziter Suffix
#   ./analyze_scd_matching_failures.sh bolt://localhost:7687 neo4j "pass" "custom"  # Alle Parameter

# Results for CQ3 Table 4.5 and Figure 4.2

# Wechsle zum Projekt-Root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/../.."

# Setze Java 21 für Gradle
export JAVA_HOME="/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home"

# Parameter: Prüfe zuerst Umgebungsvariablen, dann Argumente, dann Standardwerte
DB_URI="${1:-${NEO4J_URI:-bolt://localhost:7687}}"
DB_USER="${2:-${NEO4J_USER:-neo4j}}"
DB_PASSWORD="${3:-${NEO4J_PASSWORD:-}}"
SUFFIX="${4:-paper_mapping}"

# Prüfe, ob Passwort gesetzt ist
if [ -z "$DB_PASSWORD" ]; then
    echo "FEHLER: Kein Neo4j-Passwort angegeben!"
    echo "Bitte setze die Umgebungsvariable NEO4J_PASSWORD oder übergebe das Passwort als drittes Argument."
    echo "Beispiel: export NEO4J_PASSWORD=dein_passwort"
    exit 1
fi

# JSON Output-Datei
OUTPUT_JSON="output/analysis/scd_matching_statistics_${SUFFIX}.json"

echo "Starte SCD Matching Failure Analyse..."
echo "  Datenbank URI: $DB_URI"
echo "  Benutzer: $DB_USER"
echo "  Suffix: $SUFFIX"
echo "  Ausgabe-JSON: $OUTPUT_JSON"
echo ""

# Führe die Analyse direkt mit Gradle aus
./gradlew :medgraph:runAnalyzeScdMatchingFailures --args="$DB_URI $DB_USER $DB_PASSWORD $OUTPUT_JSON"

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✓ Analyse erfolgreich abgeschlossen!"
    echo ""
    echo "Erstelle Visualisierung..."
    
    # Führe Python-Skript zur Visualisierung aus
    if [ -f "scripts/plots/visualize_scd_matching_failures.py" ]; then
        cd scripts/plots
        if [ -d "venv" ]; then
            source venv/bin/activate
            python3 visualize_scd_matching_failures.py "../../$OUTPUT_JSON"
        else
            python3 visualize_scd_matching_failures.py "../../$OUTPUT_JSON"
        fi
        cd ../..
    else
        echo "  Python-Skript nicht gefunden. Visualisierung übersprungen."
    fi
else
    echo ""
    echo "✗ Analyse fehlgeschlagen (Exit-Code: $EXIT_CODE)"
    exit $EXIT_CODE
fi

