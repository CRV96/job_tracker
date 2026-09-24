package com.jobtracker.jobs;

import com.jobtracker.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;

/**
 * Starts only the jobs module (its services, repositories and entities) against a real database, which
 * proves it doesn't secretly need another module's beans. Database tests for the module's services go here,
 * because the migrations only run in the app module.
 */
@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class JobsModuleTests {

	@Test
	void startsOnItsOwn() {
	}

}
