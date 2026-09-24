package com.jobtracker.web.controller;

import com.jobtracker.jobs.service.ApplicationService;
import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.web.constants.AppConstants;
import com.jobtracker.web.user.CurrentUser;
import com.jobtracker.web.dto.NewEventForm;
import jakarta.validation.Valid;

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

@Controller
@RequestMapping(AppConstants.ControllerConstants.APPLICATIONS)
class ApplicationController extends BaseController {

	private final ApplicationService applications;

	private final CurrentUser currentUser;

	ApplicationController(ApplicationService applications, CurrentUser currentUser) {
		this.applications = applications;
		this.currentUser = currentUser;
	}

	@GetMapping
	String list(@RequestParam(required = false) ApplicationStatus status, Model model) {
		// TODO: redirect to /profiles if no profile is selected; otherwise add the
		//  current profile's applications (filtered by status) to the model
		return "applications/list";
	}

	@GetMapping("/{id}")
	String detail(@PathVariable long id, Model model) {
		// TODO: add the application to the model, or respond 404 if the current profile doesn't own it
		model.addAttribute("applicationId", id);
		return "applications/detail";
	}

	/**
	 * Called by HTMX from the detail page. Returns only the timeline fragment, which HTMX swaps in place.
	 */
	@PostMapping("/{id}/events")
	String addEvent(@PathVariable long id, @Valid NewEventForm form, BindingResult result, Model model) {
		// TODO: add the event, put the refreshed application in the model and return "applications/detail :: timeline"
		throw new UnsupportedOperationException("Not implemented yet");
	}

	/**
	 * Called by HTMX. Respond with an {@code HX-Redirect: /applications} header to send the browser back to the list.
	 */
	@DeleteMapping("/{id}")
	ResponseEntity<Void> delete(@PathVariable long id) {
		// TODO: delete the application and redirect via the HX-Redirect header
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
