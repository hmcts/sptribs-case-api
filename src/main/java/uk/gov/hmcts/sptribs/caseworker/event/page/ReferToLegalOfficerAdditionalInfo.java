package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.ReferToLegalOfficer;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ReferToLegalOfficerAdditionalInfo implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("referToLegalOfficerAdditionalInfo")
            .pageLabel("Additional information")
            .complex(CaseData::getReferToLegalOfficer)
            .optional(ReferToLegalOfficer::getAdditionalInformation)
            .done();
    }
}
