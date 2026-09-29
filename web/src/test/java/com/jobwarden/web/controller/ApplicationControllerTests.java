package com.jobwarden.web.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.jobwarden.common.exception.BusinessRuleException;
import com.jobwarden.identity.dto.User;
import com.jobwarden.jobs.dto.ApplicationDetails;
import com.jobwarden.jobs.dto.ApplicationSummary;
import com.jobwarden.jobs.dto.NewApplication;
import com.jobwarden.jobs.dto.NewTimelineEvent;
import com.jobwarden.jobs.dto.TimelineEvent;
import com.jobwarden.jobs.enums.ApplicationStatus;
import com.jobwarden.jobs.exception.JobsErrorCode;
import com.jobwarden.jobs.service.ApplicationService;
import com.jobwarden.web.user.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
					.eventDate(LocalDate.of(2026, 9, 21))
					.stageCategory(ApplicationStatus.APPLIED)
					.build(),
				TimelineEvent.builder()
					.id(2)
					.eventDate(LocalDate.of(2026, 9, 25))
					.eventTime(LocalTime.of(14, 30))
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
					"25 Sep 2026, 14:30", "21 Sep 2026", "Clear time",
					"hx-post=\"/applications/7/events\"", "id=\"request-error\"")
			.doesNotContain("21 Sep 2026, ");
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
			.contains("Add an application", "name=\"statusTime\"", "Clear time");
	}

	@Test
	void addsAnApplicationByHand() {
		given(this.applications.create(any())).willReturn(9L);

		assertThat(this.mvc.post()
			.uri("/applications")
			.param("title", " Platform engineer ")
			.param("company", "Initech")
			.param("link", "")
			.param("status", "APPLIED")
			.param("statusDate", "2026-09-12")
			.param("statusTime", "09:30")).hasRedirectedUrl("/applications/9");
		NewApplication expected = NewApplication.builder()
			.userId(1)
			.title("Platform engineer")
			.company("Initech")
			.initialStatus(ApplicationStatus.APPLIED)
			.initialStatusDate(LocalDate.of(2026, 9, 12))
			.initialStatusTime(LocalTime.of(9, 30))
			.build();
		then(this.applications).should().create(expected);
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
		then(this.applications).should(never()).create(any());
	}

	@Test
	void addsTheEventAndReturnsOnlyTheRefreshedFragment() {
		assertThat(this.mvc.post()
			.uri("/applications/7/events")
			.param("eventDate", "2026-09-28")
			.param("eventTime", "14:05")
			.param("stageCategory", "OFFER")
			.param("stageLabel", " Final call ")
			.param("note", "")).hasStatusOk().bodyText().contains("id=\"application\"").doesNotContain("<html");
		NewTimelineEvent expected = NewTimelineEvent.builder()
			.eventDate(LocalDate.of(2026, 9, 28))
			.eventTime(LocalTime.of(14, 5))
			.stageCategory(ApplicationStatus.OFFER)
			.stageLabel("Final call")
			.build();
		then(this.applications).should().addEvent(1, 7, expected);
	}

	@Test
	void leavesTheTimeOutWhenItIsCleared() {
		assertThat(this.mvc.post()
			.uri("/applications/7/events")
			.param("eventDate", "2026-09-28")
			.param("eventTime", "")
			.param("stageCategory", "OFFER")).hasStatusOk();

		ArgumentCaptor<NewTimelineEvent> event = ArgumentCaptor.captor();
		then(this.applications).should().addEvent(eq(1L), eq(7L), event.capture());
		assertThat(event.getValue().eventDate()).isEqualTo(LocalDate.of(2026, 9, 28));
		assertThat(event.getValue().eventTime()).isNull();
	}

	@Test
	void statusButtonsRecordTheMomentOfTheClick() {
		LocalTime before = LocalTime.now().withNano(0);

		assertThat(this.mvc.post().uri("/applications/7/events").param("stageCategory", "OFFER")).hasStatusOk();

		ArgumentCaptor<NewTimelineEvent> event = ArgumentCaptor.captor();
		then(this.applications).should().addEvent(eq(1L), eq(7L), event.capture());
		assertThat(event.getValue().eventDate()).isEqualTo(LocalDate.now());
		assertThat(event.getValue().eventTime()).isBetween(before, LocalTime.now());
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
			.param("rejectionReason", "OTHER")).hasStatusOk()
			.bodyText()
			.contains("A rejection reason can only be given when the status is Rejected.");
	}

	@Test
	void deletesAndSendsHtmxBackToTheList() {
		assertThat(this.mvc.delete().uri("/applications/7")).hasStatus(HttpStatus.NO_CONTENT)
			.headers()
			.hasValue("HX-Redirect", "/applications");
		then(this.applications).should().delete(1, 7);
	}

}
