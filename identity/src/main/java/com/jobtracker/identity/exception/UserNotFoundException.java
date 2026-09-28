package com.jobtracker.identity.exception;

import com.jobtracker.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {

	public UserNotFoundException(long userId) {
		super(IdentityErrorCode.USER_NOT_FOUND, "Profile " + userId + " not found");
	}

}
