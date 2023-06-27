package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class FlagLevel implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseWorkerCaseFlagSelectFlagLevel")
            .pageLabel("Where should this flag be added?")
            .label("LabelCaseWorkerCaseFlagSelectFlagLevel","")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getFlagLevel, "Case or Party Level?")
            .done();
    }
}
