package com.jobwarden.web.dto;

/**
 * Each field is a comma-separated list, as typed by the user.
 */
public record SearchForm(String jobTitles, String keywords, String excludedKeywords, String locations) {
}
