package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CancelHearing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CancelHearingReasonSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerCancelHearingReason")
            .pageLabel("Reasons for cancellation")
            .complex(CaseData::getCancelHearing)
            .mandatory(CancelHearing::getHearingCancellationReason)
            .optional(CancelHearing::getAdditionalDetail)
            .done();
    }
}
