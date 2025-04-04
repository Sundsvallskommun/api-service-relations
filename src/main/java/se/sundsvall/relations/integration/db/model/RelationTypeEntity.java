package se.sundsvall.relations.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Data
@Builder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "relation_type",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_relation_type", columnNames = {
			"type"
		}),
		@UniqueConstraint(name = "uq_relation_counter_type", columnNames = {
			"counter_type"
		})
	})
public class RelationTypeEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "type_display_name")
	private String typeDisplayName;

	@Column(name = "counter_type")
	private String counterType;

	@Column(name = "counter_type_display_name")
	private String counterTypeDisplayName;
}
