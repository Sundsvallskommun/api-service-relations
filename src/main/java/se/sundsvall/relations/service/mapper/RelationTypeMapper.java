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
		var relationTypeEntity = RelationTypeEntity.builder()
			.withName(relationType.getName())
			.withDisplayName(relationType.getDisplayName())
			.build();

		if (relationType.getCounterName() != null) {
			relationTypeEntity.setCounterType(RelationTypeEntity.builder()
				.withName(relationType.getCounterName())
				.withDisplayName(relationType.getCounterDisplayName())
				.withCounterType(relationTypeEntity)
				.build());
		}
		return relationTypeEntity;
	}

	public RelationType toRelationType(RelationTypeEntity relationTypeEntity) {
		if (relationTypeEntity == null) {
			return null;
		}
		var relationType = RelationType.builder()
			.withName(relationTypeEntity.getName())
			.withDisplayName(relationTypeEntity.getDisplayName())
			.build();
		if (relationTypeEntity.getCounterType() != null) {
			relationType.setCounterName(relationTypeEntity.getCounterType().getName());
			relationType.setCounterDisplayName(relationTypeEntity.getCounterType().getDisplayName());
		}
		return relationType;
	}
}
