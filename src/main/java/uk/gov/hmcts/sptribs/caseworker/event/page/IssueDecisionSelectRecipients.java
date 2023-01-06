package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class IssueDecisionSelectRecipients implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("issueDecisionSelectRecipients", this::midEvent)
            .pageLabel("Select recipients")
            .label("LabelRecipients","Who should receive this decision notice?")
            .complex(CaseData::getCaseIssueDecision)
            .optional(CaseIssueDecision::getRecipientSubject,"")
            .optional(CaseIssueDecision::getRecipientRepresentative,"")
            .optional(CaseIssueDecision::getRecipientRespondent,"")
            .optional(CaseIssueDecision::getRecipientOther,"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();


        if (CollectionUtils.isEmpty(data.getCaseIssueDecision().getRecipientSubject())
            && CollectionUtils.isEmpty(data.getCaseIssueDecision().getRecipientRepresentative())
            && CollectionUtils.isEmpty(data.getCaseIssueDecision().getRecipientRespondent())
            && CollectionUtils.isEmpty(data.getCaseIssueDecision().getRecipientOther())) {
            errors.add("One field must be selected.");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
