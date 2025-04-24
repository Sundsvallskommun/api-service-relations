package se.sundsvall.relations.service;

import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;

import java.util.List;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.relations.api.model.RelationType;
import se.sundsvall.relations.integration.db.RelationRepository;
import se.sundsvall.relations.integration.db.RelationTypeRepository;
import se.sundsvall.relations.service.mapper.RelationTypeMapper;

@Service
public class RelationTypeService {
	private static final String NOT_FOUND_MSG = "Relation type with type '%s' not found";
	private static final String SAME_TYPE_AND_COUNTER_TYPE_MSG = "Type and counter type cannot be the same: '%s'";
	private static final String VALUE_ALREADY_EXISTS_MSG = "Value '%s' already exists as a type or counter type.";
	private static final String TYPE_IS_USED_BY_RELATIONS_MSG = "Type '%s' is used by one or many Relations";

	private final RelationTypeRepository relationTypeRepository;
	private final RelationRepository relationRepository;
	private final RelationTypeMapper mapper;

	public RelationTypeService(RelationTypeRepository relationTypeRepository, RelationRepository relationRepository, RelationTypeMapper mapper) {
		this.relationTypeRepository = relationTypeRepository;
		this.relationRepository = relationRepository;
		this.mapper = mapper;
	}

	public String createType(RelationType type) {
		if (type.getName().equalsIgnoreCase(type.getCounterName())) {
			throw Problem.valueOf(CONFLICT, SAME_TYPE_AND_COUNTER_TYPE_MSG.formatted(type.getName()));
		}
		if (relationTypeRepository.existsByName(type.getName())) {
			throw Problem.valueOf(CONFLICT, VALUE_ALREADY_EXISTS_MSG.formatted(type.getName()));
		}
		if (type.getCounterName() != null && relationTypeRepository.existsByName(type.getCounterName())) {
			throw Problem.valueOf(CONFLICT, VALUE_ALREADY_EXISTS_MSG.formatted(type.getCounterName()));
		}
		return relationTypeRepository.save(mapper.toRelationTypeEntity(type)).getName();
	}

	public RelationType getType(String type) {
		return relationTypeRepository.findByName(type)
			.map(mapper::toRelationType)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(type)));
	}

	public List<RelationType> getAllTypes() {
		return relationTypeRepository.findAll().stream()
			.map(mapper::toRelationType)
			.toList();
	}

	public void deleteRelationType(String type) {
		final var entity = relationTypeRepository.findByName(type)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(type)));
		if (relationRepository.existsByType(entity)) {
			throw Problem.valueOf(CONFLICT, TYPE_IS_USED_BY_RELATIONS_MSG.formatted(entity.getName()));
		}
		relationTypeRepository.delete(entity);
	}

	public RelationType saveRelationType(String type, RelationType relationType) {
		final var existingEntity = relationTypeRepository.findByName(type)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(type)));

		if (relationType.getName().equalsIgnoreCase(relationType.getCounterName())) {
			throw Problem.valueOf(CONFLICT, SAME_TYPE_AND_COUNTER_TYPE_MSG.formatted(relationType.getName()));
		}
		if (!existingEntity.getName().equalsIgnoreCase(relationType.getName()) && relationTypeRepository.existsByName(relationType.getName())) {
			throw Problem.valueOf(CONFLICT, VALUE_ALREADY_EXISTS_MSG.formatted(relationType.getName()));
		}

		boolean relationCounterTypeExists = relationType.getCounterName() != null;
		boolean existingCounterTypeIsDifferent = !(existingEntity.getCounterType() != null && existingEntity.getCounterType().getName().equalsIgnoreCase(relationType.getCounterName()));

		if (relationCounterTypeExists && existingCounterTypeIsDifferent && relationTypeRepository.existsByName(relationType.getCounterName())) {
			throw Problem.valueOf(CONFLICT, VALUE_ALREADY_EXISTS_MSG.formatted(relationType.getCounterName()));
		}

		final var updatedEntity = mapper.toRelationTypeEntity(relationType);

		updatedEntity.setId(existingEntity.getId());
		if (existingEntity.getCounterType() != null) {
			if (updatedEntity.getCounterType() != null) {
				updatedEntity.getCounterType().setId(existingEntity.getCounterType().getId());
			} else if (relationRepository.existsByType(existingEntity.getCounterType())) {
				throw Problem.valueOf(CONFLICT, TYPE_IS_USED_BY_RELATIONS_MSG.formatted(existingEntity.getCounterType().getName()));
			}
		}

		return mapper.toRelationType(relationTypeRepository.save(updatedEntity));
	}
}
