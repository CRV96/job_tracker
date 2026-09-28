package com.jobtracker.capture.controller;

import java.util.Map;

import com.jobtracker.capture.dto.CaptureRequest;
import com.jobtracker.capture.dto.CaptureResponse;
import com.jobtracker.identity.exception.UserNotFoundException;
import com.jobtracker.identity.service.UserService;
import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.dto.TrackedApplication;
import com.jobtracker.jobs.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Receives the extension's captures and saves them in the jobs module.
 */
@RestController
@RequestMapping("${jobtracker.capture.path}")
@CustomLog
@RequiredArgsConstructor
class CaptureController {

	private final ApplicationService applications;

	private final UserService users;

	private final JsonMapper jsonMapper;

	/**
	 * Saving a link the profile
	 * @return 201 with the new application's id, or 200 with the existing application's id
	 */
	@PostMapping
	ResponseEntity<CaptureResponse> capture(@Valid @RequestBody CaptureRequest request) {
		if (!this.users.exists(request.userId())) {
			throw new UserNotFoundException(request.userId());
		}
		//TODO: Create validation for the request fields, e.g. title, company, link, etc.
		TrackedApplication application = this.applications.track(toNewApplication(request));

		log.debug("Captured {} for profile {}: application {} ({})", request.action(), request.userId(),
				application.id(), application.created() ? "new" : "already tracked");

		return ResponseEntity.status(application.created() ? HttpStatus.CREATED : HttpStatus.OK)
			.body(new CaptureResponse(application.id()));
	}

	private NewApplication toNewApplication(CaptureRequest request) {
		return NewApplication.builder()
			.userId(request.userId())
			.title(request.title())
			.company(request.company())
			.description(request.description())
			.link(request.link())
			.location(request.location())
			.salary(request.salary())
			.employmentType(request.employmentType())
			.rawPayload(toJson(request.rawPayload()))
			.initialStatus(request.action().getInitialStatus())
			.build();
	}

	private @Nullable String toJson(@Nullable Map<String, Object> rawPayload) {
		return (rawPayload != null) ? this.jsonMapper.writeValueAsString(rawPayload) : null;
	}

}
