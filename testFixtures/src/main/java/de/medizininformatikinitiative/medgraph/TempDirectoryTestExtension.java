package de.medizininformatikinitiative.medgraph;

import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Test extension which supplies a temporary directory for each test. By specifying a {@link File} or {@link Path}
 * parameter on your test, setup, teardown or related methods, you can access this directory, as it will be injected
 * with this parameter.
 *
 * @author Markus Budeus
 */
public class TempDirectoryTestExtension implements Extension, BeforeEachCallback, AfterEachCallback, ParameterResolver {

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(TempDirectoryTestExtension.class);
	private static final String DIRECTORY_KEY = "tempdir";
	private static final String DIRECTORY_NAME = "de.medizininformatikinitiative.medgraph.test";

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		context.getStore(NAMESPACE).put(DIRECTORY_KEY, Files.createTempDirectory(DIRECTORY_NAME));
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
	throws ParameterResolutionException {
		return parameterContext.getParameter().getType() == File.class
				|| parameterContext.getParameter().getType() == Path.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
	throws ParameterResolutionException {
		Path path = (Path) extensionContext.getStore(NAMESPACE).get(DIRECTORY_KEY);
		if (parameterContext.getParameter().getType() == File.class) return path.toFile();
		return path;
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		ExtensionContext.Store store = context.getStore(NAMESPACE);
		Path dir = (Path) store.get(DIRECTORY_KEY);
		if (dir != null) {
			Files.walk(dir)
			     .sorted(Comparator.reverseOrder())
			     .map(Path::toFile)
			     .forEach(java.io.File::delete);

			store.remove(DIRECTORY_KEY);
		}
	}
}
