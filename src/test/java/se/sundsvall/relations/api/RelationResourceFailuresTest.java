package se.sundsvall.relations.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.relations.api.RelationTestUtil.createRelationInstance;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.relations.Application;
import se.sundsvall.relations.service.RelationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class RelationResourceFailuresTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String RELATION_ID = UUID.randomUUID().toString();

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private RelationService serviceMock;

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

		verifyNoInteractions(serviceMock);
	}

	private static Stream<Arguments> allMethodsAndUriWithRequestBody() {
		Function<UriBuilder, URI> relationUri = uriBuilder -> uriBuilder.path("/{municipalityId}/relations").build(Map.of("municipalityId", MUNICIPALITY_ID));
		Function<UriBuilder, URI> relationUriWithId = uriBuilder -> uriBuilder.path("/{municipalityId}/relations/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", "some-id"));

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

		verifyNoInteractions(serviceMock);
	}

	private static Stream<Arguments> allMethodsAndUri() {
		Function<UriBuilder, URI> relationUri = uriBuilder -> uriBuilder.path("/{municipalityId}/relations").build(Map.of("municipalityId", "1234"));
		Function<UriBuilder, URI> relationUriWithId = uriBuilder -> uriBuilder.path("/{municipalityId}/relations/{id}").build(Map.of("municipalityId", "1234", "id", RELATION_ID));

		return Stream.of(
			Arguments.of("POST", relationUri),
			Arguments.of("GET", relationUri),
			Arguments.of("GET", relationUriWithId),
			Arguments.of("PUT", relationUriWithId),
			Arguments.of("DELETE", relationUriWithId));
	}

	@ParameterizedTest
	@MethodSource("allMethodsAndUriWithFaultyId")
	void validateId(String httpType, Function<UriBuilder, URI> uriFunction) {
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
			.hasSize(1)
			.extracting("message").allMatch("not a valid UUID"::equals);

		verifyNoInteractions(serviceMock);
	}

	private static Stream<Arguments> allMethodsAndUriWithFaultyId() {
		Function<UriBuilder, URI> relationUriWithId = uriBuilder -> uriBuilder.path("/{municipalityId}/relations/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", "bad-id"));

		return Stream.of(
			Arguments.of("GET", relationUriWithId),
			Arguments.of("PUT", relationUriWithId),
			Arguments.of("DELETE", relationUriWithId));
	}
}
