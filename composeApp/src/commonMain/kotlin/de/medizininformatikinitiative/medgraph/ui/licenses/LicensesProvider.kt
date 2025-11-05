package de.medizininformatikinitiative.medgraph.ui.licenses

import de.medizininformatikinitiative.medgraph.graphdbpopulator.CodingSystem
import de.medizininformatikinitiative.medgraph.ui.Application
import java.time.format.DateTimeFormatter

class LicenseProvider {

    /**
     * The license of the project code itself.
     */
    val license: License

    /**
     * The licenses of dependencies.
     */
    val dependencyLicenses: List<License>

    init {
        license = License(
            Application.NAME,
            "https://github.com/medizininformatik-initiative/Medication-Graph-FHIR-Converter",
            DefaultLicense.MIT,
            "Chair of Medical Informatics, TUM University Hospital",
            "2023-2025"
        )
        dependencyLicenses = listOf(
            License(
                "HAPI FHIR",
                "https://hapifhir.io/",
                DefaultLicense.APACHE_2_0,
                notice = """
                    Copyright 2015, University Health Network
                    Licensed under the Apache License, Version 2.0 (the "License");
                    you may not use this work except in compliance with the License.
                    You may obtain a copy of the License at

                    http://www.apache.org/licenses/LICENSE-2.0

                    Unless required by applicable law or agreed to in writing, software
                    distributed under the License is distributed on an "AS IS" BASIS,
                    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                    See the License for the specific language governing permissions and
                    limitations under the License.
                """.trimIndent()
            ),
            License(
                "EDQM Standard Terms",
                "https://standardterms.edqm.eu/",
                """
                The EDQM Standard Terms data (in the knowledge graph, all nodes with the EDQM label) is from the  EDQM Standard Terms database (http://standardterms.edqm.eu) and is reproduced with the permission of the European Directorate for the Quality of Medicines & HealthCare, Council of Europe (EDQM).
                
                The EDQM Standard Terms data was retrieved on """
                    .trimIndent()
                        + DateTimeFormatter.ISO_LOCAL_DATE.format(CodingSystem.EDQM.dateOfRetrieval)
                        + ". Please be aware the EDQM Standard Terms database is not a static list and content can change over time."
            ),
            License(
                "Compose Multiplatform",
                "https://github.com/JetBrains/compose-multiplatform",
                DefaultLicense.APACHE_2_0,
                "JetBrains s.r.o. and and respective authors and developers.",
                "2020-2021"
            ),
            License(
                "Neo4j Java Driver", "https://github.com/neo4j/neo4j-java-driver", DefaultLicense.APACHE_2_0,
                notice = """
                    Copyright (c) "Neo4j"
                    Neo4j Sweden AB [https://neo4j.com]

                    This file is part of Neo4j.

                    Licensed under the Apache License, Version 2.0 (the "License");
                    you may not use this file except in compliance with the License.

                    Unless required by applicable law or agreed to in writing, software
                    distributed under the License is distributed on an "AS IS" BASIS,
                    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                    See the License for the specific language governing permissions and
                    limitations under the License.

                    Full license texts are found in LICENSES.txt.


                    Third-party licenses
                    --------------------
                """.trimIndent()
            ),
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
            License("Apache Commons CLI", "https://commons.apache.org/proper/commons-cli/", DefaultLicense.APACHE_2_0),
            License("Gson", "https://github.com/google/gson", DefaultLicense.APACHE_2_0, "Google Inc.", "2008-2011"),
            License("OpenCSV", "https://opencsv.sourceforge.net", DefaultLicense.APACHE_2_0),
            License("Apache Log4j 2", "https://logging.apache.org/log4j/2.x/", DefaultLicense.APACHE_2_0),
        )
    }

}
