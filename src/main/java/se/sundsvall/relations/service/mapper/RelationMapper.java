package se.sundsvall.relations.service.mapper;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.api.model.ResourceIdentifier;
import se.sundsvall.relations.integration.db.model.RelationEntity;
import se.sundsvall.relations.integration.db.model.ResourceIdentifierEntity;

@Component
public class RelationMapper {

	public RelationEntity toRelationEntity(String municipalityId, Relation relation) {
		return RelationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withType(relation.getType())
			.withSource(toResourceIdentifierEntity(relation.getSource()))
			.withTarget(toResourceIdentifierEntity(relation.getTarget()))
			.build();
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
			.withType(entity.getType())
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
