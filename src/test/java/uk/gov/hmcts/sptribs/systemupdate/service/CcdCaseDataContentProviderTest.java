package uk.gov.hmcts.sptribs.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@ExtendWith(MockitoExtension.class)
class CcdCaseDataContentProviderTest {

    private static final String START_EVENT_TOKEN = "startEventToken";
    private static final String SUMMARY = "Summary";
    private static final String DESCRIPTION = "Description";

    @Mock
    private CaseDetailsUpdater caseDetailsUpdater;

    @InjectMocks
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @Test
    void shouldCreateCaseDataContentWhenCaseTaskNotPassed() {
        final Map<String, Object> data = new HashMap<>();
        final StartEventResponse startEventResponse =
            StartEventResponse.builder()
                .eventId(SYSTEM_MIGRATE_CASE)
                .token(START_EVENT_TOKEN)
                .caseDetails(CaseDetails.builder().data(data).build())
                .build();

        final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
            startEventResponse,
            SUMMARY,
            DESCRIPTION);

        assertThat(caseDataContent)
            .extracting(
                CaseDataContent::getEventToken,
                CaseDataContent::getData,
                c -> c.getEvent().getId(),
                c -> c.getEvent().getSummary(),
                c -> c.getEvent().getDescription())
            .contains(START_EVENT_TOKEN, data, SYSTEM_MIGRATE_CASE, SUMMARY, DESCRIPTION);
    }

    @Test
    void shouldCreateCaseDataContentWhenCaseTaskPassed() {
        final CaseTask caseTask = mock(CaseTask.class);
        final StartEventResponse startEventResponse =
            StartEventResponse.builder()
                .eventId(SYSTEM_MIGRATE_CASE)
                .token(START_EVENT_TOKEN)
                .build();

        final CaseData data = CaseData.builder().build();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setData(data);

        when(caseDetailsUpdater.updateCaseData(caseTask, startEventResponse))
            .thenReturn(caseDetails);

        final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
            startEventResponse,
            SUMMARY,
            DESCRIPTION,
            caseTask);

        assertThat(caseDataContent)
            .extracting(
                CaseDataContent::getEventToken,
                CaseDataContent::getData,
                c -> c.getEvent().getId(),
                c -> c.getEvent().getSummary(),
                c -> c.getEvent().getDescription())
            .contains(START_EVENT_TOKEN, data, SYSTEM_MIGRATE_CASE, SUMMARY, DESCRIPTION);
    }
}
