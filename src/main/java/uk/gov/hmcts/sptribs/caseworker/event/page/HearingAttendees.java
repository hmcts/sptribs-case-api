package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
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
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .complex(CaseData::getListing)
            .complex(Listing::getSummary)
            .optional(HearingSummary::getJudge)
            .mandatory(HearingSummary::getIsFullPanel)
            .mandatory(HearingSummary::getMemberList, "isFullPanel = \"Yes\"")
            .done();
    }
}
