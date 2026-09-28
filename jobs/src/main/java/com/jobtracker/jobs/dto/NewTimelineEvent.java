package com.jobtracker.jobs.dto;

import java.time.LocalDate;
import java.util.Objects;

import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.enums.RejectionReason;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Build it with {@link #builder()}; fields left out are {@code null}.
 *
 * @param rejectionReason only allowed when {@code stageCategory} is {@link ApplicationStatus#REJECTED}
 */
@Builder
public record NewTimelineEvent(
		LocalDate eventDate,
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
