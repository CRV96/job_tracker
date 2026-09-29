package com.jobwarden.jobs.repository;

import java.util.List;
import java.util.Optional;

import com.jobwarden.jobs.dto.ApplicationSummary;
import com.jobwarden.jobs.entity.ApplicationEntity;
import com.jobwarden.jobs.enums.ApplicationStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

	/**
	 * The list page's rows. Selects only the columns the list shows: the description and the raw payload can be
	 * large, and loading them for every row would be wasted work.
	 */
	@Query("""
			select new com.jobwarden.jobs.dto.ApplicationSummary(
			    a.id, a.title, a.company, a.location, a.currentStatus, a.capturedAt)
			from ApplicationEntity a
			where a.userId = :userId
			order by a.capturedAt desc""")
	List<ApplicationSummary> findSummaries(long userId);

	/**
	 * Like {@link #findSummaries(long)}, only for applications with this status.
	 */
	@Query("""
			select new com.jobwarden.jobs.dto.ApplicationSummary(
			    a.id, a.title, a.company, a.location, a.currentStatus, a.capturedAt)
			from ApplicationEntity a
			where a.userId = :userId and a.currentStatus = :status
			order by a.capturedAt desc""")
	List<ApplicationSummary> findSummaries(long userId, ApplicationStatus status);

	/**
	 * Loads the timeline in the same query: every caller (the detail page, adding an event, deleting) needs it.
	 */
	@EntityGraph(attributePaths = "events")
	Optional<ApplicationEntity> findByIdAndUserId(long id, long userId);

	Optional<ApplicationEntity> findFirstByUserIdAndLink(long userId, String link);

}
