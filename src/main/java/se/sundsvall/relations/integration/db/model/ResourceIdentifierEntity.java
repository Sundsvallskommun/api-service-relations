package se.sundsvall.relations.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

@Data
@Builder(setterPrefix = "with")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resource_identifier")
public class ResourceIdentifierEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "modified")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime modified;

	@Column(name = "resource_id", nullable = false)
	private String resourceId;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "service", nullable = false)
	private String service;

	@Column(name = "namespace")
	private String namespace;

	@PreUpdate
	void onUpdate() {
		modified = now(ZoneId.systemDefault()).truncatedTo(MILLIS);
	}
}
