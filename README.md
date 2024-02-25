This is a Java tool meant for supporting the transformation of clinical medication-related documentation
into a structured FHIR format. Using it requires a Neo4j database to be available.

It currently comprises three core features:
- Setting up a knowledge graph containing data related to pharmaceutical products in a Neo4j database
- Using the knowledge graph to create FHIR **Medication**, **Substance**, and **Organization** instances
- Searching pharmaceutical products in the knowledge graph

As of: Februrary 25, 2024

This project was originially created as part of the Master's thesis of Markus Budeus during the winter semester 2023/24.