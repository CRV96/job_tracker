package com.jobtracker.identity.exception;

public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException(long userId) {
		super("Profile " + userId + " not found");
	}

}
