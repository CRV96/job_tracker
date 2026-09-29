package com.jobwarden.capture.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.jobwarden.capture.dto.CaptureRequest;
import com.jobwarden.capture.dto.CaptureResponse;
import com.jobwarden.identity.exception.UserNotFoundException;
import com.jobwarden.identity.service.UserService;
import com.jobwarden.jobs.dto.NewApplication;
import com.jobwarden.jobs.dto.TrackedApplication;
import com.jobwarden.jobs.service.ApplicationService;
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
@RequestMapping("${jobwarden.capture.path}")
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
		TrackedApplication application = this.applications.track(toNewApplication(request));

		log.debug("Captured {} for profile {}: application {} ({})", request.action(), request.userId(),
				application.id(), application.created() ? "new" : "already tracked");

		return ResponseEntity.status(application.created() ? HttpStatus.CREATED : HttpStatus.OK)
			.body(new CaptureResponse(application.id()));
	}

	private NewApplication toNewApplication(CaptureRequest request) {
		// In the server's time zone, like every date and time the pages show
		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		boolean includeTime = !Boolean.FALSE.equals(request.includeTime());
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
			.initialStatusDate(now.toLocalDate())
			.initialStatusTime(includeTime ? now.toLocalTime() : null)
			.build();
	}

	private @Nullable String toJson(@Nullable Map<String, Object> rawPayload) {
		return (rawPayload != null) ? this.jsonMapper.writeValueAsString(rawPayload) : null;
	}

}
