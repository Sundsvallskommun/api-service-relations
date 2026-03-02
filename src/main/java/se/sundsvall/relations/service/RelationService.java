package se.sundsvall.relations.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.api.model.RelationPageParameters;
import se.sundsvall.relations.api.model.RelationPagedResponse;
import se.sundsvall.relations.integration.db.RelationRepository;
import se.sundsvall.relations.integration.db.RelationTypeRepository;
import se.sundsvall.relations.integration.db.model.RelationEntity;
import se.sundsvall.relations.service.mapper.RelationMapper;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class RelationService {

	private static final String NOT_FOUND_MSG = "Relation with id '%s' not found";
	private static final String INVALID_TYPE = "'%s' is not a valid type";

	private final RelationRepository relationRepository;
	private final RelationTypeRepository relationTypeRepository;
	private final RelationMapper mapper;

	public RelationService(final RelationRepository relationRepository, final RelationTypeRepository relationTypeRepository, final RelationMapper mapper) {
		this.relationRepository = relationRepository;
		this.relationTypeRepository = relationTypeRepository;
		this.mapper = mapper;
	}

	public static Specification<RelationEntity> withMunicipalityId(final String municipalityId) {
		return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("municipalityId"), municipalityId);
	}

	public String createRelation(final String municipalityId, final Relation relation) {
		final var type = relationTypeRepository.findByName(relation.getType())
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, INVALID_TYPE.formatted(relation.getType())));

		final var primaryRelation = mapper.toRelationEntity(municipalityId, relation, type);

		if (type.getCounterType() != null) {
			primaryRelation.setInverseRelation(mapper.toInverseRelationEntity(primaryRelation));
		}

		return relationRepository.save(primaryRelation).getId();
	}

	public RelationPagedResponse findRelations(final String municipalityId, final Specification<RelationEntity> filter, final RelationPageParameters pageParameters) {
		final var filterWithMunicipalityId = Optional.ofNullable(filter)
			.map(withMunicipalityId(municipalityId)::and)
			.orElse(withMunicipalityId(municipalityId));

		final var matches = relationRepository.findAll(filterWithMunicipalityId, PageRequest.of(pageParameters.getPage() - 1, pageParameters.getLimit(), pageParameters.sort()));
		final List<Relation> relations = matches.stream().map(mapper::toRelation).toList();

		return RelationPagedResponse.builder()
			.withRelations(relations)
			.withMetaData(PagingAndSortingMetaData.create().withPageData(matches))
			.build();
	}

	public Relation saveRelation(final String municipalityId, final Relation relation) {
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

	public void deleteRelation(final String municipalityId, final String id) {

		final var entity = relationRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(id)));

		relationRepository.delete(entity);
	}

	public Relation getRelation(final String municipalityId, final String id) {
		final var entity = relationRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOT_FOUND_MSG.formatted(id)));

		return mapper.toRelation(entity);
	}
}
