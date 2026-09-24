package com.jobtracker.jobs.entity;

import java.time.Instant;
import java.time.LocalDate;

import com.jobtracker.jobs.dto.NewTimelineEvent;
import com.jobtracker.jobs.enums.ApplicationStatus;
import com.jobtracker.jobs.enums.RejectionReason;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "application_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public class TimelineEventEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "application_id")
	private ApplicationEntity application;

	private LocalDate eventDate;

	@Enumerated(EnumType.STRING)
	private ApplicationStatus stageCategory;

	private String stageLabel;

	private String note;

	@Enumerated(EnumType.STRING)
	private RejectionReason rejectionReason;

	@CreationTimestamp
	private Instant createdAt;

	public TimelineEventEntity(ApplicationEntity application, NewTimelineEvent event) {
		this.application = application;
		this.eventDate = event.eventDate();
		this.stageCategory = event.stageCategory();
		this.stageLabel = event.stageLabel();
		this.note = event.note();
		this.rejectionReason = event.rejectionReason();
	}

}
