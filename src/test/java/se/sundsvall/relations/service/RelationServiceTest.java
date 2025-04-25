package se.sundsvall.relations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static se.sundsvall.relations.service.RelationService.withMunicipalityId;

import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.Problem;
import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.api.model.RelationPageParameters;
import se.sundsvall.relations.integration.db.RelationRepository;
import se.sundsvall.relations.integration.db.RelationTypeRepository;
import se.sundsvall.relations.integration.db.model.RelationEntity;
import se.sundsvall.relations.integration.db.model.RelationTypeEntity;
import se.sundsvall.relations.service.mapper.RelationMapper;

@ExtendWith(MockitoExtension.class)
class RelationServiceTest {

	@Mock
	private RelationRepository relationRepositoryMock;

	@Mock
	private RelationTypeRepository relationTypeRepositoryMock;

	@Mock
	private RelationMapper mapperMock;

	@Mock
	private RelationTypeEntity relationTypeEntityMock;

	@Mock
	private Page<RelationEntity> pageMock;

	@Spy
	private FilterSpecificationConverter filterSpecificationConverterSpy;

	@Captor
	private ArgumentCaptor<Specification<RelationEntity>> specificationCaptor;

	@Captor
	private ArgumentCaptor<Pageable> pageableCaptor;

	@InjectMocks
	private RelationService service;

	private static final String MUNICIPALITY_ID = "municipalityId";

	@Test
	void createRelation() {
		final var id = "id";
		final var typeName = "typeName";
		final var relation = Relation.builder().withType(typeName).build();
		final var entity = RelationEntity.builder().build();
		final var inverseEntity = RelationEntity.builder().build();

		when(relationTypeRepositoryMock.findByName(any())).thenReturn(Optional.of(relationTypeEntityMock));
		when(mapperMock.toRelationEntity(any(), any(), any())).thenReturn(entity);
		when(relationTypeEntityMock.getCounterType()).thenReturn(RelationTypeEntity.builder().build());
		when(mapperMock.toInverseRelationEntity(any())).thenReturn(inverseEntity);
		when(relationRepositoryMock.save(any())).thenReturn(RelationEntity.builder().withId(id).build());

		final var result = service.createRelation(MUNICIPALITY_ID, relation);

		assertThat(result).isEqualTo(id);
		verify(relationTypeRepositoryMock).findByName(typeName);
		verify(mapperMock).toRelationEntity(eq(MUNICIPALITY_ID), same(relation), same(relationTypeEntityMock));
		verify(relationTypeEntityMock).getCounterType();
		verify(mapperMock).toInverseRelationEntity(same(entity));
		verify(relationRepositoryMock).save(same(entity));
		assertThat(entity.getInverseRelation()).isSameAs(inverseEntity);

		verifyNoMoreInteractions(relationRepositoryMock, mapperMock, relationTypeRepositoryMock);
	}

	@Test
	void createRelationInvalidType() {
		final var typeName = "typeName";
		final var relation = Relation.builder().withType(typeName).build();

		when(relationTypeRepositoryMock.findByName(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.createRelation("municipalityId", relation))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Request: 'typeName' is not a valid type");

		verify(relationTypeRepositoryMock).findByName(typeName);
		verifyNoMoreInteractions(relationTypeRepositoryMock);
		verifyNoInteractions(relationRepositoryMock, mapperMock);

	}

	@Test
	void findRelations() {
		final Specification<RelationEntity> filter = filterSpecificationConverterSpy.convert("id: 'someId'");
		final var page = new RelationPageParameters();
		page.setPage(11);
		page.setLimit(22);
		page.setSortBy(List.of("created"));
		page.setSortDirection(DESC);
		final var relation = Relation.builder().build();
		final var entity = RelationEntity.builder().build();

		when(relationRepositoryMock.findAll(ArgumentMatchers.<Specification<RelationEntity>>any(), any(Pageable.class))).thenReturn(pageMock);
		when(pageMock.stream()).thenReturn(Stream.of(entity));
		when(pageMock.getTotalPages()).thenReturn(2);
		when(pageMock.getTotalElements()).thenReturn(3L);
		when(pageMock.getNumberOfElements()).thenReturn(4);
		when(pageMock.getSize()).thenReturn(100);
		when(pageMock.getNumber()).thenReturn(0);
		when(pageMock.getSort()).thenReturn(Sort.unsorted());
		when(mapperMock.toRelation(any())).thenReturn(relation);

		final var result = service.findRelations(MUNICIPALITY_ID, filter, page);

		verify(relationRepositoryMock).findAll(specificationCaptor.capture(), pageableCaptor.capture());
		verify(mapperMock).toRelation(same(entity));
		verify(pageMock).stream();
		verify(pageMock).getTotalPages();
		verify(pageMock).getTotalElements();
		verify(pageMock).getNumberOfElements();
		verify(pageMock).getSize();
		verify(pageMock).getNumber();
		verify(pageMock, times(2)).getSort();
		verifyNoMoreInteractions(relationRepositoryMock, pageMock, mapperMock);

		assertThat(result.getRelations()).containsExactly(relation);
		assertThat(result.getMetaData().getTotalPages()).isEqualTo(2);
		assertThat(result.getMetaData().getTotalRecords()).isEqualTo(3);
		assertThat(result.getMetaData().getCount()).isEqualTo(4L);
		assertThat(result.getMetaData().getLimit()).isEqualTo(100);
		assertThat(result.getMetaData().getPage()).isEqualTo(1);
		assertThat(specificationCaptor.getValue()).usingRecursiveComparison().isEqualTo(withMunicipalityId(MUNICIPALITY_ID).and(filter));
		assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(10);
		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(22);
		assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by(DESC, "created"));

	}

