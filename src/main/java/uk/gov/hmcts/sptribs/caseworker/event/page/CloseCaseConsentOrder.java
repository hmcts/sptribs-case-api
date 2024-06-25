package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CloseCaseConsentOrder implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("closeCaseConsentOrder")
            .pageLabel("Consent order details")
            .complex(CaseData::getCloseCase)
            .mandatory(CloseCase::getConsentOrderDate)
            .done();
    }
}
