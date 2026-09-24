package com.jobtracker.jobs.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.jobtracker.jobs.dto.NewApplication;
import com.jobtracker.jobs.enums.ApplicationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public class ApplicationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// A plain id, not a relation: modules reference each other's data by id, never by entity.
	private long userId;

	private String title;

	private String company;

	private String description;

	private String link;

	private String location;

	private String salary;

	private String employmentType;

	private String source;

	@JdbcTypeCode(SqlTypes.JSON)
	private String rawPayload;

	// Denormalized from the latest timeline event, so the list can filter without joining.
	@Enumerated(EnumType.STRING)
	private ApplicationStatus currentStatus;

	private Instant capturedAt;

	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("eventDate ASC, id ASC")
	private List<TimelineEventEntity> events = new ArrayList<>();

	public ApplicationEntity(NewApplication application, Instant capturedAt) {
		this.userId = application.userId();
		this.title = application.title();
		this.company = application.company();
		this.description = application.description();
		this.link = application.link();
		this.location = application.location();
		this.salary = application.salary();
		this.employmentType = application.employmentType();
		this.source = application.source();
		this.rawPayload = application.rawPayload();
		this.currentStatus = application.initialStatus();
		this.capturedAt = capturedAt;
	}

}
