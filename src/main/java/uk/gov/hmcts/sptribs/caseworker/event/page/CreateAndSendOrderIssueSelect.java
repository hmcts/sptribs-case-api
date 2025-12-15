package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CreateAndSendOrderIssueSelect implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "cicCaseCreateAndSendIssuingTypes=\"NEVER_SHOW\"";
    private static final String ALWAYS_SHOW = "cicCaseOrderIssuingType=\"\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerCreateSendOrderSelectOrderIssuingType", this::midEvent)
            .pageLabel("Select order")
            .label("LabelCaseworkerCreateSendOrderSelectOrderIssuingType","")
            .complex(CaseData::getCicCase)
                .mandatory(CicCase::getCreateAndSendIssuingTypes, ALWAYS_SHOW)
                .readonly(CicCase::getOrderIssuingType, NEVER_SHOW)
//                .readonly(CicCase::getAnonymityAlreadyApplied, NEVER_SHOW)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> caseDetails,
                                                                   CaseDetails<CaseData, State> caseDetailsBefore) {

        CaseData caseData = caseDetails.getData();
        caseData.getCicCase().setOrderIssuingType(null);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }
}
