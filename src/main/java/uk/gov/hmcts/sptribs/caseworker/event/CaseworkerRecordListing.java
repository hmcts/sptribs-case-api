package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingTypeAndFormat;
import uk.gov.hmcts.sptribs.caseworker.event.page.HearingVenues;
import uk.gov.hmcts.sptribs.caseworker.event.page.RecordNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.ListingCreatedNotification;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.DELETE;

@Component
@Slf4j
public class CaseworkerRecordListing implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration hearingTypeAndFormat = new HearingTypeAndFormat();
    private static final CcdPageConfiguration hearingVenues = new HearingVenues();
    private static final CcdPageConfiguration recordNotifyParties = new RecordNotifyParties();

    @Autowired
    private RecordListHelper recordListHelper;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ListingCreatedNotification listingCreatedNotification;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_RECORD_LISTING)
            .forStates(CaseManagement)
            .name("Hearings: Create listing")
            .description("Hearings: Create listing")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(DELETE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE));

        hearingTypeAndFormat.addTo(pageBuilder);
        addRegionInfo(pageBuilder);
        hearingVenues.addTo(pageBuilder);
        addRemoteHearingInfo(pageBuilder);
        addOtherInformation(pageBuilder);
        recordNotifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        DynamicList regionList = locationService.getAllRegions();
        caseData.getListing().setRegionList(regionList);

        String regionMessage = regionList == null || regionList.getListItems().isEmpty()
            ? "Unable to retrieve Region data"
            : null;
        caseData.getListing().setRegionsMessage(regionMessage);
        caseData.setCurrentEvent(CASEWORKER_RECORD_LISTING);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker record listing callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();
        if (null != caseData.getListing()
            && null != caseData.getListing().getNumberOfDays()
            && caseData.getListing().getNumberOfDays().equals(YesOrNo.NO)) {
            caseData.getListing().setAdditionalHearingDate(null);
        }
        Set<NotificationParties> partiesSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartySubject())) {
            partiesSet.add(NotificationParties.SUBJECT);
        }
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRepresentative())) {
            partiesSet.add(NotificationParties.REPRESENTATIVE);
        }
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartyRespondent())) {
            partiesSet.add(NotificationParties.RESPONDENT);
        }
        caseData.getCicCase().setHearingNotificationParties(partiesSet);

        caseData.setListing(recordListHelper.checkAndUpdateVenueInformation(caseData.getListing()));
        caseData.setCurrentEvent("");

        caseData.getListing().setHearingStatus(Listed);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(AwaitingHearing)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        var cicCase = data.getCicCase();
        Set<NotificationParties> notificationPartiesSet = cicCase.getHearingNotificationParties();
        String caseNumber = data.getHyphenatedCaseRef();

        if (notificationPartiesSet.contains(NotificationParties.SUBJECT)) {
            listingCreatedNotification.sendToSubject(details.getData(), caseNumber);
        }
        if (notificationPartiesSet.contains(NotificationParties.REPRESENTATIVE)) {
            listingCreatedNotification.sendToRepresentative(details.getData(), caseNumber);
        }
        if (notificationPartiesSet.contains(NotificationParties.RESPONDENT)) {
            listingCreatedNotification.sendToRespondent(details.getData(), caseNumber);
        }
        data.getListing().setHearingStatus(Listed);
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Listing record created %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase().getHearingNotificationParties())))
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        String selectedRegion = caseData.getListing().getSelectedRegionVal();
        String regionId = getRegionId(selectedRegion);

        if (null != regionId) {
            DynamicList hearingVenueList = locationService.getHearingVenuesByRegion(regionId);
            caseData.getListing().setHearingVenues(hearingVenueList);

            String hearingVenueMessage = hearingVenueList == null || hearingVenueList.getListItems().isEmpty()
                ? "Unable to retrieve Hearing Venues data"
                : null;
            caseData.getListing().setHearingVenuesMessage(hearingVenueMessage);

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

    private String getRegionId(String selectedRegion) {
        String[] values = selectedRegion != null
            ? Arrays.stream(selectedRegion.split(HYPHEN)).map(String::trim).toArray(String[]::new)
            : null;
        return values != null && values.length > 0 ? values[0] : null;
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
            .done();
    }

}
