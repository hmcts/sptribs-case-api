package uk.gov.hmcts.sptribs.systemupdate.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

import java.time.LocalDate;
import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService.SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION;
import static uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService.SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CcdUpdateServiceIT {
    @Autowired
    private CcdUpdateService ccdUpdateService;

    @MockitoBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockitoBean
    private CaseDetailsUpdater caseDetailsUpdater;

    @Test
    void shouldSubmitEvent() {
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().id("1").build());
        final String eventId = "system-remove-failed-cases";

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(eventId)
            .token("startEventToken")
            .caseDetails(CaseDetails.builder()
                .id(TEST_CASE_ID)
                .data(new HashMap<>())
                .build())
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build())
            .data(startEventResponse.getCaseDetails().getData())
            .build();

        when(coreCaseDataApi.startEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            "1",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            eventId
        )).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            "1",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent
        )).thenReturn(CaseDetails.builder().build());

        ccdUpdateService.submitEvent(TEST_CASE_ID, eventId, user, SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi).startEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            "1",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            eventId
        );

        verify(coreCaseDataApi).submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            "1",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent);

        verifyNoMoreInteractions(coreCaseDataApi);
    }

    @Test
    void shouldSubmitEventWithRetry() {
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().id("1").build());
        final String eventId = "system-remove-failed-cases";

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(eventId)
            .token("startEventToken")
            .caseDetails(CaseDetails.builder()
                .id(TEST_CASE_ID)
                .data(new HashMap<>())
                .build())
            .build();

        final CaseTask caseTask = caseDetails -> {
            caseDetails.getData().setDueDate(LocalDate.of(2023, 1, 2));
            return caseDetails;
        };

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> details = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(SPTRIBS_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(SPTRIBS_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build())
            .build();

        when(coreCaseDataApi.startEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            "1",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            eventId
        )).thenReturn(startEventResponse);

        when(caseDetailsUpdater.updateCaseData(caseTask, startEventResponse))
            .thenReturn(details);

        when(coreCaseDataApi.submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            "1",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent
        )).thenReturn(CaseDetails.builder().build());

        ccdUpdateService.submitEventWithRetry(TEST_CASE_ID.toString(), eventId, caseTask, user, SERVICE_AUTHORIZATION);

        verify(coreCaseDataApi).startEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            "1",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            eventId
        );

        verify(coreCaseDataApi).submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            "1",
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent);
    }
}
