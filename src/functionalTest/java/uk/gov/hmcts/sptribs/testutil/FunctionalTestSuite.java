package uk.gov.hmcts.sptribs.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.services.cdam.CdamUrlDebugger;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_CASE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@TestPropertySource("classpath:application.yaml")
public abstract class FunctionalTestSuite {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 4, 28, 1, 0);
    private static final Logger log = LoggerFactory.getLogger(FunctionalTestSuite.class);

    @Value("${test-url}")
    protected String testUrl;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected IdamService idamService;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    protected CoreCaseDataApi coreCaseDataApi;

    @Autowired
    protected CcdSearchService searchService;

    @Autowired
    protected CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AppsConfig appsConfig;

    @Autowired
    protected CdamUrlDebugger cdamUrlDebugger;

    protected static final String EVENT_PARAM = "event";
    protected static final String UPDATE = "UPDATE";
    protected static final String UPDATE_CASE = "UPDATE_CASE";
    protected static final String SUBMIT = "SUBMIT";

    protected CaseDetails createCaseInCcd() {
        String caseworkerToken = idamTokenGenerator.generateIdamTokenForCaseworker();
        String s2sTokenForCaseApi = serviceAuthenticationGenerator.generate("sptribs_case_api");
        String caseworkerUserId = idamTokenGenerator.getUserDetailsFor(caseworkerToken).getId();
        StartEventResponse startEventResponse = startEventForCreateCase(caseworkerToken, s2sTokenForCaseApi, caseworkerUserId);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(CASEWORKER_CREATE_CASE)
                .summary("Create draft case")
                .description("Create draft case for functional tests")
                .build())
            .data(Map.of(
            ))
            .build();

        return submitNewCase(caseDataContent, caseworkerToken, s2sTokenForCaseApi, caseworkerUserId);
    }

    private StartEventResponse startEventForCreateCase(String caseworkerToken, String s2sToken, String caseworkerUserId) {
        // not including in try catch to fail fast the method
        return coreCaseDataApi.startForCaseworker(
            caseworkerToken,
            s2sToken,
            caseworkerUserId,
            CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION.getJurisdictionId(),
            CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName(),
            CASEWORKER_CREATE_CASE
        );
    }

    private CaseDetails submitNewCase(CaseDataContent caseDataContent, String solicitorToken, String s2sToken, String solicitorUserId) {
        // not including in try catch to fast fail the method
        return coreCaseDataApi.submitForCaseworker(
            solicitorToken,
            s2sToken,
            solicitorUserId,
            CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION.getJurisdictionId(),
            CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName(),
            true,
            caseDataContent
        );
    }

    protected Response triggerCallback(Map<String, Object> caseData, String eventId, String url) throws IOException {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(
                CaseDetails
                    .builder()
                    .id(1234567890123456L)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .build())
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(1234567890123456L)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .build()
            )
            .build();

        return triggerCallback(request, url);
    }

    protected Response triggerCallback(Map<String, Object> caseData, String eventId, String url, Long caseId) throws IOException {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(
                CaseDetails
                    .builder()
                    .id(caseId)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .build())
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(caseId)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .build()
            )
            .build();

        return triggerCallback(request, url);
    }

    protected Response triggerCallback(Map<String, Object> caseData, String eventId, String url, State state) throws IOException {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(
                CaseDetails
                    .builder()
                    .id(1234567890123456L)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .state(state.getName())
                    .build()
            )
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(1234567890123456L)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName())
                    .state(state.getName())
                    .build()
            )
            .build();

        return triggerCallback(request, url);
    }

    protected Response triggerCallback(CallbackRequest request, String url) {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(request)
            .when()
            .post(url);
    }

    protected List<CaseDetails> searchForCasesWithQuery(BoolQueryBuilder query) {
        return searchService.searchForAllCasesWithQuery(
            query,
            idamService.retrieveSystemUpdateUserDetails(),
            serviceAuthenticationGenerator.generate(),
            State.Draft
        );
    }

    protected CaseData getCaseData(Map<String, Object> data) {
        return objectMapper.convertValue(data, CaseData.class);
    }

    protected long createTestCaseAndGetCaseReference() {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .body(getDssCaseData())
            .when()
            .post("/case/dss-orchestration/create")
            .getBody()
            .path("id");
    }

    protected long createAndSubmitTestCaseAndGetCaseReference() {
        final long caseReference = createTestCaseAndGetCaseReference();
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .param(EVENT_PARAM, SUBMIT)
            .body(getDssCaseData())
            .when()
            .put("/case/dss-orchestration/" + caseReference +  "/update")
            .getBody()
            .path("id");
    }

    protected Response createAndSubmitTestCaseAndGetResponse() {
        final long caseReference = createTestCaseAndGetCaseReference();
        log.debug("Test url: {}", testUrl);
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .param(EVENT_PARAM, SUBMIT)
            .body(getDssCaseData())
            .when()
            .put("/case/dss-orchestration/" + caseReference +  "/update");
    }

    protected void updateTestCaseAndGetResponse(long caseReference) {
        RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .param(EVENT_PARAM, SUBMIT)
            .body(getDssCaseData())
            .when()
            .put("/case/dss-orchestration/" + caseReference + "/update?event=UPDATE_CASE");
    }

    protected DssCaseData getDssCaseData() {
        return DssCaseData.builder()
            .subjectFullName("Test Name")
            .subjectDateOfBirth(LocalDate.of(1990, 1, 1))
            .subjectEmailAddress("test@email.com")
            .subjectContactNumber("07123412345")
            .caseTypeOfApplication("CIC")
            .build();
    }

    protected DssCaseData getDssCaseDataUpdated() {
        return DssCaseData.builder()
            .caseTypeOfApplication("CIC")
            .additionalInformation("some additional info")
            .build();
    }

    protected UploadResponse uploadTestDocument(ClassPathResource resource) {
        cdamUrlDebugger.logUrls();
        final List<AppsConfig.AppsDetails> appDetails = appsConfig.getApps();
        if (!appDetails.isEmpty() && appDetails.getFirst() != null) {
            final String caseType = appsConfig.getApps().getFirst().getCaseType();
            final String jurisdiction = appsConfig.getApps().getFirst().getJurisdiction();
            try {
                final InMemoryMultipartFile inMemoryMultipartFile =
                        new InMemoryMultipartFile(resource.getFilename(), resource.getContentAsByteArray());

                final DocumentUploadRequest documentUploadRequest =
                    new DocumentUploadRequest(Classification.RESTRICTED.toString(),
                        caseType,
                        jurisdiction,
                        List.of(inMemoryMultipartFile));

                final String serviceToken = serviceAuthenticationGenerator.generate();
                final String userToken = idamTokenGenerator.generateIdamTokenForSystemUser();

                return caseDocumentClientApi.uploadDocuments(userToken, serviceToken, documentUploadRequest);
            } catch (IOException ioException) {
                log.error("Failed to upload test document due to {}", ioException.toString());
            }
        }
        return null;
    }
}
