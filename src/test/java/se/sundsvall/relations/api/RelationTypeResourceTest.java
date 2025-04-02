package se.sundsvall.relations.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class RelationTypeResourceTest {

	private static final String TYPE = "type";
	private static final String COUNTER_TYPE = "counterType";

	@MockitoBean
	private RelationTypeService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createRelationType() {

		final var relationType = RelationType.builder().withType(TYPE).withCounterType(COUNTER_TYPE).build();

		when(serviceMock.createType(any())).thenReturn(TYPE);

		webTestClient.post()
			.uri("/relation-types")
			.contentType(APPLICATION_JSON)
			.bodyValue(relationType)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/relation-types/" + TYPE)
			.expectBody().isEmpty();

		verify(serviceMock).createType(relationType);
	}

	@Test
	void getRelationType() {
		final var relationType = RelationType.builder().withType(TYPE).withCounterType(COUNTER_TYPE).build();

		when(serviceMock.getType(any())).thenReturn(relationType);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/relation-types/{type}").build(Map.of("type", TYPE)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(RelationType.class)
			.returnResult()
			.getResponseBody();

		verify(serviceMock).getType(TYPE);
		assertThat(response).isNotNull().isEqualTo(relationType);
	}

	@Test
	void getAllRelationTypes() {
		final var relationTypeFirst = RelationType.builder().withType(TYPE).withCounterType(COUNTER_TYPE).build();
		final var relationTypeSecond = RelationType.builder().withType(TYPE + "-2").withCounterType(COUNTER_TYPE + "-2").build();

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
		final var relationType = RelationType.builder().withType(TYPE).withCounterType(COUNTER_TYPE).build();

		when(serviceMock.saveRelationType(any(), any())).thenReturn(relationType);

		final var response = webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path("/relation-types/{type}").build(Map.of("type", TYPE + "-old-value")))
			.bodyValue(relationType)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(RelationType.class)
			.returnResult()
			.getResponseBody();

		verify(serviceMock).saveRelationType(TYPE + "-old-value", relationType);
		assertThat(response).isNotNull().isEqualTo(relationType);
	}

	@Test
	void deleteRelation() {
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path("/relation-types/{type}").build(Map.of("type", TYPE)))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		verify(serviceMock).deleteRelationType(TYPE);
	}
}
