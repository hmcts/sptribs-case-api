package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.maintainCaseLinks;

public class MaintainLinkCaseSelectCase implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("maintainLinkCaseSelectCase")
            .pageLabel("Select the cases you want to unlink from this case")
            .pageShowConditions(maintainCaseLinks())
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getLinkDynamicList)
            .done();
    }
}
