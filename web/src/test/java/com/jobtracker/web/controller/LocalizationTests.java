package com.jobtracker.web.controller;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.jobtracker.common.exception.BusinessRuleException;
import com.jobtracker.identity.dto.User;
import com.jobtracker.identity.enums.IdentityErrorCode;
import com.jobtracker.identity.service.UserService;
import com.jobtracker.web.enums.Language;
import com.jobtracker.web.user.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * The profiles page stands in for every page: they all get their language the same way.
 */
@WebMvcTest(ProfileController.class)
class LocalizationTests {

	private static final User ROBERT = new User(1, "Robert", Instant.parse("2026-09-28T10:00:00Z"));

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private UserService users;

	@MockitoBean
	private CurrentUser currentUser;

	@BeforeEach
	void oneProfile() {
		given(this.users.findAll()).willReturn(List.of(ROBERT));
		given(this.currentUser.get()).willReturn(Optional.of(ROBERT));
	}

	@Test
	void showsThePageInTheBrowsersLanguage() {
		assertThat(this.mvc.get().uri("/profiles").header(HttpHeaders.ACCEPT_LANGUAGE, "ro-RO,ro;q=0.9,en;q=0.8"))
			.hasStatusOk()
			.bodyText()
			.contains("<html lang=\"ro\"", "Cine folosește Job Tracker?", "Schimbă profilul");
	}

	@Test
	void fallsBackToEnglishForALanguageTheAppDoesNotHave() {
		assertThat(this.mvc.get().uri("/profiles").header(HttpHeaders.ACCEPT_LANGUAGE, "it-IT"))
			.bodyText()
			.contains("<html lang=\"en\"", "Who’s using Job Tracker?");
	}

	@Test
	void remembersTheLanguagePickedInTheHeader() {
		assertThat(this.mvc.get().uri("/profiles").param("lang", "de").header(HttpHeaders.ACCEPT_LANGUAGE, "ro"))
			.hasStatusOk()
			.cookies()
			.hasValue("language", "de");
		assertThat(this.mvc.get().uri("/profiles").param("lang", "de")).bodyText().contains("Wer nutzt Job Tracker?");
	}

	@Test
	void ignoresALanguageTheAppDoesNotHave() {
		assertThat(this.mvc.get().uri("/profiles").param("lang", "it")).cookies().doesNotContainCookie("language");
	}

	@Test
	void offersEveryLanguageInThePicker() {
		String[] options = Arrays.stream(Language.values())
			.flatMap(language -> Stream.of("value=\"" + language.getTag() + "\"", language.getNativeName()))
			.toArray(String[]::new);

		assertThat(this.mvc.get().uri("/profiles")).bodyText().contains(options);
	}

	@Test
	void translatesFormErrors() {
		assertThat(this.mvc.post().uri("/profiles").param("name", " ").header(HttpHeaders.ACCEPT_LANGUAGE, "fr"))
			.bodyText()
			.contains("Un profil doit avoir un nom.");
	}

	@Test
	void translatesTheModulesErrors() {
		given(this.users.create("Robert")).willThrow(new BusinessRuleException(IdentityErrorCode.USER_NAME_TAKEN,
				"A profile with this name already exists", "Robert"));

		assertThat(this.mvc.post().uri("/profiles").param("name", "Robert").header(HttpHeaders.ACCEPT_LANGUAGE, "ro"))
			.bodyText()
			.contains("Există deja un profil cu numele Robert.");
	}

}
