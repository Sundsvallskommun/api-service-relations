package se.sundsvall.relations.api;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.relations.Application;
import se.sundsvall.relations.api.model.RelationType;
import se.sundsvall.relations.service.RelationTypeService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class RelationTypeResourceFailuresTest {

	@MockitoBean
	private RelationTypeService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createRelationType() {

		final var response = webTestClient.post()
			.uri("/relation-types")
			.contentType(APPLICATION_JSON)
			.bodyValue(RelationType.builder().build()) // All null values
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult();

		assertThat(response.getResponseBody()).isNotNull();
		assertThat(response.getResponseBody().getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getResponseBody().getViolations())
			.hasSize(1)
			.extracting("field", "message").containsExactlyInAnyOrder(
				Tuple.tuple("name", "must not be blank"));

		verifyNoInteractions(serviceMock);
	}
}
