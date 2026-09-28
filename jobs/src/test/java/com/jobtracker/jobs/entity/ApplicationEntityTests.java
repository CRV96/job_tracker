package com.jobtracker.jobs.entity;

import java.time.Instant;
import java.time.LocalDate;

import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.dto.NewTimelineEvent;
import com.jobtracker.jobs.enums.ApplicationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * The entity's own rules, without a database. The service tests in the app module cover the rest.
 */
class ApplicationEntityTests {

	private static final LocalDate TODAY = LocalDate.of(2026, 9, 28);

	@Test
	void statusFollowsTheLatestEvent() {
		ApplicationEntity application = newApplication();

		application.addEvent(event(TODAY, ApplicationStatus.APPLIED));
		application.addEvent(event(TODAY.plusDays(2), ApplicationStatus.INTERVIEWING));
		application.addEvent(event(TODAY.minusDays(1), ApplicationStatus.SAVED));

		assertThat(application.getCurrentStatus()).isEqualTo(ApplicationStatus.INTERVIEWING);
		assertThat(application.getEvents()).hasSize(3);
	}

	@Test
	void eventAddedLastOnTheSameDateIsTheLatest() {
		ApplicationEntity application = newApplication();

		application.addEvent(event(TODAY, ApplicationStatus.APPLIED));
		application.addEvent(event(TODAY, ApplicationStatus.REJECTED));

		assertThat(application.getCurrentStatus()).isEqualTo(ApplicationStatus.REJECTED);
	}

	@Test
	void refusesAnEventWithoutItsRequiredFields() {
		assertThatExceptionOfType(NullPointerException.class)
			.isThrownBy(() -> NewTimelineEvent.builder().eventDate(TODAY).build())
			.withMessage("stageCategory");
	}

	@Test
	void timelineChangesOnlyThroughAddEvent() {
		ApplicationEntity application = newApplication();

		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> application.getEvents().clear());
	}

	private static ApplicationEntity newApplication() {
		NewApplication application = NewApplication.builder()
			.userId(1)
			.title("Backend engineer")
			.initialStatus(ApplicationStatus.APPLIED)
			.build();
		return new ApplicationEntity(application, null, Instant.parse("2026-09-28T10:00:00Z"));
	}

	private static NewTimelineEvent event(LocalDate date, ApplicationStatus stageCategory) {
		return NewTimelineEvent.builder().eventDate(date).stageCategory(stageCategory).build();
	}

}
