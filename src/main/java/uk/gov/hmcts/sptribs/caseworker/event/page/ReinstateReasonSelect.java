package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class ReinstateReasonSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("reinstateReason")
            .pageLabel("Reason for reinstatement")
            .label("LabelReinstateCaseReason", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getReinstateReason)
            .optional(CicCase::getReinstateAdditionalDetail)
            .done();
    }
}
