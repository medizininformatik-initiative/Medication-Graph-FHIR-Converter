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

# Setze Java 21 für Gradle (der Code wurde mit Java 21 kompiliert)
export JAVA_HOME="/Users/lucy/Library/Java/JavaVirtualMachines/corretto-21.0.5/Contents/Home"

# Parameter mit Standardwerten
DB_URI="${1:-bolt://localhost:7687}"
DB_USER="${2:-neo4j}"
DB_PASSWORD="${3:-7o7MP~8_)h~0}"

echo "Starte EDQM Dose Form Mapping Analyse..."
echo "  Datenbank URI: $DB_URI"
echo "  Benutzer: $DB_USER"
echo ""

# Führe die Analyse direkt mit Gradle aus
./gradlew :medgraph:runAnalyzeDoseFormMapping --args="$DB_URI $DB_USER $DB_PASSWORD"

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✓ Analyse erfolgreich abgeschlossen!"
else
    echo ""
    echo "✗ Analyse fehlgeschlagen (Exit-Code: $EXIT_CODE)"
    exit $EXIT_CODE
fi

