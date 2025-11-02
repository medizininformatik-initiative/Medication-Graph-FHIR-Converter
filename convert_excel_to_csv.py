#!/usr/bin/env python3
"""
Script to convert EDQM-RX.xlsx to edqm_rxnorm_dose_form_mapping.csv
"""

import pandas as pd
import os

def convert_excel_to_csv():
   
    excel_path = "medgraph/src/main/resources/EDQM-RX.xlsx"
    csv_path = "medgraph/src/main/resources/edqm_rxnorm_dose_form_mapping.csv"
    
    # Prüfen ob Excel-Datei existiert
    if not os.path.exists(excel_path):
        print(f"Excel-Datei nicht gefunden: {excel_path}")
        return False
    
    try:
        # Excel einlesen - nur das Sheet mit allen Daten verwenden
        print(f"Lese Excel-Datei: {excel_path}")
        xls = pd.ExcelFile(excel_path)
        
        # Nur das Sheet "Ontology + EDQM + RxNorm" verwenden
        target_sheet = "Ontology + EDQM + RxNorm"
        if target_sheet not in xls.sheet_names:
            print(f"❌ Sheet '{target_sheet}' nicht gefunden!")
            print(f"Verfügbare Sheets: {xls.sheet_names}")
            return False
            
        print(f"  - Verarbeite Sheet: {target_sheet}")
        data = pd.read_excel(xls, sheet_name=target_sheet)
        print(f"✅ {len(data)} Zeilen aus Sheet '{target_sheet}' gelesen")
        
        # Die erste Zeile als Header verwenden (enthält die echten Spaltennamen)
        data.columns = data.iloc[0]
        data = data.drop(data.index[0]).reset_index(drop=True)
        print(f"Header-Zeile als Spaltennamen verwendet")
        
        # Doppelte Spalten entfernen (behalte nur die ersten Vorkommen)
        data = data.loc[:, ~data.columns.duplicated()]
        print(f"Doppelte Spalten entfernt")
        
        # Geforderte Spalten definieren
        required_columns = [
            "edqm_dose_form",
            "rxnorm_dose_form", 
            "has_trac_code",
            "has_trac_term",
            "has_rca_code",
            "has_rca_term",
            "has_amec_code",
            "has_amec_term",
            "has_isic_code",
            "has_isic_term"
        ]
        
        # Aktuelle Spalten anzeigen
        print(f"📋 Aktuelle Spalten: {list(data.columns)}")
        
        # Spalten-Mapping basierend auf dem "Ontology + EDQM + RxNorm" Sheet
        column_mapping = {
            'edqm_dose_form': 'EDQM DOSAGE FORMS',
            'rxnorm_dose_form': 'RXNORM DOSAGE FORMS',
            'has_trac_code': 'has_trac_code',
            'has_trac_term': 'has_trac_term',
            'has_rca_code': 'has_rca_code',
            'has_rca_term': 'has_rca_term',
            'has_amec_code': 'has_amec_code',
            'has_amec_term': 'has_amec_term',
            'has_isic_code': 'has_isic_code',
            'has_isic_term': 'has_isic_split_term'  # Diese Spalte enthält die ISIC Terms
        }
        
        print(f"🔍 Spalten-Mapping gefunden: {column_mapping}")
        
        # Neue DataFrame mit korrekten Spalten erstellen
        new_data = pd.DataFrame()
        
        for required_col in required_columns:
            if required_col in column_mapping:
                # Verwende die gemappte Spalte
                new_data[required_col] = data[column_mapping[required_col]]
            else:
                # Leere Spalte hinzufügen
                new_data[required_col] = ""
        
        data = new_data
        
        # Leere Zeilen entfernen und nur Zeilen mit EDQM oder RxNorm Dose Forms behalten
        data = data.dropna(how='all')
        
        # Nur Zeilen behalten, die entweder EDQM oder RxNorm Dose Forms haben
        data = data[
            (data['edqm_dose_form'].notna() & (data['edqm_dose_form'] != '')) |
            (data['rxnorm_dose_form'].notna() & (data['rxnorm_dose_form'] != ''))
        ]
        
        # NaN-Werte durch leere Strings ersetzen
        data = data.fillna('')
        
        # CSV speichern
        print(f"Speichere CSV: {csv_path}")
        data.to_csv(csv_path, index=False, encoding="utf-8")
        
        print(f"✅ Konvertierung erfolgreich!")
        print(f"   - {len(data)} Zeilen verarbeitet")
        print(f"   - CSV gespeichert: {csv_path}")
        
        # Erste paar Zeilen anzeigen
        print("\n Erste 5 Zeilen der CSV:")
        print(data.head().to_string())
        
        return True
        
    except Exception as e:
        print(f"❌ Fehler bei der Konvertierung: {e}")
        return False

if __name__ == "__main__":
    convert_excel_to_csv()
