package com.jobtracker.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Codes for errors that don't belong to one module.
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

	/** A bug or an outage, e.g. the database is down: anything the modules didn't throw on purpose. */
	UNEXPECTED_ERROR("COMMON-001"),

	/** The request itself is wrong, e.g. invalid JSON or a required field missing, so no module ever saw it. */
	INVALID_REQUEST("COMMON-002");

	private final String code;

}
