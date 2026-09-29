package com.jobwarden.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewProfileForm(
		@NotBlank(message = "{validation.profile.name.required}")
		@Size(max = NewProfileForm.NAME_MAX_LENGTH, message = "{validation.profile.name.size}")
		String name) {

	/** Also the {@code maxlength} of the name field in profiles/list.html. */
	public static final int NAME_MAX_LENGTH = 100;

}
