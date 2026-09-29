package com.jobwarden.capture.controller;

import com.jobwarden.common.exception.BusinessRuleException;
import com.jobwarden.identity.exception.UserNotFoundException;
import com.jobwarden.jobs.exception.JobsErrorCode;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

// Spring Boot leaves a test's nested classes out of component scanning, so the controller is imported
@WebMvcTest(CaptureExceptionHandlerTests.ThrowingController.class)
@Import(CaptureExceptionHandlerTests.ThrowingController.class)
class CaptureExceptionHandlerTests {

	@Autowired
	private MockMvcTester mvc;

	@Test
	void answersNotFoundAsProblemDetailWithErrorCode() {
		assertThat(this.mvc.get().uri("/test/unknown-profile")).hasStatus(HttpStatus.NOT_FOUND)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyJson()
			.extractingPath("$.errorCode")
			.isEqualTo("IDENTITY-001");
	}

	@Test
	void answersBrokenBusinessRuleAsBadRequestWithErrorCode() {
		assertThat(this.mvc.get().uri("/test/broken-rule")).hasStatus(HttpStatus.BAD_REQUEST)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyJson()
			.extractingPath("$.errorCode")
			.isEqualTo("JOBS-002");
	}

	@Test
	void answersUnexpectedErrorAs500WithoutItsDetails() {
		assertThat(this.mvc.get().uri("/test/unexpected")).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyJson()
			.satisfies(body -> {
				assertThat(body).extractingPath("$.errorCode").isEqualTo("COMMON-001");
				assertThat(body).extractingPath("$.detail").isEqualTo("Something went wrong on the server.");
			});
	}

	@Test
	void keepsTheStatusOfSpringMvcErrors() {
		assertThat(this.mvc.get().uri("/test/numbers/not-a-number")).hasStatus(HttpStatus.BAD_REQUEST)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
	}

	/**
	 * Throws what the modules throw, so the handler can be tested before the capture endpoint is implemented.
	 */
	@RestController
	static class ThrowingController {

		@GetMapping("/test/unknown-profile")
		void unknownProfile() {
			throw new UserNotFoundException(99);
		}

		@GetMapping("/test/broken-rule")
		void brokenRule() {
			throw new BusinessRuleException(JobsErrorCode.REJECTION_REASON_NOT_ALLOWED, "Not a rejection");
		}

		@GetMapping("/test/unexpected")
		void unexpected() {
			throw new IllegalStateException("The database is down");
		}

		@GetMapping("/test/numbers/{number}")
		void number(@PathVariable long number) {
		}

	}

}
