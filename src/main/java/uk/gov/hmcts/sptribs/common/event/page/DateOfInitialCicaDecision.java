package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class DateOfInitialCicaDecision implements CcdPageConfiguration {
    @Override
    public <T extends CaseData> void addTo(PageBuilder<T> pageBuilder) {
        pageBuilder
            .page("initialDecisionDateObjects")
            .pageLabel("Date of CICA initial review decision letter")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getInitialCicaDecisionDate)
            .done();
    }
}
