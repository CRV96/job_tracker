package com.jobwarden.capture.enums;

import com.jobwarden.jobs.enums.ApplicationStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The button the user clicked in the extension popup.
 */
@Getter
@RequiredArgsConstructor
public enum CaptureAction {

	/** Already applied, or applying now: track it as applied. */
	APPLY(ApplicationStatus.APPLIED),

	/** Keep it to revisit or apply to later. */
	BOOKMARK(ApplicationStatus.SAVED);

	private final ApplicationStatus initialStatus;

}
