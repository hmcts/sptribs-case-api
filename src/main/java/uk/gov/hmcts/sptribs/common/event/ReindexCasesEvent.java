package uk.gov.hmcts.sptribs.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.ccd.sdk.CaseReindexingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SUPERUSER_REINDEX_CASES;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@RequiredArgsConstructor
public class ReindexCasesEvent implements CCDConfig<CaseData, State, UserRole> {

    private final CaseReindexingService reindexQueueService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(SUPERUSER_REINDEX_CASES)
            .forAllStates()
            .name("Reindex cases")
            .description("Enqueue cases for Elasticsearch indexing")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER));

        pageBuilder.page("reindexCasesCriteria", this::midEvent)
            .pageLabel("Reindex cases")
            .label(
                "reindexCasesCriteriaLabel",
                "Select a date; cases modified since this date will be enqueued for indexing."
            )
            .mandatory(CaseData::getReindexCasesModifiedSince)
            .done();

        pageBuilder.page("reindexCasesConfirm")
            .pageLabel("Confirm reindex")
            .label("reindexCasesConfirmLabel", "Review the count before submitting.")
            .readonly(CaseData::getReindexCasesModifiedSince)
            .readonly(CaseData::getReindexCasesMatchingCount)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> beforeDetails
    ) {
        CaseData caseData = details.getData();
        LocalDate since = caseData.getReindexCasesModifiedSince();

        List<String> errors = new ArrayList<>();
        if (since == null) {
            errors.add("Enter a date.");
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(errors)
                .build();
        }

        long matching = reindexQueueService.countCasesModifiedSince(since);
        caseData.setReindexCasesMatchingCount(matching);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> beforeDetails
    ) {
        CaseData caseData = details.getData();
        LocalDate since = caseData.getReindexCasesModifiedSince();

        List<String> errors = new ArrayList<>();
        if (since == null) {
            errors.add("Enter a date.");
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(errors)
                .build();
        }

        reindexQueueService.enqueueCasesModifiedSince(since);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                              CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = details.getData();
        LocalDate since = caseData.getReindexCasesModifiedSince();
        if (since == null) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader("# Reindex submitted")
                .build();
        }

        long matching = reindexQueueService.countCasesModifiedSince(since);
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Reindex queued%n## %d cases modified since %s", matching, since))
            .build();
    }
}
