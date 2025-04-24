package se.sundsvall.relations.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import se.sundsvall.relations.api.model.RelationType;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;

class RelationTypeMapperTest {

	private final RelationTypeMapper mapper = new RelationTypeMapper();

	private static final String TYPE = "TYPE";
	private static final String TYPE_DISPLAY_NAME = "Type Display Name";
	private static final String COUNTER_TYPE = "COUNTER_TYPE";
	private static final String COUNTER_TYPE_DISPLAY_NAME = "Counter Type Display Name";

	@Test
	void toRelationTypeEntity() {
		final var relationType = RelationType.builder()
			.withName(TYPE)
			.withDisplayName(TYPE_DISPLAY_NAME)
			.withCounterName(COUNTER_TYPE)
			.withCounterDisplayName(COUNTER_TYPE_DISPLAY_NAME)
			.build();

		final var entity = mapper.toRelationTypeEntity(relationType);

		assertThat(entity).hasNoNullFieldsOrPropertiesExcept("id");
		assertEquals(TYPE, entity.getName());
		assertEquals(TYPE_DISPLAY_NAME, entity.getDisplayName());
		assertEquals(COUNTER_TYPE, entity.getCounterType().getName());
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
			.withName(TYPE)
			.withDisplayName(TYPE_DISPLAY_NAME)
			.withCounterType(RelationTypeEntity.builder()
				.withName(COUNTER_TYPE)
				.withDisplayName(COUNTER_TYPE_DISPLAY_NAME)
				.build())
			.build();

		final var relationType = mapper.toRelationType(entity);

		assertThat(relationType).hasNoNullFieldsOrProperties();
		assertEquals(TYPE, relationType.getName());
		assertEquals(TYPE_DISPLAY_NAME, relationType.getDisplayName());
		assertEquals(COUNTER_TYPE, relationType.getCounterName());
		assertEquals(COUNTER_TYPE_DISPLAY_NAME, relationType.getCounterDisplayName());
	}

	@Test
	void toRelationTypeForNullRelationTypeEntity() {
		final var relationType = mapper.toRelationType(null);
		assertNull(relationType);
	}
}
