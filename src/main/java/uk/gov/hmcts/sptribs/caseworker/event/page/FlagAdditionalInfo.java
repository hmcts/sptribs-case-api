package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseFlag;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class FlagAdditionalInfo implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectFlagLevel")
            .label("selectFlagLevel", "<h3>Add comments for this flag (Optional)\n</h3>")
            .complex(CaseData::getCaseFlag)
            .optional(CaseFlag::getAdditionalDetail)
            .done();
    }
}
