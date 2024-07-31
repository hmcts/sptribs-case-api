package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.ReferToJudge;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ReferToJudgeAdditionalInfo implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "cicCaseReferralTypeForWA=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("referToJudgeAdditionalInfo")
            .pageLabel("Additional information")
            .complex(CaseData::getReferToJudge)
            .optional(ReferToJudge::getAdditionalInformation)
            .done()
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getReferralTypeForWA, NEVER_SHOW);
    }
}
