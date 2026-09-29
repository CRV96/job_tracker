package com.jobwarden.identity.service;

import com.jobwarden.identity.dto.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

	User create(String name);

	List<User> findAll();

	Optional<User> findById(long id);

	boolean exists(long id);

}
