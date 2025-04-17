package se.sundsvall.relations.service;

import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;

import java.util.List;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.relations.api.model.RelationType;
import se.sundsvall.relations.integration.db.RelationTypeRepository;
import se.sundsvall.relations.service.mapper.RelationTypeMapper;

@Service
public class RelationTypeService {
	private static final String NOT_FOUND_MSG = "Relation type with type '%s' not found";
	private static final String DUPLICATE_TYPE_MSG = "Relation type with type '%s' already exists";
	private static final String DUPLICATE_COUNTER_TYPE_MSG = "Relation type with counter type '%s' already exists";
	private static final String SAME_TYPE_AND_COUNTER_TYPE_MSG = "Type and counter type cannot be the same: '%s'";
	private static final String VALUE_ALREADY_EXISTS_MSG = "Value '%s' already exists as a type or counter type.";

	private final RelationTypeRepository relationTypeRepository;
	private final RelationTypeMapper mapper;

	public RelationTypeService(RelationTypeRepository relationTypeRepository, RelationTypeMapper mapper) {
		this.relationTypeRepository = relationTypeRepository;
		this.mapper = mapper;
	}

	public String createType(RelationType type) {
		if (type.getType().equalsIgnoreCase(type.getCounterType())) {
			throw Problem.valueOf(CONFLICT, SAME_TYPE_AND_COUNTER_TYPE_MSG.formatted(type.getType()));
		}
		if (relationTypeRepository.existsByTypeOrCounterType(type.getType())) {
			throw Problem.valueOf(CONFLICT, VALUE_ALREADY_EXISTS_MSG.formatted(type.getType()));
		}
		if (type.getCounterType() != null && relationTypeRepository.existsByTypeOrCounterType(type.getCounterType())) {
			throw Problem.valueOf(CONFLICT, VALUE_ALREADY_EXISTS_MSG.formatted(type.getCounterType()));
		}
		return relationTypeRepository.save(mapper.toRelationTypeEntity(type)).getId();
	}

	public RelationType getType(String type) {
		return relationTypeRepository.findByType(type)
			.map(mapper::toRelationType)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(type)));
	}

	public List<RelationType> getAllTypes() {
		return relationTypeRepository.findAll().stream()
			.map(mapper::toRelationType)
			.toList();
	}

	public void deleteRelationType(String type) {
		final var entity = relationTypeRepository.findByType(type)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(type)));
		relationTypeRepository.delete(entity);
	}

	public RelationType saveRelationType(String type, RelationType relationType) {
		final var existingEntity = relationTypeRepository.findByType(type)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(type)));

		if (relationType.getType().equalsIgnoreCase(relationType.getCounterType())) {
			throw Problem.valueOf(CONFLICT, SAME_TYPE_AND_COUNTER_TYPE_MSG.formatted(relationType.getType()));
		}
		if (!existingEntity.getType().equalsIgnoreCase(relationType.getType()) && relationTypeRepository.existsByTypeOrCounterType(relationType.getType())) {
			throw Problem.valueOf(CONFLICT, VALUE_ALREADY_EXISTS_MSG.formatted(relationType.getType()));
		}
		if (relationType.getCounterType() != null && !existingEntity.getCounterType().equalsIgnoreCase(relationType.getCounterType()) && relationTypeRepository.existsByTypeOrCounterType(relationType.getCounterType())) {
			throw Problem.valueOf(CONFLICT, VALUE_ALREADY_EXISTS_MSG.formatted(relationType.getCounterType()));
		}

		final var updatedEntity = mapper.toRelationTypeEntity(relationType);
		updatedEntity.setId(existingEntity.getId());
		final var savedEntity = relationTypeRepository.save(updatedEntity);
		return mapper.toRelationType(savedEntity);
	}
}
