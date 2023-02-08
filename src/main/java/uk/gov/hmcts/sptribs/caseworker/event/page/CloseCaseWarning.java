package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class CloseCaseWarning implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("closeCaseWarning")
            .pageLabel("Are you sure you want to close this case?")
            .label("closeCaseWarningCheck", "Check that it meets all requirements before closing it")
            .done();
    }
}
