package com.jobtracker.identity.service.impl;

import java.util.List;
import java.util.Optional;

import com.jobtracker.common.exception.BusinessRuleException;
import com.jobtracker.identity.dto.User;
import com.jobtracker.identity.entity.UserEntity;
import com.jobtracker.identity.exception.IdentityErrorCode;
import com.jobtracker.identity.mapper.UserMapper;
import com.jobtracker.identity.repository.UserRepository;
import com.jobtracker.identity.service.UserService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logs by profile id only: names are personal data and stay out of the logs.
 */
@Service
@Transactional(readOnly = true)
@CustomLog
@RequiredArgsConstructor
class DefaultUserService implements UserService {

	private static final Sort BY_NAME = Sort.by(Sort.Order.asc("name").ignoreCase());

	private final UserRepository users;

	/**
	 * @throws BusinessRuleException if a profile already has this name, ignoring case
	 */
	@Override
	@Transactional
	public User create(String name) {
		String trimmedName = name.strip();
		if (this.users.existsByNameIgnoreCase(trimmedName)) {
			throw new BusinessRuleException(IdentityErrorCode.USER_NAME_TAKEN,
					"A profile named " + trimmedName + " already exists");
		}
		User user = UserMapper.toUser(this.users.save(new UserEntity(trimmedName)));
		log.info("Created profile {}", user.id());
		return user;
	}

	@Override
	public List<User> findAll() {
		List<User> users = this.users.findAll(BY_NAME).stream().map(UserMapper::toUser).toList();
		log.debug("Found {} profiles", users.size());
		return users;
	}

	@Override
	public Optional<User> findById(long id) {
		Optional<User> user = this.users.findById(id).map(UserMapper::toUser);
		log.debug("Profile {} {}", id, user.isPresent() ? "found" : "not found");
		return user;
	}

	@Override
	public boolean exists(long id) {
		return this.users.existsById(id);
	}

}
