package com.jobwarden.web.controller;

import com.jobwarden.common.exception.BusinessRuleException;
import com.jobwarden.identity.service.UserService;
import com.jobwarden.web.constants.AppConstants.ControllerConstants;
import com.jobwarden.web.constants.AppConstants.ModelAttributes;
import com.jobwarden.web.constants.AppConstants.Views;
import com.jobwarden.web.dto.NewProfileForm;
import jakarta.validation.Valid;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ControllerConstants.PROFILES)
@CustomLog
@RequiredArgsConstructor
class ProfileController extends BaseController {

	private final UserService users;

	@GetMapping
	String list(Model model) {
		return showProfiles(model, null, null);
	}

	/**
	 * Creates the profile and selects it. An invalid or taken name shows the page again, with the error.
	 */
	@PostMapping
	String create(@Valid NewProfileForm form, BindingResult result, Model model) {
		if (result.hasErrors()) {
			log.debug("Profile not created: the name is blank or too long");
			return showProfiles(model, form.name(), errorMessage(result));
		}
		try {
			selectProfile(this.users.create(form.name()).id());
		} catch (BusinessRuleException exception) {
			log.debug("Profile not created: the name is taken");
			return showProfiles(model, form.name(), message(exception));
		}
		return redirectTo(ControllerConstants.APPLICATIONS);
	}

	@PostMapping("/{id}/select")
	String select(@PathVariable long id) {
		selectProfile(id);
		return redirectTo(ControllerConstants.APPLICATIONS);
	}

	private String showProfiles(Model model, @Nullable String name, @Nullable String nameError) {
		model.addAttribute(ModelAttributes.PROFILES, this.users.findAll());
		model.addAttribute(ModelAttributes.NAME, name);
		model.addAttribute(ModelAttributes.NAME_ERROR, nameError);
		return Views.PROFILE_LIST;
	}

}
