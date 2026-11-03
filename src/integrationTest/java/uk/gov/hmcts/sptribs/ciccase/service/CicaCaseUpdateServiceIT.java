package uk.gov.hmcts.sptribs.ciccase.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import feign.FeignException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.common.repositories.impl.CaseDataRepositoryImpl;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseUpdate;
import uk.gov.hmcts.sptribs.exception.CaseNotFoundException;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

/** Exercises the real access/update/role services and CCD Feign clients against a local HTTP server. */
@SpringBootTest
class CicaCaseUpdateServiceIT {

    private static final String CASE_ID = "1234567890123456";
    private static final String CITIZEN_ID = "citizen-user-id";
    private static final String CITIZEN_EMAIL = "citizen@example.test";
    private static final String CITIZEN_AUTH = "Bearer citizen-token";
    private static final String SYSTEM_AUTH = "Bearer system-token";
    private static final String SERVICE_TOKEN = "service-token";
    private static final String START_URL = "/cases/" + CASE_ID + "/event-triggers/" + CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
    private static final String SUBMIT_URL = "/cases/" + CASE_ID + "/events";
    private static final String RECOVERY = "creator-role-recovery";
    private static final String ROLE_REQUIRED = "role-required";
    private static final String ROLE_CHECKED = "role-checked";
    private static final String ROLE_ENSURED = "role-ensured";
    private static final WireMockServer CCD = new WireMockServer(wireMockConfig().dynamicPort());

    @Autowired
    private CicaCaseUpdateService service;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CaseDataRepositoryImpl caseDataRepository;

    @MockitoBean
    private IdamService idamService;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @DynamicPropertySource
    static void configureCcd(DynamicPropertyRegistry registry) {
        CCD.start();
        registry.add("core_case_data.api.url", CCD::baseUrl);
    }

    @AfterAll
    static void stopCcd() {
        CCD.stop();
    }