	@Test
	void saveRelation() {
		final var id = "id";
		final var typeName = "typeName";
		final var relation = Relation.builder()
			.withId(id)
			.withType(typeName)
			.build();
		final var entity = RelationEntity.builder().build();
		final var savedEntity = RelationEntity.builder().build();
		final var responseRelation = Relation.builder().build();

		when(relationRepositoryMock.findByIdAndMunicipalityId(any(), any())).thenReturn(Optional.of(entity));
		when(relationTypeRepositoryMock.findByName(any())).thenReturn(Optional.of(relationTypeEntityMock));
		when(relationRepositoryMock.save(any())).thenReturn(savedEntity);
		when(mapperMock.toRelation(any())).thenReturn(responseRelation);

		final var result = service.saveRelation(MUNICIPALITY_ID, relation);

		assertThat(result).isSameAs(responseRelation);
		verify(relationRepositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verify(relationTypeRepositoryMock).findByName(typeName);
		verify(mapperMock).updateRelationEntity(same(entity), same(relation), same(relationTypeEntityMock));
		verify(relationTypeEntityMock).getCounterType();
		verify(mapperMock).toRelation(same(savedEntity));

		verifyNoMoreInteractions(relationRepositoryMock, mapperMock, relationTypeRepositoryMock);
	}

	@Test
	void saveRelationUpdateFromTwoWayRelationToOnewayRelation() {
		final var id = "id";
		final var typeName = "typeName";
		final var relation = Relation.builder()
			.withId(id)
			.withType(typeName)
			.build();
		final var inverseEntity = RelationEntity.builder().build();
		final var entity = RelationEntity.builder().withInverseRelation(inverseEntity).build();
		final var savedEntity = RelationEntity.builder().build();
		final var responseRelation = Relation.builder().build();

		when(relationRepositoryMock.findByIdAndMunicipalityId(any(), any())).thenReturn(Optional.of(entity));
		when(relationTypeRepositoryMock.findByName(any())).thenReturn(Optional.of(relationTypeEntityMock));
		when(relationRepositoryMock.save(any())).thenReturn(savedEntity);
		when(mapperMock.toRelation(any())).thenReturn(responseRelation);

		final var result = service.saveRelation(MUNICIPALITY_ID, relation);

		assertThat(result).isSameAs(responseRelation);
		verify(relationRepositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verify(relationTypeRepositoryMock).findByName(typeName);
		verify(mapperMock).updateRelationEntity(same(entity), same(relation), same(relationTypeEntityMock));
		verify(relationTypeEntityMock).getCounterType();
		verify(relationRepositoryMock).delete(same(inverseEntity));
		verify(mapperMock).toRelation(same(savedEntity));

		verifyNoMoreInteractions(relationRepositoryMock, mapperMock, relationTypeRepositoryMock);
	}

	@Test
	void saveRelationNotFound() {
		final var id = "id";

		assertThatThrownBy(() -> service.saveRelation(MUNICIPALITY_ID, Relation.builder().withId(id).build()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Relation with id 'id' not found");

		verify(relationRepositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verifyNoMoreInteractions(relationRepositoryMock);
		verifyNoInteractions(mapperMock, relationTypeRepositoryMock);
	}

	@Test
	void saveRelationTypeNotFound() {
		final var id = "id";
		final var typeName = "typeName";
		when(relationRepositoryMock.findByIdAndMunicipalityId(any(), any())).thenReturn(Optional.of(RelationEntity.builder().build()));

		assertThatThrownBy(() -> service.saveRelation(MUNICIPALITY_ID, Relation.builder().withId(id).withType(typeName).build()))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Request: 'typeName' is not a valid type");

		verify(relationRepositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verify(relationTypeRepositoryMock).findByName(typeName);
		verifyNoMoreInteractions(relationRepositoryMock, relationTypeRepositoryMock);
		verifyNoInteractions(mapperMock);
	}

	@Test
	void deleteRelation() {
		final var id = "id";
		final var entity = RelationEntity.builder().build();

		when(relationRepositoryMock.findByIdAndMunicipalityId(any(), any())).thenReturn(Optional.of(entity));

		service.deleteRelation(MUNICIPALITY_ID, id);

		verify(relationRepositoryMock).delete(same(entity));
		verifyNoMoreInteractions(relationRepositoryMock);
		verifyNoInteractions(mapperMock);
	}

	@Test
	void deleteRelationNotFound() {
		final var id = "id";

		assertThatThrownBy(() -> service.deleteRelation(MUNICIPALITY_ID, id))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Relation with id 'id' not found");

		verify(relationRepositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verifyNoInteractions(mapperMock);
		verifyNoMoreInteractions(relationRepositoryMock);
	}

	@Test
	void getRelation() {
		final var id = "id";
		final var entity = RelationEntity.builder().build();
		final var relation = Relation.builder().build();

		when(relationRepositoryMock.findByIdAndMunicipalityId(any(), any())).thenReturn(Optional.of(entity));
		when(mapperMock.toRelation(any())).thenReturn(relation);

		final var result = service.getRelation(MUNICIPALITY_ID, id);

		assertThat(result).isSameAs(relation);
		verify(relationRepositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verify(mapperMock).toRelation(same(entity));
		verifyNoMoreInteractions(relationRepositoryMock, mapperMock);
	}

	@Test
	void getRelationNotFound() {
		final var id = "id";

		assertThatThrownBy(() -> service.getRelation(MUNICIPALITY_ID, id))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Relation with id 'id' not found");

		verify(relationRepositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verifyNoInteractions(mapperMock);
		verifyNoMoreInteractions(relationRepositoryMock);
	}
}
