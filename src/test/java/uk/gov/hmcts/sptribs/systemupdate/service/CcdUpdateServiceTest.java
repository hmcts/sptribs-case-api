package uk.gov.hmcts.sptribs.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;
import uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task.MigrateRetiredFields;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService.SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService.SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;


@ExtendWith(MockitoExtension.class)
class CcdUpdateServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @Mock
    private CaseDataContent caseDataContent;

    @InjectMocks
    private CcdUpdateService ccdUpdateService;

    public static final String SYSTEM_REMOVE_FAILED_CASES = "system-remove-failed-cases";

    public static final String SYSTEM_PROGRESS_HELD_CASE = "system-progress-held-case";

    @Test
    void shouldSubmitActionEvent() {

        final User user = systemUpdateUser();
        final Map<String, Object> caseData = new HashMap<>();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
                new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(CaseData.builder().build());

        final StartEventResponse startEventResponse = getStartEventResponse();

        when(ccdCaseDataContentProvider
                .createCaseDataContent(
                        startEventResponse,
                        SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY,
                        SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION))
                .thenReturn(caseDataContent);
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(),  anyString(),
                anyString(), any(), any())).thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
                .createCaseDataContent(
                        startEventResponse,
                        SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY,
                        SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION))
                .thenReturn(caseDataContent);

        ccdUpdateService.submitEvent(TEST_CASE_ID, SYSTEM_REMOVE_FAILED_CASES, user, SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi).submitEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION,
                SYSTEM_USER_USER_ID,
                ST_CIC_JURISDICTION,
                ST_CIC_CASE_TYPE,
                TEST_CASE_ID.toString(),
                true,
                caseDataContent);

        verifyNoMoreInteractions(coreCaseDataApi);

    }

    @Test
    void shouldThrowCcdManagementExceptionWhenSubmitEvent() {
        final String message = "Submit Event Failed";
        final User user = systemUpdateUser();

        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(),  anyString(),
            anyString(), any(), any())).thenThrow(
                new CcdManagementException(message,new Throwable()));

        final CcdManagementException ccdManagementException =
            assertThrows(CcdManagementException.class, () ->
                ccdUpdateService.submitEvent(TEST_CASE_ID, SYSTEM_REMOVE_FAILED_CASES, user, SERVICE_AUTHORIZATION));

        assertTrue(ccdManagementException.getMessage().contains(message));
        assertNotNull(ccdManagementException.getCause());
    }

    @Test
    void shouldThrowCcdConflictExceptionWhenSubmitEvent() {
        final String message = "Submit Event Failed With a Conflict";
        final User user = systemUpdateUser();

        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(),  anyString(),
            anyString(), any(), any())).thenThrow(
                new CcdConflictException(message,new Throwable()));

        final CcdConflictException ccdConflictException =
            assertThrows(CcdConflictException.class, () ->
                ccdUpdateService.submitEvent(TEST_CASE_ID, SYSTEM_REMOVE_FAILED_CASES, user, SERVICE_AUTHORIZATION));

        assertTrue(ccdConflictException.getMessage().contains(message));
        assertNotNull(ccdConflictException.getCause());
    }

    @Test
    void shouldSubmitEventWithRetry() {

        final User user = systemUpdateUser();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(CaseData.builder().build());

        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseTask caseTask = new MigrateRetiredFields();

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY,
                SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseTask))
            .thenReturn(caseDataContent);
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(),  anyString(),
            anyString(), any(), any())).thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY,
                SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseTask))
            .thenReturn(caseDataContent);

        ccdUpdateService.submitEventWithRetry(
            TEST_CASE_ID.toString(),
            SYSTEM_REMOVE_FAILED_CASES,
            caseTask,
            user, SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi).submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            SYSTEM_USER_USER_ID,
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent);

        verifyNoMoreInteractions(coreCaseDataApi);
    }

    private StartEventResponse getStartEventResponse() {
        return StartEventResponse.builder()
                .eventId(SYSTEM_PROGRESS_HELD_CASE)
                .token("startEventToken")
                .caseDetails(getCaseDetails(new HashMap<>()))
                .build();
    }


    private CaseDetails getCaseDetails(final Map<String, Object> caseData) {
        return CaseDetails.builder()
                .id(TEST_CASE_ID)
                .data(caseData)
                .build();
    }

    private User systemUpdateUser() {
        return new User(
                SYSTEM_UPDATE_AUTH_TOKEN,
                UserDetails.builder()
                        .id(SYSTEM_USER_USER_ID)
                        .build());
    }
}
