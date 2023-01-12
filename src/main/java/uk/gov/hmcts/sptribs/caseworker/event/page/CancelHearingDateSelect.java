package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CancelHearingDateSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerCancelHearingSelectHearingDate")
            .pageLabel("Select hearing")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getHearingList,"Choose a hearing to cancel")
            .done();
    }
}
