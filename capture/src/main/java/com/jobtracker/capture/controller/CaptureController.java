package com.jobtracker.capture.controller;

import com.jobtracker.jobs.service.ApplicationService;
import com.jobtracker.capture.dto.CaptureRequest;
import com.jobtracker.capture.dto.CaptureResponse;
import com.jobtracker.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captures")
@RequiredArgsConstructor
class CaptureController {

	private final ApplicationService applications;

	private final UserService users;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	CaptureResponse capture(@Valid @RequestBody CaptureRequest request) {
		// TODO: reject unknown users; map the request to a NewApplication (source = the link's host,
		//  raw payload serialized to a JSON string) and create it. Also decide what happens when
		//  the same link is captured twice.
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
