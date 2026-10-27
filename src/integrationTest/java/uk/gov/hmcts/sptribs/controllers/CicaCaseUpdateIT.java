package uk.gov.hmcts.sptribs.controllers;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.ciccase.service.CicaCaseService;
import uk.gov.hmcts.sptribs.ciccase.service.CicaRoleService;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.controllers.model.DssCaseDataRequest;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
class CicaCaseUpdateIT {

    private static final String CASE_ID = "1234567891234567";
    private static final String URL = "/cases/cica/" + CASE_ID + "/updates";
    private static final String CCD_TOKEN = "ccd-service-token";
    private static final String UPDATE = """
        {
          "event": {"id": "citizen-cic-dss-update-case"},
          "data": {
            "dssCaseDataCaseTypeOfApplication": "CIC",
            "dssCaseDataAdditionalInformation": "Updated details",
            "dssCaseDataOtherInfoDocuments": [
              {
                "id": "document-id",
                "value": {
                  "documentLink": {
                    "document_url": "http://example.test/documents/document-id",
                    "document_binary_url": "http://example.test/documents/document-id/binary",
                    "document_filename": "test.pdf"
                  },
                  "comment": "Supporting evidence"
                }
              }
            ]
          }
        }
        """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private CicaCaseService cicaCaseService;

    @MockitoBean
    private CicaRoleService cicaRoleService;

    @MockitoBean
    private CoreCaseDataApi coreCaseDataApi;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldSubmitUpdateOnceWithCitizensTokenAndEventData() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(CCD_TOKEN);
        when(coreCaseDataApi.startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenReturn(startEvent("event-token"));
        when(coreCaseDataApi.createEvent(eq(TEST_AUTHORIZATION_TOKEN), eq(CCD_TOKEN), eq(CASE_ID),
            any(CaseDataContent.class))).thenReturn(updatedCase());

        updateRequest().andExpect(status().isCreated())
            .andExpect(jsonPath("$.case_id").value(CASE_ID))
            .andExpect(jsonPath("$.state").value("CaseManagement"));

        var calls = inOrder(cicaCaseService, coreCaseDataApi);
        calls.verify(cicaCaseService).checkIfUserHasAccess(CASE_ID, TEST_AUTHORIZATION_TOKEN);
        calls.verify(coreCaseDataApi).startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        ArgumentCaptor<CaseDataContent> submitted = ArgumentCaptor.forClass(CaseDataContent.class);
        calls.verify(coreCaseDataApi).createEvent(eq(TEST_AUTHORIZATION_TOKEN), eq(CCD_TOKEN), eq(CASE_ID),
            submitted.capture());
        assertThat(submitted.getValue().getEventToken()).isEqualTo("event-token");
        assertThat(submitted.getValue().getEvent().getId()).isEqualTo(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        assertThat(submitted.getValue().getData()).isInstanceOf(DssCaseDataRequest.class);
        DssCaseDataRequest submittedData = (DssCaseDataRequest) submitted.getValue().getData();
        assertThat(submittedData.getDssCaseDataCaseTypeOfApplication()).isEqualTo("CIC");
        assertThat(submittedData.getDssCaseDataAdditionalInformation()).isEqualTo("Updated details");
        assertThat(submittedData.getDssCaseDataOtherInfoDocuments()).hasSize(1);
        assertThat(submittedData.getDssCaseDataOtherInfoDocuments().getFirst().getValue().getComment())
            .isEqualTo("Supporting evidence");
        assertThat(submittedData.getDssCaseDataOtherInfoDocuments().getFirst().getValue().getDocumentLink().getFilename())
            .isEqualTo("test.pdf");
        verifyNoInteractions(cicaRoleService);
    }

    @Test
    void shouldRejectUpdateWithoutCaseData() throws Exception {
        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"event\":{\"id\":\"citizen-cic-dss-update-case\"}}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(cicaCaseService, cicaRoleService, coreCaseDataApi);
    }

    @Test
    void shouldAssignRoleAndRetryOnlyEventStartWhenCcdHidesCase() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(CCD_TOKEN);
        when(coreCaseDataApi.startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION))
            .thenThrow(ccdFailure(404, "No case found for reference"))
            .thenReturn(startEvent("retry-token"));
        when(coreCaseDataApi.createEvent(eq(TEST_AUTHORIZATION_TOKEN), eq(CCD_TOKEN), eq(CASE_ID),
            any(CaseDataContent.class))).thenReturn(updatedCase());

        updateRequest().andExpect(status().isCreated()).andExpect(jsonPath("$.case_id").value(CASE_ID));

