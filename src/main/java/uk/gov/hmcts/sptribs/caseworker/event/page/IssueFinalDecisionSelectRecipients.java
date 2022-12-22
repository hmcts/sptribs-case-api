package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.*;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class IssueFinalDecisionSelectRecipients implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("issueFinalDecisionSelectRecipients", this::midEvent)
            .pageLabel("Select recipients")
            .label("LabelRecipients","Who should receive this decision notice?")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getRecipientSubjectCIC, "")
            .optional(CicCase::getRecipientRepresentativeCIC,"")
            .optional(CicCase::getRecipientRespondentCIC,"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (null != data.getCicCase()
            && (null == data.getCicCase().getRecipientSubjectCIC() || data.getCicCase().getRecipientSubjectCIC().size() == 0)
            && (null == data.getCicCase().getRecipientRepresentativeCIC() || data.getCicCase().getRecipientRepresentativeCIC().size() == 0)
            && (null == data.getCicCase().getRecipientRespondentCIC() || data.getCicCase().getRecipientRespondentCIC().size() == 0)) {
            errors.add("One field must be selected.");
        }
        if (null != data.getCicCase()
            && null != data.getCicCase().getRecipientRepresentativeCIC()
            && data.getCicCase().getRecipientRepresentativeCIC().contains(FinalDecisionRecipientRepresentativeCIC.REPRESENTATIVE)
            && (null == data.getCicCase().getPartiesCIC()|| !data.getCicCase().getPartiesCIC().contains(PartiesCIC.REPRESENTATIVE))
        ) {
            errors.add("A representative has not been specified for this case.");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
