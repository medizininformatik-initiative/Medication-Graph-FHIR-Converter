package de.medizininformatikinitiative.medgraph.commandline;

import org.apache.commons.cli.CommandLine;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Alias for the {@link HeadlessFileFhirExporter} which is kept for backward compatibility.
 *
 * @author Markus Budeus
 */
public class LegacyHeadlessFileFhirExporter extends HeadlessFileFhirExporter {

	@Override
	public @NotNull ExitStatus invoke(CommandLine commandLine, List<String> args) {
		System.out.println("The command to invoke the FHIR export to files has been changed from \"export\" to \"export-json\". " +
				"Please update your scripts, as this command may be removed or reassigned in a future version.");
		return super.invoke(commandLine, args);
	}

	@Override
	public String getCallArgument() {
		return "export";
	}

}
