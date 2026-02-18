package se.sundsvall.relations.service.mapper;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.api.model.ResourceIdentifier;
import se.sundsvall.relations.integration.db.model.RelationEntity;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;
import se.sundsvall.relations.integration.db.model.ResourceIdentifierEntity;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RelationMapperTest {

	private final RelationMapper mapper = new RelationMapper();

	@Test
	void toRelationEntity() {
		final var municipalityId = "municipalityId";
		final var typeName = "typeName";
		final var sourceResourceId = "sourceResourceId";
		final var sourceType = "sourceType";
		final var sourceService = "sourceService";
		final var sourceNamespace = "sourceNamespace";
		final var targetResourceId = "targetResourceId";
		final var targetType = "targetType";
		final var targetService = "targetService";
		final var targetNamespace = "targetNamespace";
		final var relationType = RelationTypeEntity.builder().withName(typeName).build();
		final var relation = Relation.builder()
			.withType(typeName)
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

		assertThat(relation).hasNoNullFieldsOrPropertiesExcept("id", "created", "modified", "inverseRelation");

		final var entity = mapper.toRelationEntity(municipalityId, relation, relationType);

		assertThat(entity).hasNoNullFieldsOrPropertiesExcept("id", "created", "modified", "inverseRelation");
		assertThat(entity.getType()).isSameAs(relationType);
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
		final var typeName = "typeName";
		final var typeEntity = RelationTypeEntity.builder().withName(typeName).build();
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
			.withType(typeEntity)
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
			.withInverseRelation(RelationEntity.builder().build())
			.build();

		assertThat(entity).hasNoNullFieldsOrProperties();

		final var relation = mapper.toRelation(entity);

		assertThat(relation).hasNoNullFieldsOrProperties();
		assertThat(relation.getId()).isEqualTo(id);
		assertThat(relation.getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(relation.getModified()).isCloseTo(OffsetDateTime.now().plusDays(3), within(2, SECONDS));
		assertThat(relation.getType()).isEqualTo(typeName);
		assertThat(relation.getSource().getResourceId()).isEqualTo(sourceResourceId);
		assertThat(relation.getSource().getService()).isEqualTo(sourceService);
		assertThat(relation.getSource().getType()).isEqualTo(sourceType);
		assertThat(relation.getSource().getNamespace()).isEqualTo(sourceNamespace);
		assertThat(relation.getTarget().getResourceId()).isEqualTo(targetResourceId);
		assertThat(relation.getTarget().getService()).isEqualTo(targetService);
		assertThat(relation.getTarget().getType()).isEqualTo(targetType);
		assertThat(relation.getTarget().getNamespace()).isEqualTo(targetNamespace);
	}

	@Test
	void toInverseRelationEntity() {
		final var id = "id";
		final var municipalityId = "municipalityId";
		final var typeName = "typeName";
		final var counterTypeEntity = RelationTypeEntity.builder().build();
		final var typeEntity = RelationTypeEntity.builder().withName(typeName).withCounterType(counterTypeEntity).build();
		final var created = OffsetDateTime.now();
		final var modified = OffsetDateTime.now().plusDays(1);
		final var primarySource = ResourceIdentifierEntity.builder().build();
		final var primaryTarget = ResourceIdentifierEntity.builder().build();
		final var primaryEntity = RelationEntity.builder()
			.withId(id)
			.withType(typeEntity)
			.withMunicipalityId(municipalityId)
			.withCreated(created)
			.withModified(modified)
			.withSource(primarySource)
			.withTarget(primaryTarget)
			.build();

		final var inverseEntity = mapper.toInverseRelationEntity(primaryEntity);

		assertThat(inverseEntity).hasNoNullFieldsOrPropertiesExcept("id", "created", "modified");
		assertThat(inverseEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(inverseEntity.getType()).isSameAs(primaryEntity.getType().getCounterType());
		assertThat(inverseEntity.getSource()).isSameAs(primaryEntity.getTarget());
		assertThat(inverseEntity.getTarget()).isSameAs(primaryEntity.getSource());
		assertThat(inverseEntity.getInverseRelation()).isSameAs(primaryEntity);
	}

	@Test
	void updateRelationEntityWithNewInverse() {
		final var id = "id";
		final var municipalityId = "municipalityId";
		final var typeName = "typeName";
		final var sourceResourceId = "sourceResourceId";
		final var sourceType = "sourceType";
		final var sourceService = "sourceService";
		final var sourceNamespace = "sourceNamespace";
		final var targetResourceId = "targetResourceId";
		final var targetType = "targetType";
		final var targetService = "targetService";
		final var targetNamespace = "targetNamespace";
		final var counterType = RelationTypeEntity.builder().build();
		final var relationType = RelationTypeEntity.builder().withName(typeName).withCounterType(counterType).build();
		final var relation = Relation.builder()
			.withType(typeName)
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
		final var primarySource = ResourceIdentifierEntity.builder().build();
		final var primaryTarget = ResourceIdentifierEntity.builder().build();
		final var entity = RelationEntity.builder()
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withSource(primarySource)
			.withTarget(primaryTarget)
			.build();

		mapper.updateRelationEntity(entity, relation, relationType);

		assertThat(entity).hasNoNullFieldsOrPropertiesExcept("created", "modified");
		assertThat(entity.getId()).isEqualTo(id);
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(entity.getType()).isSameAs(relationType);
		assertThat(entity.getSource().getResourceId()).isEqualTo(sourceResourceId);
		assertThat(entity.getSource().getService()).isEqualTo(sourceService);
		assertThat(entity.getSource().getType()).isEqualTo(sourceType);
		assertThat(entity.getSource().getNamespace()).isEqualTo(sourceNamespace);
		assertThat(entity.getTarget().getResourceId()).isEqualTo(targetResourceId);
		assertThat(entity.getTarget().getService()).isEqualTo(targetService);
		assertThat(entity.getTarget().getType()).isEqualTo(targetType);
		assertThat(entity.getTarget().getNamespace()).isEqualTo(targetNamespace);

		final var inverseEntity = entity.getInverseRelation();
		assertThat(inverseEntity).hasNoNullFieldsOrPropertiesExcept("id", "created", "modified");
		assertThat(inverseEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(inverseEntity.getType()).isSameAs(entity.getType().getCounterType());
		assertThat(inverseEntity.getSource()).isSameAs(entity.getTarget());
		assertThat(inverseEntity.getTarget()).isSameAs(entity.getSource());
		assertThat(inverseEntity.getInverseRelation()).isSameAs(entity);
	}

	@Test
	void updateRelationEntityUpdatedType() {
		final var primaryId = "primaryId";
		final var inverseId = "inverseId";
		final var municipalityId = "municipalityId";
		final var typeName = "typeName";
		final var sourceResourceId = "sourceResourceId";
		final var sourceType = "sourceType";
		final var sourceService = "sourceService";
		final var sourceNamespace = "sourceNamespace";
		final var targetResourceId = "targetResourceId";
		final var targetType = "targetType";
		final var targetService = "targetService";
		final var targetNamespace = "targetNamespace";

		final var counterType = RelationTypeEntity.builder().build();
		final var relationType = RelationTypeEntity.builder().withName(typeName).withCounterType(counterType).build();
		counterType.setCounterType(relationType);
		final var newCounterType = RelationTypeEntity.builder().build();
		final var newRelationType = RelationTypeEntity.builder().withName(typeName).withCounterType(newCounterType).build();
		newCounterType.setCounterType(newRelationType);

		final var relation = Relation.builder()
			.withType(typeName)
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
		final var primarySource = ResourceIdentifierEntity.builder().build();
		final var primaryTarget = ResourceIdentifierEntity.builder().build();
		final var primaryEntity = RelationEntity.builder()
			.withId(primaryId)
			.withType(relationType)
			.withMunicipalityId(municipalityId)
			.withSource(primarySource)
			.withTarget(primaryTarget)
			.build();
		final var inverseEntity = RelationEntity.builder()
			.withId(inverseId)
			.withType(counterType)
			.withMunicipalityId(municipalityId)
			.withSource(primaryTarget)
			.withTarget(primarySource)
			.withInverseRelation(primaryEntity)
			.build();
		primaryEntity.setInverseRelation(inverseEntity);

		mapper.updateRelationEntity(primaryEntity, relation, newRelationType);

		assertThat(primaryEntity.getType()).isSameAs(newRelationType);
		assertThat(inverseEntity.getType()).isSameAs(newCounterType);
	}
}
