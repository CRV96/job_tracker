package com.jobwarden.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.jobwarden.common.exception.JobWardenException;
import com.jobwarden.identity.dto.User;
import com.jobwarden.web.constants.AppConstants.ModelAttributes;
import com.jobwarden.web.user.CurrentUser;
import com.jobwarden.web.user.ProfileNotSelectedException;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;

abstract class BaseController {

	private CurrentUser currentUser;

	private MessageSource messages;

	/**
	 * Setters rather than a constructor, so subclasses keep their Lombok constructors: those can't pass arguments up
	 * to this class.
	 */
	@Autowired
	final void setCurrentUser(CurrentUser currentUser) {
		this.currentUser = currentUser;
	}

	@Autowired
	final void setMessages(MessageSource messages) {
		this.messages = messages;
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
	 * What users see for one of the modules' exceptions: its code's message, in their language.
	 */
	protected String message(JobWardenException exception) {
		String inEnglish = Objects.requireNonNullElse(exception.getMessage(), exception.getErrorCode().getCode());
		return this.messages.getMessage(exception.getErrorCode().getMessageKey(), exception.getArguments(), inEnglish,
				LocaleContextHolder.getLocale());
	}

	/**
	 * The first error of each field, by field name, in the user's language, to show next to the form's fields.
	 */
	protected Map<String, String> fieldErrors(BindingResult result) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError error : result.getFieldErrors()) {
			// The validation message, or for a value that couldn't even be converted (e.g. text as a date) the
			// typeMismatch message
			errors.putIfAbsent(error.getField(), this.messages.getMessage(error, LocaleContextHolder.getLocale()));
		}
		return errors;
	}

	/**
	 * All the form's errors as one text, for forms that show a single error line.
	 */
	protected String errorMessage(BindingResult result) {
		return String.join(" ", fieldErrors(result).values());
	}

}
