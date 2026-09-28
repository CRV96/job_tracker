package com.jobtracker.web.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.jobtracker.common.exception.BusinessRuleException;
import com.jobtracker.identity.dto.User;
import com.jobtracker.jobs.dto.ApplicationDetails;
import com.jobtracker.jobs.dto.ApplicationSummary;
import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.dto.NewTimelineEvent;
import com.jobtracker.jobs.dto.TimelineEvent;
import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.exception.JobsErrorCode;
import com.jobtracker.jobs.service.ApplicationService;
import com.jobtracker.web.user.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTests {

	private static final User ROBERT = new User(1, "Robert", Instant.parse("2026-09-28T10:00:00Z"));

	private static final ApplicationDetails APPLICATION = ApplicationDetails.builder()
		.id(7)
		.title("Backend engineer")
		.company("Acme")
		.description("Build things")
		.link("https://example.com/jobs/7")
		.location("Remote")
		.employmentType("Full-time")
		.source("example.com")
		.status(ApplicationStatus.INTERVIEWING)
		.capturedAt(Instant.parse("2026-09-20T10:00:00Z"))
		.timeline(List.of(
				TimelineEvent.builder()
					.id(1)
					.eventDate(LocalDate.of(2026, 9, 20))
					.stageCategory(ApplicationStatus.APPLIED)
					.build(),
				TimelineEvent.builder()
					.id(2)
					.eventDate(LocalDate.of(2026, 9, 25))
					.stageCategory(ApplicationStatus.INTERVIEWING)
					.stageLabel("Technical interview")
					.note("Went well")
					.build()))
		.build();

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private ApplicationService applications;

	@MockitoBean
	private CurrentUser currentUser;

	@BeforeEach
	void selectRobert() {
		given(this.currentUser.get()).willReturn(Optional.of(ROBERT));
		given(this.applications.findById(1, 7)).willReturn(Optional.of(APPLICATION));
	}

	@Test
	void sendsTheBrowserToTheProfilePickerWhenNoProfileIsSelected() {
		given(this.currentUser.get()).willReturn(Optional.empty());

		assertThat(this.mvc.get().uri("/applications")).hasRedirectedUrl("/profiles");
	}

	@Test
	void tellsHtmxToLoadTheProfilePickerWhenNoProfileIsSelected() {
		given(this.currentUser.get()).willReturn(Optional.empty());

		assertThat(this.mvc.delete().uri("/applications/7").header("HX-Request", "true")).hasStatusOk()
			.headers()
			.hasValue("HX-Redirect", "/profiles");
	}

	@Test
	void listsTheProfilesApplicationsWithTheChosenStatus() {
		ApplicationSummary summary = ApplicationSummary.builder()
			.id(7)
			.title("Backend engineer")
			.company("Acme")
			.location("Remote")
			.status(ApplicationStatus.INTERVIEWING)
			.capturedAt(Instant.now())
			.build();
		given(this.applications.findAll(1, ApplicationStatus.INTERVIEWING)).willReturn(List.of(summary));

		assertThat(this.mvc.get().uri("/applications").param("status", "INTERVIEWING")).hasStatusOk()
			.bodyText()
			.contains("Backend engineer", "Acme", "/applications/7");
	}

	@Test
	void showsTheApplicationWithItsTimeline() {
		assertThat(this.mvc.get().uri("/applications/7")).hasStatusOk()
			.bodyText()
			.contains("Backend engineer", "Technical interview", "Went well", "Lack of experience",
					"hx-post=\"/applications/7/events\"", "id=\"request-error\"");
	}

	@Test
	void answers404ForAnApplicationTheProfileDoesNotOwn() {
		given(this.applications.findById(1, 8)).willReturn(Optional.empty());

		assertThat(this.mvc.get().uri("/applications/8")).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void showsTheFormForAddingAnApplication() {
		assertThat(this.mvc.get().uri("/applications/new")).hasStatusOk()
			.hasViewName("applications/new")
			.bodyText()
			.contains("Add an application");
	}

	@Test
	void addsAnApplicationByHand() {
		given(this.applications.create(any(), any())).willReturn(9L);

		assertThat(this.mvc.post()
			.uri("/applications")
			.param("title", " Platform engineer ")
			.param("company", "Initech")
			.param("link", "")
			.param("status", "APPLIED")
			.param("statusDate", "2026-09-12")).hasRedirectedUrl("/applications/9");
		NewApplication expected = NewApplication.builder()
			.userId(1)
			.title("Platform engineer")
			.company("Initech")
			.initialStatus(ApplicationStatus.APPLIED)
			.build();
		then(this.applications).should().create(expected, LocalDate.of(2026, 9, 12));
	}

	@Test
	void showsTheFormAgainWithTheErrorsAndWhatWasTyped() {
		assertThat(this.mvc.post()
			.uri("/applications")
			.param("title", "")
			.param("link", "not a link")
			.param("status", "APPLIED")
			.param("statusDate", "2026-09-12")).hasStatusOk()
			.bodyText()
			.contains("Give the job a title.", "The link must be a full web address", "value=\"not a link\"");
		then(this.applications).should(never()).create(any(), any());
	}

	@Test
	void addsTheEventAndReturnsOnlyTheRefreshedFragment() {
		assertThat(this.mvc.post()
			.uri("/applications/7/events")
			.param("eventDate", "2026-09-28")
			.param("stageCategory", "OFFER")
			.param("stageLabel", " Final call ")
			.param("note", "")).hasStatusOk().bodyText().contains("id=\"application\"").doesNotContain("<html");
		NewTimelineEvent expected = NewTimelineEvent.builder()
			.eventDate(LocalDate.of(2026, 9, 28))
			.stageCategory(ApplicationStatus.OFFER)
			.stageLabel("Final call")
			.build();
		then(this.applications).should().addEvent(1, 7, expected);
	}

	@Test
	void showsAnInvalidEventOnTheEventForm() {
		assertThat(this.mvc.post()
			.uri("/applications/7/events")
			.param("eventDate", "2026-09-28")
			.param("stageCategory", "OFFER")
			.param("stageLabel", "x".repeat(201))).hasStatusOk()
			.bodyText()
			.contains("The stage label can be at most 200 characters long.");
		then(this.applications).should(never()).addEvent(anyLong(), anyLong(), any());
	}

	@Test
	void showsABrokenRuleOnTheEventForm() {
		willThrow(new BusinessRuleException(JobsErrorCode.REJECTION_REASON_NOT_ALLOWED, "Only on a rejection"))
			.given(this.applications)
			.addEvent(anyLong(), anyLong(), any());

		assertThat(this.mvc.post()
			.uri("/applications/7/events")
			.param("eventDate", "2026-09-28")
			.param("stageCategory", "OFFER")
			.param("rejectionReason", "OTHER")).hasStatusOk().bodyText().contains("Only on a rejection");
	}

	@Test
	void deletesAndSendsHtmxBackToTheList() {
		assertThat(this.mvc.delete().uri("/applications/7")).hasStatus(HttpStatus.NO_CONTENT)
			.headers()
			.hasValue("HX-Redirect", "/applications");
		then(this.applications).should().delete(1, 7);
	}

}
