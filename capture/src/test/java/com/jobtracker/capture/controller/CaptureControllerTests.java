package com.jobtracker.capture.controller;

import com.jobtracker.identity.service.UserService;
import com.jobtracker.jobs.service.ApplicationService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(CaptureController.class)
class CaptureControllerTests {

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private ApplicationService applications;

	@MockitoBean
	private UserService users;

	@Test
	void rejectsCaptureWithoutTitleAsProblemDetail() {
		assertThat(this.mvc.post()
			.uri("/api/captures")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{ "userId": 1, "action": "SAVE", "link": "https://example.com/job" }
					"""))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
	}

}
