package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CancelHearingReasonSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerCancelHearingReason")
            .pageLabel("Reasons for cancellation")
            .label("LabelCaseworkerCancelHearingReason", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getHearingCancellationReason)
            .optional(CicCase::getCancelHearingAdditionalDetail)
            .done();
    }
}
