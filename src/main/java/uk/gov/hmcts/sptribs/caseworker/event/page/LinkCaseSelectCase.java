package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class LinkCaseSelectCase implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("linkCaseSelectCase")
            .pageLabel("Select a case you want to link to this case")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getLinkCaseNumber)
            .mandatoryWithLabel(CicCase::getLinkCaseReason, "Select all that apply")
            //   .mandatory(CicCase::getLinkCaseOtherDescription, "linkLinkCaseReason = \"other\"")
            .done();
    }
}
