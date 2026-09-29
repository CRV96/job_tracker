package com.jobwarden.common.exception;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Base class for the errors the modules throw on purpose. Each carries an {@link ErrorCode}, which the exception
 * handlers in {@code web} and {@code capture} log and report. Modules throw one of the subclasses, whose type tells
 * the handlers which HTTP status to answer with.
 * <p>
 * The exception's own message is English, for the logs. Users see the code's message in their language instead
 * (see {@link ErrorCode#getMessageKey()}), with {@link #getArguments()} filling its placeholders.
 */
@Getter
public abstract class JobWardenException extends RuntimeException {

	private final ErrorCode errorCode;

	@Getter(AccessLevel.NONE)
	private final Object[] arguments;

	/**
	 * @param message in English, for the logs; leave out personal data such as names
	 * @param arguments the values for the {@code {0}}, {@code {1}}... placeholders of the code's message
	 */
	protected JobWardenException(ErrorCode errorCode, String message, Object... arguments) {
		super(message);
		this.errorCode = errorCode;
		this.arguments = arguments.clone();
	}

	public Object[] getArguments() {
		return this.arguments.clone();
	}

}
