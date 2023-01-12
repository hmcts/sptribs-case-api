package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
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
        pageBuilder
            .page("issueFinalDecisionSelectRecipients", this::midEvent)
            .pageLabel("Select recipients")
            .label("labelIssueFinalDecisionSelectRecipients", "Who should receive this decision notice?")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getRepresentativeCIC, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getCicCase)
            .readonlyWithLabel(CicCase::getFullName, " ")
            .optional(CicCase::getNotifyPartySubject, "")
            .label("issueFinalDecisionSelectRecipientsNotifyPartiesRepresentative", "")
            .readonlyWithLabel(CicCase::getRepresentativeFullName, " ")
            .optional(CicCase::getNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ")
            .label("issueFinalDecisionSelectRecipientsNotifyPartiesRespondent", "")
            .readonlyWithLabel(CicCase::getRespondantName, " ")
            .optional(CicCase::getNotifyPartyRespondent, "cicCaseRespondantName!=\"\" ")
            .done();
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
