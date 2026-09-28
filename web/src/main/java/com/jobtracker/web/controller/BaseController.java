package com.jobtracker.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.jobtracker.identity.dto.User;
import com.jobtracker.web.constants.AppConstants.ModelAttributes;
import com.jobtracker.web.user.CurrentUser;
import com.jobtracker.web.user.ProfileNotSelectedException;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;

abstract class BaseController {

	private static final String INVALID_VALUE_MESSAGE = "This value isn't valid.";

	private CurrentUser currentUser;

	/**
	 * A setter rather than a constructor, so subclasses keep their Lombok constructors: those can't pass arguments up
	 * to this class.
	 */
	@Autowired
	final void setCurrentUser(CurrentUser currentUser) {
		this.currentUser = currentUser;
	}

	/**
	 * Spring calls this before every handler method of a subclass, so every page has the selected profile (or
	 * {@code null}) for the header in layout.html. It's the only place a request looks the profile up.
	 */
	@ModelAttribute(ModelAttributes.CURRENT_PROFILE)
	@Nullable User currentProfile() {
		return this.currentUser.get().orElse(null);
	}

	/**
	 * For pages that need a profile: returns the one {@link #currentProfile()} put in the model.
	 * @throws ProfileNotSelectedException if none is selected, which sends the browser to the profile picker
	 */
	protected static User requireProfile(Model model) {
		if (model.getAttribute(ModelAttributes.CURRENT_PROFILE) instanceof User profile) {
			return profile;
		}
		throw new ProfileNotSelectedException();
	}

	protected void selectProfile(long userId) {
		this.currentUser.select(userId);
	}

	protected static String redirectTo(String path) {
		return "redirect:" + path;
	}

	/**
	 * The first error of each field, by field name, to show next to the form's fields.
	 */
	protected static Map<String, String> fieldErrors(BindingResult result) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError error : result.getFieldErrors()) {
			// A value that couldn't even be converted, e.g. text as a date, only has a technical message
			String message = error.isBindingFailure() ? null : error.getDefaultMessage();
			errors.putIfAbsent(error.getField(), Objects.requireNonNullElse(message, INVALID_VALUE_MESSAGE));
		}
		return errors;
	}

	/**
	 * All the form's errors as one text, for forms that show a single error line.
	 */
	protected static String errorMessage(BindingResult result) {
		return String.join(" ", fieldErrors(result).values());
	}

}
