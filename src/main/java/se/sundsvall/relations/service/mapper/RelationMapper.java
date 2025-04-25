package se.sundsvall.relations.service.mapper;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.api.model.ResourceIdentifier;
import se.sundsvall.relations.integration.db.model.RelationEntity;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;
import se.sundsvall.relations.integration.db.model.ResourceIdentifierEntity;

@Component
public class RelationMapper {

	public RelationEntity toInverseRelationEntity(RelationEntity primary) {
		return RelationEntity.builder()
			.withMunicipalityId(primary.getMunicipalityId())
			.withType(primary.getType().getCounterType())
			.withSource(primary.getTarget())
			.withTarget(primary.getSource())
			.withInverseRelation(primary)
			.build();
	}

	public RelationEntity toRelationEntity(String municipalityId, Relation relation, RelationTypeEntity type) {
		return RelationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withType(type)
			.withSource(toResourceIdentifierEntity(relation.getSource()))
			.withTarget(toResourceIdentifierEntity(relation.getTarget()))
			.build();
	}

	public void updateRelationEntity(RelationEntity relationEntity, Relation relation, RelationTypeEntity type) {
		relationEntity.setType(type);
		relationEntity.setSource(updateResourceIdentifierEntity(relationEntity.getSource(), relation.getSource()));
		relationEntity.setTarget(updateResourceIdentifierEntity(relationEntity.getTarget(), relation.getTarget()));

		if (type.getCounterType() != null) {
			if (relationEntity.getInverseRelation() == null) {
				// Update from oneway relation to two-way relation
				relationEntity.setInverseRelation(toInverseRelationEntity(relationEntity));
			}
			// Update RelationType
			relationEntity.getInverseRelation().setType(type.getCounterType());
		}
	}

	private ResourceIdentifierEntity updateResourceIdentifierEntity(ResourceIdentifierEntity entity, ResourceIdentifier identifier) {
		entity.setResourceId(identifier.getResourceId());
		entity.setType(identifier.getType());
		entity.setService(identifier.getService());
		entity.setNamespace(identifier.getNamespace());
		return entity;
	}

	private ResourceIdentifierEntity toResourceIdentifierEntity(ResourceIdentifier identifier) {
		return ResourceIdentifierEntity.builder()
			.withResourceId(identifier.getResourceId())
			.withType(identifier.getType())
			.withService(identifier.getService())
			.withNamespace(identifier.getNamespace())
			.build();
	}

	public Relation toRelation(RelationEntity entity) {
		return Relation.builder()
			.withId(entity.getId())
			.withType(entity.getType().getName())
			.withSource(toResourceIdentifier(entity.getSource()))
			.withTarget(toResourceIdentifier(entity.getTarget()))
			.withCreated(entity.getCreated())
			.withModified(getLatestModified(entity))
			.build();
	}

	private ResourceIdentifier toResourceIdentifier(ResourceIdentifierEntity entity) {
		return ResourceIdentifier.builder()
			.withResourceId(entity.getResourceId())
			.withService(entity.getService())
			.withNamespace(entity.getNamespace())
			.withType(entity.getType())
			.build();
	}

	private OffsetDateTime getLatestModified(RelationEntity entity) {
		return Stream.of(entity.getModified(), entity.getSource().getModified(), entity.getTarget().getModified())
			.filter(Objects::nonNull)
			.max(OffsetDateTime::compareTo)
			.orElse(null);
	}
}
