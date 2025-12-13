package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CreateAndSendOrderIssueSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerCreateSendOrderSelectOrderIssuingType")
            .pageLabel("Select order")
            .label("LabelCaseworkerCreateSendOrderSelectOrderIssuingType","")
            .complex(CaseData::getCicCase)
                .mandatory(CicCase::getCreateAndSendIssuingTypes)
                .done()
            .done();
    }
}
