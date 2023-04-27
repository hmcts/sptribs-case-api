package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class FlagTypePage implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseWorkerCaseFLagSelectFlagType")
            .pageLabel("Select flag type")
            .label("LabelCaseWorkerCaseFLagSelectFlagType", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getFlagType)
            .mandatory(CicCase::getFlagOtherDescription, "cicCaseFlagType = \"Other\"")
            .done();
    }
}
