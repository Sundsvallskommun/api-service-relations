package se.sundsvall.relations.apptest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.relations.Application;
import se.sundsvall.relations.integration.db.RelationRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@WireMockAppTestSuite(files = "classpath:/RelationsIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class RelationsIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/relations";
	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";

	@Autowired
	private RelationRepository relationRepository;

	@Test
	void test01_getRelationById() {
		setupCall()
			.withServicePath(PATH + "/9557fadb-14b1-4202-a933-a8ad3c0c07ab")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_createRelation() {
		final var headers = setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of(PATH + "/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
			.sendRequestAndVerifyResponse()
			.getResponseHeaders();

		final var id = headers.get(LOCATION).getFirst().replace(PATH + "/", "");
		assertThat(relationRepository.findByIdAndMunicipalityId(id, "2281"))
			.hasValueSatisfying(relation -> {
				assertThat(relation.getInverseRelation()).isNotNull();
				assertThat(relation.getInverseRelation().getType().getName()).isEqualTo("counter_type-1");
			});
	}

	@Test
	void test03_updateRelation() {
		setupCall()
			.withServicePath(PATH + "/9557fadb-14b1-4202-a933-a8ad3c0c07ab")
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();

		setupCall()
			.withServicePath(PATH + "/4bb60383-c522-4187-ad43-f7a29635fc14")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse("response-inverse.json")
			.sendRequestAndVerifyResponse();

	}

	@Test
	void test04_findRelations() {
		setupCall()
			.withServicePath(PATH + "?filter=type.name:'type-1'")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_deleteRelation() {
		// Delete
		setupCall()
			.withServicePath(PATH + "/9557fadb-14b1-4202-a933-a8ad3c0c07ab")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Verify primary
		setupCall()
			.withServicePath(PATH + "/9557fadb-14b1-4202-a933-a8ad3c0c07ab")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
		// Verify inverse
		setupCall()
			.withServicePath(PATH + "/4bb60383-c522-4187-ad43-f7a29635fc14")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}
}
