package de.tum.med.aiim.markusbudeus.fhirvalidator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RandomOrderFileSet implements Supplier<File> {

	private int index = 0;
	private final File[] files;

	public RandomOrderFileSet(File directory, Random random) {
		this(directory, (dir, name) -> true, random);
	}

	public RandomOrderFileSet(File directory, FilenameFilter filter, Random random) {
		List<File> files = Arrays.stream(Objects.requireNonNull(directory.listFiles(filter))).collect(Collectors.toList());
		Collections.shuffle(files, random);
		this.files = files.toArray(new File[0]);
	}

	public File getNext() {
		return files[index++];
	}

	@Override
	public File get() {
		return getNext();
	}

	public int size() {
		return files.length;
	}
}
