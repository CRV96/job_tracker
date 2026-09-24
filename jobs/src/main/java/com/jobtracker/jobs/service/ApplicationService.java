package com.jobtracker.jobs.service;

import java.util.List;
import java.util.Optional;

import com.jobtracker.jobs.dto.ApplicationDetails;
import com.jobtracker.jobs.dto.ApplicationSummary;
import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.dto.NewTimelineEvent;
import com.jobtracker.jobs.enums.ApplicationStatus;
import org.jspecify.annotations.Nullable;

/**
 * Every method takes the owning user's id, so one profile can never read or change another's data.
 */
public interface ApplicationService {

	/**
	 * @return the new application's id
	 */
	long create(NewApplication application);

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
