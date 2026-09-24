package com.jobtracker.jobs.enums;

/**
 * Top-level stage of an application, used for filtering and the dashboard. The company-specific
 * sub-stage ("Technical interview", "Onsite round 2") is free text on each timeline event.
 */
public enum ApplicationStatus {

	SAVED, APPLIED, INTERVIEWING, OFFER, REJECTED, WITHDRAWN

}
