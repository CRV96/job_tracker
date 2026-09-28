package com.jobtracker.web.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class ControllerConstants {
		public static final String HOMEPAGE = "/";
		public static final String APPLICATIONS = "/applications";
		public static final String PROFILES = "/profiles";
		public static final String SEARCH = "/search";
	}

	/**
	 * Headers HTMX sends and understands. See https://htmx.org/reference/#headers
	 */
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class HtmxHeaders {
		/** Sent by HTMX on every request it makes. */
		public static final String REQUEST = "HX-Request";
		/** Tells HTMX to load another page, instead of swapping the response into the current one. */
		public static final String REDIRECT = "HX-Redirect";
	}

	/**
	 * Thymeleaf templates, by their path under {@code templates/}.
	 */
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class Views {
		public static final String APPLICATION_LIST = "applications/list";
		public static final String APPLICATION_DETAIL = "applications/detail";
		/** The part of the detail page that HTMX swaps after an event is added. */
		public static final String APPLICATION_DETAIL_FRAGMENT = APPLICATION_DETAIL + " :: application";
		public static final String NEW_APPLICATION = "applications/new";
		public static final String PROFILE_LIST = "profiles/list";
	}

	/**
	 * Names the templates read from the model. Thymeleaf reserves {@code application}, {@code session} and
	 * {@code param}, so never use those.
	 */
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class ModelAttributes {
		/** The selected profile, or {@code null}; every page's header shows it. */
		public static final String CURRENT_PROFILE = "currentProfile";
		public static final String APPLICATIONS = "applications";
		public static final String JOB_APPLICATION = "jobApplication";
		public static final String STATUSES = "statuses";
		public static final String SELECTED_STATUS = "selectedStatus";
		public static final String REJECTION_REASONS = "rejectionReasons";
		public static final String TODAY = "today";
		/** What was typed into a form that's shown again with errors. */
		public static final String FORM = "form";
		/** Each form field's error message, by field name. */
		public static final String FIELD_ERRORS = "fieldErrors";
		public static final String EVENT_ERROR = "eventError";
		public static final String PROFILES = "profiles";
		public static final String NAME = "name";
		public static final String NAME_ERROR = "nameError";
	}
}
