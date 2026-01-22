package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class PartyStatementSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("SELECT PARTY") //make sure no clashes!!
            .pageLabel("Select which party... ")
            .label("labelSelectWhichParty", "")
            .complex(CaseData::getCicCase)//need clarity on this
            .mandatory(CicCase::getWhichPartyStatementDynamicRadioList)
            .done();
    }


}
