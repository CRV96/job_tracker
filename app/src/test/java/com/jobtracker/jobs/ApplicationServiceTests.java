package com.jobtracker.jobs;

import java.time.LocalDate;

import com.jobtracker.TestcontainersConfiguration;
import com.jobtracker.common.exception.BusinessRuleException;
import com.jobtracker.jobs.dto.ApplicationDetails;
import com.jobtracker.jobs.dto.ApplicationSummary;
import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.dto.NewTimelineEvent;
import com.jobtracker.jobs.dto.TimelineEvent;
import com.jobtracker.jobs.dto.TrackedApplication;
import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.enums.RejectionReason;
import com.jobtracker.jobs.exception.ApplicationNotFoundException;
import com.jobtracker.jobs.exception.JobsErrorCode;
import com.jobtracker.jobs.service.ApplicationService;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.modulith.test.ApplicationModuleTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The jobs module starts here without identity, so the profiles that own the applications are inserted with SQL.
 */
@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class ApplicationServiceTests {

	private static final LocalDate TODAY = LocalDate.now();

	@Autowired
	private ApplicationService applications;

	@Autowired
	private JdbcClient jdbc;

	private long userId;

	private long otherUserId;

	@BeforeEach
	void createProfiles() {
		this.jdbc.sql("TRUNCATE users CASCADE").update();
		this.userId = insertProfile("Robert");
		this.otherUserId = insertProfile("Someone else");
	}

	@Test
	void startsTheTimelineWithTheInitialStatus() {
		long id = create(this.userId, "Backend engineer", ApplicationStatus.APPLIED);

		ApplicationDetails application = this.applications.findById(this.userId, id).orElseThrow();
		assertThat(application.title()).isEqualTo("Backend engineer");
		assertThat(application.status()).isEqualTo(ApplicationStatus.APPLIED);
		assertThat(application.capturedAt()).isNotNull();
		assertThat(application.timeline()).singleElement().satisfies(event -> {
			assertThat(event.eventDate()).isEqualTo(TODAY);
			assertThat(event.stageCategory()).isEqualTo(ApplicationStatus.APPLIED);
		});
	}

	@Test
	void datesTheFirstEventWithTheGivenDate() {
		long id = this.applications.create(newApplication(this.userId, "Backend engineer", ApplicationStatus.APPLIED),
				TODAY.minusDays(10));

		assertThat(this.applications.findById(this.userId, id).orElseThrow().timeline()).singleElement()
			.extracting(TimelineEvent::eventDate)
			.isEqualTo(TODAY.minusDays(10));
	}

	@Test
	void takesTheSourceFromTheLink() {
		long fromLinkedIn = this.applications.create(newApplication(this.userId, "From LinkedIn",
				ApplicationStatus.SAVED, "https://www.linkedin.com/jobs/view/42"), TODAY);
		long referral = this.applications.create(newApplication(this.userId, "Referral", ApplicationStatus.APPLIED,
				null), TODAY);

		assertThat(this.applications.findById(this.userId, fromLinkedIn).orElseThrow().source())
			.isEqualTo("linkedin.com");
		assertThat(this.applications.findById(this.userId, referral).orElseThrow()).satisfies(application -> {
			assertThat(application.link()).isNull();
			assertThat(application.source()).isNull();
		});
	}

	@Test
	void storesTheRawPayloadAsJson() {
		long id = create(this.userId, "Backend engineer", ApplicationStatus.SAVED);

		assertThat(this.jdbc.sql("SELECT raw_payload ->> '@type' FROM applications WHERE id = ?")
			.param(id)
			.query(String.class)
			.single()).isEqualTo("JobPosting");
	}

	@Test
	void listsNewestFirstAndFiltersByStatus() {
		long first = create(this.userId, "First", ApplicationStatus.SAVED);
		long second = create(this.userId, "Second", ApplicationStatus.APPLIED);
		long third = create(this.userId, "Third", ApplicationStatus.SAVED);

		assertThat(this.applications.findAll(this.userId, null)).extracting(ApplicationSummary::id)
			.containsExactly(third, second, first);
		assertThat(this.applications.findAll(this.userId, ApplicationStatus.SAVED)).extracting(ApplicationSummary::id)
			.containsExactly(third, first);
	}

	@Test
	void keepsEachProfilesApplicationsToItself() {
		long othersApplication = create(this.otherUserId, "Not yours", ApplicationStatus.APPLIED);
		NewTimelineEvent rejection = event(TODAY, ApplicationStatus.REJECTED);

		assertThat(this.applications.findAll(this.userId, null)).isEmpty();
		assertThat(this.applications.findById(this.userId, othersApplication)).isEmpty();
		assertThatExceptionOfType(ApplicationNotFoundException.class)
			.isThrownBy(() -> this.applications.addEvent(this.userId, othersApplication, rejection));
		assertThatExceptionOfType(ApplicationNotFoundException.class)
			.isThrownBy(() -> this.applications.delete(this.userId, othersApplication));
		assertThat(this.applications.findById(this.otherUserId, othersApplication)).get()
			.extracting(ApplicationDetails::status)
			.isEqualTo(ApplicationStatus.APPLIED);
	}

	@Test
	void statusFollowsTheLatestEvent() {
		long id = create(this.userId, "Backend engineer", ApplicationStatus.APPLIED);

		this.applications.addEvent(this.userId, id, event(TODAY.plusDays(2), ApplicationStatus.INTERVIEWING));
		// Dated before the latest event: it joins the timeline but doesn't change the status
		this.applications.addEvent(this.userId, id, event(TODAY.minusDays(3), ApplicationStatus.SAVED));

		ApplicationDetails application = this.applications.findById(this.userId, id).orElseThrow();
		assertThat(application.status()).isEqualTo(ApplicationStatus.INTERVIEWING);
		assertThat(application.timeline()).extracting(TimelineEvent::stageCategory)
			.containsExactly(ApplicationStatus.SAVED, ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEWING);
	}

	@Test
	void eventAddedLastOnTheSameDateIsTheLatest() {
		long id = create(this.userId, "Backend engineer", ApplicationStatus.SAVED);

		this.applications.addEvent(this.userId, id, event(TODAY, ApplicationStatus.APPLIED));

		ApplicationDetails application = this.applications.findById(this.userId, id).orElseThrow();
		assertThat(application.status()).isEqualTo(ApplicationStatus.APPLIED);
		assertThat(application.timeline()).extracting(TimelineEvent::stageCategory)
			.containsExactly(ApplicationStatus.SAVED, ApplicationStatus.APPLIED);
	}

	@Test
	void allowsARejectionReasonOnlyOnARejection() {
		long id = create(this.userId, "Backend engineer", ApplicationStatus.APPLIED);
		NewTimelineEvent interviewWithReason = NewTimelineEvent.builder()
			.eventDate(TODAY)
			.stageCategory(ApplicationStatus.INTERVIEWING)
			.rejectionReason(RejectionReason.OTHER)
			.build();

		assertThatThrownBy(() -> this.applications.addEvent(this.userId, id, interviewWithReason))
			.isInstanceOfSatisfying(BusinessRuleException.class, exception -> assertThat(exception.getErrorCode())
				.isEqualTo(JobsErrorCode.REJECTION_REASON_NOT_ALLOWED));

		this.applications.addEvent(this.userId, id, NewTimelineEvent.builder()
			.eventDate(TODAY)
			.stageCategory(ApplicationStatus.REJECTED)
			.stageLabel("After the final round")
			.note("They hired internally")
			.rejectionReason(RejectionReason.POSITION_FILLED)
			.build());

		ApplicationDetails application = this.applications.findById(this.userId, id).orElseThrow();
		assertThat(application.status()).isEqualTo(ApplicationStatus.REJECTED);
		assertThat(application.timeline()).hasSize(2).last().satisfies(event -> {
			assertThat(event.stageLabel()).isEqualTo("After the final round");
			assertThat(event.note()).isEqualTo("They hired internally");
			assertThat(event.rejectionReason()).isEqualTo(RejectionReason.POSITION_FILLED);
		});
	}

	@Test
	void deletesTheApplicationWithItsTimeline() {
		long id = create(this.userId, "Backend engineer", ApplicationStatus.APPLIED);
		this.applications.addEvent(this.userId, id, event(TODAY, ApplicationStatus.INTERVIEWING));

		this.applications.delete(this.userId, id);

		assertThat(this.applications.findById(this.userId, id)).isEmpty();
		assertThat(this.jdbc.sql("SELECT count(*) FROM application_events").query(Long.class).single()).isZero();
	}

	@Test
	void tracksALinkOncePerProfile() {
		TrackedApplication first = this.applications.track(newApplication(this.userId, "Backend engineer",
				ApplicationStatus.SAVED));
		TrackedApplication again = this.applications.track(newApplication(this.userId, "Backend engineer",
				ApplicationStatus.SAVED));
		TrackedApplication othersCopy = this.applications.track(newApplication(this.otherUserId, "Backend engineer",
				ApplicationStatus.SAVED));

		assertThat(first.created()).isTrue();
		assertThat(again).isEqualTo(new TrackedApplication(first.id(), false));
		assertThat(othersCopy.created()).isTrue();
		assertThat(this.applications.findAll(this.userId, null)).hasSize(1);
	}

	@Test
	void savingAFavoriteAgainMarksItApplied() {
		long id = this.applications.track(newApplication(this.userId, "Backend engineer", ApplicationStatus.SAVED))
			.id();

		this.applications.track(newApplication(this.userId, "Backend engineer", ApplicationStatus.APPLIED));

		ApplicationDetails application = this.applications.findById(this.userId, id).orElseThrow();
		assertThat(application.status()).isEqualTo(ApplicationStatus.APPLIED);
		assertThat(application.timeline()).extracting(TimelineEvent::stageCategory)
			.containsExactly(ApplicationStatus.SAVED, ApplicationStatus.APPLIED);
	}

	@Test
	void neverMergesApplicationsWithoutALink() {
		this.applications.track(newApplication(this.userId, "Referral", ApplicationStatus.APPLIED, null));
		this.applications.track(newApplication(this.userId, "Another referral", ApplicationStatus.APPLIED, null));

		assertThat(this.applications.findAll(this.userId, null)).hasSize(2);
	}

	@Test
	void capturingAgainNeverMovesAnApplicationBack() {
		long id = this.applications.track(newApplication(this.userId, "Backend engineer", ApplicationStatus.APPLIED))
			.id();
		this.applications.addEvent(this.userId, id, event(TODAY, ApplicationStatus.INTERVIEWING));

		this.applications.track(newApplication(this.userId, "Backend engineer", ApplicationStatus.SAVED));
		this.applications.track(newApplication(this.userId, "Backend engineer", ApplicationStatus.APPLIED));

		ApplicationDetails application = this.applications.findById(this.userId, id).orElseThrow();
		assertThat(application.status()).isEqualTo(ApplicationStatus.INTERVIEWING);
		assertThat(application.timeline()).hasSize(2);
	}

	private long insertProfile(String name) {
		return this.jdbc.sql("INSERT INTO users (name) VALUES (?) RETURNING id")
			.param(name)
			.query(Long.class)
			.single();
	}

	private long create(long userId, String title, ApplicationStatus initialStatus) {
		return this.applications.create(newApplication(userId, title, initialStatus), TODAY);
	}

	private static NewApplication newApplication(long userId, String title, ApplicationStatus initialStatus) {
		return newApplication(userId, title, initialStatus, "https://example.com/jobs/1");
	}

	private static NewApplication newApplication(long userId, String title, ApplicationStatus initialStatus,
			@Nullable String link) {
		return NewApplication.builder()
			.userId(userId)
			.title(title)
			.company("Acme")
			.description("Build things")
			.link(link)
			.location("Remote")
			.employmentType("Full-time")
			.rawPayload("{\"@type\": \"JobPosting\"}")
			.initialStatus(initialStatus)
			.build();
	}

	private static NewTimelineEvent event(LocalDate date, ApplicationStatus stageCategory) {
		return NewTimelineEvent.builder().eventDate(date).stageCategory(stageCategory).build();
	}

}
