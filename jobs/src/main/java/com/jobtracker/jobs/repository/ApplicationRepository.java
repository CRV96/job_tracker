package com.jobtracker.jobs.repository;

import java.util.List;
import java.util.Optional;

import com.jobtracker.jobs.entity.ApplicationEntity;
import com.jobtracker.jobs.enums.ApplicationStatus;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

	List<ApplicationEntity> findByUserIdOrderByCapturedAtDesc(long userId);

	List<ApplicationEntity> findByUserIdAndCurrentStatusOrderByCapturedAtDesc(long userId, ApplicationStatus status);

	Optional<ApplicationEntity> findByIdAndUserId(long id, long userId);

}
