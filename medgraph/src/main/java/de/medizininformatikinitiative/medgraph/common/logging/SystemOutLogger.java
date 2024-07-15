package de.medizininformatikinitiative.medgraph.common.logging;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple {@link Logger}-implementation.
 *
 * @author Markus Budeus
 */
class SystemOutLogger implements Logger {

	static final LoggerFactory FACTORY = new LoggerFactory() {
		@Override
		public @NotNull Logger getLogger(Class<?> clazz) {
			return new SystemOutLogger(clazz);
		}
	};

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd hh:mm:ss");
	private final String tag;

	public SystemOutLogger(Class<?> clazz) {
		this(clazz.getSimpleName());
	}

	public SystemOutLogger(String tag) {
		this.tag = tag;
	}

	@Override
	public void log(Level level, String message) {
		System.out.println(parse(level, message));
	}

	@Override
	public void log(Level level, String message, Throwable throwable) {
		log(level, message);
		throwable.printStackTrace(System.out);
	}

	private String parse(Level level, String message) {
		return FORMATTER.format(LocalDateTime.now()) + " " + tag + " " + level + ": " + message;
	}

}
