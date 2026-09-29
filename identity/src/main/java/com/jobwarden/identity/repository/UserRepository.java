package com.jobwarden.identity.repository;

import com.jobwarden.identity.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	boolean existsByNameIgnoreCase(String name);

}
