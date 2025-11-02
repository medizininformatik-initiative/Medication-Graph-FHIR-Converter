import requests 


resp = requests.get("https://rxnav.nlm.nih.gov/REST/rxcui/171212/properties?format=json").json()
dose_forms = [p["propValue"].lower() for p in resp["propConceptGroup"]["propConcept"]]
print(dose_forms[:20])


