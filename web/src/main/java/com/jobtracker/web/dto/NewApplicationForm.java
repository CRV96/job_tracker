package com.jobtracker.web.dto;

import java.time.LocalDate;

import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.enums.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.jspecify.annotations.Nullable;

import org.springframework.format.annotation.DateTimeFormat;

import static com.jobtracker.web.dto.FormValues.blankToNull;

/**
 * An application added by hand, for a job found without the browser extension, e.g. through a referral.
 *
 * @param status and {@code statusDate} start the timeline, so an application sent last week can be added as it was
 */
public record NewApplicationForm(
		@NotBlank(message = "Give the job a title.") String title,
		@Nullable String company,
		@Nullable String location,
		@URL(message = "The link must be a full web address, starting with https://") @Nullable String link,
		@Nullable String employmentType,
		@Nullable String salary,
		@Nullable String description,
		@NotNull(message = "Pick a status.") ApplicationStatus status,
		@NotNull(message = "Pick a date.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statusDate) {

	public NewApplication toNewApplication(long userId) {
		return NewApplication.builder()
			.userId(userId)
			.title(this.title.strip())
			.company(blankToNull(this.company))
			.description(blankToNull(this.description))
			.link(blankToNull(this.link))
			.location(blankToNull(this.location))
			.salary(blankToNull(this.salary))
			.employmentType(blankToNull(this.employmentType))
			.initialStatus(this.status)
			.build();
	}

}
