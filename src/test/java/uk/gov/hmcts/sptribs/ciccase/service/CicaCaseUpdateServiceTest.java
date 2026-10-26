package uk.gov.hmcts.sptribs.ciccase.service;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.controllers.model.DssCaseDataRequest;
import uk.gov.hmcts.sptribs.exception.UnauthorisedCaseAccessException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_STRING;

@ExtendWith(MockitoExtension.class)
class CicaCaseUpdateServiceTest {

    private static final String CITIZEN_TOKEN = "Bearer citizen-token";
    private static final String SERVICE_TOKEN = "Bearer service-token";

    @Mock
    private CicaCaseService cicaCaseService;

    @Mock
    private CicaRoleService cicaRoleService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CicaCaseUpdateService updateService;

    @Test
    void shouldSubmitUpdateWithTheCitizensTokenWithoutAssigningRoleWhenStartSucceeds() {
        DssCaseDataRequest update = DssCaseDataRequest.builder().dssCaseDataAdditionalInformation("Update").build();
        CaseResource updatedCase = new CaseResource();
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi.startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenReturn(startEvent("first-token"));
        when(coreCaseDataApi.createEvent(eq(CITIZEN_TOKEN), eq(SERVICE_TOKEN), eq(TEST_CASE_ID_STRING),
            any(CaseDataContent.class))).thenReturn(updatedCase);

        assertThat(updateService.updateCase(TEST_CASE_ID_STRING, CITIZEN_TOKEN, update)).isSameAs(updatedCase);

        var calls = inOrder(cicaCaseService, coreCaseDataApi);
        calls.verify(cicaCaseService).checkIfUserHasAccess(TEST_CASE_ID_STRING, CITIZEN_TOKEN);
        calls.verify(coreCaseDataApi).startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        ArgumentCaptor<CaseDataContent> content = ArgumentCaptor.forClass(CaseDataContent.class);
        calls.verify(coreCaseDataApi).createEvent(eq(CITIZEN_TOKEN), eq(SERVICE_TOKEN), eq(TEST_CASE_ID_STRING),
            content.capture());
        assertThat(content.getValue().getEventToken()).isEqualTo("first-token");
        assertThat(content.getValue().getEvent().getId()).isEqualTo(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        assertThat(content.getValue().getData()).isSameAs(update);
        verifyNoInteractions(cicaRoleService);
    }

    @Test
    void shouldAssignRoleAndRetryStartOnceWhenCcdHidesCaseFromCitizen() {
        DssCaseDataRequest update = new DssCaseDataRequest();
        FeignException missingRole = ccdFailure(404, "No case found for reference");
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi.startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenThrow(missingRole).thenReturn(startEvent("retry-token"));

        updateService.updateCase(TEST_CASE_ID_STRING, CITIZEN_TOKEN, update);

        var calls = inOrder(cicaCaseService, coreCaseDataApi, cicaRoleService);
        calls.verify(cicaCaseService).checkIfUserHasAccess(TEST_CASE_ID_STRING, CITIZEN_TOKEN);
        calls.verify(coreCaseDataApi).startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        calls.verify(cicaRoleService).assignCreatorRole(TEST_CASE_ID_STRING, CITIZEN_TOKEN);
        calls.verify(coreCaseDataApi).startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        ArgumentCaptor<CaseDataContent> content = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataApi).createEvent(eq(CITIZEN_TOKEN), eq(SERVICE_TOKEN), eq(TEST_CASE_ID_STRING),
            content.capture());
        assertThat(content.getValue().getEventToken()).isEqualTo("retry-token");
        assertThat(content.getValue().getData()).isSameAs(update);
    }

    @Test
    void shouldReturnSecondFailureWithoutAnotherRoleAssignment() {
        FeignException missingRole = ccdFailure(404, "No case found for reference");
        FeignException retryFailure = ccdFailure(404, "No case found for reference");
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi.startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenThrow(missingRole, retryFailure);

        assertThatThrownBy(() -> updateService.updateCase(TEST_CASE_ID_STRING, CITIZEN_TOKEN, new DssCaseDataRequest()))
            .isSameAs(retryFailure);

        verify(cicaRoleService).assignCreatorRole(TEST_CASE_ID_STRING, CITIZEN_TOKEN);
        verify(coreCaseDataApi, times(2)).startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        verify(coreCaseDataApi, never()).createEvent(any(), any(), any(), any());
    }

