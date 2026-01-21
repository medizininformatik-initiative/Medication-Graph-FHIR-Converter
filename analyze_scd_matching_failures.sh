#!/bin/bash

# Script zum Ausführen der SCD Matching Failure Analyse
# 
# Verwendung:
#   ./analyze_scd_matching_failures.sh [database-uri] [database-user] [database-password] [output-json]
#
# Parameter:
#   database-uri      - Neo4j Datenbank URI (optional, Standard: bolt://localhost:7687)
#   database-user     - Neo4j Benutzername (optional, Standard: neo4j)
#   database-password - Neo4j Passwort (optional, Standard: 7o7MP~8_)h~0)
#   output-json       - Ausgabe-JSON-Datei (optional, Standard: scd_matching_statistics.json)
#
# Beispiele:
#   ./analyze_scd_matching_failures.sh                                    # Standard-Datenbankverbindung
#   ./analyze_scd_matching_failures.sh bolt://localhost:7687 neo4j "passwort"  # Explizite DB-Verbindung

# Wechsle zum Projekt-Root (wo das Skript liegt)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Setze Java 21 für Gradle
export JAVA_HOME="/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home"

# Parameter mit Standardwerten (nur wenn nicht leer)
DB_URI="${1:-bolt://localhost:7687}"
DB_USER="${2:-neo4j}"
DB_PASSWORD="${3:-7o7MP~8_)h~0}"
OUTPUT_JSON="${4:-scd_matching_statistics_paper_mapping.json}"

echo "Starte SCD Matching Failure Analyse..."
echo "  Datenbank URI: $DB_URI"
echo "  Benutzer: $DB_USER"
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
            python3 visualize_scd_matching_failures.py "../../medgraph/$OUTPUT_JSON"
        else
            python3 visualize_scd_matching_failures.py "../../medgraph/$OUTPUT_JSON"
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

