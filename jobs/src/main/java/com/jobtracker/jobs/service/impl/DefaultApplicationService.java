package com.jobtracker.jobs.service.impl;

import java.util.List;
import java.util.Optional;

import com.jobtracker.jobs.dto.ApplicationDetails;
import com.jobtracker.jobs.dto.ApplicationSummary;
import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.dto.NewTimelineEvent;
import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.repository.ApplicationRepository;
import com.jobtracker.jobs.service.ApplicationService;
import com.jobtracker.jobs.service.ApplicationService;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultApplicationService implements ApplicationService {

	private final ApplicationRepository applications;

	DefaultApplicationService(ApplicationRepository applications) {
		this.applications = applications;
	}

	@Override
	@Transactional
	public long create(NewApplication application) {
		// TODO: save a new ApplicationEntity (captured now) and start its timeline with
		//  an event for the initial status, so every timeline has a first entry
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<ApplicationSummary> findAll(long userId, @Nullable ApplicationStatus status) {
		// TODO: newest first, filtered by status when one is given; map with ApplicationMapper.toApplicationSummary
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<ApplicationDetails> findById(long userId, long applicationId) {
		// TODO: find it for this user and map it with ApplicationMapper.toApplicationDetails
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	@Transactional
	public void addEvent(long userId, long applicationId, NewTimelineEvent event) {
		// TODO: throw ApplicationNotFoundException if the user has no such application; reject a rejection
		//  reason on non-REJECTED events; add the event, and update
		//  the application's current status if this is now its latest event
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	@Transactional
	public void delete(long userId, long applicationId) {
		// TODO: delete the application if it belongs to the user (its events go with it),
		//  otherwise throw ApplicationNotFoundException
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
