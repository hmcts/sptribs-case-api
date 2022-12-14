package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class CloseCaseChooseType implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("closeCasePage")
            .label("closeCasePage", "<H2>Close Case Reason</H2>")
            .complex(CaseData::getCloseCase)
            .done();
    }
}
