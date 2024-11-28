This is a Java tool meant for supporting the transformation of clinical medication-related documentation from the MMI Pharmindex
into a structured FHIR format. Using it requires a Neo4j database and the [MMI Pharmindex data](https://www.mmi.de/mmi-pharmindex/mmi-pharmindex-daten) to be available. (The latter is sadly not available for free, but that is also the reason I can't distribute it here.)

It currently comprises three core features:
- Setting up a knowledge graph containing data related to pharmaceutical products in a Neo4j database
- Using the knowledge graph to create FHIR **Medication**, **Substance**, and **Organization** instances
- Searching pharmaceutical products in the knowledge graph

For documentation on how to use, head over to the [wiki](https://github.com/medizininformatik-initiative/Medication-Graph-FHIR-Converter/wiki).


As of: July 3rd, 2024

This project was originially created as part of the Master's thesis of Markus Budeus during the winter semester 2023/24.

---

### Regarding the license:
I know that people are waiting for me to set up a license for this project so other people can
use it without running into legal issues. I am on it, but unfortunately, I need to make sure the license
is in line with MII/DIFUTURE and TUM/MRI regulations.

It is planned to provide the source code under the MIT license, whereas the binaries will likely be restricted to the Apache 2.0 license requirements, as this is required by included dependencies.
Sadly, a new licensing issue has popped up on August 1st which requires me to investigate this issue AGAIN :(
I am currently busy with performing required prerequisites which I must adhere to before I can assign a license. I am sadly not currently able to make a reasonable accurate prediction on when the license will be ready.

As of: September 9th, 2024

---

This project was created as part of the efforts of the [DIFUTURE](https://difuture.de/en/home-2/) consortium and funded by the BMBF as part of the [Medical Informatics Initiative](https://www.medizininformatik-initiative.de/en/start). (Förderkennzeichen: **01ZZ2304A**)


<img src="https://difuture.de/wp-content/uploads/2024/06/cropped-DIFUTURE.png" width="450">&emsp;&emsp;&emsp;
<img src="https://www.medizininformatik-initiative.de/themes/custom/mii/assets/img/Logo_MII_270px_Hoehe_en.png" width="220">&emsp;&emsp;&emsp;
<img src="https://github.com/user-attachments/assets/7b7339f0-6f6d-4978-9a60-98ab86c1a310" width="180">
