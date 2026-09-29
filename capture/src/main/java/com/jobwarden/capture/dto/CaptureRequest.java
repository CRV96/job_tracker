package com.jobwarden.capture.dto;

import java.util.Map;

import com.jobwarden.capture.enums.CaptureAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.jspecify.annotations.Nullable;

/**
 * What the extension sends: the fields it extracted from the page, plus the raw data they came from.
 *
 * @param includeTime whether the timeline records the time of the capture, not only its date; yes when left out
 */
public record CaptureRequest(
		@NotNull Long userId,
		@NotNull CaptureAction action,
		@NotBlank String title,
		@Nullable String company,
		@Nullable String description,
		@NotBlank @URL String link,
		@Nullable String location,
		@Nullable String salary,
		@Nullable String employmentType,
		@Nullable Map<String, Object> rawPayload,
		@Nullable Boolean includeTime) {
}
