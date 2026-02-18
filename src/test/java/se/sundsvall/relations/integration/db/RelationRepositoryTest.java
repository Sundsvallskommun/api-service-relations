package se.sundsvall.relations.integration.db;

import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.relations.integration.db.model.RelationEntity;
import se.sundsvall.relations.integration.db.model.ResourceIdentifierEntity;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class RelationRepositoryTest {

	@Autowired
	private RelationRepository repository;

	@Autowired
	private RelationTypeRepository typeRepository;

	@Autowired
	private FilterSpecificationConverter filterSpecificationConverter;

	@Test
	void create() {
		final var type = typeRepository.findById("rt1").orElseThrow(() -> new RuntimeException("Error in test data"));
		final var municipalityId = "municipalityId";
		final var sourceId = "sourceId";
		final var sourceType = "sourceType";
		final var sourceService = "sourceService";
		final var sourceNamespace = "sourceNamespace";
		final var targetId = "targetId";
		final var targetType = "targetType";
		final var targetService = "targetService";
		final var targetNamespace = "targetNamespace";

		final var relationEntity = RelationEntity.builder()
			.withType(type)
			.withMunicipalityId(municipalityId)
			.withSource(ResourceIdentifierEntity.builder()
				.withResourceId(sourceId)
				.withType(sourceType)
				.withService(sourceService)
				.withNamespace(sourceNamespace).build())
			.withTarget(ResourceIdentifierEntity.builder()
				.withResourceId(targetId)
				.withType(targetType)
				.withService(targetService)
				.withNamespace(targetNamespace).build())
			.build();

		final var inverseEntity = RelationEntity.builder()
			.withType(type.getCounterType())
			.withMunicipalityId(municipalityId)
			.withSource(relationEntity.getTarget())
			.withTarget(relationEntity.getSource())
			.withInverseRelation(relationEntity)
			.build();

		relationEntity.setInverseRelation(inverseEntity);

		final var persistedEntity = repository.save(relationEntity);

		assertThat(persistedEntity).isNotNull();
		assertThat(persistedEntity.getId()).isNotNull();
		assertThat(persistedEntity.getType().getName()).isEqualTo("type-1");
		assertThat(persistedEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(persistedEntity.getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(persistedEntity.getModified()).isNull();
		assertThat(persistedEntity.getSource().getId()).isNotNull();
		assertThat(persistedEntity.getSource().getResourceId()).isEqualTo(sourceId);
		assertThat(persistedEntity.getSource().getType()).isEqualTo(sourceType);
		assertThat(persistedEntity.getSource().getService()).isEqualTo(sourceService);
		assertThat(persistedEntity.getSource().getNamespace()).isEqualTo(sourceNamespace);
		assertThat(persistedEntity.getTarget().getId()).isNotNull();
		assertThat(persistedEntity.getTarget().getResourceId()).isEqualTo(targetId);
		assertThat(persistedEntity.getTarget().getType()).isEqualTo(targetType);
		assertThat(persistedEntity.getTarget().getService()).isEqualTo(targetService);
		assertThat(persistedEntity.getTarget().getNamespace()).isEqualTo(targetNamespace);
		// Inverse
		assertThat(persistedEntity.getInverseRelation().getId()).isNotNull();
		assertThat(persistedEntity.getInverseRelation().getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(persistedEntity.getInverseRelation()).isSameAs(inverseEntity);
		assertThat(persistedEntity.getInverseRelation().getInverseRelation()).isSameAs(persistedEntity);
		assertThat(persistedEntity.getSource().getId()).isEqualTo(persistedEntity.getInverseRelation().getTarget().getId());
		assertThat(persistedEntity.getTarget().getId()).isEqualTo(persistedEntity.getInverseRelation().getSource().getId());
	}

	@Test
	void update() {

		final var entity = repository.findById("1").orElseThrow(() -> new RuntimeException("Error in test data"));
		final var newSourceNamespace = "newSourceNamespace";
		final var newTyp = typeRepository.findById("rt3").orElseThrow(() -> new RuntimeException("Error in test data"));

		assertThat(entity.getType().getId()).isEqualTo("rt1");

		entity.getSource().setNamespace(newSourceNamespace);
		entity.setType(newTyp);
		final var updatedEntity = repository.save(entity);
		repository.flush();

		assertThat(updatedEntity).isNotNull();
		assertThat(updatedEntity.getType()).isEqualTo(newTyp);
		assertThat(updatedEntity.getModified()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(updatedEntity.getSource().getNamespace()).isEqualTo(newSourceNamespace);
		assertThat(updatedEntity.getSource().getModified()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));

		// Inverse
		assertThat(updatedEntity.getInverseRelation().getTarget().getNamespace()).isEqualTo(newSourceNamespace);
		assertThat(updatedEntity.getInverseRelation().getTarget().getModified()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
	}

	@Test
	void delete() {

		assertThat(repository.findById("1")) // Primary
			.isNotEmpty()
			.hasValueSatisfying(relation -> relation.getType().getName().equalsIgnoreCase("type-1"));

		assertThat(repository.findById("2")).isNotEmpty(); // Inverse

		repository.deleteById("1");
		repository.flush();

		assertThat(repository.findById("1")).isEmpty();
		assertThat(repository.findById("2")).isEmpty();
		// Ensure that deletion does not cascade to RelationType
		assertThat(typeRepository.existsByName("type-1")).isTrue();
	}

	@Test
	void read() {
		final var entity = repository.findByIdAndMunicipalityId("1", "2281");

		assertThat(entity).isNotEmpty();
		assertThat(entity.get().getType().getName()).isEqualTo("type-1");
	}

	@Test
	void readWithSpecification() {

		final Specification<RelationEntity> specification = filterSpecificationConverter.convert("(source.resourceId : 'source_id-2')");
		final Pageable pageable = PageRequest.of(0, 10);

		final var relations = repository.findAll(specification, pageable);

		assertThat(relations).isNotNull();
		assertThat(relations.getTotalElements()).isEqualTo(1);
		assertThat(relations).extracting(RelationEntity::getId, RelationEntity::getMunicipalityId).containsExactly(
			tuple("3", "2281"));
	}

	@Test
	void readWithSpecificationNotFound() {
		final Specification<RelationEntity> specification = filterSpecificationConverter.convert("(source.resourceId : 'DOES_NOT_EXIST')");
		final Pageable pageable = PageRequest.of(0, 10);

		final var relations = repository.findAll(specification, pageable);

		assertThat(relations).isNotNull();
		assertThat(relations.getTotalElements()).isZero();
	}

	@Test
	void existsByType() {
		final var rt1 = typeRepository.findById("rt1").orElseThrow(() -> new RuntimeException("Error in test data"));
		final var rt5 = typeRepository.findById("rt5").orElseThrow(() -> new RuntimeException("Error in test data"));

		assertThat(repository.existsByType(rt1)).isTrue();
		assertThat(repository.existsByType(rt5)).isFalse();
	}
}
