package de.medizininformatikinitiative.medgraph.common.utils;

public class MemoryAnalyzer {
    public static void logMemoryUsage(String stage) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("[" + stage + "] Used Memory: " + (usedMemory / (1024 * 1024)) + " MB");
    }
}
