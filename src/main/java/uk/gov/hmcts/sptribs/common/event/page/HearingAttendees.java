package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class HearingAttendees implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("hearingAttendees")
            .pageLabel("Hearing attendees")
            .label("LabelHearingAttendees","")
            .complex(CaseData::getHearingSummary)
            .mandatory(HearingSummary::getJudge)
            .mandatory(HearingSummary::getFullPanelHearing)
            .mandatory(HearingSummary::getPanelMemberList, "hearingSummaryFullPanelHearing = \"Yes\"")
            .done();
    }
}
