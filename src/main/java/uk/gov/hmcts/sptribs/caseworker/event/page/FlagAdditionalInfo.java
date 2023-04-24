package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class FlagAdditionalInfo implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("flagAdditionalInfo")
            .label("flagAdditionalInfo", "<h2>Add comments for this flag (Optional)\n</h2>")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getFlagAdditionalDetail)
            .done();
    }
}
