package com.jobtracker.web.dto;

import java.time.LocalDate;

import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.enums.RejectionReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

public record NewEventForm(
		@NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
		@NotNull ApplicationStatus stageCategory,
		@Size(max = 200) String stageLabel,
		String note,
		RejectionReason rejectionReason) {
}
