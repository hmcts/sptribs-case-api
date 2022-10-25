package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseReinstate;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class ReinstateReasonSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("reinstateReason")
            .label("reinstateReason", "<h1>Reason for reinstatement</h1>")
            .complex(CaseData::getCaseReinstate)
            .mandatory(CaseReinstate::getReinstateReason)
            .optional(CaseReinstate::getAdditionalDetail)
            .done();
    }
}
