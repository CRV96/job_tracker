package com.jobwarden.jobs.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import com.jobwarden.jobs.enums.ApplicationStatus;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * A job posting to start tracking. Build it with {@link #builder()}; fields left out are {@code null}.
 *
 * @param link the posting's address. Optional for applications added by hand; the application's source (the
 * site's domain, e.g. {@code linkedin.com}) is taken from it.
 * @param rawPayload everything that was captured, as JSON (e.g. the page's schema.org JobPosting data)
 * @param initialStatusDate when the initial status was set, e.g. when you applied; it starts the timeline
 * @param initialStatusTime the time of day, when it's known
 */
@Builder
public record NewApplication(
		long userId,
		String title,
		@Nullable String company,
		@Nullable String description,
		@Nullable String link,
		@Nullable String location,
		@Nullable String salary,
		@Nullable String employmentType,
		@Nullable String rawPayload,
		ApplicationStatus initialStatus,
		LocalDate initialStatusDate,
		@Nullable LocalTime initialStatusTime) {

	/**
	 * A builder can't make sure every required field is set, so a missing one fails here, right away.
	 */
	public NewApplication {
		Objects.requireNonNull(title, "title");
		Objects.requireNonNull(initialStatus, "initialStatus");
		Objects.requireNonNull(initialStatusDate, "initialStatusDate");
	}

}
