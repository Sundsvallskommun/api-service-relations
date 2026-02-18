package se.sundsvall.relations.api;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.relations.Application;
import se.sundsvall.relations.api.model.RelationType;
import se.sundsvall.relations.service.RelationTypeService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class RelationTypeResourceTest {

	private static final String TYPE_NAME = "TYPE_NAME";
	private static final String TYPE_DISPLAY_NAME = "TYPE_DISPLAY_NAME";
	private static final String COUNTER_TYPE_NAME = "COUNTER_TYPE_NAME";
	private static final String COUNTER_TYPE_DISPLAY_NAME = "COUNTER_TYPE_DISPLAY_NAME";

	@MockitoBean
	private RelationTypeService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createRelationType() {

		final var relationType = RelationType.builder()
			.withName(TYPE_NAME)
			.withDisplayName(TYPE_DISPLAY_NAME)
			.withCounterName(COUNTER_TYPE_NAME)
			.withCounterDisplayName(COUNTER_TYPE_DISPLAY_NAME)
			.build();

		when(serviceMock.createType(any())).thenReturn(TYPE_NAME);

		webTestClient.post()
			.uri("/relation-types")
			.contentType(APPLICATION_JSON)
			.bodyValue(relationType)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/relation-types/" + TYPE_NAME)
			.expectBody().isEmpty();

		verify(serviceMock).createType(relationType);
	}

	@Test
	void getRelationType() {
		final var relationType = RelationType.builder()
			.withName(TYPE_NAME)
			.withDisplayName(TYPE_DISPLAY_NAME)
			.withCounterName(COUNTER_TYPE_NAME)
			.withCounterDisplayName(COUNTER_TYPE_DISPLAY_NAME)
			.build();

		when(serviceMock.getType(any())).thenReturn(relationType);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/relation-types/{type}").build(Map.of("type", TYPE_NAME)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(RelationType.class)
			.returnResult()
			.getResponseBody();

		verify(serviceMock).getType(TYPE_NAME);
		assertThat(response).isNotNull().isEqualTo(relationType);
	}

	@Test
	void getAllRelationTypes() {
		final var relationTypeFirst = RelationType.builder().withName(TYPE_NAME).withCounterName(COUNTER_TYPE_NAME).build();
		final var relationTypeSecond = RelationType.builder().withName(TYPE_NAME + "-2").withCounterName(COUNTER_TYPE_NAME + "-2").build();

		when(serviceMock.getAllTypes()).thenReturn(List.of(relationTypeFirst, relationTypeSecond));

		final var response = webTestClient.get()
			.uri("/relation-types")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(RelationType.class)
			.returnResult()
			.getResponseBody();

		verify(serviceMock).getAllTypes();
		assertThat(response)
			.isNotNull()
			.hasSize(2)
			.containsExactlyInAnyOrder(relationTypeFirst, relationTypeSecond);
	}

	@Test
	void saveRelationType() {
		final var relationType = RelationType.builder().withName(TYPE_NAME).withCounterName(COUNTER_TYPE_NAME).build();

		when(serviceMock.saveRelationType(any(), any())).thenReturn(relationType);

		final var response = webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path("/relation-types/{type}").build(Map.of("type", TYPE_NAME + "-old-value")))
			.bodyValue(relationType)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(RelationType.class)
			.returnResult()
			.getResponseBody();

		verify(serviceMock).saveRelationType(TYPE_NAME + "-old-value", relationType);
		assertThat(response).isNotNull().isEqualTo(relationType);
	}

	@Test
	void deleteRelation() {
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path("/relation-types/{type}").build(Map.of("type", TYPE_NAME)))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		verify(serviceMock).deleteRelationType(TYPE_NAME);
	}
}
