package com.jobwarden.jobs;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * V5 gave the timeline's events a time, and V6 made it optional. The other tests only ever see an empty database, so
 * this one migrates to V4, adds events the old way (a date only), and checks what V5 and V6 make of them.
 */
class TimelineEventTimeMigrationTests {

	private record EventDateAndTime(LocalDate date, LocalTime time) {
	}

	@Test
	void keepsTheTimeAnEventWasRecordedOnItsOwnDateAndOnlyTheDateOtherwise() {
		try (PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"))) {
			postgres.start();
			SimpleDriverDataSource dataSource = new SimpleDriverDataSource(new org.postgresql.Driver(),
					postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
			JdbcClient jdbc = JdbcClient.create(dataSource);

			migrate(dataSource, "4");
			jdbc.sql("INSERT INTO users (id, name) VALUES (1, 'Robert')").update();
			jdbc.sql("""
					INSERT INTO applications (id, user_id, title, link, current_status, captured_at)
					VALUES (1, 1, 'Backend engineer', 'https://example.com/jobs/1', 'INTERVIEWING', now())""").update();
			// Recorded at noon (UTC) on its own date, and a backdated one recorded two days after its date
			jdbc.sql("""
					INSERT INTO application_events (id, application_id, event_date, stage_category, created_at)
					VALUES (1, 1, DATE '2026-09-20', 'APPLIED', TIMESTAMPTZ '2026-09-20 12:00:00+00'),
					       (2, 1, DATE '2026-09-18', 'SAVED', TIMESTAMPTZ '2026-09-20 12:00:00+00')""").update();

			migrate(dataSource, "latest");

			// In the connection's time zone, which the Postgres driver sets to the JVM's
			ZonedDateTime recorded = Instant.parse("2026-09-20T12:00:00Z").atZone(ZoneId.systemDefault());
			assertThat(dateAndTime(jdbc, 1))
				.isEqualTo(new EventDateAndTime(recorded.toLocalDate(), recorded.toLocalTime()));
			assertThat(dateAndTime(jdbc, 2)).isEqualTo(new EventDateAndTime(LocalDate.of(2026, 9, 18), null));
		}
	}

	private static void migrate(SimpleDriverDataSource dataSource, String version) {
		Flyway.configure().dataSource(dataSource).target(version).load().migrate();
	}

	private static EventDateAndTime dateAndTime(JdbcClient jdbc, long eventId) {
		return jdbc.sql("SELECT event_date, event_time FROM application_events WHERE id = ?")
			.param(eventId)
			.query((row, number) -> new EventDateAndTime(row.getObject(1, LocalDate.class),
					row.getObject(2, LocalTime.class)))
			.single();
	}

}