    @Test
    void shouldNotRetryIfRoleAssignmentFails() {
        FeignException missingRole = ccdFailure(404, "No case found for reference");
        FeignException assignmentFailure = ccdFailure(503, "Assignment unavailable");
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi.startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenThrow(missingRole);
        doThrow(assignmentFailure).when(cicaRoleService).assignCreatorRole(TEST_CASE_ID_STRING, CITIZEN_TOKEN);

        assertThatThrownBy(() -> updateService.updateCase(TEST_CASE_ID_STRING, CITIZEN_TOKEN, new DssCaseDataRequest()))
            .isSameAs(assignmentFailure);

        verify(coreCaseDataApi).startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        verify(coreCaseDataApi, never()).createEvent(any(), any(), any(), any());
    }

    @Test
    void shouldNotAssignRoleForUnrelatedCcdFailure() {
        FeignException forbidden = ccdFailure(403, "Event is not allowed in this state");
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi.startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenThrow(forbidden);

        assertThatThrownBy(() -> updateService.updateCase(TEST_CASE_ID_STRING, CITIZEN_TOKEN, new DssCaseDataRequest()))
            .isSameAs(forbidden);

        verifyNoInteractions(cicaRoleService);
    }

    @Test
    void shouldNotAssignRoleForUnrelatedCcdNotFoundFailure() {
        FeignException notFound = ccdFailure(404, "Event not found");
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi.startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenThrow(notFound);

        assertThatThrownBy(() -> updateService.updateCase(TEST_CASE_ID_STRING, CITIZEN_TOKEN, new DssCaseDataRequest()))
            .isSameAs(notFound);

        verifyNoInteractions(cicaRoleService);
    }

    @Test
    void shouldNotAssignRoleOrRetryIfSubmittingTheUpdateFails() {
        FeignException forbidden = ccdFailure(403, "Update rejected");
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi.startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION)).thenReturn(startEvent("first-token"));
        when(coreCaseDataApi.createEvent(eq(CITIZEN_TOKEN), eq(SERVICE_TOKEN), eq(TEST_CASE_ID_STRING),
            any(CaseDataContent.class))).thenThrow(forbidden);

        assertThatThrownBy(() -> updateService.updateCase(TEST_CASE_ID_STRING, CITIZEN_TOKEN, new DssCaseDataRequest()))
            .isSameAs(forbidden);

        verifyNoInteractions(cicaRoleService);
        verify(coreCaseDataApi).startEvent(CITIZEN_TOKEN, SERVICE_TOKEN, TEST_CASE_ID_STRING,
            CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
    }

    @Test
    void shouldNotCallCcdWhenCitizenDoesNotBelongToCase() {
        var denied = new UnauthorisedCaseAccessException("Not associated with case");
        doThrow(denied).when(cicaCaseService)
            .checkIfUserHasAccess(TEST_CASE_ID_STRING, CITIZEN_TOKEN);

        assertThatThrownBy(() -> updateService.updateCase(TEST_CASE_ID_STRING, CITIZEN_TOKEN, new DssCaseDataRequest()))
            .isSameAs(denied);

        verifyNoInteractions(authTokenGenerator, coreCaseDataApi, cicaRoleService);
    }

    private StartEventResponse startEvent(String token) {
        return StartEventResponse.builder().token(token).build();
    }

    private FeignException ccdFailure(int status, String message) {
        Request request = Request.create(Request.HttpMethod.GET, "http://ccd/cases/event-triggers",
            Map.of(), null, StandardCharsets.UTF_8);
        Response response = Response.builder().status(status).request(request)
            .body(message, StandardCharsets.UTF_8).build();
        return FeignException.errorStatus("startEvent", response);
    }
}
