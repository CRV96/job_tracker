package com.jobtracker.jobs.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.jobtracker.jobs.dto.ApplicationDetails;
import com.jobtracker.jobs.dto.ApplicationSummary;
import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.dto.NewTimelineEvent;
import com.jobtracker.jobs.dto.TrackedApplication;
import com.jobtracker.jobs.enums.ApplicationStatus;
import org.jspecify.annotations.Nullable;

/**
 * Every method takes the owning user's id, so one profile can never read or change another's data.
 */
public interface ApplicationService {

	/**
	 * Starts tracking a job posting. Its timeline starts with an event for the initial status.
	 *
	 * @param initialStatusDate the date of that first event, e.g. when you applied
	 * @return the new application's id
	 */
	long create(NewApplication application, LocalDate initialStatusDate);

	/**
	 * Starts tracking a job posting, unless the profile already tracks the same link. Then nothing is duplicated:
	 * the existing application is returned, and if it's still {@link ApplicationStatus#SAVED} it moves to the
	 * posting's initial status. So a favorite you've since applied to becomes {@code APPLIED}; an application
	 * that's further along never moves back. Without a link, it's always a new application.
	 */
	TrackedApplication track(NewApplication application);

	/**
	 * @param status only return applications with this status, or all of them when {@code null}
	 */
	List<ApplicationSummary> findAll(long userId, @Nullable ApplicationStatus status);

	Optional<ApplicationDetails> findById(long userId, long applicationId);

	/**
	 * Adds an event to the timeline. The application's status follows its latest event.
	 */
	void addEvent(long userId, long applicationId, NewTimelineEvent event);

	void delete(long userId, long applicationId);

}
