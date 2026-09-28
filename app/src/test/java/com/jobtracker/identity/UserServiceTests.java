package com.jobtracker.identity;

import com.jobtracker.TestcontainersConfiguration;
import com.jobtracker.common.exception.BusinessRuleException;
import com.jobtracker.identity.dto.User;
import com.jobtracker.identity.exception.IdentityErrorCode;
import com.jobtracker.identity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.modulith.test.ApplicationModuleTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class UserServiceTests {

	@Autowired
	private UserService users;

	@Autowired
	private JdbcClient jdbc;

	@BeforeEach
	void deleteAllProfiles() {
		this.jdbc.sql("TRUNCATE users CASCADE").update();
	}

	@Test
	void createsProfileWithTrimmedName() {
		User created = this.users.create("  Robert ");

		assertThat(created.name()).isEqualTo("Robert");
		assertThat(created.createdAt()).isNotNull();
		assertThat(this.users.findById(created.id())).get().extracting(User::name).isEqualTo("Robert");
	}

	@Test
	void rejectsNameAlreadyTakenIgnoringCase() {
		this.users.create("Robert");

		assertThatThrownBy(() -> this.users.create("robert")).isInstanceOfSatisfying(BusinessRuleException.class,
				exception -> assertThat(exception.getErrorCode()).isEqualTo(IdentityErrorCode.USER_NAME_TAKEN));
	}

	@Test
	void theDatabaseAlsoRejectsANameTakenIgnoringCase() {
		this.users.create("Robert");

		assertThatThrownBy(() -> this.jdbc.sql("INSERT INTO users (name) VALUES ('robert')").update())
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void listsProfilesByNameIgnoringCase() {
		this.users.create("bob");
		this.users.create("Carol");
		this.users.create("Alice");

		assertThat(this.users.findAll()).extracting(User::name).containsExactly("Alice", "bob", "Carol");
	}

	@Test
	void findsNothingForUnknownId() {
		assertThat(this.users.findById(Long.MAX_VALUE)).isEmpty();
	}

	@Test
	void tellsWhetherAProfileExists() {
		User robert = this.users.create("Robert");

		assertThat(this.users.exists(robert.id())).isTrue();
		assertThat(this.users.exists(Long.MAX_VALUE)).isFalse();
	}

}
