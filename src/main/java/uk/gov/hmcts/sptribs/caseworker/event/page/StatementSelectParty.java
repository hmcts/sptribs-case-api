package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.statement.model.StatementUpload;

@Component
public class StatementSelectParty implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("statementSelectParty")
            .pageLabel("Select statement party")
            .complex(CaseData::getStatementUpload)
            .mandatory(StatementUpload::getStatementParty)
            .done();
    }
}
