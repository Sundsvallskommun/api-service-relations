package se.sundsvall.relations.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
			.withName("type")
			.withDisplayName("typeDisplayName")
			.build();
		final var relationCounterType = RelationTypeEntity.builder()
			.withName("counterType")
			.withDisplayName("counterTypeDisplayName")
			.withCounterType(relationType)
			.build();
		relationType.setCounterType(relationCounterType);

		final var savedEntity = repository.save(relationType);

		assertThat(savedEntity).isNotNull();
		assertThat(savedEntity.getId()).isNotNull();
		assertThat(savedEntity.getName()).isEqualTo("type");
		assertThat(savedEntity.getDisplayName()).isEqualTo("typeDisplayName");
		assertThat(savedEntity.getCounterType()).isSameAs(relationCounterType);
		assertThat(savedEntity.getCounterType().getName()).isEqualTo("counterType");
		assertThat(savedEntity.getCounterType().getDisplayName()).isEqualTo("counterTypeDisplayName");
	}

	@Test
	void read() {
		final var relationType = repository.findByName("type-1");

		assertThat(relationType).isNotEmpty();
		assertThat(relationType.get().getName()).isEqualTo("type-1");
		assertThat(relationType.get().getDisplayName()).isEqualTo("type_display_name-1");
		assertThat(relationType.get().getCounterType().getName()).isEqualTo("counter_type-1");
		assertThat(relationType.get().getCounterType().getDisplayName()).isEqualTo("counter_type_display_name-1");
	}

	@Test
	void update() {
		final var relationType = repository.findByName("type-1");
		final var newType = "newType";
		relationType.get().setName(newType);

		final var updatedRelationType = repository.save(relationType.get());
		repository.flush();

		assertThat(updatedRelationType.getName()).isEqualTo(newType);
		assertThat(relationType.get().getDisplayName()).isEqualTo("type_display_name-1");
		assertThat(relationType.get().getCounterType().getName()).isEqualTo("counter_type-1");
		assertThat(relationType.get().getCounterType().getDisplayName()).isEqualTo("counter_type_display_name-1");
	}

	@Test
	void delete() {
		assertThat(repository.findByName("type-1")).isNotEmpty();
		assertThat(repository.findByName("counter_type-1")).isNotEmpty();
		assertThat(repository.findByName("type-3")).isNotEmpty();
		assertThat(repository.findByName("counter_type-3")).isNotEmpty();

		repository.deleteByName("type-3");
		repository.flush();

		assertThat(repository.findByName("type-1")).isNotEmpty();
		assertThat(repository.findByName("counter_type-1")).isNotEmpty();
		assertThat(repository.findByName("type-3")).isEmpty();
		assertThat(repository.findByName("counter_type-3")).isEmpty();
	}

	@Test
	void deleteWhenTypeIsInUse() {
		assertThatThrownBy(() -> {
			repository.deleteByName("type-1");
			repository.flush();
		})
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("foreign key constraint");
	}

	@Test
	void existsByName() {
		assertThat(repository.existsByName("type-1")).isTrue();
		assertThat(repository.existsByName("non-existing-type")).isFalse();
	}

	@Test
	void typeConstraint() {
		final var relationType = RelationTypeEntity.builder()
			.withName("type-1")
			.build();

		assertThatThrownBy(() -> repository.saveAndFlush(relationType))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("constraint [uq_relation_type_name]");
	}
}
