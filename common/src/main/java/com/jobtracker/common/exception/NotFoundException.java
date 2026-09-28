package com.jobtracker.common.exception;

/**
 * What the caller asked for doesn't exist, or belongs to another profile. Answered with 404.
 * Each module subclasses it once per resource, e.g. {@code ApplicationNotFoundException}.
 */
public abstract class NotFoundException extends JobTrackerException {

	protected NotFoundException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

}
