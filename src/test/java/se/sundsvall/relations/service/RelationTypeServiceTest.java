package se.sundsvall.relations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.relations.api.model.RelationType;
import se.sundsvall.relations.integration.db.RelationRepository;
import se.sundsvall.relations.integration.db.RelationTypeRepository;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;
import se.sundsvall.relations.service.mapper.RelationTypeMapper;

@ExtendWith(MockitoExtension.class)
class RelationTypeServiceTest {

	private static final String TYPE_NAME = "TYPE_NAME";
	private static final String COUNTER_TYPE_NAME = "COUNTER_TYPE_NAME";
	private static final String OTHER_TYPE_NAME = "OTHER_TYPE_NAME";
	private static final String OTHER_COUNTER_TYPE_NAME = "OTHER_COUNTER_TYPE_NAME";
	private static final String ID = "someId";

	@Mock
	private RelationTypeRepository relationTypeRepositoryMock;

	@Mock
	private RelationRepository relationRepositoryMock;

	@Mock
	private RelationTypeMapper mapperMock;

	@InjectMocks
	private RelationTypeService relationTypeService;

	@Test
	void createType_shouldReturnNameOnSuccess() {
		final var relationType = RelationType.builder().withName(TYPE_NAME).withCounterName(COUNTER_TYPE_NAME).build();
		final var entity = RelationTypeEntity.builder().withId(ID).withName(TYPE_NAME).withCounterType(RelationTypeEntity.builder().withName(COUNTER_TYPE_NAME).build()).build();

		when(relationTypeRepositoryMock.existsByName(TYPE_NAME)).thenReturn(false);
		when(relationTypeRepositoryMock.existsByName(COUNTER_TYPE_NAME)).thenReturn(false);
		when(mapperMock.toRelationTypeEntity(any())).thenReturn(entity);
		when(relationTypeRepositoryMock.save(any())).thenReturn(entity);

		final var result = relationTypeService.createType(relationType);

		assertEquals(TYPE_NAME, result);
		verify(relationTypeRepositoryMock).existsByName(same(TYPE_NAME));
		verify(relationTypeRepositoryMock).existsByName(same(COUNTER_TYPE_NAME));
		verify(relationTypeRepositoryMock).save(same(entity));
		verify(mapperMock).toRelationTypeEntity(same(relationType));
	}

