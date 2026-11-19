package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class StatementSelectParty implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("caseworkerStatementSelectParty")
            .pageLabel("Select which party the statement is from")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getUploadPartyStatementParty);
    }
}
