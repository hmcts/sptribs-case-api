package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class FlagAdditionalInfo implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseWorkerFlagAdditionalInfo")
            .pageLabel("Add comments for this flag (Optional)")
            .label("LabelCaseWorkerFlagAdditionalInfo", "")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getFlagAdditionalDetail)
            .done();
    }
}
