package com.jobtracker.jobs.exception;

import com.jobtracker.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobsErrorCode implements ErrorCode {

	APPLICATION_NOT_FOUND("JOBS-001"),

	REJECTION_REASON_NOT_ALLOWED("JOBS-002");

	private final String code;

}
