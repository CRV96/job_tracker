package com.jobwarden.jobs.exception;

import com.jobwarden.common.exception.NotFoundException;

/**
 * The application doesn't exist, or it belongs to another profile. Both cases look the same to the caller,
 * so one profile can't discover another profile's applications.
 */
public class ApplicationNotFoundException extends NotFoundException {

	public ApplicationNotFoundException(long applicationId) {
		// As text, so the message shows 1000 and not 1,000
		super(JobsErrorCode.APPLICATION_NOT_FOUND, "Application " + applicationId + " not found",
				String.valueOf(applicationId));
	}

}
