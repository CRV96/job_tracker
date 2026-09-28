package com.jobtracker.common.exception;

import lombok.Getter;

/**
 * Base class for the errors the modules throw on purpose. Each carries an {@link ErrorCode}, which the exception
 * handlers in {@code web} and {@code capture} log and report. Modules throw one of the subclasses, whose type tells
 * the handlers which HTTP status to answer with.
 */
@Getter
public abstract class JobTrackerException extends RuntimeException {

	private final ErrorCode errorCode;

	protected JobTrackerException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

}
