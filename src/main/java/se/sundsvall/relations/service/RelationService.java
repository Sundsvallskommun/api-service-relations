package se.sundsvall.relations.service;

import static org.zalando.problem.Status.NOT_FOUND;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.api.model.RelationPageParameters;
import se.sundsvall.relations.api.model.RelationPagedResponse;
import se.sundsvall.relations.integration.db.RelationRepository;
import se.sundsvall.relations.integration.db.model.RelationEntity;
import se.sundsvall.relations.service.mapper.RelationMapper;

@Service
public class RelationService {

	private static final String NOT_FOUND_MSG = "Relation with id '%s' not found";

	@Autowired
	private RelationRepository relationRepository;

	@Autowired
	RelationMapper mapper;

	public String createRelation(String municipalityId, Relation relation) {
		return relationRepository.save(mapper.toRelationEntity(municipalityId, relation)).getId();
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

		final var replacement = mapper.toRelationEntity(municipalityId, relation);

		// Transfer properties not covered my mapper
		replacement.setId(entity.getId());
		replacement.setCreated(entity.getCreated());
		replacement.setModified(entity.getModified());
		replacement.getSource().setModified(entity.getSource().getModified());
		replacement.getTarget().setModified(entity.getTarget().getModified());

		return mapper.toRelation(relationRepository.save(replacement));
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
