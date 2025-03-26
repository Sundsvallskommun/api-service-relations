package se.sundsvall.relations.api;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.relations.Application;
import se.sundsvall.relations.api.model.Relation;
import se.sundsvall.relations.service.RelationService;

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

	private static Relation createRelationInstance() {
		return Relation.builder()
			.withSourceId("sourceId")
			.withSourceService("sourceService")
			.withSourceNamespace("sourceNamespace")
			.withSourceType("sourceType")
			.withTargetId("targetId")
			.withTargetService("targetService")
			.withTargetNamespace("targetNamespace")
			.withTargetType("targetType")
			.withType("type")
			.build();
	}

	@Test
	void createRelation() {

		final var relationInstance = createRelationInstance();

		when(serviceMock.createRelation(any(), any())).thenReturn(RELATION_ID);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/relation").build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(relationInstance)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/relation/" + RELATION_ID)
			.expectBody().isEmpty();

		verify(serviceMock).createRelation(MUNICIPALITY_ID, relationInstance);
	}

	@Test
	void getRelation() {

		final var relationInstance = createRelationInstance();

		when(serviceMock.getRelation(any(), any())).thenReturn(relationInstance);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/relation/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", RELATION_ID)))
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
		final var relationInstance = createRelationInstance();
		final var pageable = PageRequest.of(0, 20);
		final var matches = new PageImpl<>(List.of(relationInstance), pageable, 1);
		final var filter = "sourceId:'some-id'";

		when(serviceMock.findRelations(any(), any(), any())).thenReturn(matches);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/relation")
				.queryParam("filter", filter)
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(new ParameterizedTypeReference<RestResponsePage<Relation>>() {

			})
			.returnResult()
			.getResponseBody();

		verify(serviceMock).findRelations(eq(MUNICIPALITY_ID), any(), eq(pageable));
		assertThat(response).isNotNull();
		assertThat(response.getContent()).hasSize(1).first().isEqualTo(relationInstance);

	}

	@Test
	void saveRelation() {
		final var relationInstance = createRelationInstance();

		when(serviceMock.saveRelation(any(), any())).thenReturn(relationInstance);

		final var response = webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/relation/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", RELATION_ID)))
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
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/relation/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", RELATION_ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		verify(serviceMock).deleteRelation(MUNICIPALITY_ID, RELATION_ID);
	}

	@ParameterizedTest
	@MethodSource("allMethodsAndUriWithRequestBody")
	void validateReadOnly(String httpType, Function<UriBuilder, URI> uriFunction) {

		final var relationInstance = createRelationInstance();

		relationInstance.setCreated(OffsetDateTime.now());
		relationInstance.setModified(OffsetDateTime.now());
		relationInstance.setId("id");

		final var response = webTestClient.method(HttpMethod.valueOf(httpType))
			.uri(uriFunction)
			.bodyValue(relationInstance)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult();

		assertThat(response.getResponseBody()).isNotNull();
		assertThat(response.getResponseBody().getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getResponseBody().getViolations())
			.hasSize(3)
			.extracting("field", "message").containsExactlyInAnyOrder(
				Tuple.tuple("id", "must be null"),
				Tuple.tuple("created", "must be null"),
				Tuple.tuple("modified", "must be null"));
	}

	private static Stream<Arguments> allMethodsAndUriWithRequestBody() {
		Function<UriBuilder, URI> relationUri = uriBuilder -> uriBuilder.path("/{municipalityId}/relation").build(Map.of("municipalityId", MUNICIPALITY_ID));
		Function<UriBuilder, URI> relationUriWithId = uriBuilder -> uriBuilder.path("/{municipalityId}/relation/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", "some-id"));

		return Stream.of(
			Arguments.of("POST", relationUri),
			Arguments.of("PUT", relationUriWithId));
	}

	@ParameterizedTest
	@MethodSource("allMethodsAndUri")
	void validateMunicipalityId(String httpType, Function<UriBuilder, URI> uriFunction) {
		final var response = webTestClient.method(HttpMethod.valueOf(httpType))
			.uri(uriFunction)
			.bodyValue(createRelationInstance())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult();

		assertThat(response.getResponseBody()).isNotNull();
		assertThat(response.getResponseBody().getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getResponseBody().getViolations())
			.hasSize(1).first()
			.extracting("message").isEqualTo("not a valid municipality ID");
	}

	private static Stream<Arguments> allMethodsAndUri() {
		Function<UriBuilder, URI> relationUri = uriBuilder -> uriBuilder.path("/{municipalityId}/relation").build(Map.of("municipalityId", "1234"));
		Function<UriBuilder, URI> relationUriWithId = uriBuilder -> uriBuilder.path("/{municipalityId}/relation/{id}").build(Map.of("municipalityId", "1234", "id", "some-id"));

		return Stream.of(
			Arguments.of("POST", relationUri),
			Arguments.of("GET", relationUri),
			Arguments.of("GET", relationUriWithId),
			Arguments.of("PUT", relationUriWithId),
			Arguments.of("DELETE", relationUriWithId));
	}

	// Helper implementation of Page
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class RestResponsePage<T> extends PageImpl<T> {

		@Serial
		private static final long serialVersionUID = -7361702892303169935L;

		@JsonCreator(mode = PROPERTIES)
		public RestResponsePage(@JsonProperty("content") final List<T> content, @JsonProperty("number") final int number, @JsonProperty("size") final int size,
			@JsonProperty("totalElements") final Long totalElements, @JsonProperty("pageable") final JsonNode pageable, @JsonProperty("last") final boolean last,
			@JsonProperty("totalPages") final int totalPages, @JsonProperty("sort") final JsonNode sort, @JsonProperty("first") final boolean first,
			@JsonProperty("numberOfElements") final int numberOfElements) {
			super(content, PageRequest.of(number, size), totalElements);
		}

		public RestResponsePage(final List<T> content, final Pageable pageable, final long total) {
			super(content, pageable, total);
		}

		public RestResponsePage(final List<T> content) {
			super(content);
		}

		public RestResponsePage() {
			super(new ArrayList<>());
		}
	}
}
