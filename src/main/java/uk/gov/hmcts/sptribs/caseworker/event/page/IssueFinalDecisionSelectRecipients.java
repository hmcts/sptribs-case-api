package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.FinalDecisionRecipientRepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
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
            && CollectionUtils.isEmpty(data.getCicCase().getRecipientSubjectCIC())
            && CollectionUtils.isEmpty(data.getCicCase().getRecipientRepresentativeCIC())
            && CollectionUtils.isEmpty(data.getCicCase().getRecipientRespondentCIC())) {
            errors.add("One field must be selected.");
        }
        if (null != data.getCicCase()
            && null != data.getCicCase().getRecipientRepresentativeCIC()
            && data.getCicCase().getRecipientRepresentativeCIC().contains(FinalDecisionRecipientRepresentativeCIC.REPRESENTATIVE)
            && (null == data.getCicCase().getRepresentativeCIC() || !data.getCicCase().getRepresentativeCIC().contains(RepresentativeCIC.REPRESENTATIVE))
        ) {
            errors.add("A representative has not been set to receive information about the case.");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
