package com.jobtracker.capture.enums;

import com.jobtracker.jobs.enums.ApplicationStatus;

/**
 * The button the user clicked in the extension popup.
 */
public enum CaptureAction {

	/** Already applied, or applying now: track it. */
	SAVE(ApplicationStatus.APPLIED),

	/** Bookmark to revisit or apply later. */
	FAVORITE(ApplicationStatus.SAVED);

	private final ApplicationStatus initialStatus;

	CaptureAction(ApplicationStatus initialStatus) {
		this.initialStatus = initialStatus;
	}

	ApplicationStatus initialStatus() {
		return initialStatus;
	}

}
