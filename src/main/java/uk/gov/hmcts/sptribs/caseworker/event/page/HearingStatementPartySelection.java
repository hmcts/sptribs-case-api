package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.*;
import uk.gov.hmcts.sptribs.ciccase.model.*;
import uk.gov.hmcts.sptribs.common.ccd.*;

public class HearingStatementPartySelection implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("uploadStatementSelectParty")
            .pageLabel("Who is this statement from?")
            .label("LabelUploadStatementSelectParty", " ")
            .complex(CaseData::getStatementRecord)
            .mandatory(StatementRecord::getPartyType)
            .done();
    }
}
