package com.jobtracker.jobs.dto;

import java.time.LocalDate;

import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.enums.RejectionReason;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Something that happened during an application: a stage change, an interview, a note to remember.
 */
@Builder
public record TimelineEvent(
		long id,
		LocalDate eventDate,
		ApplicationStatus stageCategory,
		@Nullable String stageLabel,
		@Nullable String note,
		@Nullable RejectionReason rejectionReason) {
}
