#!/bin/bash

# Script zum Ausführen des Top 20 Substanzen Analyzers
# 
# Verwendung:
#   ./analyze_top_substances.sh [database-uri] [database-user] [database-password] [suffix]
#
# Parameter:
#   database-uri      - Neo4j Datenbank URI (optional)
#   database-user     - Neo4j Benutzername (optional)
#   database-password - Neo4j Passwort (optional)
#   suffix            - Optionaler Suffix für Ausgabedateien (z.B. "paper_mapping", "custom_mapping")
#                       Wenn nicht angegeben, werden Standard-Namen verwendet
#
# Beispiele:
#   ./analyze_top_substances.sh                                    # Standard-Datenbankverbindung, Standard-Namen
#   ./analyze_top_substances.sh "" "" "" "paper_mapping"           # Standard-DB, aber mit Suffix "paper_mapping"
#   ./analyze_top_substances.sh bolt://localhost:7687 neo4j "pass" "custom"  # Alle Parameter

# Wechsle zum Projekt-Root (wo das Skript liegt)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Setze Java 21 für Gradle (der Code wurde mit Java 21 kompiliert)
export JAVA_HOME="/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home"

# Optionale Datenbankverbindungsparameter
DB_URI="${1:-}"
DB_USER="${2:-}"
DB_PASSWORD="${3:-}"
SUFFIX="${4:-}"

# JSON Output-Datei (relativ zum medgraph Verzeichnis, da Gradle dort läuft)
if [ -n "$SUFFIX" ]; then
    JSON_OUTPUT="top_substances_analysis_${SUFFIX}.json"
else
    JSON_OUTPUT="top_substances_analysis.json"
fi

echo "Starte Top 20 Substanzen Analyse..."
echo "  JSON Output: medgraph/$JSON_OUTPUT"
if [ -n "$SUFFIX" ]; then
    echo "  Suffix: $SUFFIX"
fi
echo ""

# Baue den Gradle-Befehl zusammen
GRADLE_CMD="./gradlew :medgraph:run --args=\"analyze-top-substances $JSON_OUTPUT\""

# Füge Datenbankverbindungsparameter hinzu, falls angegeben
if [ -n "$DB_URI" ] && [ -n "$DB_USER" ] && [ -n "$DB_PASSWORD" ]; then
    echo "Verwende angegebene Datenbankverbindung:"
    echo "  URI: $DB_URI"
    echo "  User: $DB_USER"
    echo ""
    GRADLE_CMD="./gradlew :medgraph:run --args=\"analyze-top-substances -r $DB_URI -u $DB_USER -p $DB_PASSWORD $JSON_OUTPUT\""
elif [ -n "$DB_URI" ] || [ -n "$DB_USER" ] || [ -n "$DB_PASSWORD" ]; then
    echo "FEHLER: Wenn Datenbankparameter angegeben werden, müssen alle drei angegeben werden (URI, User, Password)"
    echo "Verwendung: ./analyze_top_substances.sh [database-uri] [database-user] [database-password] [suffix]"
    exit 1
else
    echo "Verwende Standard-Datenbankverbindung (aus Neo4j Desktop Konfiguration)"
    echo ""
fi

# Führe den Analyzer aus
eval $GRADLE_CMD

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✓ Analyse erfolgreich abgeschlossen!"
    echo ""
    echo "Erstelle Visualisierung..."
    
    # Führe Python-Skript zur Visualisierung aus
    if [ -f "scripts/plots/visualize_top_substances.py" ]; then
        cd scripts/plots
        if [ -d "venv" ]; then
            source venv/bin/activate
            # Übergib auch den Suffix für den Plot-Dateinamen
            if [ -n "$SUFFIX" ]; then
                python3 visualize_top_substances.py "../../medgraph/$JSON_OUTPUT" "$SUFFIX"
            else
                python3 visualize_top_substances.py "../../medgraph/$JSON_OUTPUT"
            fi
        else
            if [ -n "$SUFFIX" ]; then
                python3 visualize_top_substances.py "../../medgraph/$JSON_OUTPUT" "$SUFFIX"
            else
                python3 visualize_top_substances.py "../../medgraph/$JSON_OUTPUT"
            fi
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
