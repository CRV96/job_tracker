package com.jobtracker.jobs.dto;

import java.time.Instant;
import java.util.List;

import com.jobtracker.jobs.enums.ApplicationStatus;
import org.jspecify.annotations.Nullable;

/**
 * Everything shown on an application's detail page.
 *
 * @param timeline events in date order, oldest first
 */
public record ApplicationDetails(
		long id,
		String title,
		@Nullable String company,
		@Nullable String description,
		String link,
		@Nullable String location,
		@Nullable String salary,
		@Nullable String employmentType,
		@Nullable String source,
		ApplicationStatus status,
		Instant capturedAt,
		List<TimelineEvent> timeline) {
}
