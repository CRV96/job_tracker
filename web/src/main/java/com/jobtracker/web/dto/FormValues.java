package com.jobtracker.web.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FormValues {

	/**
	 * Empty text fields arrive as empty strings; they're stored as no value.
	 */
	static @Nullable String blankToNull(@Nullable String text) {
		return (text == null || text.isBlank()) ? null : text.strip();
	}

}
