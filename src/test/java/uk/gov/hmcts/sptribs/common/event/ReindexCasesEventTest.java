package uk.gov.hmcts.sptribs.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.CaseReindexingService;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SUPERUSER_REINDEX_CASES;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class ReindexCasesEventTest {

    @Mock
    private CaseReindexingService reindexQueueService;

    @InjectMocks
    private ReindexCasesEvent reindexCasesEvent;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        reindexCasesEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SUPERUSER_REINDEX_CASES);
    }

    @Test
    void shouldReturnErrorWhenDateMissingOnMidEvent() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(new CaseData());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            reindexCasesEvent.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).containsExactly("Enter a date.");
    }

    @Test
    void shouldPopulateMatchingCountOnMidEvent() {
        final LocalDate since = LocalDate.now().minusDays(3);
        final CaseData caseData = new CaseData();
        caseData.setReindexCasesModifiedSince(since);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        when(reindexQueueService.countCasesModifiedSince(since)).thenReturn(12L);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            reindexCasesEvent.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getData().getReindexCasesMatchingCount()).isEqualTo(12L);
        verify(reindexQueueService).countCasesModifiedSince(since);
    }

    @Test
    void shouldReturnErrorWhenDateMissingOnAboutToSubmit() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(new CaseData());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            reindexCasesEvent.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).containsExactly("Enter a date.");
        verify(reindexQueueService, never()).enqueueCasesModifiedSince(any());
    }

    @Test
    void shouldEnqueueCasesOnAboutToSubmit() {
        final LocalDate since = LocalDate.of(2024, 1, 1);
        final CaseData caseData = new CaseData();
        caseData.setReindexCasesModifiedSince(since);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            reindexCasesEvent.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNullOrEmpty();
        verify(reindexQueueService).enqueueCasesModifiedSince(since);
    }

    @Test
    void shouldReturnSubmittedHeaderWhenDateMissing() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(new CaseData());

        SubmittedCallbackResponse response = reindexCasesEvent.submitted(caseDetails, caseDetails);

        assertThat(response.getConfirmationHeader()).isEqualTo("# Reindex submitted");
        verify(reindexQueueService, never()).countCasesModifiedSince(any());
    }

    @Test
    void shouldReturnSubmittedHeaderWithCount() {
        final LocalDate since = LocalDate.of(2024, 1, 2);
        final CaseData caseData = new CaseData();
        caseData.setReindexCasesModifiedSince(since);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        when(reindexQueueService.countCasesModifiedSince(since)).thenReturn(5L);

        SubmittedCallbackResponse response = reindexCasesEvent.submitted(caseDetails, caseDetails);

        assertThat(response.getConfirmationHeader())
            .isEqualTo("# Reindex queued\n## 5 cases modified since 2024-01-02");
        verify(reindexQueueService).countCasesModifiedSince(since);
    }
}