        var calls = inOrder(cicaCaseService, cicaRoleService, coreCaseDataApi);
        calls.verify(cicaCaseService).checkIfUserHasAccess(CASE_ID, TEST_AUTHORIZATION_TOKEN);
        calls.verify(coreCaseDataApi).startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        calls.verify(cicaRoleService).ensureCreatorRole(CASE_ID, TEST_AUTHORIZATION_TOKEN);
        calls.verify(coreCaseDataApi).startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        ArgumentCaptor<CaseDataContent> submitted = ArgumentCaptor.forClass(CaseDataContent.class);
        calls.verify(coreCaseDataApi).createEvent(eq(TEST_AUTHORIZATION_TOKEN), eq(CCD_TOKEN), eq(CASE_ID),
            submitted.capture());
        assertThat(submitted.getValue().getEventToken()).isEqualTo("retry-token");
    }

    @Test
    void shouldNotContactCcdOrAssignRoleWhenMembershipCheckFails() throws Exception {
        doThrow(new UnauthorisedCaseAccessException("Not associated with case"))
            .when(cicaCaseService).checkIfUserHasAccess(CASE_ID, TEST_AUTHORIZATION_TOKEN);

        updateRequest().andExpect(status().isForbidden());

        verifyNoInteractions(authTokenGenerator, cicaRoleService, coreCaseDataApi);
    }

    @Test
    void shouldNotAssignRoleWhenCcdRejectsEventForOtherReason() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(CCD_TOKEN);
        when(coreCaseDataApi.startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenThrow(ccdFailure(404, "Event not found: private details"));

        updateRequest().andExpect(status().isNotFound()).andExpect(content().string(""));

        verifyNoInteractions(cicaRoleService);
        verify(coreCaseDataApi, never()).createEvent(any(), any(), any(), any());
    }

    @Test
    void shouldReturnRetryFailureWithoutSubmittingOrLeakingCcdResponse() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(CCD_TOKEN);
        when(coreCaseDataApi.startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION))
            .thenThrow(ccdFailure(404, "No case found for reference"), ccdFailure(409, "private case data"));

        updateRequest().andExpect(status().isConflict()).andExpect(content().string(""));

        verify(cicaRoleService).ensureCreatorRole(CASE_ID, TEST_AUTHORIZATION_TOKEN);
        verify(coreCaseDataApi, never()).createEvent(any(), any(), any(), any());
    }

    @Test
    void shouldNotRetryEventStartWhenRoleAssignmentFails() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(CCD_TOKEN);
        when(coreCaseDataApi.startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenThrow(ccdFailure(404, "No case found for reference"));
        doThrow(ccdFailure(503, "private case data"))
            .when(cicaRoleService).ensureCreatorRole(CASE_ID, TEST_AUTHORIZATION_TOKEN);

        updateRequest().andExpect(status().isServiceUnavailable()).andExpect(content().string(""));

        verify(coreCaseDataApi).startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        verify(coreCaseDataApi, never()).createEvent(any(), any(), any(), any());
    }

    @Test
    void shouldNotRetrySubmissionWhenCcdRejectsUpdate() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(CCD_TOKEN);
        when(coreCaseDataApi.startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenReturn(startEvent("event-token"));
        when(coreCaseDataApi.createEvent(eq(TEST_AUTHORIZATION_TOKEN), eq(CCD_TOKEN), eq(CASE_ID),
            any(CaseDataContent.class))).thenThrow(ccdFailure(409, "private case data"));

        updateRequest().andExpect(status().isConflict()).andExpect(content().string(""));

        verifyNoInteractions(cicaRoleService);
        verify(coreCaseDataApi).startEvent(TEST_AUTHORIZATION_TOKEN, CCD_TOKEN, CASE_ID,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        verify(coreCaseDataApi).createEvent(eq(TEST_AUTHORIZATION_TOKEN), eq(CCD_TOKEN), eq(CASE_ID),
            any(CaseDataContent.class));
    }

    private ResultActions updateRequest() throws Exception {
        return mockMvc.perform(post(URL)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(UPDATE));
    }

    private StartEventResponse startEvent(String token) {
        return StartEventResponse.builder().token(token).build();
    }

    private CaseResource updatedCase() {
        CaseResource result = new CaseResource();
        result.setReference(CASE_ID);
        result.setState("CaseManagement");
        return result;
    }

    private FeignException ccdFailure(int status, String message) {
        Request request = Request.create(Request.HttpMethod.GET, "http://ccd/cases/event-triggers",
            Map.of(), null, StandardCharsets.UTF_8);
        Response response = Response.builder().status(status).request(request)
            .body(message, StandardCharsets.UTF_8).build();
        return FeignException.errorStatus("startEvent", response);
    }
}
