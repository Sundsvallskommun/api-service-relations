package se.sundsvall.relations.service.mapper;

import org.junit.jupiter.api.Test;
import se.sundsvall.relations.api.model.RelationType;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RelationTypeMapperTest {

	private final RelationTypeMapper mapper = new RelationTypeMapper();

	private static final String TYPE_NAME = "TYPE_NAME";
	private static final String TYPE_DISPLAY_NAME = "Type Display Name";
	private static final String COUNTER_TYPE_NAME = "COUNTER_TYPE_NAME";
	private static final String COUNTER_TYPE_DISPLAY_NAME = "Counter Type Display Name";

	@Test
	void toRelationTypeEntity() {
		final var relationType = RelationType.builder()
			.withName(TYPE_NAME)
			.withDisplayName(TYPE_DISPLAY_NAME)
			.withCounterName(COUNTER_TYPE_NAME)
			.withCounterDisplayName(COUNTER_TYPE_DISPLAY_NAME)
			.build();

		final var entity = mapper.toRelationTypeEntity(relationType);

		assertThat(entity).hasNoNullFieldsOrPropertiesExcept("id");
		assertEquals(TYPE_NAME, entity.getName());
		assertEquals(TYPE_DISPLAY_NAME, entity.getDisplayName());
		assertEquals(COUNTER_TYPE_NAME, entity.getCounterType().getName());
		assertEquals(COUNTER_TYPE_DISPLAY_NAME, entity.getCounterType().getDisplayName());
	}

	@Test
	void toRelationTypeEntityForNullRelationType() {
		final var entity = mapper.toRelationTypeEntity(null);
		assertNull(entity);
	}

	@Test
	void toRelationType() {
		final var entity = RelationTypeEntity.builder()
			.withName(TYPE_NAME)
			.withDisplayName(TYPE_DISPLAY_NAME)
			.withCounterType(RelationTypeEntity.builder()
				.withName(COUNTER_TYPE_NAME)
				.withDisplayName(COUNTER_TYPE_DISPLAY_NAME)
				.build())
			.build();

		final var relationType = mapper.toRelationType(entity);

		assertThat(relationType).hasNoNullFieldsOrProperties();
		assertEquals(TYPE_NAME, relationType.getName());
		assertEquals(TYPE_DISPLAY_NAME, relationType.getDisplayName());
		assertEquals(COUNTER_TYPE_NAME, relationType.getCounterName());
		assertEquals(COUNTER_TYPE_DISPLAY_NAME, relationType.getCounterDisplayName());
	}

	@Test
	void toRelationTypeForNullRelationTypeEntity() {
		final var relationType = mapper.toRelationType(null);
		assertNull(relationType);
	}
}
