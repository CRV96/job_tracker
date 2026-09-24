package com.jobtracker.jobs.dto;

import java.time.LocalDate;

import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.enums.RejectionReason;
import org.jspecify.annotations.Nullable;

/**
 * @param rejectionReason only allowed when {@code stageCategory} is {@link ApplicationStatus#REJECTED}
 */
public record NewTimelineEvent(
		LocalDate eventDate,
		ApplicationStatus stageCategory,
		@Nullable String stageLabel,
		@Nullable String note,
		@Nullable RejectionReason rejectionReason) {
}
