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


# Wechsle zum Projekt-Root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/../.."

# Setze Java 21 für Gradle (der Code wurde mit Java 21 kompiliert)
export JAVA_HOME="/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home"

# Optionale Datenbankverbindungsparameter
DB_URI="${1:-}"
DB_USER="${2:-}"
DB_PASSWORD="${3:-}"
SUFFIX="${4:-}"

# JSON Output-Datei
if [ -n "$SUFFIX" ]; then
    JSON_OUTPUT="output/analysis/top_substances_analysis_${SUFFIX}.json"
else
    JSON_OUTPUT="output/analysis/top_substances_analysis.json"
fi

echo "Starte Top 20 Substanzen Analyse..."
echo "  JSON Output: $JSON_OUTPUT"
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
    echo "[OK] Analyse erfolgreich abgeschlossen!"
    echo ""
    echo "Erstelle Visualisierung..."
    
    # Führe Python-Skript zur Visualisierung aus
    if [ -f "scripts/plots/visualize_top_substances.py" ]; then
        # Bestimme die beiden JSON-Dateien für den Vergleich
        JSON_PAPER="output/analysis/top_substances_analysis.json"
        JSON_PROPOSED="output/analysis/top_substances_analysis_custom_mapping.json"
        
        # Wenn ein Suffix angegeben wurde, versuche die Standard-Datei und die Datei mit Suffix zu finden
        if [ -n "$SUFFIX" ]; then
            JSON_PROPOSED="output/analysis/top_substances_analysis_${SUFFIX}.json"
        fi
        
        # Prüfe, ob beide Dateien existieren
        if [ ! -f "$JSON_PAPER" ] && [ ! -f "$JSON_PROPOSED" ]; then
            echo "  Warnung: Keine JSON-Dateien für Visualisierung gefunden."
            echo "  Erwartet: $JSON_PAPER oder $JSON_PROPOSED"
        elif [ ! -f "$JSON_PAPER" ]; then
            echo "  Warnung: Paper Mapping JSON nicht gefunden: $JSON_PAPER"
            echo "  Visualisierung übersprungen (benötigt zwei JSON-Dateien für Vergleich)"
        elif [ ! -f "$JSON_PROPOSED" ]; then
            echo "  Warnung: Proposed Mapping JSON nicht gefunden: $JSON_PROPOSED"
            echo "  Visualisierung übersprungen (benötigt zwei JSON-Dateien für Vergleich)"
        else
            cd scripts/plots
            if [ -d "venv" ]; then
                source venv/bin/activate 2>/dev/null || true
            fi
            
            # Führe Visualisierung mit beiden JSON-Dateien aus
            python3 visualize_top_substances.py "../../$JSON_PAPER" "../../$JSON_PROPOSED"
            VIS_EXIT_CODE=$?
            
            cd ../..
            
            if [ $VIS_EXIT_CODE -eq 0 ]; then
                echo "  [OK] Visualisierung erfolgreich erstellt!"
            else
                echo "  [FEHLER] Visualisierung fehlgeschlagen (Exit-Code: $VIS_EXIT_CODE)"
            fi
        fi
    else
        echo "  Python-Skript nicht gefunden. Visualisierung übersprungen."
    fi
else
    echo ""
    echo "[FEHLER] Analyse fehlgeschlagen (Exit-Code: $EXIT_CODE)"
    exit $EXIT_CODE
fi
