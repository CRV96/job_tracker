package com.jobtracker.capture.controller;

import com.jobtracker.identity.service.UserService;
import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.dto.TrackedApplication;
import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@WebMvcTest(CaptureController.class)
class CaptureControllerTests {

	private static final String CAPTURE = """
			{
			  "userId": 1,
			  "action": "SAVE",
			  "title": "Backend engineer",
			  "company": "Acme",
			  "link": "https://www.linkedin.com/jobs/view/42",
			  "rawPayload": { "@type": "JobPosting" }
			}
			""";

	@Autowired
	private MockMvcTester mvc;

	@Value("${jobtracker.capture.path}")
	private String capturePath;

	@MockitoBean
	private ApplicationService applications;

	@MockitoBean
	private UserService users;

	@Test
	void rejectsCaptureWithoutTitleAsProblemDetail() {
		assertThat(this.mvc.post()
			.uri(this.capturePath)
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{ "userId": 1, "action": "SAVE", "link": "https://example.com/job" }
					"""))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyJson()
			.satisfies(body -> {
				assertThat(body).extractingPath("$.errorCode").isEqualTo("COMMON-002");
				assertThat(body).extractingPath("$.errors.title").isEqualTo("must not be blank");
			});
	}

	@Test
	void givesMalformedJsonAnErrorCodeToo() {
		assertThat(this.mvc.post().uri(this.capturePath).contentType(MediaType.APPLICATION_JSON).content("{ not json"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.extractingPath("$.errorCode")
			.isEqualTo("COMMON-002");
	}

	@Test
	void rejectsALinkThatIsNotAWebAddress() {
		assertThat(this.mvc.post()
			.uri(this.capturePath)
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{ "userId": 1, "action": "SAVE", "title": "Backend engineer", "link": "not a link" }
					"""))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
	}

	@Test
	void rejectsAnUnknownProfile() {
		given(this.users.exists(1)).willReturn(false);

		assertThat(this.mvc.post().uri(this.capturePath).contentType(MediaType.APPLICATION_JSON).content(CAPTURE))
			.hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.extractingPath("$.errorCode")
			.isEqualTo("IDENTITY-001");
	}

	@Test
	void tracksTheCapturedPosting() {
		given(this.users.exists(1)).willReturn(true);
		given(this.applications.track(any())).willReturn(new TrackedApplication(7, true));

		assertThat(this.mvc.post().uri(this.capturePath).contentType(MediaType.APPLICATION_JSON).content(CAPTURE))
			.hasStatus(HttpStatus.CREATED)
			.bodyJson()
			.extractingPath("$.applicationId")
			.isEqualTo(7);

		ArgumentCaptor<NewApplication> tracked = ArgumentCaptor.captor();
		then(this.applications).should().track(tracked.capture());
		assertThat(tracked.getValue()).satisfies(application -> {
			assertThat(application.title()).isEqualTo("Backend engineer");
			assertThat(application.link()).isEqualTo("https://www.linkedin.com/jobs/view/42");
			assertThat(application.rawPayload()).isEqualTo("{\"@type\":\"JobPosting\"}");
			assertThat(application.initialStatus()).isEqualTo(ApplicationStatus.APPLIED);
		});
	}

	@Test
	void answers200WithTheExistingApplicationForALinkAlreadyTracked() {
		given(this.users.exists(1)).willReturn(true);
		given(this.applications.track(any())).willReturn(new TrackedApplication(7, false));

		assertThat(this.mvc.post().uri(this.capturePath).contentType(MediaType.APPLICATION_JSON).content(CAPTURE))
			.hasStatusOk()
			.bodyJson()
			.extractingPath("$.applicationId")
			.isEqualTo(7);
	}

}
