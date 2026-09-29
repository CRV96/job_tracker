package com.jobwarden.jobs.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.jobwarden.jobs.enums.ApplicationStatus;
import com.jobwarden.jobs.enums.RejectionReason;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Something that happened during an application: a stage change, an interview, a note to remember.
 *
 * @param eventTime the time of day, when it's known
 */
@Builder
public record TimelineEvent(
		long id,
		LocalDate eventDate,
		@Nullable LocalTime eventTime,
		ApplicationStatus stageCategory,
		@Nullable String stageLabel,
		@Nullable String note,
		@Nullable RejectionReason rejectionReason) {
}
