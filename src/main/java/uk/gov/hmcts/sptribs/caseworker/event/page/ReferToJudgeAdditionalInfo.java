package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToJudge;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Component
public class ReferToJudgeAdditionalInfo implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("referToJudgeAdditionalInfo")
            .pageLabel("Additional information")
            .complex(CaseData::getReferToJudge)
            .optional(ReferToJudge::getAdditionalInformation)
            .done();
    }
}
