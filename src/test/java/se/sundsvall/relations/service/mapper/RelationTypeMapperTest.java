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
			.withType(TYPE)
			.withTypeDisplayName(TYPE_DISPLAY_NAME)
			.withCounterType(COUNTER_TYPE)
			.withCounterTypeDisplayName(COUNTER_TYPE_DISPLAY_NAME)
			.build();

		final var entity = mapper.toRelationTypeEntity(relationType);

		assertThat(entity).hasNoNullFieldsOrPropertiesExcept("id");
		assertEquals(TYPE, entity.getType());
		assertEquals(TYPE_DISPLAY_NAME, entity.getTypeDisplayName());
		assertEquals(COUNTER_TYPE, entity.getCounterType());
		assertEquals(COUNTER_TYPE_DISPLAY_NAME, entity.getCounterTypeDisplayName());
	}

	@Test
	void toRelationTypeEntityForNullRelationType() {
		final var entity = mapper.toRelationTypeEntity(null);
		assertNull(entity);
	}

	@Test
	void toRelationType() {
		final var entity = RelationTypeEntity.builder()
			.withType(TYPE)
			.withTypeDisplayName(TYPE_DISPLAY_NAME)
			.withCounterType(COUNTER_TYPE)
			.withCounterTypeDisplayName(COUNTER_TYPE_DISPLAY_NAME)
			.build();

		final var relationType = mapper.toRelationType(entity);

		assertThat(relationType).hasNoNullFieldsOrProperties();
		assertEquals(TYPE, relationType.getType());
		assertEquals(TYPE_DISPLAY_NAME, relationType.getTypeDisplayName());
		assertEquals(COUNTER_TYPE, relationType.getCounterType());
		assertEquals(COUNTER_TYPE_DISPLAY_NAME, relationType.getCounterTypeDisplayName());
	}

	@Test
	void toRelationTypeForNullRelationTypeEntity() {
		final var relationType = mapper.toRelationType(null);
		assertNull(relationType);
	}
}
