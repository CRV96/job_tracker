package com.jobwarden.web.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.jobwarden.common.exception.BusinessRuleException;
import com.jobwarden.identity.dto.User;
import com.jobwarden.jobs.enums.ApplicationStatus;
import com.jobwarden.jobs.enums.RejectionReason;
import com.jobwarden.jobs.exception.ApplicationNotFoundException;
import com.jobwarden.jobs.service.ApplicationService;
import com.jobwarden.web.constants.AppConstants.ControllerConstants;
import com.jobwarden.web.constants.AppConstants.HtmxHeaders;
import com.jobwarden.web.constants.AppConstants.ModelAttributes;
import com.jobwarden.web.constants.AppConstants.Views;
import com.jobwarden.web.dto.NewApplicationForm;
import com.jobwarden.web.dto.NewEventForm;
import jakarta.validation.Valid;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Every page here needs a profile: {@link #requireProfile} sends the browser to the profile picker if none is
 * selected. An application the profile doesn't own answers 404.
 */
@Controller
@RequestMapping(ControllerConstants.APPLICATIONS)
@CustomLog
@RequiredArgsConstructor
class ApplicationController extends BaseController {

	private final ApplicationService applications;

	@GetMapping
	String list(@RequestParam(required = false) @Nullable ApplicationStatus status, Model model) {
		User user = requireProfile(model);
		model.addAttribute(ModelAttributes.APPLICATIONS, this.applications.findAll(user.id(), status));
		model.addAttribute(ModelAttributes.STATUSES, ApplicationStatus.values());
		model.addAttribute(ModelAttributes.SELECTED_STATUS, status);
		return Views.APPLICATION_LIST;
	}

	@GetMapping("/new")
	String newApplication(Model model) {
		requireProfile(model);
		return showNewApplicationForm(model, null, Map.of());
	}

	/**
	 * Adds an application by hand. Invalid input shows the form again, with each error next to its field.
	 */
	@PostMapping
	String create(@Valid NewApplicationForm form, BindingResult result, Model model) {
		User user = requireProfile(model);
		if (result.hasErrors()) {
			Map<String, String> errors = fieldErrors(result);
			log.debug("New application not added, fields with errors: {}", errors.keySet());
			return showNewApplicationForm(model, form, errors);
		}
		long id = this.applications.create(form.toNewApplication(user.id()));
		return redirectTo(ControllerConstants.APPLICATIONS + "/" + id);
	}

	@GetMapping("/{id}")
	String detail(@PathVariable long id, Model model) {
		addApplication(model, requireProfile(model).id(), id);
		return Views.APPLICATION_DETAIL;
	}

	/**
	 * Called by HTMX from the detail page, both by the add-event form and the quick status buttons. Returns the page's
	 * {@code application} fragment, which HTMX swaps in place, so the status and the timeline show the new event.
	 */
	@PostMapping("/{id}/events")
	String addEvent(@PathVariable long id, @Valid NewEventForm form, BindingResult result, Model model) {
		long userId = requireProfile(model).id();
		String error = result.hasErrors() ? errorMessage(result) : tryAddEvent(userId, id, form);
		if (error != null) {
			log.debug("Event not added to application {}: {}", id, error);
		}
		model.addAttribute(ModelAttributes.EVENT_ERROR, error);
		addApplication(model, userId, id);
		return Views.APPLICATION_DETAIL_FRAGMENT;
	}

	/**
	 * Called by HTMX. The {@code HX-Redirect} header sends the browser back to the list.
	 */
	@DeleteMapping("/{id}")
	ResponseEntity<Void> delete(@PathVariable long id, Model model) {
		this.applications.delete(requireProfile(model).id(), id);
		return ResponseEntity.noContent().header(HtmxHeaders.REDIRECT, ControllerConstants.APPLICATIONS).build();
	}

	/**
	 * @return why the event wasn't added, or {@code null} if it was
	 */
	private @Nullable String tryAddEvent(long userId, long applicationId, NewEventForm form) {
		try {
			this.applications.addEvent(userId, applicationId, form.toNewTimelineEvent());
			return null;
		} catch (BusinessRuleException exception) {
			return message(exception);
		}
	}

	private String showNewApplicationForm(Model model, @Nullable NewApplicationForm form,
			Map<String, String> fieldErrors) {
		model.addAttribute(ModelAttributes.FORM, form);
		model.addAttribute(ModelAttributes.FIELD_ERRORS, fieldErrors);
		addFormChoices(model);
		return Views.NEW_APPLICATION;
	}

	private void addApplication(Model model, long userId, long applicationId) {
		model.addAttribute(ModelAttributes.JOB_APPLICATION, this.applications.findById(userId, applicationId)
			.orElseThrow(() -> new ApplicationNotFoundException(applicationId)));
		addFormChoices(model);
	}

	/**
	 * What the forms on the new-application and detail pages offer: statuses, rejection reasons, and now as the
	 * default date and time.
	 */
	private static void addFormChoices(Model model) {
		model.addAttribute(ModelAttributes.STATUSES, ApplicationStatus.values());
		model.addAttribute(ModelAttributes.REJECTION_REASONS, RejectionReason.values());
		model.addAttribute(ModelAttributes.NOW, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
	}

}
