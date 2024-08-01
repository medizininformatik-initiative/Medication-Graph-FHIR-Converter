### Regarding the license:
I know that people are waiting for me to set up a license for this project so other people can
use it without running into legal issues. I am on it, but unfortunately, I need to make sure the license
is in line with MII/DIFUTURE and TUM/MRI regulations.

It is planned to provide the source code under the MIT license, whereas the binaries will likely be restricted to the Apache 2.0 license requirements, as this is required by included dependencies.
Sadly, a new licensing issue has popped up on August 1st which requires me to investigate this issue AGAIN :(
I am currently waiting for confirmation from the MRI and hope I can provide a license and a binary by the mid of August.

As of: August 1st, 2024

---

This is a Java tool meant for supporting the transformation of clinical medication-related documentation
into a structured FHIR format. Using it requires a Neo4j database and the MMI Pharmindex data to be available.

It currently comprises three core features:
- Setting up a knowledge graph containing data related to pharmaceutical products in a Neo4j database
- Using the knowledge graph to create FHIR **Medication**, **Substance**, and **Organization** instances
- Searching pharmaceutical products in the knowledge graph

For documentation on how to use, head over to the [wiki](https://github.com/medizininformatik-initiative/Medication-Graph-FHIR-Converter/wiki).


As of: July 3rd, 2024

This project was originially created as part of the Master's thesis of Markus Budeus during the winter semester 2023/24.
