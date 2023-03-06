package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class HearingAttendeesRolePage implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("hearingAttendeesRole")
            .pageLabel("Hearing attendees")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .complex(CaseData::getHearingSummary)
            .mandatory(HearingSummary::getHearingAttendeesRole)
            .mandatory(HearingSummary::getOtherAttendee, "hearingSummaryHearingAttendeesRoleCONTAINS \"other\"")
            .done();
    }
}