    @BeforeEach
    void setUp() {
        CCD.resetAll();
        when(idamService.retrieveUser(CITIZEN_AUTH)).thenReturn(new CICUser(CITIZEN_AUTH,
            UserInfo.builder().uid(CITIZEN_ID).sub(CITIZEN_EMAIL).build()));
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    void shouldSubmitDocumentsAndTextAsCitizenWithoutRequiringPostcode() throws Exception {
        givenCaseMember();
        stubStart("event-token");
        stubSubmission();

        service.updateCase(CASE_ID, CITIZEN_AUTH, update());

        verify(caseDataRepository).checkCaseExists(CASE_ID);
        verify(caseDataRepository).checkIfUserHasAccessToCase(CASE_ID, CITIZEN_EMAIL);
        CCD.verify(1, getRequestedFor(urlEqualTo(START_URL))
            .withHeader(AUTHORIZATION, equalTo(CITIZEN_AUTH))
            .withHeader(SERVICE_AUTHORIZATION, equalTo(SERVICE_TOKEN)));
        CCD.verify(1, postRequestedFor(urlEqualTo(SUBMIT_URL))
            .withHeader(AUTHORIZATION, equalTo(CITIZEN_AUTH))
            .withHeader(SERVICE_AUTHORIZATION, equalTo(SERVICE_TOKEN)));

        JsonNode submitted = submittedBody("event-token");
        assertThat(submitted.path("event").path("id").asText()).isEqualTo(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        JsonNode data = submitted.path("data");
        assertThat(data.size()).isEqualTo(3);
        assertThat(data.path("dssCaseDataCaseTypeOfApplication").asText()).isEqualTo("CIC");
        assertThat(data.path("dssCaseDataAdditionalInformation").asText()).isEqualTo("Additional information");
        JsonNode documents = data.path("dssCaseDataOtherInfoDocuments");
        assertThat(documents.size()).isEqualTo(1);
        assertThat(documents.get(0).path("id").asText()).isNotBlank();
        assertThat(documents.get(0).path("value")).isEqualTo(objectMapper.readTree("""
            {
              "documentLink": {
                "document_url": "http://documents/1",
                "document_binary_url": "http://documents/1/binary",
                "document_filename": "evidence.pdf"
              },
              "comment": "Evidence"
            }
            """));
        assertNoRoleRequests();
    }

    @ParameterizedTest
    @CsvSource({"true,false", "false,true"})
    void shouldSubmitTextOnlyAndDocumentsOnlyUpdates(boolean includeText, boolean includeDocuments) throws Exception {
        givenCaseMember();
        stubStart("event-token");
        stubSubmission();

        service.updateCase(CASE_ID, CITIZEN_AUTH, new CicaCaseUpdate(
            includeText ? update().additionalInformation() : null, includeDocuments ? update().documents() : List.of()));

        JsonNode data = submittedBody("event-token").path("data");
        assertThat(data.path("dssCaseDataOtherInfoDocuments").size()).isEqualTo(includeDocuments ? 1 : 0);
        if (includeText) {
            assertThat(data.path("dssCaseDataAdditionalInformation").asText()).isEqualTo("Additional information");
        } else {
            // The configured HTTP encoder omits null map values.
            assertThat(data.has("dssCaseDataAdditionalInformation")).isFalse();
        }
        assertNoRoleRequests();
    }

    @Test
    void shouldEnsureCreatorRoleAfterStart404BeforeRetryingWithCitizenToken() throws Exception {
        givenCaseMember();
        stubStart404();
        stubRoleRecovery(false);
        stubRetryStart(200);
        stubSubmission();

        service.updateCase(CASE_ID, CITIZEN_AUTH, update());

        assertRoleAssignedToCitizen();
        CCD.verify(2, getRequestedFor(urlEqualTo(START_URL)).withHeader(AUTHORIZATION, equalTo(CITIZEN_AUTH)));
        CCD.verify(1, postRequestedFor(urlEqualTo(SUBMIT_URL)).withHeader(AUTHORIZATION, equalTo(CITIZEN_AUTH)));
        assertThat(submittedBody("retry-token").path("event_token").asText()).isEqualTo("retry-token");
    }

    @Test
    void shouldObtainFreshTokenAfterSubmission404AndResubmitTheSameUpdate() throws Exception {
        givenCaseMember();
        CCD.stubFor(get(urlEqualTo(START_URL)).inScenario(RECOVERY).whenScenarioStateIs(STARTED)
            .willReturn(okJson(startResponse("first-token"))));
        CCD.stubFor(post(urlEqualTo(SUBMIT_URL)).inScenario(RECOVERY).whenScenarioStateIs(STARTED)
            .withRequestBody(matchingJsonPath("$.event_token", equalTo("first-token")))
            .willReturn(aResponse().withStatus(404)).willSetStateTo(ROLE_REQUIRED));
        stubRoleRecovery(false);
        stubRetryStart(200);
        CCD.stubFor(post(urlEqualTo(SUBMIT_URL)).inScenario(RECOVERY).whenScenarioStateIs(ROLE_ENSURED)
            .withRequestBody(matchingJsonPath("$.event_token", equalTo("retry-token")))
            .willReturn(okJson("{}")));

        service.updateCase(CASE_ID, CITIZEN_AUTH, update());

        assertRoleAssignedToCitizen();
        CCD.verify(2, postRequestedFor(urlEqualTo(SUBMIT_URL)).withHeader(AUTHORIZATION, equalTo(CITIZEN_AUTH)));
        assertThat(submittedBody("retry-token").path("data")).isEqualTo(submittedBody("first-token").path("data"));
    }

    @Test
    void shouldRetryWithoutAssigningCreatorRoleWhenItAlreadyExists() {
        givenCaseMember();
        stubStart404();
        stubRoleRecovery(true);
        stubRetryStart(200);
        stubSubmission();

        service.updateCase(CASE_ID, CITIZEN_AUTH, update());

        CCD.verify(1, getRequestedFor(urlPathEqualTo("/case-users")));
        CCD.verify(0, postRequestedFor(urlEqualTo("/case-users")));
        CCD.verify(2, getRequestedFor(urlEqualTo(START_URL)));
        CCD.verify(1, postRequestedFor(urlEqualTo(SUBMIT_URL)));
    }

    @Test
    void shouldStopAfterOneRecoveryAttemptWhenRetryAlsoReturns404() {
        givenCaseMember();
        stubStart404();
        stubRoleRecovery(false);
        stubRetryStart(404);

        assertThatThrownBy(() -> service.updateCase(CASE_ID, CITIZEN_AUTH, update()))
            .isInstanceOf(FeignException.NotFound.class);

        assertRoleAssignedToCitizen();
        CCD.verify(2, getRequestedFor(urlEqualTo(START_URL)));
        CCD.verify(0, postRequestedFor(urlEqualTo(SUBMIT_URL)));
    }

    @ParameterizedTest
    @CsvSource({"start,401", "start,403", "start,409", "start,500", "submit,403", "submit,409", "submit,422", "submit,503"})
    void shouldNotRecoverOrRetryNon404Failures(String stage, int status) {
        givenCaseMember();
        if ("start".equals(stage)) {
            CCD.stubFor(get(urlEqualTo(START_URL)).willReturn(aResponse().withStatus(status)));
        } else {
            stubStart("event-token");
            CCD.stubFor(post(urlEqualTo(SUBMIT_URL)).willReturn(aResponse().withStatus(status)));
        }

        assertThatThrownBy(() -> service.updateCase(CASE_ID, CITIZEN_AUTH, update()))
            .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(status));

        CCD.verify(1, getRequestedFor(urlEqualTo(START_URL)));
        CCD.verify("start".equals(stage) ? 0 : 1, postRequestedFor(urlEqualTo(SUBMIT_URL)));
        assertNoRoleRequests();
    }

    @Test
    void shouldNotAttemptAnotherRecoveryWhenRetriedSubmissionReturns404() {
        givenCaseMember();
        stubStart404();
        stubRoleRecovery(false);
        stubRetryStart(200);
        CCD.stubFor(post(urlEqualTo(SUBMIT_URL)).willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> service.updateCase(CASE_ID, CITIZEN_AUTH, update()))
            .isInstanceOf(FeignException.NotFound.class);

        assertRoleAssignedToCitizen();
        CCD.verify(1, getRequestedFor(urlPathEqualTo("/case-users")));
        CCD.verify(2, getRequestedFor(urlEqualTo(START_URL)));
        CCD.verify(1, postRequestedFor(urlEqualTo(SUBMIT_URL)));
    }

    @Test
    void shouldRejectNonMemberBeforeContactingCcdOrAssigningRoles() {
        when(caseDataRepository.checkCaseExists(CASE_ID)).thenReturn(true);
        when(caseDataRepository.checkIfUserHasAccessToCase(CASE_ID, CITIZEN_EMAIL)).thenReturn(false);

        assertThatThrownBy(() -> service.updateCase(CASE_ID, CITIZEN_AUTH, update()))
            .isInstanceOf(UnauthorisedCaseAccessException.class);

        assertThat(CCD.getAllServeEvents()).isEmpty();
        verify(idamService, never()).retrieveSystemUpdateUserDetails();
    }

    @Test
    void shouldRejectMissingCaseBeforeContactingCcdOrAssigningRoles() {
        when(caseDataRepository.checkCaseExists(CASE_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.updateCase(CASE_ID, CITIZEN_AUTH, update()))
            .isInstanceOf(CaseNotFoundException.class);

        assertThat(CCD.getAllServeEvents()).isEmpty();
        verify(caseDataRepository, never()).checkIfUserHasAccessToCase(CASE_ID, CITIZEN_EMAIL);
        verify(idamService, never()).retrieveSystemUpdateUserDetails();
    }

    @Test
    void shouldFailClosedWhenMembershipLookupFails() {
        when(caseDataRepository.checkCaseExists(CASE_ID)).thenReturn(true);
        when(caseDataRepository.checkIfUserHasAccessToCase(CASE_ID, CITIZEN_EMAIL))
            .thenThrow(new IllegalStateException("Database unavailable"));

        assertThatThrownBy(() -> service.updateCase(CASE_ID, CITIZEN_AUTH, update()))
            .isInstanceOf(UnauthorisedCaseAccessException.class);

        assertThat(CCD.getAllServeEvents()).isEmpty();
        verify(idamService, never()).retrieveSystemUpdateUserDetails();
    }

    @Test
    void shouldNotRetryUpdateWhenRoleAssignmentFailsAndRoleRemainsAbsent() {
        givenCaseMember();
        givenSystemUser();
        CCD.stubFor(get(urlEqualTo(START_URL)).willReturn(aResponse().withStatus(404)));
        CCD.stubFor(get(urlPathEqualTo("/case-users")).willReturn(okJson("{\"case_users\":[]}")));
        CCD.stubFor(post(urlEqualTo("/case-users")).willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> service.updateCase(CASE_ID, CITIZEN_AUTH, update()))
            .isInstanceOf(FeignException.ServiceUnavailable.class);

        CCD.verify(1, getRequestedFor(urlEqualTo(START_URL)));
        CCD.verify(2, getRequestedFor(urlPathEqualTo("/case-users")));
        CCD.verify(1, postRequestedFor(urlEqualTo("/case-users")));
        CCD.verify(0, postRequestedFor(urlEqualTo(SUBMIT_URL)));
    }

    @Test
    void shouldNotAssignRoleOrRetryUpdateWhenRoleLookupFails() {
        givenCaseMember();
        givenSystemUser();
        CCD.stubFor(get(urlEqualTo(START_URL)).willReturn(aResponse().withStatus(404)));
        CCD.stubFor(get(urlPathEqualTo("/case-users")).willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> service.updateCase(CASE_ID, CITIZEN_AUTH, update()))
            .isInstanceOf(FeignException.ServiceUnavailable.class);

        CCD.verify(1, getRequestedFor(urlEqualTo(START_URL)));
        CCD.verify(1, getRequestedFor(urlPathEqualTo("/case-users")));
        CCD.verify(0, postRequestedFor(urlEqualTo("/case-users")));
        CCD.verify(0, postRequestedFor(urlEqualTo(SUBMIT_URL)));
    }

    private void givenCaseMember() {
        when(caseDataRepository.checkCaseExists(CASE_ID)).thenReturn(true);
        when(caseDataRepository.checkIfUserHasAccessToCase(CASE_ID, CITIZEN_EMAIL)).thenReturn(true);
    }

    private void givenSystemUser() {
        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(new CICUser(SYSTEM_AUTH, UserInfo.builder().build()));
    }

    private CicaCaseUpdate update() {
        return new CicaCaseUpdate("Additional information", List.of(new CicaCaseUpdate.UploadedDocument(
            "http://documents/1", "http://documents/1/binary", "evidence.pdf", "Evidence")));
    }

    private String startResponse(String token) {
        return "{\"token\":\"" + token + "\",\"event_id\":\"" + CITIZEN_DSS_UPDATE_CASE_SUBMISSION + "\"}";
    }

    private void stubStart(String token) {
        CCD.stubFor(get(urlEqualTo(START_URL)).willReturn(okJson(startResponse(token))));
    }

    private void stubSubmission() {
        CCD.stubFor(post(urlEqualTo(SUBMIT_URL)).willReturn(okJson("{}")));
    }

    private void stubStart404() {
        CCD.stubFor(get(urlEqualTo(START_URL)).inScenario(RECOVERY).whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withStatus(404)).willSetStateTo(ROLE_REQUIRED));
    }

    private void stubRoleRecovery(boolean alreadyCreator) {
        givenSystemUser();
        String roles = alreadyCreator
            ? "{\"case_users\":[{\"case_id\":\"" + CASE_ID + "\",\"user_id\":\"" + CITIZEN_ID + "\",\"case_role\":\"[CREATOR]\"}]}"
            : "{\"case_users\":[]}";
        CCD.stubFor(get(urlPathEqualTo("/case-users")).inScenario(RECOVERY).whenScenarioStateIs(ROLE_REQUIRED)
            .withQueryParam("case_ids", equalTo(CASE_ID)).withQueryParam("user_ids", equalTo(CITIZEN_ID))
            .withHeader(AUTHORIZATION, equalTo(SYSTEM_AUTH))
            .withHeader(SERVICE_AUTHORIZATION, equalTo(SERVICE_TOKEN))
            .willReturn(okJson(roles)).willSetStateTo(alreadyCreator ? ROLE_ENSURED : ROLE_CHECKED));
        CCD.stubFor(post(urlEqualTo("/case-users")).inScenario(RECOVERY).whenScenarioStateIs(ROLE_CHECKED)
            .withHeader(AUTHORIZATION, equalTo(SYSTEM_AUTH))
            .withHeader(SERVICE_AUTHORIZATION, equalTo(SERVICE_TOKEN))
            .willReturn(aResponse().withStatus(201)).willSetStateTo(ROLE_ENSURED));
    }

    private void stubRetryStart(int status) {
        CCD.stubFor(get(urlEqualTo(START_URL)).inScenario(RECOVERY).whenScenarioStateIs(ROLE_ENSURED)
            .willReturn(aResponse().withStatus(status).withHeader("Content-Type", "application/json")
                .withBody(startResponse("retry-token"))));
    }

    private void assertRoleAssignedToCitizen() {
        CCD.verify(1, postRequestedFor(urlEqualTo("/case-users"))
            .withHeader(AUTHORIZATION, equalTo(SYSTEM_AUTH))
            .withHeader(SERVICE_AUTHORIZATION, equalTo(SERVICE_TOKEN))
            .withRequestBody(equalToJson("""
                {"case_users":[{"case_id":"%s","user_id":"%s","case_role":"[CREATOR]"}]}
                """.formatted(CASE_ID, CITIZEN_ID))));
    }

    private void assertNoRoleRequests() {
        CCD.verify(0, getRequestedFor(urlPathEqualTo("/case-users")));
        CCD.verify(0, postRequestedFor(urlEqualTo("/case-users")));
        verify(idamService, never()).retrieveSystemUpdateUserDetails();
    }

    private JsonNode submittedBody(String token) throws Exception {
        var requests = CCD.findAll(postRequestedFor(urlEqualTo(SUBMIT_URL))
            .withRequestBody(matchingJsonPath("$.event_token", equalTo(token))));
        assertThat(requests).hasSize(1);
        return objectMapper.readTree(requests.getFirst().getBodyAsString());
    }
}
