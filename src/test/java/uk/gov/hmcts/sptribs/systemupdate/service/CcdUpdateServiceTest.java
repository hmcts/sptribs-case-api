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
import uk.gov.hmcts.sptribs.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task.MigrateRetiredFields;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseDetailsUpdater caseDetailsUpdater;

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
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);


        when(ccdCaseDataContentProvider
                .createCaseDataContent(
                        startEventResponse,
                        SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY,
                        SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION,
                        caseData))
                .thenReturn(caseDataContent);
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(),  anyString(),
                anyString(), any(), any())).thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
                .createCaseDataContent(
                        startEventResponse,
                        SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY,
                        SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION,
                        caseData))
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
    }

    @Test
    void shouldSubmitEventWithRetry() {

        final User user = systemUpdateUser();
        final Map<String, Object> caseData = new HashMap<>();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(CaseData.builder().build());

        final StartEventResponse startEventResponse = getStartEventResponse();
        final CaseDataContent caseDataContent = mock(CaseDataContent.class);


        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY,
                SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseDetails.getData()))
            .thenReturn(caseDataContent);
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(),  anyString(),
            anyString(), any(), any())).thenReturn(startEventResponse);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                startEventResponse,
                SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY,
                SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseDetails.getData()))
            .thenReturn(caseDataContent);

        when(caseDetailsUpdater
            .updateCaseData(
                any(),
                any()))
            .thenReturn(caseDetails);

        ccdUpdateService.submitEventWithRetry(
            TEST_CASE_ID.toString(),
            SYSTEM_REMOVE_FAILED_CASES,
            new MigrateRetiredFields(),
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
