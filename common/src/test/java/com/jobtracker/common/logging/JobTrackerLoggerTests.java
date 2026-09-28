package com.jobtracker.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jobtracker.common.exception.ErrorCode;
import lombok.CustomLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@CustomLog
class JobTrackerLoggerTests {

	private final Logger logback = (Logger) LoggerFactory.getLogger(JobTrackerLoggerTests.class);

	private final ListAppender<ILoggingEvent> appender = new ListAppender<>() {

		@Override
		protected void append(ILoggingEvent event) {
			// Logback works out the caller lazily, so do it while the logging call is still on the stack
			event.getCallerData();
			super.append(event);
		}

	};

	@BeforeEach
	void captureLogs() {
		this.appender.start();
		this.logback.addAppender(this.appender);
		this.logback.setAdditive(false);
	}

	@AfterEach
	void stopCapturingLogs() {
		this.logback.detachAppender(this.appender);
		this.logback.setAdditive(true);
	}

	@Test
	void logsUnderTheNameOfTheClassUsingIt() {
		log.info("Profile {} selected", 7);

		ILoggingEvent event = loggedEvent();
		assertThat(event.getLoggerName()).isEqualTo(JobTrackerLoggerTests.class.getName());
		assertThat(event.getLevel()).isEqualTo(Level.INFO);
		assertThat(event.getFormattedMessage()).isEqualTo("Profile 7 selected");
		assertThat(event.getKeyValuePairs()).isNullOrEmpty();
	}

	@Test
	void addsTheErrorCodeToTheMessageAndAsItsOwnField() {
		log.error(TestErrorCode.SAVE_FAILED, "Could not save application {}", 42);

		ILoggingEvent event = loggedEvent();
		assertThat(event.getLevel()).isEqualTo(Level.ERROR);
		assertThat(event.getFormattedMessage()).isEqualTo("[TEST-001] Could not save application 42");
		assertThat(event.getKeyValuePairs()).singleElement().satisfies(pair -> {
			assertThat(pair.key).isEqualTo("errorCode");
			assertThat(pair.value).isEqualTo("TEST-001");
		});
	}

	@Test
	void logsAnExceptionPassedLastWithItsStackTrace() {
		log.error(TestErrorCode.SAVE_FAILED, "Could not save application {}", 42, new IllegalStateException("boom"));

		ILoggingEvent event = loggedEvent();
		assertThat(event.getFormattedMessage()).isEqualTo("[TEST-001] Could not save application 42");
		assertThat(event.getThrowableProxy().getClassName()).isEqualTo(IllegalStateException.class.getName());
		assertThat(event.getThrowableProxy().getMessage()).isEqualTo("boom");
	}

	@Test
	void reportsTheClassUsingItAsTheCaller() {
		log.warn("Something looks off");

		StackTraceElement caller = loggedEvent().getCallerData()[0];
		assertThat(caller.getClassName()).isEqualTo(JobTrackerLoggerTests.class.getName());
		assertThat(caller.getMethodName()).isEqualTo("reportsTheClassUsingItAsTheCaller");
	}

	private ILoggingEvent loggedEvent() {
		assertThat(this.appender.list).hasSize(1);
		return this.appender.list.getFirst();
	}

	@Getter
	@RequiredArgsConstructor
	private enum TestErrorCode implements ErrorCode {

		SAVE_FAILED("TEST-001");

		private final String code;

	}

}
