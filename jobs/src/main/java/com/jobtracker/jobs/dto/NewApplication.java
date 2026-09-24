package com.jobtracker.jobs.dto;

import com.jobtracker.jobs.enums.ApplicationStatus;
import org.jspecify.annotations.Nullable;

/**
 * A job posting to start tracking.
 *
 * @param source where the posting came from, e.g. the site's domain
 * @param rawPayload everything that was captured, as JSON (e.g. the page's schema.org JobPosting data)
 */
public record NewApplication(
		long userId,
		String title,
		@Nullable String company,
		@Nullable String description,
		String link,
		@Nullable String location,
		@Nullable String salary,
		@Nullable String employmentType,
		@Nullable String source,
		@Nullable String rawPayload,
		ApplicationStatus initialStatus) {
}
