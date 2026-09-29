package com.jobwarden.web.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageTests {

	@Test
	void picksTheBrowsersMostPreferredLanguageTheAppHas() {
		assertThat(Language.bestMatch("it-IT,it;q=0.9,de;q=0.8,en;q=0.7")).isEqualTo(Language.GERMAN);
	}

	@Test
	void matchesOnTheLanguageAlone() {
		assertThat(Language.bestMatch("pt-BR")).isEqualTo(Language.PORTUGUESE);
		assertThat(Language.fromTag("ro-RO")).contains(Language.ROMANIAN);
	}

	@Test
	void fallsBackToEnglish() {
		assertThat(Language.bestMatch("it, ja;q=0.5")).isEqualTo(Language.ENGLISH);
		assertThat(Language.bestMatch("*")).isEqualTo(Language.ENGLISH);
		assertThat(Language.bestMatch(null)).isEqualTo(Language.ENGLISH);
		assertThat(Language.bestMatch("not a valid header;;q=x")).isEqualTo(Language.ENGLISH);
	}

}
