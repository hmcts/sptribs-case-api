package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkNullSubjectRepresentativeRespondent;

public class IssueFinalDecisionSelectRecipients implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "caseIssueFinalDecisionFinalDecisionNotice = \"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        SelectRecipientsHelper.addTo(pageBuilder,
            "issueFinalDecisionSelectRecipients",
            this::midEvent,
            "IssueFinalDecision",
            "Who should receive this decision notice?",
            "Final Decision information",
            ALWAYS_HIDE
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (checkNullSubjectRepresentativeRespondent(data)) {
            errors.add("One recipient must be selected.");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
