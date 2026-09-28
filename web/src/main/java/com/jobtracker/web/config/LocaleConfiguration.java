package com.jobtracker.web.config;

import java.time.Duration;
import java.util.Locale;

import com.jobtracker.web.enums.Language;
import org.jspecify.annotations.Nullable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Which language a request is answered in: the one picked in the page header (kept in a cookie), otherwise the
 * browser's preferred language if the app has it, otherwise English.
 */
@Configuration(proxyBeanMethods = false)
class LocaleConfiguration implements WebMvcConfigurer {

	/** The request parameter the language picker in layout.html sends, e.g. {@code ?lang=ro}. */
	static final String LANGUAGE_PARAMETER = "lang";

	private static final String LANGUAGE_COOKIE = "language";

	private static final Duration LANGUAGE_COOKIE_MAX_AGE = Duration.ofDays(365);

	/**
	 * Named {@code localeResolver}, the name Spring MVC looks for, so it replaces Spring Boot's default.
	 */
	@Bean
	LocaleResolver localeResolver() {
		CookieLocaleResolver resolver = new CookieLocaleResolver(LANGUAGE_COOKIE);
		resolver.setCookieMaxAge(LANGUAGE_COOKIE_MAX_AGE);
		resolver.setCookieSameSite("Lax");
		resolver.setDefaultLocaleFunction(
				request -> Language.bestMatch(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).getLocale());
		return resolver;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LanguageChangeInterceptor());
	}

	/**
	 * Switches to the language in {@code ?lang=}, but only to one the app has; anything else is ignored.
	 */
	private static final class LanguageChangeInterceptor extends LocaleChangeInterceptor {

		LanguageChangeInterceptor() {
			setParamName(LANGUAGE_PARAMETER);
			setIgnoreInvalidLocale(true);
		}

		@Override
		protected @Nullable Locale parseLocaleValue(String value) {
			return Language.fromTag(value)
				.map(Language::getLocale)
				.orElseThrow(() -> new IllegalArgumentException("Not one of the app's languages: " + value));
		}

	}

}
