This module contains the components used for matching the drugs in the knowledge graph to RxNorm Semantic Clinical
Drugs (SCDs). It is currently designed to be used on a regular basis. The main class is
[RxNormMatcher](src/main/java/de/medizininformatikinitiative/medgraph/rxnorm_matching/RxNormMatcher.java) and provides
a few features, like exporting the results to CSV or printing them. However, these features are enabled or disabled by
commenting in the relevant lines of code.

To use the RxNorm-Matcher, you need the knowledge graph ready for use, and you need the UMLS RxNorm dataset as SQLite
DB. For the latter, you cae use [rxnorm_to_sqlite](https://github.com/dmcalli2/rxnorm_to_sqlite). Don't forget to add
indices to the generated DB and ANALYZE it. You can use the
[RxNormDbChecks](src/main/java/de/medizininformatikinitiative/medgraph/rxnorm_matching/RxNormDbChecks.java) for that.