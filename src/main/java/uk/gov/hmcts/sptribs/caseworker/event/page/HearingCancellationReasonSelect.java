package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CancelHearing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class HearingCancellationReasonSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectHearingCancellationReason")
            .label("selectHearingCancellationReason", "<h1>Reasons for cancellation/h1>")
            .complex(CaseData::getCancelHearing)
            .mandatory(CancelHearing::getHearingCancellationReason)
            .optional(CancelHearing::getAdditionalDetail)
            .done();
    }
}
