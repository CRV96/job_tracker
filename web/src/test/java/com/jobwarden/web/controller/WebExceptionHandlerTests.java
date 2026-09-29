package com.jobwarden.web.controller;

import com.jobwarden.common.exception.BusinessRuleException;
import com.jobwarden.identity.enums.IdentityErrorCode;
import com.jobwarden.jobs.exception.ApplicationNotFoundException;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.assertj.core.api.Assertions.assertThat;

// Spring Boot leaves a test's nested classes out of component scanning, so the controller is imported
@WebMvcTest(WebExceptionHandlerTests.ThrowingController.class)
@Import(WebExceptionHandlerTests.ThrowingController.class)
class WebExceptionHandlerTests {

	@Autowired
	private MockMvcTester mvc;

	@Test
	void answersNotFoundWith404() {
		assertThat(this.mvc.get().uri("/test/unknown-application")).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void answersBrokenBusinessRuleWith400() {
		assertThat(this.mvc.get().uri("/test/broken-rule")).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void answersUnexpectedErrorWith500() {
		assertThat(this.mvc.get().uri("/test/unexpected")).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	void keepsTheStatusOfSpringMvcErrors() {
		assertThat(this.mvc.get().uri("/test/numbers/not-a-number")).hasStatus(HttpStatus.BAD_REQUEST);
	}

	/**
	 * Throws what the modules throw, so the handler can be tested before the page controllers are implemented.
	 */
	@Controller
	static class ThrowingController {

		@GetMapping("/test/unknown-application")
		String unknownApplication() {
			throw new ApplicationNotFoundException(99);
		}

		@GetMapping("/test/broken-rule")
		String brokenRule() {
			throw new BusinessRuleException(IdentityErrorCode.USER_NAME_TAKEN, "Name taken");
		}

		@GetMapping("/test/unexpected")
		String unexpected() {
			throw new IllegalStateException("The database is down");
		}

		@GetMapping("/test/numbers/{number}")
		String number(@PathVariable long number) {
			return "unused";
		}

	}

}
