package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class ReinstateWarning implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("reinstateWarning")
            .label("reinstateWarning", "<h1>Are you sure you want to reinstate this case?</h1>")
            .readonlyWithLabel(CaseData::getClosedDayCount, " ")
            .label("reinstateWarningCheck", "Check that it meets all requirements before reinstating")
            .done();
    }
}
