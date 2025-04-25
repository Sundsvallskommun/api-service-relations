package se.sundsvall.relations.apptest;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.relations.Application;

import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@WireMockAppTestSuite(files = "classpath:/RelationTypeIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class RelationTypeIT extends AbstractAppTest {

	private static final String PATH = "/relation-types";
	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test01_getType() {
		setupCall()
			.withServicePath(PATH + "/type-1")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_createType() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of(PATH + "/newType"))
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getAllTypes() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_updateRelation() {
		setupCall()
			.withServicePath(PATH + "/type-1")
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();

		setupCall()
			.withServicePath(PATH + "/type-1")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_updateRelationToOneWayWhenInUse() {
		setupCall()
			.withServicePath(PATH + "/type-1")
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CONFLICT)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PROBLEM_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_updateRelationToOneWay() {
		setupCall()
			.withServicePath(PATH + "/type-3")
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();

		setupCall()
			.withServicePath(PATH + "/counter_type-3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_delete() {
		setupCall()
			.withServicePath(PATH + "/type-3")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withServicePath(PATH + "/type-3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PROBLEM_JSON_VALUE))
			.sendRequest();

		setupCall()
			.withServicePath(PATH + "/counter_type-3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PROBLEM_JSON_VALUE))
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_deleteWhenInUse() {
		setupCall()
			.withServicePath(PATH + "/type-1")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(CONFLICT)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PROBLEM_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
