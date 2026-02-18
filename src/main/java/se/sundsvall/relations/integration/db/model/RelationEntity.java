package se.sundsvall.relations.integration.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@Table(name = "relation",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_relation_resource_source_identifier_id", columnNames = {
			"resource_source_identifier_id"
		}),
		@UniqueConstraint(name = "uq_relation_counter_resource_target_identifier_id", columnNames = {
			"resource_target_identifier_id"
		}),
		@UniqueConstraint(name = "uq_relation_inverse_relation_id", columnNames = {
			"inverse_relation_id"
		})
	})
public class RelationEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "municipality_id", nullable = false)
	private String municipalityId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_relation_type_relation_type"))
	private RelationTypeEntity type;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "modified")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime modified;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "resource_source_identifier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_relation_source_resource_identifier"))
	private ResourceIdentifierEntity source;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "resource_target_identifier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_relation_target_resource_identifier"))
	private ResourceIdentifierEntity target;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "inverse_relation_id", unique = true, foreignKey = @ForeignKey(name = "fk_relation_inverse_relation_relation"))
	private RelationEntity inverseRelation;

	@PrePersist
	void onCreate() {
		created = now(ZoneId.systemDefault()).truncatedTo(MILLIS);
	}

	@PreUpdate
	void onUpdate() {
		modified = now(ZoneId.systemDefault()).truncatedTo(MILLIS);
	}
}
