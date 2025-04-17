package se.sundsvall.relations.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class RelationTypeRepositoryTest {

	@Autowired
	private RelationTypeRepository repository;

	@Test
	void create() {
		final var relationType = RelationTypeEntity.builder()
			.withType("type")
			.withTypeDisplayName("typeDisplayName")
			.withCounterType("counterType")
			.withCounterTypeDisplayName("counterTypeDisplayName")
			.build();

		final var savedEntity = repository.save(relationType);

		assertThat(savedEntity).isNotNull();
		assertThat(savedEntity.getId()).isNotNull();
		assertThat(savedEntity.getType()).isEqualTo("type");
		assertThat(savedEntity.getTypeDisplayName()).isEqualTo("typeDisplayName");
		assertThat(savedEntity.getCounterType()).isEqualTo("counterType");
		assertThat(savedEntity.getCounterTypeDisplayName()).isEqualTo("counterTypeDisplayName");
	}

	@Test
	void read() {
		final var relationType = repository.findByType("type-1");

		assertThat(relationType).isNotEmpty();
		assertThat(relationType.get().getType()).isEqualTo("type-1");
		assertThat(relationType.get().getTypeDisplayName()).isEqualTo("type_display_name-1");
		assertThat(relationType.get().getCounterType()).isEqualTo("counter_type-1");
		assertThat(relationType.get().getCounterTypeDisplayName()).isEqualTo("counter_type_display_name-1");
	}

	@Test
	void update() {
		final var relationType = repository.findByType("type-1");
		final var newType = "newType";
		relationType.get().setType(newType);

		final var updatedRelationType = repository.save(relationType.get());
		repository.flush();

		assertThat(updatedRelationType.getType()).isEqualTo(newType);
		assertThat(relationType.get().getTypeDisplayName()).isEqualTo("type_display_name-1");
		assertThat(relationType.get().getCounterType()).isEqualTo("counter_type-1");
		assertThat(relationType.get().getCounterTypeDisplayName()).isEqualTo("counter_type_display_name-1");
	}

	@Test
	void delete() {
		assertThat(repository.findByType("type-1")).isNotEmpty();
		assertThat(repository.findByType("type-2")).isNotEmpty();

		repository.deleteByType("type-2");
		repository.flush();

		assertThat(repository.findByType("type-1")).isNotEmpty();
		assertThat(repository.findByType("type-2")).isEmpty();
	}

	@Test
	void existsByType() {
		assertThat(repository.existsByType("type-1")).isTrue();
		assertThat(repository.existsByType("non-existing-type")).isFalse();
	}

	@Test
	void existsByCounterType() {
		assertThat(repository.existsByCounterType("counter_type-1")).isTrue();
		assertThat(repository.existsByCounterType("non-existing-counter-type")).isFalse();
	}

	@Test
	void existsByTypeOrCounterType() {
		assertThat(repository.existsByTypeOrCounterType("type-1")).isTrue();
		assertThat(repository.existsByTypeOrCounterType("counter_type-1")).isTrue();
		assertThat(repository.existsByTypeOrCounterType("non-existing-counter-type")).isFalse();
		assertThat(repository.existsByTypeOrCounterType("non-existing-type")).isFalse();
	}

	@Test
	void typeConstraint() {
		final var relationType = RelationTypeEntity.builder()
			.withType("type-1")
			.withCounterType("counterType")
			.build();

		assertThatThrownBy(() -> repository.saveAndFlush(relationType))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("constraint [uq_relation_type]");
	}

	@Test
	void counterTypeConstraint() {
		final var relationType = RelationTypeEntity.builder()
			.withType("type")
			.withCounterType("counter_type-1")
			.build();

		assertThatThrownBy(() -> repository.saveAndFlush(relationType))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("constraint [uq_relation_counter_type]");
	}
}
