package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingTypeAndFormat;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingVenues;
import uk.gov.hmcts.sptribs.caseworker.event.page.RecordNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.ListingCreatedNotification;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.time.LocalDate;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerRecordListing implements CCDConfig<CaseData, State, UserRole> {

    private static final String ALWAYS_HIDE = "venueNotListedOption=\"ALWAYS_HIDE\"";

    private static final CcdPageConfiguration hearingTypeAndFormat = new HearingTypeAndFormat();
    private static final CcdPageConfiguration hearingVenues = new HearingVenues();
    private static final CcdPageConfiguration recordNotifyParties = new RecordNotifyParties();

    @Autowired
    private RecordListHelper recordListHelper;

    @Autowired
    private LocationService locationService;

    @Autowired
    private HearingService hearingService;

    @Autowired
    private ListingCreatedNotification listingCreatedNotification;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_RECORD_LISTING)
            .forStates(CaseManagement, ReadyToList)
            .name("Hearings: Create listing")
            .description("Hearings: Create listing")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SUPER_USER,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE));

        hearingTypeAndFormat.addTo(pageBuilder);
        addRegionInfo(pageBuilder);
        hearingVenues.addTo(pageBuilder);
        addRemoteHearingInfo(pageBuilder);
        addOtherInformation(pageBuilder);
        recordNotifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {

        final CaseData caseData = details.getData();

        log.info("AboutToStart input event:{}, data: {}", CASEWORKER_RECORD_LISTING, caseData);

        caseData.setListing(new Listing());
        recordListHelper.regionData(caseData);
        caseData.setCurrentEvent(CASEWORKER_RECORD_LISTING);

        log.info("AboutToStart output event:{}, data: {}", CASEWORKER_RECORD_LISTING, caseData);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }


    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker record listing callback invoked for Case Id: {}", details.getId());

        final CaseData caseData = details.getData();
        if (caseData.getListing() != null
            && caseData.getListing().getNumberOfDays() != null
            && caseData.getListing().getNumberOfDays().equals(YesOrNo.NO)) {
            caseData.getListing().setAdditionalHearingDate(null);
        }

        recordListHelper.getNotificationParties(caseData);

        caseData.setListing(recordListHelper.checkAndUpdateVenueInformation(caseData.getListing()));
        caseData.setCurrentEvent("");
        caseData.getListing().setHearingCreatedDate(LocalDate.now());
        caseData.getListing().setHearingStatus(Listed);
        hearingService.addListing(caseData, caseData.getListing());
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(AwaitingHearing)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        final CaseData data = details.getData();
        final CicCase cicCase = data.getCicCase();
        Set<NotificationParties> notificationPartiesSet = cicCase.getHearingNotificationParties();
        String caseNumber = data.getHyphenatedCaseRef();
        try {
            if (notificationPartiesSet.contains(NotificationParties.SUBJECT)) {
                listingCreatedNotification.sendToSubject(details.getData(), caseNumber);
            }
            if (notificationPartiesSet.contains(NotificationParties.REPRESENTATIVE)) {
                listingCreatedNotification.sendToRepresentative(details.getData(), caseNumber);
            }
            if (notificationPartiesSet.contains(NotificationParties.RESPONDENT)) {
                listingCreatedNotification.sendToRespondent(details.getData(), caseNumber);
            }
            if (notificationPartiesSet.contains(NotificationParties.APPLICANT)) {
                listingCreatedNotification.sendToApplicant(details.getData(), caseNumber);
            }
        } catch (Exception notificationException) {
            log.error("Create listing notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Create listing notification failed %n## Please resend the notification"))
                .build();
        }
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Listing record created %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase().getHearingNotificationParties())))
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();

        if (isNull(caseData.getListing().getHearingVenues())) {
            recordListHelper.populateVenuesData(caseData);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void addRegionInfo(PageBuilder pageBuilder) {
        pageBuilder.page("regionInfo", this::midEvent)
            .pageLabel("Region Data")
            .label("LabelRegionInfoObj", "")
            .complex(CaseData::getListing)
            .readonly(Listing::getRegionsMessage)
            .optional(Listing::getRegionList)
            .done();
    }

    private void addRemoteHearingInfo(PageBuilder pageBuilder) {
        pageBuilder.page("remoteHearingInformation")
            .pageLabel("Remote hearing information")
            .label("LabelRemoteHearingInfoObj", "")
            .complex(CaseData::getListing)
            .optional(Listing::getVideoCallLink)
            .optional(Listing::getConferenceCallNumber)
            .done();
    }

    private void addOtherInformation(PageBuilder pageBuilder) {
        pageBuilder.page("otherInformation")
            .pageLabel("Other information")
            .label("LabelOtherInformationObj", "")
            .complex(CaseData::getListing)
            .label("otherInfoLabel",
                """
                    Enter any other important information about this hearing.
                    This may include any reasonable adjustments that need to be made, or details
                    of anyone who should be excluded from attending this hearing.
                    """)
            .optional(Listing::getImportantInfoDetails)
            .readonly(Listing::getHearingCreatedDate, ALWAYS_HIDE)
            .readonly(Listing::getPostponeReason, ALWAYS_HIDE)
            .readonly(Listing::getPostponeDate, ALWAYS_HIDE)
            .readonly(Listing::getPostponeAdditionalInformation, ALWAYS_HIDE)
            .readonly(Listing::getRecordListingChangeReason, ALWAYS_HIDE)
            .readonly(Listing::getHearingCancellationReason, ALWAYS_HIDE)
            .readonly(Listing::getCancelledDate, ALWAYS_HIDE)
            .readonly(Listing::getCancelHearingAdditionalDetail, ALWAYS_HIDE)
            .complex(Listing::getSummary)
            .readonly(HearingSummary::getJudge, ALWAYS_HIDE)
            .readonly(HearingSummary::getIsFullPanel, ALWAYS_HIDE)
            .readonly(HearingSummary::getMemberList, ALWAYS_HIDE)
            .readonly(HearingSummary::getOutcome, ALWAYS_HIDE)
            .readonly(HearingSummary::getAdjournmentReasons, ALWAYS_HIDE)
            .readonly(HearingSummary::getOthers, ALWAYS_HIDE)
            .readonly(HearingSummary::getOtherDetailsOfAdjournment, ALWAYS_HIDE)
            .readonly(HearingSummary::getRecFile, ALWAYS_HIDE)
            .readonly(HearingSummary::getRecDesc, ALWAYS_HIDE)
            .readonly(HearingSummary::getRoles, ALWAYS_HIDE)
            .readonly(HearingSummary::getSubjectName, ALWAYS_HIDE)
            .done();
    }

}
