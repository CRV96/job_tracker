package com.jobwarden.web.controller;

import com.jobwarden.web.user.CurrentUser;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(HomeController.class)
class HomeControllerTests {

	@Autowired
	private MockMvcTester mvc;

	// Every page controller extends BaseController, which needs it
	@MockitoBean
	private CurrentUser currentUser;

	@Test
	void redirectsToApplications() {
		assertThat(this.mvc.get().uri("/")).hasStatus(HttpStatus.FOUND)
			.headers()
			.hasValue(HttpHeaders.LOCATION, "/applications");
	}

}
