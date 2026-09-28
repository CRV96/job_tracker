package com.jobtracker.common.exception;

/**
 * An error identified by a stable code, such as {@code JOBS-001}, that shows up in the logs and in error responses.
 * <p>
 * Each module defines its own codes as an enum in its {@code exception} package (e.g. {@code JobsErrorCode}),
 * so this module never has to know about another module's errors.
 */
public interface ErrorCode {

	/**
	 * What the code is called in structured (JSON) logs and in error responses, so the two can be matched up.
	 */
	String PROPERTY_NAME = "errorCode";

	/**
	 * @return the module's prefix and a number, e.g. {@code JOBS-001}. Once a code is in use, never change or reuse it:
	 * people search the logs for it.
	 */
	String getCode();

}
