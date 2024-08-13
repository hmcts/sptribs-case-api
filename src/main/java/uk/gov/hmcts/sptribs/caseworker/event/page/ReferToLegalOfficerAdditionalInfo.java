package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.ReferToLegalOfficer;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ReferToLegalOfficerAdditionalInfo implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "referToLegalOfficerAdditionalInformation=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("referToLegalOfficerAdditionalInfo")
            .pageLabel("Additional information")
            .complex(CaseData::getReferToLegalOfficer)
            .optional(ReferToLegalOfficer::getAdditionalInformation)
            .done()
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getReferralTypeForWA, NEVER_SHOW);
    }
}
