package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class HearingAttendees implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "venueNotListedOption=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("hearingAttendees")
            .pageLabel("Hearing attendees")
            .label("LabelHearingAttendees","")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .complex(CaseData::getListing)
            .complex(Listing::getSummary)
            .optional(HearingSummary::getJudge)
            .readonly(HearingSummary::getJudgeList, ALWAYS_HIDE)
            .mandatory(HearingSummary::getIsFullPanel)
            .mandatory(HearingSummary::getMemberList, "isFullPanel = \"Yes\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        final CaseData caseDataBefore = detailsBefore.getData();

        // Retain judge for edit journey
        if (!isNull(caseDataBefore.getListing().getSummary().getJudge())) {
            caseData.getListing().getSummary().getJudge().setValue(caseDataBefore.getListing().getSummary().getJudge().getValue());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
