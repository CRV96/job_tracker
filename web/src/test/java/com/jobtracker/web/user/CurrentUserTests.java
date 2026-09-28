package com.jobtracker.web.user;

import java.time.Instant;
import java.util.Optional;

import com.jobtracker.identity.dto.User;
import com.jobtracker.identity.exception.UserNotFoundException;
import com.jobtracker.identity.service.UserService;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CurrentUserTests {

	private static final User ROBERT = new User(1, "Robert", Instant.parse("2026-09-28T10:00:00Z"));

	private final MockHttpSession session = new MockHttpSession();

	private final UserService users = mock();

	private final CurrentUser currentUser = new CurrentUser(this.session, this.users);

	@Test
	void hasNoProfileUntilOneIsSelected() {
		assertThat(this.currentUser.get()).isEmpty();
	}

	@Test
	void remembersTheSelectedProfileInTheSession() {
		given(this.users.exists(1)).willReturn(true);
		given(this.users.findById(1)).willReturn(Optional.of(ROBERT));

		this.currentUser.select(1);

		assertThat(this.currentUser.get()).contains(ROBERT);
	}

	@Test
	void refusesToSelectAnUnknownProfile() {
		given(this.users.exists(9)).willReturn(false);

		assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> this.currentUser.select(9));
		assertThat(this.currentUser.get()).isEmpty();
	}

	@Test
	void forgetsAProfileThatNoLongerExists() {
		given(this.users.exists(1)).willReturn(true);
		given(this.users.findById(1)).willReturn(Optional.empty());
		this.currentUser.select(1);

		assertThat(this.currentUser.get()).isEmpty();
		assertThat(this.session.getAttributeNames().hasMoreElements()).isFalse();
	}

}
