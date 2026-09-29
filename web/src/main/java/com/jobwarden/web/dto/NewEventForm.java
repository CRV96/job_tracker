package com.jobwarden.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.jobwarden.jobs.dto.NewTimelineEvent;
import com.jobwarden.jobs.enums.ApplicationStatus;
import com.jobwarden.jobs.enums.RejectionReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import org.springframework.format.annotation.DateTimeFormat;

import static com.jobwarden.web.dto.FormValues.blankToNull;
import static com.jobwarden.web.dto.FormValues.momentOrNow;

/**
 * @param eventDate from the add-event form; the status buttons send none, which means now, with the time
 * @param eventTime optional: left out when cleared
 */
public record NewEventForm(
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate eventDate,
		@DateTimeFormat(iso = DateTimeFormat.ISO.TIME) @Nullable LocalTime eventTime,
		@NotNull(message = "{validation.event.stage.required}") ApplicationStatus stageCategory,
		@Size(max = NewEventForm.STAGE_LABEL_MAX_LENGTH, message = "{validation.event.stageLabel.size}")
		@Nullable String stageLabel,
		@Nullable String note,
		@Nullable RejectionReason rejectionReason) {

	/** Also the {@code maxlength} of the stage field in applications/detail.html. */
	public static final int STAGE_LABEL_MAX_LENGTH = 200;

	public NewTimelineEvent toNewTimelineEvent() {
		FormValues.Moment moment = momentOrNow(this.eventDate, this.eventTime);
		return NewTimelineEvent.builder()
			.eventDate(moment.date())
			.eventTime(moment.time())
			.stageCategory(this.stageCategory)
			.stageLabel(blankToNull(this.stageLabel))
			.note(blankToNull(this.note))
			.rejectionReason(this.rejectionReason)
			.build();
	}

}
