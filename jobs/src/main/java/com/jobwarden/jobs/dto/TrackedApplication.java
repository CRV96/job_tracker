package com.jobwarden.jobs.dto;

/**
 * @param created whether the application is new, or the profile already tracked the same link
 */
public record TrackedApplication(long id, boolean created) {
}
