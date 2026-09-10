package uk.gov.hmcts.sptribs.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_PANEL_COMPOSITION;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerPanelComposition implements CCDConfig<CriminalInjuriesCompensationData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        new PageBuilder<>(configBuilder
            .event(CASEWORKER_PANEL_COMPOSITION)
            .forStates(CaseManagement, ReadyToList, AwaitingHearing)
            .showCondition("panel1!=\"Tribunal Judge\"")
            .name("Case: Panel Composition")
            .description("Case: Panel Composition")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_JUDGE, ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE))
            .page("casePanelComposition")
            .complex(CaseData::getListing)
                .complex(Listing::getSummary)
                    .readonly(HearingSummary::getPanel1)
                    .optional(HearingSummary::getPanel2)
                    .optional(HearingSummary::getPanel3)
                    .optional(HearingSummary::getPanelMemberInformation)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> aboutToStart(CaseDetails<CriminalInjuriesCompensationData,
        State> details) {
        final CriminalInjuriesCompensationData caseData = details.getData();
        caseData.getListing().getSummary().setPanel1("Tribunal Judge");
        return AboutToStartOrSubmitResponse.<CriminalInjuriesCompensationData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData,
        State> aboutToSubmit(CaseDetails<CriminalInjuriesCompensationData, State> details,
                                                                       CaseDetails<CriminalInjuriesCompensationData, State> beforeDetails) {

        final CriminalInjuriesCompensationData caseData = details.getData();
        caseData.getListing().getSummary().populatePanelComposition();

        return AboutToStartOrSubmitResponse.<CriminalInjuriesCompensationData, State>builder()
            .data(caseData)
            .build();
    }
}
