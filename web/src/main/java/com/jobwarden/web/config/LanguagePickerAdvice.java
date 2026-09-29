package com.jobwarden.web.config;

import com.jobwarden.web.constants.AppConstants.ModelAttributes;
import com.jobwarden.web.enums.Language;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * The languages for the picker in layout.html. On every page, the error page included, which is why it isn't in
 * BaseController: Spring Boot's error controller doesn't extend it.
 */
@ControllerAdvice
class LanguagePickerAdvice {

	@ModelAttribute(ModelAttributes.LANGUAGES)
	Language[] languages() {
		return Language.values();
	}

}
