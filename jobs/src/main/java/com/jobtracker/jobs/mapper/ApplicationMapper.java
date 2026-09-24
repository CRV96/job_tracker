package com.jobtracker.jobs.mapper;

import com.jobtracker.jobs.dto.ApplicationDetails;
import com.jobtracker.jobs.dto.ApplicationSummary;
import com.jobtracker.jobs.dto.TimelineEvent;
import com.jobtracker.jobs.entity.ApplicationEntity;
import com.jobtracker.jobs.entity.TimelineEventEntity;

/**
 * Converts entities to the records the service returns. Entities never leave the module.
 */
public final class ApplicationMapper {

	private ApplicationMapper() {
	}

	public static ApplicationSummary toApplicationSummary(ApplicationEntity application) {
		return new ApplicationSummary(application.getId(), application.getTitle(), application.getCompany(),
				application.getLocation(), application.getCurrentStatus(), application.getCapturedAt());
	}

	/**
	 * Reads the application's events, so call it inside the service's transaction.
	 */
	public static ApplicationDetails toApplicationDetails(ApplicationEntity application) {
		return new ApplicationDetails(application.getId(), application.getTitle(), application.getCompany(),
				application.getDescription(), application.getLink(), application.getLocation(),
				application.getSalary(), application.getEmploymentType(), application.getSource(),
				application.getCurrentStatus(), application.getCapturedAt(),
				application.getEvents().stream().map(ApplicationMapper::toTimelineEvent).toList());
	}

	public static TimelineEvent toTimelineEvent(TimelineEventEntity event) {
		return new TimelineEvent(event.getId(), event.getEventDate(), event.getStageCategory(),
				event.getStageLabel(), event.getNote(), event.getRejectionReason());
	}

}
