package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingTypeAndFormat;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingVenues;
import uk.gov.hmcts.sptribs.caseworker.event.page.ListingChangeReason;
import uk.gov.hmcts.sptribs.caseworker.event.page.RecordNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.SelectHearing;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
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
import uk.gov.hmcts.sptribs.notification.dispatcher.ListingUpdatedNotification;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerEditRecordListing implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration selectHearing = new SelectHearing();

    private static final CcdPageConfiguration hearingVenues = new HearingVenues();

    private static final CcdPageConfiguration recordNotifyParties = new RecordNotifyParties();

    private static final CcdPageConfiguration hearingTypeAndFormat = new HearingTypeAndFormat();

    private static final CcdPageConfiguration listingChangeReason = new ListingChangeReason();

    @Autowired
    private RecordListHelper recordListHelper;

    @Autowired
    private HearingService hearingService;

    @Autowired
    private ListingUpdatedNotification listingUpdatedNotification;

    @Value("${feature.wa.enabled}")
    private boolean isWorkAllocationEnabled;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_EDIT_RECORD_LISTING)
                .forStates(AwaitingHearing)
                .name("Hearings: Edit listing")
                .description("Hearings: Edit listing")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE)
                .grantHistoryOnly(ST_CIC_JUDGE);

        if (isWorkAllocationEnabled) {
            eventBuilder.publishToCamunda()
                        .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);
        }

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        selectHearing.addTo(pageBuilder);
        hearingTypeAndFormat.addTo(pageBuilder);
        addRegionInfo(pageBuilder);
        hearingVenues.addTo(pageBuilder);
        recordListHelper.addRemoteHearingInfo(pageBuilder);
        recordListHelper.addOtherInformation(pageBuilder);
        listingChangeReason.addTo(pageBuilder);
        recordNotifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {

        final CaseData caseData = details.getData();
        caseData.setCurrentEvent(CASEWORKER_EDIT_RECORD_LISTING);

        if (!StringUtils.isEmpty(caseData.getListing().getReadOnlyHearingVenueName())) {
            caseData.getListing().setHearingVenueNameAndAddress(null);
        }

        if (isNull(caseData.getListing().getRegionList())) {
            recordListHelper.regionData(caseData);
        }

        DynamicList hearingDateDynamicList = hearingService.getListedHearingDynamicList(caseData);
        caseData.getCicCase().setHearingList(hearingDateDynamicList);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        final CaseData caseDataBefore = detailsBefore.getData();

        recordListHelper.populateVenuesData(caseData);

        if (null != caseDataBefore.getListing().getReadOnlyHearingVenueName()
            && null != caseData.getListing().getSelectedRegionVal()
            && caseData.getListing().getSelectedRegionVal().equals(caseDataBefore.getListing().getSelectedRegionVal())
            && null != caseDataBefore.getListing().getHearingVenues()) {
            caseData.getListing().setHearingVenues(caseDataBefore.getListing().getHearingVenues());
            caseData.getListing().getHearingVenues().setValue(caseDataBefore.getListing().getHearingVenues().getValue());

        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final List<String> errors = recordListHelper.getErrorMsg(details.getData().getCicCase());
        if (null != caseData.getListing()
            && null != caseData.getListing().getNumberOfDays()
            && caseData.getListing().getNumberOfDays().equals(YesOrNo.NO)) {
            caseData.getListing().setAdditionalHearingDate(null);
        }
        recordListHelper.getNotificationParties(caseData);
        caseData.setListing(recordListHelper.checkAndUpdateVenueInformation(caseData.getListing()));
        caseData.setCurrentEvent("");

        final String hearingName = caseData.getCicCase().getHearingList().getValue().getLabel();

        hearingService.updateHearingList(caseData, hearingName);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(AwaitingHearing)
            .errors(errors)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final CicCase cicCase = caseData.getCicCase();
        Set<NotificationParties> notificationPartiesSet = cicCase.getHearingNotificationParties();
        String caseNumber = caseData.getHyphenatedCaseRef();

        try {
            if (notificationPartiesSet.contains(NotificationParties.SUBJECT)) {
                listingUpdatedNotification.sendToSubject(details.getData(), caseNumber);
            }
            if (notificationPartiesSet.contains(NotificationParties.REPRESENTATIVE)) {
                listingUpdatedNotification.sendToRepresentative(details.getData(), caseNumber);
            }
            if (notificationPartiesSet.contains(NotificationParties.RESPONDENT)) {
                listingUpdatedNotification.sendToRespondent(details.getData(), caseNumber);
            }
        } catch (Exception notificationException) {
            log.error("Update listing notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Update listing notification failed %n## Please resend the notification"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Listing record updated %n##  If any changes are made to this hearing, "
                    + " remember to make those changes in this listing record. %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase().getHearingNotificationParties())))
            .build();
    }

    private void addRegionInfo(PageBuilder pageBuilder) {
        pageBuilder.page("regionInfo", this::midEvent)
            .pageLabel("Region Data")
            .label("labelEditRecordingRegionData", "")
            .complex(CaseData::getListing)
            .readonly(Listing::getRegionsMessage)
            .optional(Listing::getRegionList)
            .done();
    }
}
