package se.sundsvall.relations.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.relations.Application;
import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.api.model.RelationPageParameters;
import se.sundsvall.relations.api.model.RelationPagedResponse;
import se.sundsvall.relations.service.RelationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.relations.api.RelationTestUtil.createRelationInstance;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class RelationResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String RELATION_ID = UUID.randomUUID().toString();

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private RelationService serviceMock;

	@Captor
	private ArgumentCaptor<Relation> relationCaptor;

	@Test
	void createRelation() {

		final var relationInstance = createRelationInstance();

		when(serviceMock.createRelation(any(), any())).thenReturn(RELATION_ID);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/relations").build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(relationInstance)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/" + MUNICIPALITY_ID + "/relations/" + RELATION_ID)
			.expectBody().isEmpty();

		verify(serviceMock).createRelation(MUNICIPALITY_ID, relationInstance);
	}

	@Test
	void getRelation() {

		final var relationInstance = createRelationInstance();

		when(serviceMock.getRelation(any(), any())).thenReturn(relationInstance);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/relations/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", RELATION_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Relation.class)
			.returnResult()
			.getResponseBody();

		verify(serviceMock).getRelation(MUNICIPALITY_ID, RELATION_ID);
		assertThat(response).isNotNull().isEqualTo(relationInstance);
	}

	@Test
	void findRelation() {
		final var pageParameters = new RelationPageParameters();
		pageParameters.setSortBy(List.of("parameters"));
		final var relationInstance = createRelationInstance();
		final var pageable = new PageImpl<>(List.of(relationInstance));
		final var matches = RelationPagedResponse.builder()
			.withRelations(pageable.getContent())
			.withMetaData(PagingAndSortingMetaData.create().withPageData(pageable))
			.build();
		final var filter = "sourceId:'some-id'";

		when(serviceMock.findRelations(any(), any(), any())).thenReturn(matches);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/relations")
				.queryParam("filter", filter)
				.queryParam("page", pageParameters.getPage())
				.queryParam("limit", pageParameters.getLimit())
				.queryParam("sortBy", pageParameters.getSortBy())
				.queryParam("sortDirection", pageParameters.getSortDirection())
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(RelationPagedResponse.class)
			.returnResult()
			.getResponseBody();

		verify(serviceMock).findRelations(eq(MUNICIPALITY_ID), any(), eq(pageParameters));
		assertThat(response).isNotNull();
		assertThat(response.getRelations()).hasSize(1).first().isEqualTo(relationInstance);

	}

	@Test
	void saveRelation() {
		final var relationInstance = createRelationInstance();

		when(serviceMock.saveRelation(any(), any())).thenReturn(relationInstance);

		final var response = webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/relations/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", RELATION_ID)))
			.bodyValue(relationInstance)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Relation.class)
			.returnResult()
			.getResponseBody();

		verify(serviceMock).saveRelation(eq(MUNICIPALITY_ID), relationCaptor.capture());
		assertThat(relationCaptor.getValue()).usingRecursiveComparison().ignoringFields("id").isEqualTo(relationInstance);
		assertThat(relationCaptor.getValue().getId()).isEqualTo(RELATION_ID);
		assertThat(response).isNotNull().isEqualTo(relationInstance);

	}

	@Test
	void deleteRelation() {
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/relations/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", RELATION_ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		verify(serviceMock).deleteRelation(MUNICIPALITY_ID, RELATION_ID);
	}
}
