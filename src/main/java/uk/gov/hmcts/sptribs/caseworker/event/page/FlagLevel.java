package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseFlag;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class FlagLevel implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectFlagLevel")
                .label("selectFlagLevel", "<h2>Where should this flag be added?\n</h2>")
                .complex(CaseData::getCaseFlag)
                .mandatoryWithLabel(CaseFlag::getFlagLevel, "Case or Party Level?")
                .done();
    }
}
