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
import se.sundsvall.relations.integration.db.RelationTypeRepository;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;
import se.sundsvall.relations.service.mapper.RelationTypeMapper;

@ExtendWith(MockitoExtension.class)
class RelationTypeServiceTest {

	private static final String TYPE = "TYPE";
	private static final String COUNTER_TYPE = "COUNTER_TYPE";
	private static final String OTHER_TYPE = "OTHER_TYPE";
	private static final String OTHER_COUNTER_TYPE = "OTHER_COUNTER_TYPE";
	private static final String ID = "someId";

	@Mock
	private RelationTypeRepository relationTypeRepositoryMock;

	@Mock
	private RelationTypeMapper mapperMock;

	@InjectMocks
	private RelationTypeService relationTypeService;

	@Test
	void createType_shouldReturnIdOnSuccess() {
		final var relationType = RelationType.builder().withType(TYPE).withCounterType(COUNTER_TYPE).build();
		final var entity = RelationTypeEntity.builder().withId(ID).withType(TYPE).withCounterType(COUNTER_TYPE).build();

		when(relationTypeRepositoryMock.existsByTypeOrCounterType(TYPE)).thenReturn(false);
		when(relationTypeRepositoryMock.existsByTypeOrCounterType(COUNTER_TYPE)).thenReturn(false);
		when(mapperMock.toRelationTypeEntity(any())).thenReturn(entity);
		when(relationTypeRepositoryMock.save(any())).thenReturn(entity);

		final var result = relationTypeService.createType(relationType);

		assertEquals(ID, result);
		verify(relationTypeRepositoryMock).existsByTypeOrCounterType(same(TYPE));
		verify(relationTypeRepositoryMock).existsByTypeOrCounterType(same(COUNTER_TYPE));
		verify(relationTypeRepositoryMock).save(same(entity));
		verify(mapperMock).toRelationTypeEntity(same(relationType));
	}

