package de.medizininformatikinitiative.medgraph.commandline;

import java.util.OptionalInt;

/**
 * Simple CLI entrypoint that delegates to {@link CommandLineExecutor}.
 */
public final class MedgraphCliLauncher {

    public static void main(String[] args) {
        CommandLineExecutor executor = new CommandLineExecutor();
        OptionalInt exit = executor.evaluateAndExecuteCommandLineArguments(args);
        if (exit.isPresent()) {
            System.exit(exit.getAsInt());
        }
        // If no CLI utility was selected, print help and exit with error to avoid starting GUI in headless run
        System.out.println("Usage: medgraph [populate|export] [options]\nUse -h for help.");
        System.exit(1);
    }
}


