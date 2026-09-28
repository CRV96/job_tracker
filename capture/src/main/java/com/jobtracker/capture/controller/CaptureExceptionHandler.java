package com.jobtracker.capture.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.jobtracker.common.exception.BusinessRuleException;
import com.jobtracker.common.exception.CommonErrorCode;
import com.jobtracker.common.exception.ErrorCode;
import com.jobtracker.common.exception.JobTrackerException;
import com.jobtracker.common.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Returns the capture API's errors as RFC 9457 problem details (JSON), which the extension can show the user.
 * The base class already does this for Spring MVC's own errors, such as a request that fails validation (400).
 * <p>
 * The modules' exceptions are expected, so they're logged once as warnings. Anything else is a bug or an outage:
 * logged as an error with its code and stack trace, and answered with 500. Every response carries an error code,
 * under {@link ErrorCode#PROPERTY_NAME}, including Spring MVC's own errors.
 */
@RestControllerAdvice(basePackageClasses = CaptureExceptionHandler.class)
@CustomLog
class CaptureExceptionHandler extends ResponseEntityExceptionHandler {

	// The details stay in the log; the extension only gets this and the code
	private static final String UNEXPECTED_ERROR_DETAIL = "Something went wrong on the server";

	private static final String FIELD_ERRORS_PROPERTY = "errors";

	@ExceptionHandler(NotFoundException.class)
	ProblemDetail notFound(NotFoundException exception) {
		return fromModuleException(HttpStatus.NOT_FOUND, exception);
	}

	@ExceptionHandler(BusinessRuleException.class)
	ProblemDetail businessRuleBroken(BusinessRuleException exception) {
		return fromModuleException(HttpStatus.BAD_REQUEST, exception);
	}

	/**
	 * Spring MVC's own errors never get here: the base class has a more specific handler for each of them.
	 */
	@ExceptionHandler(Exception.class)
	ProblemDetail unexpected(Exception exception, HttpServletRequest request) {
		log.error(CommonErrorCode.UNEXPECTED_ERROR, "Unexpected error on {} {}", request.getMethod(),
				request.getRequestURI(), exception);
		return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_DETAIL, CommonErrorCode.UNEXPECTED_ERROR);
	}

	/**
	 * Adds which fields failed and why, e.g. {@code "link": "must be a valid URL"}, so the extension can say what to fix.
	 */
	@Override
	protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError error : exception.getBindingResult().getFieldErrors()) {
			fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		exception.getBody().setProperty(FIELD_ERRORS_PROPERTY, fieldErrors);
		return super.handleMethodArgumentNotValid(exception, headers, status, request);
	}

	/**
	 * Every response of the base class passes through here: gives Spring MVC's own errors a code too, so the extension
	 * always gets one.
	 */
	@Override
	protected ResponseEntity<Object> createResponseEntity(@Nullable Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		if (body instanceof ProblemDetail problem
				&& (problem.getProperties() == null || !problem.getProperties().containsKey(ErrorCode.PROPERTY_NAME))) {
			ErrorCode errorCode = statusCode.is5xxServerError() ? CommonErrorCode.UNEXPECTED_ERROR
					: CommonErrorCode.INVALID_REQUEST;
			problem.setProperty(ErrorCode.PROPERTY_NAME, errorCode.getCode());
		}
		return super.createResponseEntity(body, headers, statusCode, request);
	}

	/**
	 * The modules' messages are written for users, so the extension can show them as they are.
	 */
	private static ProblemDetail fromModuleException(HttpStatus status, JobTrackerException exception) {
		log.warn("{}", exception.getMessage());
		return problemDetail(status, exception.getMessage(), exception.getErrorCode());
	}

	private static ProblemDetail problemDetail(HttpStatus status, String detail, ErrorCode errorCode) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setProperty(ErrorCode.PROPERTY_NAME, errorCode.getCode());
		return problem;
	}

}
