package com.jobtracker.capture.controller;

import com.jobtracker.identity.exception.UserNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Returns the capture API's errors as RFC 9457 problem details (JSON), which the extension can show the user.
 * The base class already does this for Spring MVC's own errors, such as a request that fails validation (400).
 */
@RestControllerAdvice(basePackageClasses = CaptureExceptionHandler.class)
class CaptureExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(UserNotFoundException.class)
	ProblemDetail userNotFound(UserNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

}
