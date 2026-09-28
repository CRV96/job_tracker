package com.jobtracker.common.exception;

/**
 * The request breaks one of the domain's rules, e.g. a profile name that's already taken. Answered with 400.
 * Thrown as is, with the module's code for the rule, so a new rule doesn't need a new exception class:
 * <pre>{@code throw new BusinessRuleException(JobsErrorCode.REJECTION_REASON_NOT_ALLOWED, "...");}</pre>
 */
public class BusinessRuleException extends JobTrackerException {

	public BusinessRuleException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

}
