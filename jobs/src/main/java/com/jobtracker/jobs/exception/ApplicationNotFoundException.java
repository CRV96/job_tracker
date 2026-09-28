package com.jobtracker.jobs.exception;

import com.jobtracker.common.exception.NotFoundException;

/**
 * The application doesn't exist, or it belongs to another profile. Both cases look the same to the caller,
 * so one profile can't discover another profile's applications.
 */
public class ApplicationNotFoundException extends NotFoundException {

	public ApplicationNotFoundException(long applicationId) {
		super(JobsErrorCode.APPLICATION_NOT_FOUND, "Application " + applicationId + " not found");
	}

}
