package se.sundsvall.relations.service.mapper;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.api.model.ResourceIdentifier;
import se.sundsvall.relations.integration.db.model.RelationEntity;
import se.sundsvall.relations.integration.db.model.ResourceIdentifierEntity;

class RelationMapperTest {

	private final RelationMapper mapper = new RelationMapper();

	@Test
	void toRelationEntity() {
		final var municipalityId = "municipalityId";
		final var type = "type";
		final var sourceResourceId = "sourceResourceId";
		final var sourceType = "sourceType";
		final var sourceService = "sourceService";
		final var sourceNamespace = "sourceNamespace";
		final var targetResourceId = "targetResourceId";
		final var targetType = "targetType";
		final var targetService = "targetService";
		final var targetNamespace = "targetNamespace";
		final var relation = Relation.builder()
			.withType(type)
			.withSource(ResourceIdentifier.builder()
				.withResourceId(sourceResourceId)
				.withService(sourceService)
				.withType(sourceType)
				.withNamespace(sourceNamespace)
				.build())
			.withTarget(ResourceIdentifier.builder()
				.withResourceId(targetResourceId)
				.withService(targetService)
				.withType(targetType)
				.withNamespace(targetNamespace)
				.build())
			.build();

		assertThat(relation).hasNoNullFieldsOrPropertiesExcept("id", "created", "modified");

		final var entity = mapper.toRelationEntity(municipalityId, relation);

		assertThat(entity).hasNoNullFieldsOrPropertiesExcept("id", "created", "modified");
		assertThat(entity.getType()).isEqualTo(type);
		assertThat(entity.getSource().getResourceId()).isEqualTo(sourceResourceId);
		assertThat(entity.getSource().getService()).isEqualTo(sourceService);
		assertThat(entity.getSource().getType()).isEqualTo(sourceType);
		assertThat(entity.getSource().getNamespace()).isEqualTo(sourceNamespace);
		assertThat(entity.getTarget().getResourceId()).isEqualTo(targetResourceId);
		assertThat(entity.getTarget().getService()).isEqualTo(targetService);
		assertThat(entity.getTarget().getType()).isEqualTo(targetType);
		assertThat(entity.getTarget().getNamespace()).isEqualTo(targetNamespace);
	}

	@Test
	void toRelation() {
		final var id = "id";
		final var municipalityId = "municipalityId";
		final var type = "type";
		final var created = OffsetDateTime.now();
		final var modified = OffsetDateTime.now().plusDays(1);
		final var sourceModified = OffsetDateTime.now().plusDays(2);
		final var targetModified = OffsetDateTime.now().plusDays(3);
		final var sourceResourceId = "sourceResourceId";
		final var sourceType = "sourceType";
		final var sourceService = "sourceService";
		final var sourceNamespace = "sourceNamespace";
		final var targetResourceId = "targetResourceId";
		final var targetType = "targetType";
		final var targetService = "targetService";
		final var targetNamespace = "targetNamespace";
		final var entity = RelationEntity.builder()
			.withId(id)
			.withType(type)
			.withMunicipalityId(municipalityId)
			.withCreated(created)
			.withModified(modified)
			.withSource(ResourceIdentifierEntity.builder()
				.withResourceId(sourceResourceId)
				.withService(sourceService)
				.withType(sourceType)
				.withNamespace(sourceNamespace)
				.withModified(sourceModified)
				.build())
			.withTarget(ResourceIdentifierEntity.builder()
				.withResourceId(targetResourceId)
				.withService(targetService)
				.withType(targetType)
				.withNamespace(targetNamespace)
				.withModified(targetModified)
				.build())
			.build();

		assertThat(entity).hasNoNullFieldsOrProperties();

		final var relation = mapper.toRelation(entity);

		assertThat(relation).hasNoNullFieldsOrProperties();
		assertThat(relation.getId()).isEqualTo(id);
		assertThat(relation.getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(relation.getModified()).isCloseTo(OffsetDateTime.now().plusDays(3), within(2, SECONDS));
		assertThat(relation.getType()).isEqualTo(type);
		assertThat(relation.getSource().getResourceId()).isEqualTo(sourceResourceId);
		assertThat(relation.getSource().getService()).isEqualTo(sourceService);
		assertThat(relation.getSource().getType()).isEqualTo(sourceType);
		assertThat(relation.getSource().getNamespace()).isEqualTo(sourceNamespace);
		assertThat(relation.getTarget().getResourceId()).isEqualTo(targetResourceId);
		assertThat(relation.getTarget().getService()).isEqualTo(targetService);
		assertThat(relation.getTarget().getType()).isEqualTo(targetType);
		assertThat(relation.getTarget().getNamespace()).isEqualTo(targetNamespace);
	}
}
