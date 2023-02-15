package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class HearingAttendeesRolePage implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("hearingAttendeesRole")
            .pageLabel("Hearing attendees")
            .complex(CaseData::getHearingSummary)
            .mandatory(HearingSummary::getHearingAttendeesRole)
            .mandatory(HearingSummary::getOtherAttendee, "hearingSummaryHearingAttendeesRoleCONTAINS \"other\"")
            .done();
    }
}
