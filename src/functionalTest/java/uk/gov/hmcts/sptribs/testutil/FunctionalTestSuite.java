package uk.gov.hmcts.sptribs.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_CASE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.controllers.model.DssCaseDataRequest.convertDssCaseDataToRequest;

@ActiveProfiles("functional")
public abstract class FunctionalTestSuite {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 4, 28, 1, 0);
    private static final Logger log = LoggerFactory.getLogger(FunctionalTestSuite.class);

    protected static final ClassPathResource DRAFT_ORDER_FILE =
        new ClassPathResource("files/DRAFT :Order--[Subject Name]--26-04-2024 10:09:12.pdf");

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

    protected static final String EVENT_PARAM = "event";
    protected static final String UPDATE = "UPDATE";
    protected static final String UPDATE_CASE = "UPDATE_CASE";
    protected static final String SUBMIT = "SUBMIT";

    protected CaseDetails createCaseInCcd() {
        String caseworkerToken = idamTokenGenerator.generateIdamTokenForCaseworker();
        String s2sTokenForCaseApi = serviceAuthenticationGenerator.generate();
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

    private Long createPersistedCaseReference(Map<String, Object> caseData) {
        CaseDetails createdCase = createCaseInCcd();
        CaseData formatter = CaseData.builder().build();
        caseData.put("hyphenatedCaseRef", formatter.formatCaseRef(createdCase.getId()));
        return createdCase.getId();
    }

    protected Response triggerCallback(Map<String, Object> caseData, String eventId, String url) throws IOException {
        return triggerCallback(caseData, eventId, url, true);
    }

    private Response triggerCallback(Map<String, Object> caseData, String eventId, String url, boolean createCase)
        throws IOException {
        if (createCase && TestConstants.SUBMITTED_URL.equals(url)) {
            return triggerCallback(caseData, eventId, url, createPersistedCaseReference(caseData));
        }

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
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateCcdDataToken())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(request)
            .when()
            .post(url);
    }

    protected Response triggerCallbackWithoutPersistedCase(Map<String, Object> caseData, String eventId, String url)
        throws IOException {
        return triggerCallback(caseData, eventId, url, false);
    }

    protected List<CaseDetails> searchForCasesWithQuery(BoolQueryBuilder query) {
        return searchService.searchForAllCasesWithQuery(
            query,
            idamService.retrieveSystemUpdateUserDetails(),
            serviceAuthenticationGenerator.generateCcdDataToken(),
            State.Draft
        );
    }

    protected CaseData getCaseData(Map<String, Object> data) {
        return objectMapper.convertValue(data, CaseData.class);
    }

    protected long createAndSubmitCitizenCaseAndGetCaseReference() {
        return createAndSubmitCitizenCaseAndGetCaseDetails().getId();
    }

    protected CaseDetails createAndSubmitCitizenCaseAndGetCaseDetails() {
        CaseData caseData = getCaseDataWithDssData();
        AppsConfig.AppsDetails details = AppsUtil.getExactAppsDetails(appsConfig, caseData.getDssCaseData());
        CaseDetails caseDetails = createCitizenCase();

        return updateCitizenCase(EventConstants.CITIZEN_CIC_SUBMIT_CASE, caseDetails.getId(),caseData);
    }

    protected CaseDetails updateCitizenCase(String eventId, Long caseId, CaseData caseData) {
        final String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        final String userId = idamService.retrieveUser(citizenToken).getUserDetails().getId();
        AppsConfig.AppsDetails details = AppsUtil.getExactAppsDetails(appsConfig, caseData.getDssCaseData());

        final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
            citizenToken,
            serviceAuthenticationGenerator.generate(),
            userId,
            details.getJurisdiction(),
            details.getCaseType(),
            String.valueOf(caseId),
            eventId
        );
        final String eventToken = startEventResponse.getToken();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(convertDssCaseDataToRequest(caseData.getDssCaseData()))
            .event(Event.builder().id(eventId).build())
            .eventToken(eventToken)
            .build();

        return coreCaseDataApi.submitEventForCitizen(
            citizenToken,
            serviceAuthenticationGenerator.generate(),
            userId,
            details.getJurisdiction(),
            details.getCaseType(),
            String.valueOf(caseId),
            true,
            caseDataContent
        );
    }

    protected CaseData getCaseDataWithDssData() {
        return CaseData.builder()
            .dssCaseData(getDssCaseData())
            .build();
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

    private CaseDetails createCitizenCase() {
        final String citizenToken = idamTokenGenerator.generateIdamTokenForCitizen();
        final String userId = idamService.retrieveUser(citizenToken).getUserDetails().getId();
        final AppsConfig.AppsDetails appsDetails = AppsUtil.getExactAppsDetails(this.appsConfig, getDssCaseData());
        final StartEventResponse createEventResponse = coreCaseDataApi.startForCitizen(
            citizenToken,
            serviceAuthenticationGenerator.generate(),
            userId,
            appsDetails.getJurisdiction(),
            appsDetails.getCaseType(),
            appsDetails.getEventIds().getCreateEvent()
        );

        final String createEventResponseToken = createEventResponse.getToken();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(convertDssCaseDataToRequest(getDssCaseData()))
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(appsDetails.getEventIds().getCreateEvent()).build())
            .eventToken(createEventResponseToken)
            .build();
        return coreCaseDataApi.submitForCitizen(
            citizenToken,
            serviceAuthenticationGenerator.generate(),
            userId,
            appsDetails.getJurisdiction(),
            appsDetails.getCaseType(),
            true,
            caseDataContent
        );
    }

    protected void checkAndUpdateDraftOrderDocument(Map<String, Object> caseData) {
        UploadResponse uploadResponse = uploadTestDocumentIfMissing("5d76ff31-8547-4702-b2c8-34c43a53d220", DRAFT_ORDER_FILE);

        if (uploadResponse != null) {
            log.info("Document uploaded: {}", uploadResponse.getDocuments().getFirst());
            updateOrderTemplate(uploadResponse.getDocuments().getFirst(), caseData);
        }
    }

    protected UploadResponse uploadTestDocumentIfMissing(String documentId, ClassPathResource resource) {
        if (!checkDocumentExists(documentId)) {
            return uploadTestDocument(resource);
        }
        return null;
    }

    private boolean checkDocumentExists(String documentId) {
        final String serviceToken = serviceAuthenticationGenerator.generate();
        final String userToken = idamTokenGenerator.generateIdamTokenForSystemUser();

        try {
            ResponseEntity<Document> documentResponse =
                caseDocumentClientApi.getDocument(userToken, serviceToken, UUID.fromString(documentId));
            return documentResponse.getStatusCode().is2xxSuccessful();
        } catch (FeignException.NotFound exception) {
            log.info("Document {} not found", documentId);
            return false;
        } catch (FeignException feignException) {
            log.info("Feign exception {}", feignException.getMessage());
            return false;
        }
    }

    private UploadResponse uploadTestDocument(ClassPathResource resource) {
        log.debug("Uploading FT test document");
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

    @SuppressWarnings("unchecked")
    private void updateOrderTemplate(Document document, Map<String, Object> caseData) {
        Map<String, Object> orderTemplateIssued = (Map<String, Object>) caseData.get("cicCaseOrderTemplateIssued");
        if (orderTemplateIssued != null) {
            orderTemplateIssued.put("document_url", document.links.self.href);
            orderTemplateIssued.put("document_filename", document.originalDocumentName);
            orderTemplateIssued.put("document_binary_url", document.links.binary.href);
        }
        caseData.put("cicCaseOrderTemplateIssued", orderTemplateIssued);

        List<Map<String, Object>> draftOrderList = (List<Map<String, Object>>) caseData.get("cicCaseDraftOrderCICList");
        if (draftOrderList != null && !draftOrderList.isEmpty()) {
            Map<String, Object> firstItem = draftOrderList.getFirst();
            Map<String, Object> value = (Map<String, Object>) firstItem.get("value");
            if (value != null) {
                Map<String, Object> templateGeneratedDocument = (Map<String, Object>) value.get("templateGeneratedDocument");
                if (templateGeneratedDocument != null) {
                    templateGeneratedDocument.put("document_url", document.links.self.href);
                    templateGeneratedDocument.put("document_filename", document.originalDocumentName);
                    templateGeneratedDocument.put("document_binary_url", document.links.binary.href);
                }
                value.put("templateGeneratedDocument", templateGeneratedDocument);
            }
        }
        caseData.put("cicCaseDraftOrderCICList", draftOrderList);
    }
}
