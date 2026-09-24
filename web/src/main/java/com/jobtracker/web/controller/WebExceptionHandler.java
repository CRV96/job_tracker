package com.jobtracker.web.controller;

import java.io.IOException;

import com.jobtracker.identity.exception.UserNotFoundException;
import com.jobtracker.jobs.exception.ApplicationNotFoundException;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Maps domain exceptions from the page controllers to HTTP statuses. Spring Boot then renders the status
 * with templates/error.html, the same page every other error uses.
 */
@ControllerAdvice(basePackageClasses = WebExceptionHandler.class)
class WebExceptionHandler {

	@ExceptionHandler({ ApplicationNotFoundException.class, UserNotFoundException.class })
	void notFound(HttpServletResponse response) throws IOException {
		response.sendError(HttpStatus.NOT_FOUND.value());
	}

}
