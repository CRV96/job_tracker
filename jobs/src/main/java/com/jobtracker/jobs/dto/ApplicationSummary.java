package com.jobtracker.jobs.dto;

import java.time.Instant;

import com.jobtracker.jobs.enums.ApplicationStatus;
import org.jspecify.annotations.Nullable;

/**
 * One row of the application list.
 */
public record ApplicationSummary(
		long id,
		String title,
		@Nullable String company,
		@Nullable String location,
		ApplicationStatus status,
		Instant capturedAt) {
}
