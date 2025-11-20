package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ApplyAnonymity implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerApplyAnonymity", this::midEvent)
            .pageLabel("Anonymity")
            .label("LabelCaseworkerApplyAnonymity","")
            .complex(CaseData::getCicCase)
                .mandatory(CicCase::getAnonymiseYesOrNo)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> caseDetails,
                                                                  CaseDetails<CaseData, State> caseDetailsBefore) {
        final CaseData caseData = caseDetails.getData();

        // call to db to create anonymised name
        caseData.getCicCase().setAnonymisedAppellantName("Anonymised Name");
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
