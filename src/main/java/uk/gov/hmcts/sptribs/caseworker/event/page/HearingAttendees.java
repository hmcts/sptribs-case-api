package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.judicialrefdata.JudicialService;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getPanelMembers;

@Slf4j
@Component
public class HearingAttendees implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "venueNotListedOption=\"ALWAYS_HIDE\"";

    @Autowired
    private JudicialService judicialService;

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
            caseData.getListing().getSummary().setJudge(caseDataBefore.getListing().getSummary().getJudge());
            caseData.getListing().getSummary().getJudge().setValue(caseDataBefore.getListing().getSummary().getJudge().getValue());
        } else {
            DynamicList judicialUsersDynamicList = judicialService.getAllUsers(caseData);
            caseData.getListing().getSummary().setJudge(judicialUsersDynamicList);
        }

        // Retain members list for edit journey
        if (!isNull(caseDataBefore.getListing().getSummary().getMemberList())){
            caseData.getListing().getSummary().setMemberList(caseDataBefore.getListing().getSummary().getMemberList());
            caseData.getListing().getSummary().getMemberList().setValue(
                caseDataBefore.getListing().getSummary().getMemberList().getValue()
            );
        } else {
            DynamicList judicialUsersDynamicList = judicialService.getAllUsers(caseData);
            caseData.getListing().getSummary().setMemberList(getPanelMembers(judicialUsersDynamicList));
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
