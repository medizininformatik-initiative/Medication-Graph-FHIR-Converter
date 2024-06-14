package de.medizininformatikinitiative.medgraph.ui.licenses

class LicenseProvider {

    val licenses: List<License>

    init {
        licenses = listOf(
            License(
                "EDQM Standard Terms",
                "https://standardterms.edqm.eu/",
                """
                The EDQM Standard Terms data (in the knowledge graph, all nodes with the EDQM label) is from the  EDQM Standard Terms database (http://standardterms.edqm.eu) and is reproduced with the permission of the European Directorate for the Quality of Medicines & HealthCare, Council of Europe (EDQM).
                
                The date on which the data was retrieved can be seen in the EDQM's CodingSystem node. Please be aware the EDQM Standard Terms database is not a static list and content can change over time.
                """.trimIndent()
            ),
            License(
                "Compose Multiplatform",
                "https://github.com/JetBrains/compose-multiplatform",
                DefaultLicense.APACHE_2_0,
                "JetBrains s.r.o. and and respective authors and developers.",
                "2020-2021"
            ),
            License("Neo4j Java Driver", "https://github.com/neo4j/neo4j-java-driver", DefaultLicense.APACHE_2_0),
            License("Voyager", "https://github.com/adrielcafe/voyager", DefaultLicense.MIT, "Adriel Café", "2021"),
            License(
                "Kotlin Coroutines",
                "https://github.com/Kotlin/kotlinx.coroutines",
                DefaultLicense.APACHE_2_0,
                "JetBrains s.r.o. and Kotlin Programming Language contributors.",
                "2000-2020"
            ),
            License(
                "JetBrains Java Annotations",
                "https://github.com/JetBrains/java-annotations",
                DefaultLicense.APACHE_2_0,
                "JetBrains s.r.o.",
                "2000-2016"
            ),
            License("Apache Commons", "https://commons.apache.org", DefaultLicense.APACHE_2_0),
            License("Gson", "https://github.com/google/gson", DefaultLicense.APACHE_2_0, "Google Inc.", "2008-2011"),
            License("OpenCSV", "https://opencsv.sourceforge.net", DefaultLicense.APACHE_2_0)
        )
    }

}
