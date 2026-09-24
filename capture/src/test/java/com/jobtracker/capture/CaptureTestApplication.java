package com.jobtracker.capture;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Web tests like {@code @WebMvcTest} start from a {@code @SpringBootApplication} class. The real one lives
 * in the app module, which this module can't see, so this one stands in for it during tests.
 */
@SpringBootApplication
class CaptureTestApplication {

}
