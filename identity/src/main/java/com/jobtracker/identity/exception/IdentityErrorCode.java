package com.jobtracker.identity.exception;

import com.jobtracker.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IdentityErrorCode implements ErrorCode {

	USER_NOT_FOUND("IDENTITY-001"),

	USER_NAME_TAKEN("IDENTITY-002");

	private final String code;

}
