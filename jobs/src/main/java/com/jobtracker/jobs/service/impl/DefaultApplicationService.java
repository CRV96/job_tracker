package com.jobtracker.jobs.service.impl;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.jobtracker.common.exception.BusinessRuleException;
import com.jobtracker.jobs.dto.ApplicationDetails;
import com.jobtracker.jobs.dto.ApplicationSummary;
import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.dto.NewTimelineEvent;
import com.jobtracker.jobs.dto.TrackedApplication;
import com.jobtracker.jobs.entity.ApplicationEntity;
import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.exception.ApplicationNotFoundException;
import com.jobtracker.jobs.exception.JobsErrorCode;
import com.jobtracker.jobs.mapper.ApplicationMapper;
import com.jobtracker.jobs.repository.ApplicationRepository;
import com.jobtracker.jobs.service.ApplicationService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@CustomLog
@RequiredArgsConstructor
class DefaultApplicationService implements ApplicationService {

	private static final String WWW_PREFIX = "www.";

	private final ApplicationRepository applications;

	@Override
	@Transactional
	public long create(NewApplication application, LocalDate initialStatusDate) {
		ApplicationEntity entity = new ApplicationEntity(application, sourceOf(application.link()), Instant.now());
		// Every timeline starts with the initial status, so it always has a first entry
		entity.addEvent(statusChange(initialStatusDate, application.initialStatus()));
		long id = this.applications.save(entity).getId();
		log.info("Created application {} for profile {} as {}", id, application.userId(), application.initialStatus());
		return id;
	}

	@Override
	@Transactional
	public TrackedApplication track(NewApplication application) {
		String link = application.link();
		Optional<ApplicationEntity> existing = (link != null)
				? this.applications.findFirstByUserIdAndLink(application.userId(), link) : Optional.empty();

		if (existing.isEmpty()) {
			log.debug("No existing application for profile {} and link {}", application.userId(), link);
			return new TrackedApplication(create(application, LocalDate.now()), true);
		}

		ApplicationEntity tracked = existing.get();
		log.debug("Link already tracked as application {}", tracked.getId());

		if (tracked.getCurrentStatus() == ApplicationStatus.SAVED
				&& application.initialStatus() != ApplicationStatus.SAVED) {
			tracked.addEvent(statusChange(LocalDate.now(), application.initialStatus()));

			log.info("Application {} captured again, moved from SAVED to {}", tracked.getId(),
					tracked.getCurrentStatus());
		}

		return new TrackedApplication(tracked.getId(), false);
	}

	@Override
	public List<ApplicationSummary> findAll(long userId, @Nullable ApplicationStatus status) {
		List<ApplicationSummary> found = (status != null) ? this.applications.findSummaries(userId, status)
				: this.applications.findSummaries(userId);
		log.debug("Found {} applications for profile {} (status: {})", found.size(), userId,
				(status != null) ? status : "any");
		return found;
	}

	@Override
	public Optional<ApplicationDetails> findById(long userId, long applicationId) {
		Optional<ApplicationDetails> application = this.applications.findByIdAndUserId(applicationId, userId)
			.map(ApplicationMapper::toApplicationDetails);
		log.debug("Application {} for profile {} {}", applicationId, userId,
				application.isPresent() ? "found" : "not found");
		return application;
	}

	/**
	 * @throws ApplicationNotFoundException if the user has no such application
	 * @throws BusinessRuleException if the event has a rejection reason but isn't a rejection
	 */
	@Override
	@Transactional
	public void addEvent(long userId, long applicationId, NewTimelineEvent event) {
		ApplicationEntity application = getOwnedApplication(userId, applicationId);
		requireRejectionReasonOnlyOnRejection(event);
		ApplicationStatus previousStatus = application.getCurrentStatus();
		application.addEvent(event);
		if (application.getCurrentStatus() != previousStatus) {
			log.info("Application {} moved from {} to {}", applicationId, previousStatus,
					application.getCurrentStatus());
		} else {
			log.debug("Added a {} event to application {}; its status stays {}", event.stageCategory(),
					applicationId, previousStatus);
		}
	}

	/**
	 * @throws ApplicationNotFoundException if the user has no such application
	 */
	@Override
	@Transactional
	public void delete(long userId, long applicationId) {
		this.applications.delete(getOwnedApplication(userId, applicationId));
		log.info("Deleted application {}", applicationId);
	}

	/**
	 * The site the posting came from, e.g. {@code linkedin.com}.
	 */
	private static @Nullable String sourceOf(@Nullable String link) {
		if (link == null) {
			return null;
		}
		try {
			String host = URI.create(link).getHost();
			return (host != null && host.startsWith(WWW_PREFIX)) ? host.substring(WWW_PREFIX.length()) : host;
		} catch (IllegalArgumentException exception) {
			// Not a link Java can parse. The source is optional, so the application is saved without one.
			return null;
		}
	}

	/**
	 * An event that only records a new status, with no label or note.
	 */
	private static NewTimelineEvent statusChange(LocalDate date, ApplicationStatus status) {
		return NewTimelineEvent.builder().eventDate(date).stageCategory(status).build();
	}

	private static void requireRejectionReasonOnlyOnRejection(NewTimelineEvent event) {
		if (event.rejectionReason() != null && event.stageCategory() != ApplicationStatus.REJECTED) {
			throw new BusinessRuleException(JobsErrorCode.REJECTION_REASON_NOT_ALLOWED,
					"A rejection reason is only allowed on a REJECTED event, not on " + event.stageCategory());
		}
	}

	private ApplicationEntity getOwnedApplication(long userId, long applicationId) {
		return this.applications.findByIdAndUserId(applicationId, userId)
			.orElseThrow(() -> new ApplicationNotFoundException(applicationId));
	}

}
