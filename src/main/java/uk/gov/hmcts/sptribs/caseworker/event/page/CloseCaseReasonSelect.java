package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class CloseCaseReasonSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("closeCasePageSelectReason")
            .pageLabel("Close this case")
            .label("LabelCloseCasePageSelectReason"," ")
            .complex(CaseData::getCloseCase)
            .mandatory(CloseCase::getCloseCaseReason)
            .optional(CloseCase::getAdditionalDetail)
            .done();
    }
}
