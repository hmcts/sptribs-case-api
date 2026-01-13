package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class DateOfInitialCicaDecision implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("initialDecisionDateObjects")
            .pageLabel("Date of CICA initial review decision letter")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getInitialCicaDecisionDate, "Date of CICA initial review decision letter")
            .done();
    }
}
