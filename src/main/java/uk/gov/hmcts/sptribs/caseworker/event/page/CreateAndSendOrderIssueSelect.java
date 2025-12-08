package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CreateAndSendOrderIssueSelect implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "cicCaseCreateAndSendIssuingTypes=\"NEVER_SHOW\"";
    private static final String ALWAYS_SHOW = "cicCaseOrderIssuingType=\"\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerCreateSendOrderSelectOrderIssuingType")
            .pageLabel("Select order")
            .label("LabelCaseworkerCreateSendOrderSelectOrderIssuingType","")
            .complex(CaseData::getCicCase)
                .mandatory(CicCase::getCreateAndSendIssuingTypes, ALWAYS_SHOW)
                .readonly(CicCase::getOrderIssuingType, NEVER_SHOW)
                .done()
            .done();
    }
}
