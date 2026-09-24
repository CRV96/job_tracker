package com.jobtracker.capture.dto;

import java.util.Map;

import com.jobtracker.capture.enums.CaptureAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * What the extension sends: the fields it extracted from the page, plus the raw data they came from.
 */
public record CaptureRequest(
		@NotNull Long userId,
		@NotNull CaptureAction action,
		@NotBlank String title,
		@Nullable String company,
		@Nullable String description,
		@NotBlank String link,
		@Nullable String location,
		@Nullable String salary,
		@Nullable String employmentType,
		@Nullable Map<String, Object> rawPayload) {
}
