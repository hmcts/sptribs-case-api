package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.maintainCaseLinks;

public class LinkCaseSelectCase implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("linkCaseSelectCase")
            .pageLabel("Select a case you want to link to this case")
            .pageShowConditions(maintainCaseLinks())
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getLinkCaseNumber)
            .mandatory(CicCase::getLinkCaseReason)
            .mandatory(CicCase::getLinkCaseOtherDescription, "cicCaseLinkCaseReason = \"other\"")
            .done();
    }
}
