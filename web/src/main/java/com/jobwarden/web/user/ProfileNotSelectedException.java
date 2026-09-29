package com.jobwarden.web.user;

/**
 * A page needs a profile, but none is selected in this browser session. The web layer then sends the browser to the
 * profile picker.
 */
public class ProfileNotSelectedException extends RuntimeException {

	public ProfileNotSelectedException() {
		super("No profile selected");
	}

}
