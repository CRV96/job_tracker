package com.jobtracker.web.dto;

import java.time.LocalDate;

import com.jobtracker.jobs.dto.NewTimelineEvent;
import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.enums.RejectionReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import org.springframework.format.annotation.DateTimeFormat;

import static com.jobtracker.web.dto.FormValues.blankToNull;

public record NewEventForm(
		@NotNull(message = "Pick a date.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
		@NotNull(message = "Pick a stage.") ApplicationStatus stageCategory,
		@Size(max = NewEventForm.STAGE_LABEL_MAX_LENGTH, message = "The stage label can be at most {max} characters long.")
		@Nullable String stageLabel,
		@Nullable String note,
		@Nullable RejectionReason rejectionReason) {

	/** Also the {@code maxlength} of the stage field in applications/detail.html. */
	public static final int STAGE_LABEL_MAX_LENGTH = 200;

	public NewTimelineEvent toNewTimelineEvent() {
		return NewTimelineEvent.builder()
			.eventDate(this.eventDate)
			.stageCategory(this.stageCategory)
			.stageLabel(blankToNull(this.stageLabel))
			.note(blankToNull(this.note))
			.rejectionReason(this.rejectionReason)
			.build();
	}

}
