### Regarding the license:
I know that people are waiting for me to set up a license for this project so other people can
use it without running into legal issues. I am on it, but unfortunately, I need to make sure the license
is in line with MII/DIFUTURE and TUM/MRI regulations, which might take a while... :(

As of: June 14th, 2024

---

This is a Java tool meant for supporting the transformation of clinical medication-related documentation
into a structured FHIR format. Using it requires a Neo4j database to be available.

It currently comprises three core features:
- Setting up a knowledge graph containing data related to pharmaceutical products in a Neo4j database
- Using the knowledge graph to create FHIR **Medication**, **Substance**, and **Organization** instances
- Searching pharmaceutical products in the knowledge graph

As of: April 3, 2024

This project was originially created as part of the Master's thesis of Markus Budeus during the winter semester 2023/24.
