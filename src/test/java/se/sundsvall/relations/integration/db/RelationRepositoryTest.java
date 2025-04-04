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

@SpringBootTest
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class RelationRepositoryTest {

	@Autowired
	RelationRepository repository;

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
			.withSourceId(sourceId)
			.withSourceType(sourceType)
			.withSourceService(sourceService)
			.withSourceNamespace(sourceNamespace)
			.withTargetId(targetId)
			.withTargetType(targetType)
			.withTargetService(targetService)
			.withTargetNamespace(targetNamespace)
			.build();

		final var persistedEntity = repository.save(relationEntity);

		assertThat(persistedEntity).isNotNull();
		assertThat(persistedEntity.getId()).isNotNull();
		assertThat(persistedEntity.getType()).isEqualTo(type);
		assertThat(persistedEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(persistedEntity.getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(persistedEntity.getModified()).isNull();
		assertThat(persistedEntity.getSourceId()).isEqualTo(sourceId);
		assertThat(persistedEntity.getSourceType()).isEqualTo(sourceType);
		assertThat(persistedEntity.getSourceService()).isEqualTo(sourceService);
		assertThat(persistedEntity.getSourceNamespace()).isEqualTo(sourceNamespace);
		assertThat(persistedEntity.getTargetId()).isEqualTo(targetId);
		assertThat(persistedEntity.getTargetType()).isEqualTo(targetType);
		assertThat(persistedEntity.getTargetService()).isEqualTo(targetService);
		assertThat(persistedEntity.getTargetNamespace()).isEqualTo(targetNamespace);

	}

	@Test
	void update() {

		final var entity = repository.findById("1");
		final var newSourceNamespace = "newSourceNamespace";

		entity.get().setSourceNamespace(newSourceNamespace);
		final var updatedEntity = repository.save(entity.get());
		repository.flush();

		assertThat(updatedEntity).isNotNull();
		assertThat(updatedEntity.getSourceNamespace()).isEqualTo(newSourceNamespace);
		assertThat(updatedEntity.getModified()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
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

		final Specification<RelationEntity> specification = filterSpecificationConverter.convert("(sourceId : 'source_id-2')");
		final Pageable pageable = PageRequest.of(0, 10);

		final var relations = repository.findAll(specification, pageable);

		assertThat(relations).isNotNull();
		assertThat(relations.getTotalElements()).isEqualTo(1);
		assertThat(relations).extracting(RelationEntity::getId, RelationEntity::getMunicipalityId, RelationEntity::getSourceId).containsExactly(
			tuple("2", "2281", "source_id-2"));
	}

	@Test
	void readWithSpecificationNotFound() {
		final Specification<RelationEntity> specification = filterSpecificationConverter.convert("(sourceId : 'DOES_NOT_EXIST')");
		final Pageable pageable = PageRequest.of(0, 10);

		final var relations = repository.findAll(specification, pageable);

		assertThat(relations).isNotNull();
		assertThat(relations.getTotalElements()).isEqualTo(0);
	}
}
