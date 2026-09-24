package com.jobtracker.web.user;

import java.util.Optional;

import com.jobtracker.identity.dto.User;
import com.jobtracker.identity.service.UserService;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

/**
 * The profile chosen in the current browser session. The self-hosted version has no login,
 * so the selected profile's id is simply kept in the HTTP session.
 */
@Component
public class CurrentUser {

	private final HttpSession session;

	private final UserService users;

	CurrentUser(HttpSession session, UserService users) {
		this.session = session;
		this.users = users;
	}

	public Optional<User> get() {
		// TODO: read the profile id stored in the session and look it up with UserService
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void select(long userId) {
		// TODO: store the profile id in the session
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
