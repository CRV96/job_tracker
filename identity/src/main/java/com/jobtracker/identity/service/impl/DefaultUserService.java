package com.jobtracker.identity.service.impl;

import java.util.List;
import java.util.Optional;

import com.jobtracker.identity.dto.User;
import com.jobtracker.identity.repository.UserRepository;
import com.jobtracker.identity.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class DefaultUserService implements UserService {

	private final UserRepository users;

	@Override
	@Transactional
	public User create(String name) {
		// TODO: save a new UserEntity and return it via UserMapper.toUser
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<User> findAll() {
		// TODO: return every profile, ordered by name
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<User> findById(long id) {
		// TODO: look up the profile and map it with UserMapper.toUser
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
