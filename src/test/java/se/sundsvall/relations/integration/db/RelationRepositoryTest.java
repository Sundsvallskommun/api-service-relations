package se.sundsvall.relations.integration.db;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;

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
	private FilterSpecificationConverter filterSpecificationConverter;

	@Test
	void create() {
		final var type = "type";
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

		final var persistedEntity = repository.save(relationEntity);

		assertThat(persistedEntity).isNotNull();
		assertThat(persistedEntity.getId()).isNotNull();
		assertThat(persistedEntity.getType()).isEqualTo(type);
		assertThat(persistedEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(persistedEntity.getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(persistedEntity.getModified()).isNull();
		assertThat(persistedEntity.getSource().getResourceId()).isEqualTo(sourceId);
		assertThat(persistedEntity.getSource().getType()).isEqualTo(sourceType);
		assertThat(persistedEntity.getSource().getService()).isEqualTo(sourceService);
		assertThat(persistedEntity.getSource().getNamespace()).isEqualTo(sourceNamespace);
		assertThat(persistedEntity.getTarget().getResourceId()).isEqualTo(targetId);
		assertThat(persistedEntity.getTarget().getType()).isEqualTo(targetType);
		assertThat(persistedEntity.getTarget().getService()).isEqualTo(targetService);
		assertThat(persistedEntity.getTarget().getNamespace()).isEqualTo(targetNamespace);

	}

	@Test
	void update() {

		final var entity = repository.findById("1");
		final var newSourceNamespace = "newSourceNamespace";
		final var newTyp = "newType";

		entity.get().getSource().setNamespace(newSourceNamespace);
		entity.get().setType(newTyp);
		final var updatedEntity = repository.save(entity.get());
		repository.flush();

		assertThat(updatedEntity).isNotNull();
		assertThat(updatedEntity.getType()).isEqualTo(newTyp);
		assertThat(updatedEntity.getModified()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(updatedEntity.getSource().getNamespace()).isEqualTo(newSourceNamespace);
		assertThat(updatedEntity.getSource().getModified()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
	}

	@Test
	void delete() {

		assertThat(repository.findById("1")).isNotEmpty();

		repository.deleteById("1");
		repository.flush();

		assertThat(repository.findById("1")).isEmpty();
	}

	@Test
	void read() {
		final var entity = repository.findByIdAndMunicipalityId("1", "2281");

		assertThat(entity).isNotEmpty();
		assertThat(entity.get().getType()).isEqualTo("type-1");
	}

	@Test
	void readWithSpecification() {

		final Specification<RelationEntity> specification = filterSpecificationConverter.convert("(source.resourceId : 'source_id-2')");
		final Pageable pageable = PageRequest.of(0, 10);

		final var relations = repository.findAll(specification, pageable);

		assertThat(relations).isNotNull();
		assertThat(relations.getTotalElements()).isEqualTo(1);
		assertThat(relations).extracting(RelationEntity::getId, RelationEntity::getMunicipalityId).containsExactly(
			tuple("2", "2281"));
	}

	@Test
	void readWithSpecificationNotFound() {
		final Specification<RelationEntity> specification = filterSpecificationConverter.convert("(source.resourceId : 'DOES_NOT_EXIST')");
		final Pageable pageable = PageRequest.of(0, 10);

		final var relations = repository.findAll(specification, pageable);

		assertThat(relations).isNotNull();
		assertThat(relations.getTotalElements()).isZero();
	}
}
