package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class RemoveStatNextState implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("nextStateAfterStay")
            .label("nextStateAfterStay", "<h3>Update case status <br/> What status should the case now be moved to? \n</h3>")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getAfterStayState, "")
            .done();
    }
}
