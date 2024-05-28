package de.medizininformatikinitiative.medgraph.ui

import org.junit.jupiter.api.extension.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Test extension which supplies a temporary directory for each test. The directory is injected as parameter.
 *
 * @author Markus Budeus
 */
class TempDirectoryTestExtension : Extension, BeforeEachCallback, AfterEachCallback, ParameterResolver {

    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create(TempDirectoryTestExtension::class.java)
        private val DIRECTORY_KEY = "tempdir"
        private val DIRECTORY_NAME = "de.medizininformatikinitiative.medgraph.test"
    }

    override fun beforeEach(context: ExtensionContext) {
        context.getStore(NAMESPACE)
            .put(DIRECTORY_KEY, Files.createTempDirectory(DIRECTORY_NAME))
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == File::class.java
                || parameterContext.parameter.type == Path::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        val path: Path = extensionContext.getStore(NAMESPACE).get(DIRECTORY_KEY) as Path
        if (parameterContext.parameter.type == File::class.java) return path.toFile()
        return path
    }

    override fun afterEach(context: ExtensionContext) {
        val store = context.getStore(NAMESPACE)
        val dir: Path? = store.get(DIRECTORY_KEY) as? Path
        if (dir != null) {
            Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete)

            store.remove(DIRECTORY_KEY)
        }
    }

}