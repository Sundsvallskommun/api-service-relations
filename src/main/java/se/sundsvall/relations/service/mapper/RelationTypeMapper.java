package se.sundsvall.relations.service.mapper;

import org.springframework.stereotype.Component;
import se.sundsvall.relations.api.model.RelationType;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;

@Component
public class RelationTypeMapper {

	public RelationTypeEntity toRelationTypeEntity(RelationType relationType) {
		if (relationType == null) {
			return null;
		}
		return RelationTypeEntity.builder()
			.withType(relationType.getType())
			.withTypeDisplayName(relationType.getTypeDisplayName())
			.withCounterType(relationType.getCounterType())
			.withCounterTypeDisplayName(relationType.getCounterTypeDisplayName())
			.build();
	}

	public RelationType toRelationType(RelationTypeEntity relationTypeEntity) {
		if (relationTypeEntity == null) {
			return null;
		}
		return RelationType.builder()
			.withType(relationTypeEntity.getType())
			.withTypeDisplayName(relationTypeEntity.getTypeDisplayName())
			.withCounterType(relationTypeEntity.getCounterType())
			.withCounterTypeDisplayName(relationTypeEntity.getCounterTypeDisplayName())
			.build();
	}
}
