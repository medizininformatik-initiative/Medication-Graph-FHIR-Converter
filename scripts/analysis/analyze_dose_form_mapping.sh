#!/bin/bash

# Script zum Ausführen der EDQM Dose Form Mapping Analyse
# 
# Verwendung:
#   ./analyze_dose_form_mapping.sh [database-uri] [database-user] [database-password]
#
# Parameter:
#   database-uri      - Neo4j Datenbank URI (optional, Standard: bolt://localhost:7687)
#   database-user     - Neo4j Benutzername (optional, Standard: neo4j)
#   database-password - Neo4j Passwort (optional, Standard: 7o7MP~8_)h~0)
#
# Beispiele:
#   ./analyze_dose_form_mapping.sh                                    # Standard-Datenbankverbindung
#   ./analyze_dose_form_mapping.sh bolt://localhost:7687 neo4j "passwort"  # Explizite DB-Verbindung


# Results for CQ2 Figure 4.1 

# Wechsle zum Projekt-Root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/../.."

# Setze Java 21 für Gradle (der Code wurde mit Java 21 kompiliert)
export JAVA_HOME="/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home"

# Parameter mit Standardwerten
DB_URI="${1:-bolt://localhost:7687}"
DB_USER="${2:-neo4j}"
DB_PASSWORD="${3:-${NEO4J_PASSWORD:-}}"

# Prüfe, ob Passwort gesetzt ist
if [ -z "$DB_PASSWORD" ]; then
    echo "FEHLER: Kein Neo4j-Passwort angegeben!"
    echo "Bitte setze die Umgebungsvariable NEO4J_PASSWORD oder übergebe das Passwort als drittes Argument."
    exit 1
fi
SUFFIX="${4:-}"

# Bestimme JSON-Output-Pfad
if [ -n "$SUFFIX" ]; then
    JSON_OUTPUT="output/analysis/dose_form_mapping_analysis_${SUFFIX}.json"
else
    JSON_OUTPUT="output/analysis/dose_form_mapping_analysis.json"
fi

echo "Starte EDQM Dose Form Mapping Analyse..."
echo "  Datenbank URI: $DB_URI"
echo "  Benutzer: $DB_USER"
if [ -n "$SUFFIX" ]; then
    echo "  Suffix: $SUFFIX"
fi
echo "  JSON Output: $JSON_OUTPUT"
echo ""

# Führe die Analyse direkt mit Gradle aus
./gradlew :medgraph:runAnalyzeDoseFormMapping --args="$DB_URI $DB_USER $DB_PASSWORD $JSON_OUTPUT"

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✓ Analyse erfolgreich abgeschlossen!"
else
    echo ""
    echo "✗ Analyse fehlgeschlagen (Exit-Code: $EXIT_CODE)"
    exit $EXIT_CODE
fi

