package com.jobtracker.web.controller;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(HomeController.class)
class HomeControllerTests {

	@Autowired
	private MockMvcTester mvc;

	@Test
	void redirectsToApplications() {
		assertThat(this.mvc.get().uri("/")).hasStatus(HttpStatus.FOUND)
			.headers()
			.hasValue(HttpHeaders.LOCATION, "/applications");
	}

}
