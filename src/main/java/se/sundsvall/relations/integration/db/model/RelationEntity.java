package se.sundsvall.relations.integration.db.model;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.UuidGenerator;

@Data
@Builder(setterPrefix = "with")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "relation")
public class RelationEntity {

	// Relation
	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "municipality_id", nullable = false)
	private String municipalityId;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "modified")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime modified;

	// Source
	@Column(name = "source_id", nullable = false)
	private String sourceId;

	@Column(name = "source_type", nullable = false)
	private String sourceType;

	@Column(name = "source_service", nullable = false)
	private String sourceService;

	@Column(name = "source_namespace")
	private String sourceNamespace;

	// Target
	@Column(name = "target_id", nullable = false)
	private String targetId;

	@Column(name = "target_type", nullable = false)
	private String targetType;

	@Column(name = "target_service", nullable = false)
	private String targetService;

	@Column(name = "target_namespace")
	private String targetNamespace;

	@PrePersist
	void onCreate() {
		created = now(ZoneId.systemDefault()).truncatedTo(MILLIS);
	}

	@PreUpdate
	void onUpdate() {
		modified = now(ZoneId.systemDefault()).truncatedTo(MILLIS);
	}
}
