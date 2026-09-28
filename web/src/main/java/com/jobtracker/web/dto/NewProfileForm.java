package com.jobtracker.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewProfileForm(
		@NotBlank(message = "A profile needs a name.")
		@Size(max = NewProfileForm.NAME_MAX_LENGTH, message = "A profile's name can be at most {max} characters long.")
		String name) {

	/** Also the {@code maxlength} of the name field in profiles/list.html. */
	public static final int NAME_MAX_LENGTH = 100;

}
