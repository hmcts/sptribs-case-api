package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.Listing;
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
            .complex(CaseData::getListing)
            .mandatory(Listing::getHearingCancellationReason)
            .optional(Listing::getCancelHearingAdditionalDetail)
            .done();
    }
}
