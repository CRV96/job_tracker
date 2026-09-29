package com.jobwarden.jobs.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import com.jobwarden.jobs.enums.ApplicationStatus;
import com.jobwarden.jobs.enums.RejectionReason;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Build it with {@link #builder()}; fields left out are {@code null}.
 *
 * @param eventDate when it happened: when the status was set, or when the interview took place
 * @param eventTime the time of day, when it's known
 * @param rejectionReason only allowed when {@code stageCategory} is {@link ApplicationStatus#REJECTED}
 */
@Builder
public record NewTimelineEvent(
		LocalDate eventDate,
		@Nullable LocalTime eventTime,
		ApplicationStatus stageCategory,
		@Nullable String stageLabel,
		@Nullable String note,
		@Nullable RejectionReason rejectionReason) {

	/**
	 * A builder can't make sure every required field is set, so a missing one fails here, right away.
	 */
	public NewTimelineEvent {
		Objects.requireNonNull(eventDate, "eventDate");
		Objects.requireNonNull(stageCategory, "stageCategory");
	}

}
