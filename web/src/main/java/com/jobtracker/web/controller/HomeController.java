package com.jobtracker.web.controller;

import com.jobtracker.web.constants.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

@Controller
class HomeController extends BaseController {

	@GetMapping(AppConstants.ControllerConstants.HOMEPAGE)
	ResponseEntity<Void> redirectToApplications() {
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(AppConstants.ControllerConstants.APPLICATIONS)).build();
	}

}
