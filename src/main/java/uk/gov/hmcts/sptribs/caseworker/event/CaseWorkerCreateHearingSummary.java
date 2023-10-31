package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.CreateHearingSummary;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingAttendees;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingAttendeesRolePage;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingOutcomePage;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingRecordingUploadPage;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingTypeAndFormat;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingVenues;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.judicialrefdata.JudicialService;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_HEARING_SUMMARY;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getPanelMembers;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Complete;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateCategoryToCaseworkerDocument;

@Component
@Slf4j
public class CaseWorkerCreateHearingSummary implements CCDConfig<CaseData, State, UserRole> {
    private static final CcdPageConfiguration createHearingSummary = new CreateHearingSummary();
    private static final CcdPageConfiguration hearingTypeAndFormat = new HearingTypeAndFormat();
    private static final CcdPageConfiguration hearingVenues = new HearingVenues();
    private static final CcdPageConfiguration hearingAttendees = new HearingAttendees();
    private static final CcdPageConfiguration hearingAttendeesRole = new HearingAttendeesRolePage();
    private static final CcdPageConfiguration HearingOutcome = new HearingOutcomePage();

    private static final CcdPageConfiguration hearingRecordingUploadPage = new HearingRecordingUploadPage();

    @Autowired
    private RecordListHelper recordListHelper;

    @Autowired
    private HearingService hearingService;

    @Autowired
    private JudicialService judicialService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(CASEWORKER_CREATE_HEARING_SUMMARY)
                .forStates(AwaitingHearing)
                .name("Hearings: Create summary")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::summaryEdited)
                .grant(CREATE_READ_UPDATE,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE));
        createHearingSummary.addTo(pageBuilder);
        hearingTypeAndFormat.addTo(pageBuilder);
        hearingVenues.addTo(pageBuilder);
        hearingAttendees.addTo(pageBuilder);
        hearingAttendeesRole.addTo(pageBuilder);
        HearingOutcome.addTo(pageBuilder);
        hearingRecordingUploadPage.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        caseData.setCurrentEvent(CASEWORKER_CREATE_HEARING_SUMMARY);

        DynamicList hearingDateDynamicList = hearingService.getHearingDateDynamicList(details);
        caseData.getCicCase().setHearingList(hearingDateDynamicList);

        DynamicList judicialUsersDynamicList = judicialService.getAllUsers();
        caseData.getListing().getSummary().setJudge(judicialUsersDynamicList);
        caseData.getListing().getSummary().setMemberList(getPanelMembers(judicialUsersDynamicList));
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }


    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        caseData.getListing().setHearingStatus(Complete);
        caseData.setListing(recordListHelper.saveSummary(details.getData()));
        caseData.setCurrentEvent("");

        updateCategoryToCaseworkerDocument(caseData.getListing().getSummary().getRecFile());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(AwaitingOutcome)
            .build();

    }

    public SubmittedCallbackResponse summaryEdited(CaseDetails<CaseData, State> details,
                                                   CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(MessageUtil.generateSimpleMessage(
                "Hearing summary created",
                "This hearing summary has been added to the case record."))
            .build();
    }

}
