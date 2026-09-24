package com.jobtracker.jobs.exception;

/**
 * The application doesn't exist, or it belongs to another profile. Both cases look the same to the caller,
 * so one profile can't discover another profile's applications.
 */
public class ApplicationNotFoundException extends RuntimeException {

	public ApplicationNotFoundException(long applicationId) {
		super("Application " + applicationId + " not found");
	}

}