	@Test
	void createType_shouldThrowConflictOnSameTypeAndCounterType() {
		final var relationType = RelationType.builder().withType(TYPE).withCounterType(TYPE).build();

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.createType(relationType));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Type and counter type cannot be the same: 'TYPE'", exception.getMessage());
		verify(relationTypeRepositoryMock, never()).existsByTypeOrCounterType(anyString());
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationTypeEntity(any());
	}

	@Test
	void createType_shouldThrowConflictOnExistingType() {
		final var relationType = RelationType.builder().withType(TYPE).withCounterType(OTHER_COUNTER_TYPE).build();

		when(relationTypeRepositoryMock.existsByTypeOrCounterType(TYPE)).thenReturn(true);

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.createType(relationType));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Value 'TYPE' already exists as a type or counter type.", exception.getMessage());
		verify(relationTypeRepositoryMock).existsByTypeOrCounterType(same(TYPE));
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationTypeEntity(any());
	}

	@Test
	void createType_shouldThrowConflictOnExistingCounterType() {
		final var relationType = RelationType.builder().withType(OTHER_TYPE).withCounterType(COUNTER_TYPE).build();

		when(relationTypeRepositoryMock.existsByTypeOrCounterType(OTHER_TYPE)).thenReturn(false);
		when(relationTypeRepositoryMock.existsByTypeOrCounterType(COUNTER_TYPE)).thenReturn(true);

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.createType(relationType));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Value 'COUNTER_TYPE' already exists as a type or counter type.", exception.getMessage());
		verify(relationTypeRepositoryMock).existsByTypeOrCounterType(same(OTHER_TYPE));
		verify(relationTypeRepositoryMock).existsByTypeOrCounterType(same(COUNTER_TYPE));
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationTypeEntity(any());
	}

	@Test
	void getType_shouldReturnRelationTypeIfExists() {
		final var entity = RelationTypeEntity.builder().withType(TYPE).withCounterType(COUNTER_TYPE).build();
		final var relationType = RelationType.builder().withType(TYPE).withCounterType(COUNTER_TYPE).build();

		when(relationTypeRepositoryMock.findByType(anyString())).thenReturn(Optional.of(entity));
		when(mapperMock.toRelationType(any())).thenReturn(relationType);

		final var result = relationTypeService.getType(TYPE);

		assertThat(result).isSameAs(relationType);
		verify(relationTypeRepositoryMock).findByType(same(TYPE));
		verify(mapperMock).toRelationType(same(entity));
	}

	@Test
	void getType_shouldThrowNotFoundIfNotExists() {
		when(relationTypeRepositoryMock.findByType(anyString())).thenReturn(Optional.empty());

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.getType(TYPE));

		assertEquals(NOT_FOUND, exception.getStatus());
		assertEquals("Not Found: Relation type with type 'TYPE' not found", exception.getMessage());
		verify(relationTypeRepositoryMock).findByType(same(TYPE));
		verify(mapperMock, never()).toRelationType(any());
	}

	@Test
	void getAllTypes_shouldReturnListOfRelationTypes() {
		final var entityList = List.of(
			RelationTypeEntity.builder().withType(TYPE).withCounterType(COUNTER_TYPE).build(),
			RelationTypeEntity.builder().withType(OTHER_TYPE).withCounterType(OTHER_COUNTER_TYPE).build());
		final var relationTypeList = List.of(
			RelationType.builder().withType(TYPE).withCounterType(COUNTER_TYPE).build(),
			RelationType.builder().withType(OTHER_TYPE).withCounterType(OTHER_COUNTER_TYPE).build());

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
		final var entity = RelationTypeEntity.builder().withType(TYPE).build();

		when(relationTypeRepositoryMock.findByType(anyString())).thenReturn(Optional.of(entity));

		relationTypeService.deleteRelationType(TYPE);

		verify(relationTypeRepositoryMock).findByType(same(TYPE));
		verify(relationTypeRepositoryMock).delete(same(entity));
	}

	@Test
	void deleteRelationType_shouldThrowNotFoundIfNotExists() {
		when(relationTypeRepositoryMock.findByType(anyString())).thenReturn(Optional.empty());

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.deleteRelationType(TYPE));

		assertEquals(NOT_FOUND, exception.getStatus());
		assertEquals("Not Found: Relation type with type 'TYPE' not found", exception.getMessage());
		verify(relationTypeRepositoryMock).findByType(same(TYPE));
		verify(relationTypeRepositoryMock, never()).delete(any());
	}

	@Test
	void saveRelationType_shouldUpdateIfExists() {
		final var existingEntity = RelationTypeEntity.builder().withId(ID).withType(TYPE).withCounterType(COUNTER_TYPE).build();
		final var relationTypeToSave = RelationType.builder().withType(OTHER_TYPE).withCounterType(OTHER_COUNTER_TYPE).build();
		final var updatedEntity = RelationTypeEntity.builder().withId(ID).withType(OTHER_TYPE).withCounterType(OTHER_COUNTER_TYPE).build();
		final var mappedEntity = RelationTypeEntity.builder().withType(OTHER_TYPE).withCounterType(OTHER_COUNTER_TYPE).build();
		final var updatedRelationType = RelationType.builder().withType(OTHER_TYPE).withCounterType(OTHER_COUNTER_TYPE).build();

		when(relationTypeRepositoryMock.findByType(anyString())).thenReturn(Optional.of(existingEntity));
		when(relationTypeRepositoryMock.existsByTypeOrCounterType(OTHER_TYPE)).thenReturn(false);
		when(relationTypeRepositoryMock.existsByTypeOrCounterType(OTHER_COUNTER_TYPE)).thenReturn(false);
		when(relationTypeRepositoryMock.save(any())).thenReturn(updatedEntity);
		when(mapperMock.toRelationType(any())).thenReturn(updatedRelationType);
		when(mapperMock.toRelationTypeEntity(relationTypeToSave)).thenReturn(mappedEntity);

		final var argumentCaptor = ArgumentCaptor.forClass(RelationTypeEntity.class);

		final var result = relationTypeService.saveRelationType(TYPE, relationTypeToSave);

		assertEquals(updatedRelationType, result);
		verify(relationTypeRepositoryMock).findByType(same(TYPE));
		verify(relationTypeRepositoryMock).existsByTypeOrCounterType(same(OTHER_TYPE));
		verify(relationTypeRepositoryMock).existsByTypeOrCounterType(same(OTHER_COUNTER_TYPE));
		verify(mapperMock).toRelationTypeEntity(same(relationTypeToSave));
		verify(relationTypeRepositoryMock).save(argumentCaptor.capture());
		verify(mapperMock).toRelationType(same(updatedEntity));

		final var capturedEntity = argumentCaptor.getValue();
		assertEquals(ID, capturedEntity.getId());
		assertThat(capturedEntity).isSameAs(mappedEntity);

	}

	@Test
	void saveRelationType_shouldThrowNotFoundIfNotExists() {
		final var relationTypeToSave = RelationType.builder().withType(OTHER_TYPE).build();

		when(relationTypeRepositoryMock.findByType(anyString())).thenReturn(Optional.empty());

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.saveRelationType(TYPE, relationTypeToSave));

		assertEquals(NOT_FOUND, exception.getStatus());
		assertEquals("Not Found: Relation type with type 'TYPE' not found", exception.getMessage());
		verify(relationTypeRepositoryMock).findByType(same(TYPE));
		verify(relationTypeRepositoryMock, never()).existsByTypeOrCounterType(anyString());
		verify(mapperMock, never()).toRelationTypeEntity(any());
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationType(any());
	}

	@Test
	void saveRelationType_shouldThrowConflictOnUpdateToExistingType() {
		final var existingEntity = RelationTypeEntity.builder().withId(ID).withType(TYPE).withCounterType(COUNTER_TYPE).build();
		final var relationTypeToSave = RelationType.builder().withType(OTHER_TYPE).withCounterType(OTHER_COUNTER_TYPE).build();

		when(relationTypeRepositoryMock.findByType(anyString())).thenReturn(Optional.of(existingEntity));
		when(relationTypeRepositoryMock.existsByTypeOrCounterType(OTHER_TYPE)).thenReturn(true);

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.saveRelationType(TYPE, relationTypeToSave));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Value 'OTHER_TYPE' already exists as a type or counter type.", exception.getMessage());
		verify(relationTypeRepositoryMock).findByType(same(TYPE));
		verify(relationTypeRepositoryMock).existsByTypeOrCounterType(same(OTHER_TYPE));
		verify(relationTypeRepositoryMock, never()).existsByTypeOrCounterType(same(OTHER_COUNTER_TYPE));
		verify(mapperMock, never()).toRelationTypeEntity(any());
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationType(any());
	}

	@Test
	void saveRelationType_shouldThrowConflictOnUpdateToExistingCounterType() {
		final var existingEntity = RelationTypeEntity.builder().withId(ID).withType(TYPE).withCounterType(COUNTER_TYPE).build();
		final var relationTypeToSave = RelationType.builder().withType(OTHER_TYPE).withCounterType(OTHER_COUNTER_TYPE).build();

		when(relationTypeRepositoryMock.findByType(anyString())).thenReturn(Optional.of(existingEntity));
		when(relationTypeRepositoryMock.existsByTypeOrCounterType(OTHER_TYPE)).thenReturn(false);
		when(relationTypeRepositoryMock.existsByTypeOrCounterType(OTHER_COUNTER_TYPE)).thenReturn(true);

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.saveRelationType(TYPE, relationTypeToSave));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Value 'OTHER_COUNTER_TYPE' already exists as a type or counter type.", exception.getMessage());
		verify(relationTypeRepositoryMock).findByType(same(TYPE));
		verify(relationTypeRepositoryMock).existsByTypeOrCounterType(same(OTHER_TYPE));
		verify(relationTypeRepositoryMock).existsByTypeOrCounterType(same(OTHER_COUNTER_TYPE));
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationType(any());
	}

	@Test
	void saveRelationType_shouldThrowConflictOnSameTypeAndCounterTypeUpdate() {
		final var existingEntity = RelationTypeEntity.builder().withId(ID).withType(TYPE).withCounterType(COUNTER_TYPE).build();
		final var relationTypeToSave = RelationType.builder().withType(OTHER_TYPE).withCounterType(OTHER_TYPE).build();

		when(relationTypeRepositoryMock.findByType(anyString())).thenReturn(Optional.of(existingEntity));

		final var exception = assertThrows(ThrowableProblem.class, () -> relationTypeService.saveRelationType(TYPE, relationTypeToSave));

		assertEquals(CONFLICT, exception.getStatus());
		assertEquals("Conflict: Type and counter type cannot be the same: 'OTHER_TYPE'", exception.getMessage());
		verify(relationTypeRepositoryMock).findByType(same(TYPE));
		verify(relationTypeRepositoryMock, never()).existsByTypeOrCounterType(anyString());
		verify(mapperMock, never()).toRelationTypeEntity(any());
		verify(relationTypeRepositoryMock, never()).save(any());
		verify(mapperMock, never()).toRelationType(any());
	}
}
