package com.jobtracker.common.logging;

import com.jobtracker.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.CallerBoundaryAware;
import org.slf4j.spi.LoggingEventBuilder;

/**
 * The application's logger: SLF4J plus error codes. Put Lombok's {@code @CustomLog} on a class to get a
 * {@code log} field of this type (set up in the root {@code lombok.config}).
 * <p>
 * Each class gets its own instance, named after the class, so log levels can still be set per package, e.g.
 * {@code logging.level.com.jobtracker.jobs=DEBUG}. Messages use SLF4J's {@code {}} placeholders, and an exception
 * passed as the last argument is logged with its stack trace:
 * <pre>{@code log.error(JobsErrorCode.SAVE_FAILED, "Could not save application {}", id, exception);}</pre>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppLogger {

	private static final String FQCN = AppLogger.class.getName();

	private static final String ERROR_CODE_KEY = "errorCode";

	private final Logger delegate;

	public static AppLogger of(Class<?> type) {
		return new AppLogger(LoggerFactory.getLogger(type));
	}

	public void debug(String format, Object... args) {
		log(Level.DEBUG, null, format, args);
	}

	public void info(String format, Object... args) {
		log(Level.INFO, null, format, args);
	}

	public void warn(String format, Object... args) {
		log(Level.WARN, null, format, args);
	}

	public void warn(ErrorCode errorCode, String format, Object... args) {
		log(Level.WARN, errorCode, format, args);
	}

	public void error(String format, Object... args) {
		log(Level.ERROR, null, format, args);
	}

	public void error(ErrorCode errorCode, String format, Object... args) {
		log(Level.ERROR, errorCode, format, args);
	}

	private void log(Level level, @Nullable ErrorCode errorCode, String format, Object... args) {
		// Does nothing (and costs next to nothing) when the level is turned off
		LoggingEventBuilder event = this.delegate.atLevel(level);
		// Otherwise Logback would report this class as the caller (class, method, line) instead of the one that logged
		if (event instanceof CallerBoundaryAware callerBoundaryAware) {
			callerBoundaryAware.setCallerBoundary(FQCN);
		}
		String message = format;
		if (errorCode != null) {
			// In the message for the console, and as its own field for structured (JSON) logs
			event.addKeyValue(ERROR_CODE_KEY, errorCode.getCode());
			message = "[" + errorCode.getCode() + "] " + format;
		}
		event.log(message, args);
	}

}
