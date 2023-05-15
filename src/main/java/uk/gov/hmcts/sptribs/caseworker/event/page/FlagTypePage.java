package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseFlag;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class FlagTypePage implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectFlagType")
            .label("selectFlagType", "<h2>Select flag type\n</h2>")
            .complex(CaseData::getCaseFlag)
            .mandatory(CaseFlag::getFlagType)
            .mandatory(CaseFlag::getOtherDescription, "caseFlagFlagType = \"Other\"")
            .done();
    }
}
