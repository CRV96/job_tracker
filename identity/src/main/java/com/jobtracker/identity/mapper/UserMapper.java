package com.jobtracker.identity.mapper;

import com.jobtracker.identity.dto.User;
import com.jobtracker.identity.entity.UserEntity;

/**
 * Converts entities to the records the service returns. Entities never leave the module.
 */
public final class UserMapper {

	private UserMapper() {
	}

	public static User toUser(UserEntity user) {
		return new User(user.getId(), user.getName(), user.getCreatedAt());
	}

}
