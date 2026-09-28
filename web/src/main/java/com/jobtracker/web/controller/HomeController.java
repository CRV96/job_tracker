package com.jobtracker.web.controller;

import com.jobtracker.web.constants.AppConstants.ControllerConstants;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class HomeController extends BaseController {

	@GetMapping(ControllerConstants.HOMEPAGE)
	String home() {
		return redirectTo(ControllerConstants.APPLICATIONS);
	}

}
