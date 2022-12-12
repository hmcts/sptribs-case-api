package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class CloseCaseReasonSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("closeCasePage")
            .label("closeCasePage", "<H2>Close this case</H2>")
            .complex(CaseData::getCloseCase)
            .mandatoryWithLabel(CloseCase::getCloseCaseReason, "")
            .optional(CloseCase::getAdditionalDetail, "")
            .done();
    }
}
