package com.jobwarden.identity.exception;

import com.jobwarden.common.exception.NotFoundException;
import com.jobwarden.identity.enums.IdentityErrorCode;

public class UserNotFoundException extends NotFoundException {

	public UserNotFoundException(long userId) {
		// As text, so the message shows 1000 and not 1,000
		super(IdentityErrorCode.USER_NOT_FOUND, "Profile " + userId + " not found", String.valueOf(userId));
	}

}
