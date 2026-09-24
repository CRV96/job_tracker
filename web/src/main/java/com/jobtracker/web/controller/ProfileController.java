package com.jobtracker.web.controller;

import com.jobtracker.identity.service.UserService;
import com.jobtracker.web.constants.AppConstants;
import com.jobtracker.web.user.CurrentUser;
import com.jobtracker.web.dto.NewProfileForm;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(AppConstants.ControllerConstants.PROFILES)
class ProfileController extends BaseController {

	private final UserService users;

	private final CurrentUser currentUser;

	ProfileController(UserService users, CurrentUser currentUser) {
		this.users = users;
		this.currentUser = currentUser;
	}

	@GetMapping
	String list(Model model) {
		// TODO: add all profiles to the model
		return "profiles/list";
	}

	@PostMapping
	String create(@Valid NewProfileForm form, BindingResult result) {
		// TODO: on validation errors re-render the page; otherwise create the profile,
		//  select it for this session and redirect to /applications
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@PostMapping("/{id}/select")
	String select(@PathVariable long id) {
		// TODO: select the profile for this session and redirect to /applications
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
