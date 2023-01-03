package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class IssueFinalDecisionSelectRecipients implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "caseIssueFinalDecisionFinalDecisionNotice = \"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("issueFinalDecisionSelectRecipients", this::midEvent)
            .pageLabel("Select recipients")
            .label("LabelRecipients","Who should receive this decision notice?")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getRepresentativeCIC, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getCaseIssueFinalDecision)
            .optional(CaseIssueFinalDecision::getRecipientSubjectCIC, "")
            .optional(CaseIssueFinalDecision::getRecipientRepresentativeCIC,"cicCaseRepresentativeCICCONTAINS \"RepresentativeCIC\"")
            .optional(CaseIssueFinalDecision::getRecipientRespondentCIC,"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (null != data.getCaseIssueFinalDecision()
            && CollectionUtils.isEmpty(data.getCaseIssueFinalDecision().getRecipientSubjectCIC())
            && CollectionUtils.isEmpty(data.getCaseIssueFinalDecision().getRecipientRepresentativeCIC())
            && CollectionUtils.isEmpty(data.getCaseIssueFinalDecision().getRecipientRespondentCIC())) {
            errors.add("One field must be selected.");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
