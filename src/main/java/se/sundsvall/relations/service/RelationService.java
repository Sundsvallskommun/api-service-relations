package se.sundsvall.relations.service;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.api.model.RelationPageParameters;
import se.sundsvall.relations.api.model.RelationPagedResponse;
import se.sundsvall.relations.integration.db.RelationRepository;
import se.sundsvall.relations.integration.db.RelationTypeRepository;
import se.sundsvall.relations.integration.db.model.RelationEntity;
import se.sundsvall.relations.service.mapper.RelationMapper;

import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;

@Service
public class RelationService {

	private static final String NOT_FOUND_MSG = "Relation with id '%s' not found";
	private static final String INVALID_TYPE = "'%s' is not a valid type";

	private RelationRepository relationRepository;
	private RelationTypeRepository relationTypeRepository;
	private RelationMapper mapper;

	public RelationService(RelationRepository relationRepository, RelationTypeRepository relationTypeRepository, RelationMapper mapper) {
		this.relationRepository = relationRepository;
		this.relationTypeRepository = relationTypeRepository;
		this.mapper = mapper;
	}

	public String createRelation(String municipalityId, Relation relation) {
		final var type = relationTypeRepository.findByName(relation.getType())
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, INVALID_TYPE.formatted(relation.getType())));

		final var primaryRelation = mapper.toRelationEntity(municipalityId, relation, type);

		if (type.getCounterType() != null) {
			primaryRelation.setInverseRelation(mapper.toInverseRelationEntity(primaryRelation));
		}

		return relationRepository.save(primaryRelation).getId();
	}

	public RelationPagedResponse findRelations(String municipalityId, Specification<RelationEntity> filter, RelationPageParameters pageParameters) {
		final var filterWithMunicipalityId = withMunicipalityId(municipalityId).and(filter);
		final var matches = relationRepository.findAll(filterWithMunicipalityId, PageRequest.of(pageParameters.getPage() - 1, pageParameters.getLimit(), pageParameters.sort()));
		final List<Relation> relations = matches.stream().map(mapper::toRelation).toList();

		return RelationPagedResponse.builder()
			.withRelations(relations)
			.withMetaData(PagingAndSortingMetaData.create().withPageData(matches))
			.build();
	}

	public Relation saveRelation(String municipalityId, Relation relation) {
		final var entity = relationRepository.findByIdAndMunicipalityId(relation.getId(), municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(relation.getId())));
		final var type = relationTypeRepository.findByName(relation.getType())
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, INVALID_TYPE.formatted(relation.getType())));

		mapper.updateRelationEntity(entity, relation, type);

		// Update from two-way relation type to oneway relation type
		if (type.getCounterType() == null && entity.getInverseRelation() != null) {
			relationRepository.delete(entity.getInverseRelation());
		}

		return mapper.toRelation(relationRepository.save(entity));
	}

	public void deleteRelation(String municipalityId, String id) {

		final var entity = relationRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(id)));

		relationRepository.delete(entity);
	}

	public Relation getRelation(String municipalityId, String id) {
		final var entity = relationRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(id)));

		return mapper.toRelation(entity);
	}

	public static Specification<RelationEntity> withMunicipalityId(String municipalityId) {
		return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("municipalityId"), municipalityId);
	}
}