	@Test
	void createType_shouldThrowConflictOnSameTypeAndCounterType() {
		final var relationType = RelationType.builder().withName(TYPE_NAME).withCounterName(TYPE_NAME).build();

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.createType(relationType));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Type and counter type cannot be the same: 'TYPE_NAME'", exception.getMessage());
		verify(relationTypeRepositoryMock, never()).existsByName(anyString());
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationTypeEntity(any());
	}

	@Test
	void createType_shouldThrowConflictOnExistingType() {
		final var relationType = RelationType.builder().withName(TYPE_NAME).withCounterName(OTHER_COUNTER_TYPE_NAME).build();

		when(relationTypeRepositoryMock.existsByName(TYPE_NAME)).thenReturn(true);

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.createType(relationType));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Value 'TYPE_NAME' already exists as a type or counter type.", exception.getMessage());
		verify(relationTypeRepositoryMock).existsByName(same(TYPE_NAME));
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationTypeEntity(any());
	}

	@Test
	void createType_shouldThrowConflictOnExistingCounterType() {
		final var relationType = RelationType.builder().withName(OTHER_TYPE_NAME).withCounterName(COUNTER_TYPE_NAME).build();

		when(relationTypeRepositoryMock.existsByName(OTHER_TYPE_NAME)).thenReturn(false);
		when(relationTypeRepositoryMock.existsByName(COUNTER_TYPE_NAME)).thenReturn(true);

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.createType(relationType));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Value 'COUNTER_TYPE_NAME' already exists as a type or counter type.", exception.getMessage());
		verify(relationTypeRepositoryMock).existsByName(same(OTHER_TYPE_NAME));
		verify(relationTypeRepositoryMock).existsByName(same(COUNTER_TYPE_NAME));
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationTypeEntity(any());
	}

	@Test
	void getType_shouldReturnRelationTypeIfExists() {
		final var entity = RelationTypeEntity.builder().withName(TYPE_NAME).build();
		final var relationType = RelationType.builder().withName(TYPE_NAME).build();

		when(relationTypeRepositoryMock.findByName(anyString())).thenReturn(Optional.of(entity));
		when(mapperMock.toRelationType(any())).thenReturn(relationType);

		final var result = relationTypeService.getType(TYPE_NAME);

		assertThat(result).isSameAs(relationType);
		verify(relationTypeRepositoryMock).findByName(same(TYPE_NAME));
		verify(mapperMock).toRelationType(same(entity));
	}

	@Test
	void getType_shouldThrowNotFoundIfNotExists() {
		when(relationTypeRepositoryMock.findByName(anyString())).thenReturn(Optional.empty());

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.getType(TYPE_NAME));

		assertEquals(NOT_FOUND, exception.getStatus());
		assertEquals("Not Found: Relation type with type 'TYPE_NAME' not found", exception.getMessage());
		verify(relationTypeRepositoryMock).findByName(same(TYPE_NAME));
		verify(mapperMock, never()).toRelationType(any());
	}

	@Test
	void getAllTypes_shouldReturnListOfRelationTypes() {
		final var entityList = List.of(
			RelationTypeEntity.builder().withName(TYPE_NAME).build(),
			RelationTypeEntity.builder().withName(OTHER_TYPE_NAME).build());
		final var relationTypeList = List.of(
			RelationType.builder().withName(TYPE_NAME).withCounterName(COUNTER_TYPE_NAME).build(),
			RelationType.builder().withName(OTHER_TYPE_NAME).withCounterName(OTHER_COUNTER_TYPE_NAME).build());

		when(relationTypeRepositoryMock.findAll()).thenReturn(entityList);
		when(mapperMock.toRelationType(entityList.get(0))).thenReturn(relationTypeList.get(0));
		when(mapperMock.toRelationType(entityList.get(1))).thenReturn(relationTypeList.get(1));

		final var result = relationTypeService.getAllTypes();

		assertThat(result).containsAll(relationTypeList);
		verify(relationTypeRepositoryMock).findAll();
		verify(mapperMock).toRelationType(same(entityList.get(0)));
		verify(mapperMock).toRelationType(same(entityList.get(1)));
	}

	@Test
	void deleteRelationType_shouldDeleteIfExists() {
		final var entity = RelationTypeEntity.builder().withName(TYPE_NAME).build();

		when(relationTypeRepositoryMock.findByName(anyString())).thenReturn(Optional.of(entity));

		relationTypeService.deleteRelationType(TYPE_NAME);

		verify(relationTypeRepositoryMock).findByName(same(TYPE_NAME));
		verify(relationTypeRepositoryMock).delete(same(entity));
	}

	@Test
	void deleteRelationType_shouldThrowNotFoundIfNotExists() {
		when(relationTypeRepositoryMock.findByName(anyString())).thenReturn(Optional.empty());

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.deleteRelationType(TYPE_NAME));

		assertEquals(NOT_FOUND, exception.getStatus());
		assertEquals("Not Found: Relation type with type 'TYPE_NAME' not found", exception.getMessage());
		verify(relationTypeRepositoryMock).findByName(same(TYPE_NAME));
		verify(relationTypeRepositoryMock, never()).delete(any());
	}

	@Test
	void saveRelationType_shouldUpdateIfExists() {
		final var existingEntity = RelationTypeEntity.builder().withId(ID).withName(TYPE_NAME).build();
		final var relationTypeToSave = RelationType.builder().withName(OTHER_TYPE_NAME).withCounterName(OTHER_COUNTER_TYPE_NAME).build();
		final var updatedEntity = RelationTypeEntity.builder().withId(ID).withName(OTHER_TYPE_NAME).build();
		final var mappedEntity = RelationTypeEntity.builder().withName(OTHER_TYPE_NAME).build();
		final var updatedRelationType = RelationType.builder().withName(OTHER_TYPE_NAME).withCounterName(OTHER_COUNTER_TYPE_NAME).build();

		when(relationTypeRepositoryMock.findByName(anyString())).thenReturn(Optional.of(existingEntity));
		when(relationTypeRepositoryMock.existsByName(OTHER_TYPE_NAME)).thenReturn(false);
		when(relationTypeRepositoryMock.existsByName(OTHER_COUNTER_TYPE_NAME)).thenReturn(false);
		when(relationTypeRepositoryMock.save(any())).thenReturn(updatedEntity);
		when(mapperMock.toRelationType(any())).thenReturn(updatedRelationType);
		when(mapperMock.toRelationTypeEntity(relationTypeToSave)).thenReturn(mappedEntity);

		final var argumentCaptor = ArgumentCaptor.forClass(RelationTypeEntity.class);

		final var result = relationTypeService.saveRelationType(TYPE_NAME, relationTypeToSave);

		assertEquals(updatedRelationType, result);
		verify(relationTypeRepositoryMock).findByName(same(TYPE_NAME));
		verify(relationTypeRepositoryMock).existsByName(same(OTHER_TYPE_NAME));
		verify(relationTypeRepositoryMock).existsByName(same(OTHER_COUNTER_TYPE_NAME));
		verify(mapperMock).toRelationTypeEntity(same(relationTypeToSave));
		verify(relationTypeRepositoryMock).save(argumentCaptor.capture());
		verify(mapperMock).toRelationType(same(updatedEntity));

		final var capturedEntity = argumentCaptor.getValue();
		assertEquals(ID, capturedEntity.getId());
		assertThat(capturedEntity).isSameAs(mappedEntity);

	}

	@Test
	void saveRelationType_shouldThrowNotFoundIfNotExists() {
		final var relationTypeToSave = RelationType.builder().withName(OTHER_TYPE_NAME).build();

		when(relationTypeRepositoryMock.findByName(anyString())).thenReturn(Optional.empty());

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.saveRelationType(TYPE_NAME, relationTypeToSave));

		assertEquals(NOT_FOUND, exception.getStatus());
		assertEquals("Not Found: Relation type with type 'TYPE_NAME' not found", exception.getMessage());
		verify(relationTypeRepositoryMock).findByName(same(TYPE_NAME));
		verify(relationTypeRepositoryMock, never()).existsByName(anyString());
		verify(mapperMock, never()).toRelationTypeEntity(any());
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationType(any());
	}

	@Test
	void saveRelationType_shouldThrowConflictOnUpdateToExistingType() {
		final var existingEntity = RelationTypeEntity.builder().withId(ID).withName(TYPE_NAME).build();
		final var relationTypeToSave = RelationType.builder().withName(OTHER_TYPE_NAME).withCounterName(OTHER_COUNTER_TYPE_NAME).build();

		when(relationTypeRepositoryMock.findByName(anyString())).thenReturn(Optional.of(existingEntity));
		when(relationTypeRepositoryMock.existsByName(OTHER_TYPE_NAME)).thenReturn(true);

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.saveRelationType(TYPE_NAME, relationTypeToSave));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Value 'OTHER_TYPE_NAME' already exists as a type or counter type.", exception.getMessage());
		verify(relationTypeRepositoryMock).findByName(same(TYPE_NAME));
		verify(relationTypeRepositoryMock).existsByName(same(OTHER_TYPE_NAME));
		verify(relationTypeRepositoryMock, never()).existsByName(same(OTHER_COUNTER_TYPE_NAME));
		verify(mapperMock, never()).toRelationTypeEntity(any());
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationType(any());
	}

	@Test
	void saveRelationType_shouldThrowConflictOnUpdateToExistingCounterType() {
		final var existingEntity = RelationTypeEntity.builder().withId(ID).withName(TYPE_NAME).build();
		final var relationTypeToSave = RelationType.builder().withName(OTHER_TYPE_NAME).withCounterName(OTHER_COUNTER_TYPE_NAME).build();

		when(relationTypeRepositoryMock.findByName(anyString())).thenReturn(Optional.of(existingEntity));
		when(relationTypeRepositoryMock.existsByName(OTHER_TYPE_NAME)).thenReturn(false);
		when(relationTypeRepositoryMock.existsByName(OTHER_COUNTER_TYPE_NAME)).thenReturn(true);

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.saveRelationType(TYPE_NAME, relationTypeToSave));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Value 'OTHER_COUNTER_TYPE_NAME' already exists as a type or counter type.", exception.getMessage());
		verify(relationTypeRepositoryMock).findByName(same(TYPE_NAME));
		verify(relationTypeRepositoryMock).existsByName(same(OTHER_TYPE_NAME));
		verify(relationTypeRepositoryMock).existsByName(same(OTHER_COUNTER_TYPE_NAME));
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationType(any());
	}

	@Test
	void saveRelationType_shouldThrowConflictOnSameTypeAndCounterTypeUpdate() {
		final var existingEntity = RelationTypeEntity.builder().withId(ID).withName(TYPE_NAME).build();
		final var relationTypeToSave = RelationType.builder().withName(OTHER_TYPE_NAME).withCounterName(OTHER_TYPE_NAME).build();

		when(relationTypeRepositoryMock.findByName(anyString())).thenReturn(Optional.of(existingEntity));

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.saveRelationType(TYPE_NAME, relationTypeToSave));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Type and counter type cannot be the same: 'OTHER_TYPE_NAME'", exception.getMessage());
		verify(relationTypeRepositoryMock).findByName(same(TYPE_NAME));
		verify(relationTypeRepositoryMock, never()).existsByName(anyString());
		verify(mapperMock, never()).toRelationTypeEntity(any());
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationType(any());
	}

	@Test
	void saveRelationType_shouldThrowConflictOnRemovalOfCounterType() {
		final var existingCounterTypeEntity = RelationTypeEntity.builder().withName("USED").build();
		final var existingEntity = RelationTypeEntity.builder()
			.withId(ID)
			.withName(TYPE_NAME)
			.withCounterType(existingCounterTypeEntity)
			.build();
		final var updatedEntity = RelationTypeEntity.builder()
			.withId(ID)
			.withName(TYPE_NAME)
			.build();

		final var relationTypeToSave = RelationType.builder().withName(TYPE_NAME).build();

		when(relationTypeRepositoryMock.findByName(anyString())).thenReturn(Optional.of(existingEntity));
		when(mapperMock.toRelationTypeEntity(any())).thenReturn(updatedEntity);
		when(relationRepositoryMock.existsByType(any())).thenReturn(true);

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.saveRelationType(TYPE_NAME, relationTypeToSave));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Type 'USED' is used by one or many Relations", exception.getMessage());
		verify(relationTypeRepositoryMock).findByName(same(TYPE_NAME));
		verify(mapperMock).toRelationTypeEntity(same(relationTypeToSave));
		verify(relationRepositoryMock).existsByType(same(existingCounterTypeEntity));
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationType(any());
	}
}
