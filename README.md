This is a Java tool meant for supporting the transformation of clinical medication-related documentation from the MMI Pharmindex
into a structured FHIR format. Using it requires a Neo4j database and the [MMI Pharmindex data](https://www.mmi.de/mmi-pharmindex/mmi-pharmindex-daten) to be available. (The latter is sadly not available for free, but that is also the reason I can't distribute it here.)

It currently comprises three core features:
- Setting up a knowledge graph containing data related to pharmaceutical products in a Neo4j database
- Using the knowledge graph to create FHIR **Medication**, **Substance**, and **Organization** instances
- Searching pharmaceutical products in the knowledge graph

For documentation on how to use, head over to the [wiki](https://github.com/medizininformatik-initiative/Medication-Graph-FHIR-Converter/wiki).


As of: January 22nd, 2025

This project was originially created as part of the Master's thesis of Markus Budeus during the winter semester 2023/24.

---

This project was created as part of the efforts of the [DIFUTURE](https://difuture.de/en/home-2/) consortium and funded by the BMFTR as part of the [Medical Informatics Initiative](https://www.medizininformatik-initiative.de/en/start). (Förderkennzeichen: **01ZZ2304A**)


<img src="https://www.difuture.de/wp-content/uploads/2024/06/cropped-DIFUTURE.png" width="450">&emsp;&emsp;&emsp;
<img src="https://www.medizininformatik-initiative.de/themes/custom/mii/assets/img/Logo_MII_270px_Hoehe_en.png" width="220">&emsp;&emsp;&emsp;
<img src="https://github.com/user-attachments/assets/8b2f8b7a-ee2e-42c5-8968-016ebfaa6cd8" width="180">
