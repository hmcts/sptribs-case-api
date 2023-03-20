package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import uk.gov.hmcts.sptribs.caseworker.event.page.ListingChangeReason;
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
import uk.gov.hmcts.sptribs.common.notification.ListingUpdatedNotification;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerEditRecordListing implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration hearingVenues = new HearingVenues();

    private static final CcdPageConfiguration recordNotifyParties = new RecordNotifyParties();

    private static final CcdPageConfiguration hearingTypeAndFormat = new HearingTypeAndFormat();

    private static final CcdPageConfiguration listingChangeReason = new ListingChangeReason();

    @Autowired
    private RecordListHelper recordListHelper;

    @Autowired
    private ListingUpdatedNotification liistingUpdatedNotification;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_EDIT_RECORD_LISTING)
            .forStates(AwaitingHearing)
            .name("Hearings: Edit listing")
            .description("Hearings: Edit listing")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));


        hearingTypeAndFormat.addTo(pageBuilder);
        addRegionInfo(pageBuilder);
        hearingVenues.addTo(pageBuilder);
        recordListHelper.addRemoteHearingInfo(pageBuilder);
        recordListHelper.addOtherInformation(pageBuilder);
        listingChangeReason.addTo(pageBuilder);
        recordNotifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        caseData.setCurrentEvent(CASEWORKER_EDIT_RECORD_LISTING);
        if (!StringUtils.isEmpty(caseData.getListing().getReadOnlyHearingVenueName())) {
            caseData.getListing().setHearingVenueNameAndAddress(null);
        }
        if (caseData.getListing().getRegionList() == null) {
            recordListHelper.regionData(caseData);
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)
            .build();
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker updated record listing callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();
        final List<String> errors = recordListHelper.getErrorMsg(details.getData().getCicCase());
        if (null != caseData.getListing()
            && null != caseData.getListing().getNumberOfDays()
            && caseData.getListing().getNumberOfDays().equals(YesOrNo.NO)) {
            caseData.getListing().setAdditionalHearingDate(null);
        }
        recordListHelper.getNotificationParties(caseData);
        caseData.setListing(recordListHelper.checkAndUpdateVenueInformation(caseData.getListing()));
        caseData.setCurrentEvent("");
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(AwaitingHearing)
            .errors(errors)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        var data = details.getData();
        var cicCase = data.getCicCase();
        Set<NotificationParties> notificationPartiesSet = cicCase.getHearingNotificationParties();
        String caseNumber = data.getHyphenatedCaseRef();

        if (notificationPartiesSet.contains(NotificationParties.SUBJECT)) {
            liistingUpdatedNotification.sendToSubject(details.getData(), caseNumber);
        }
        if (notificationPartiesSet.contains(NotificationParties.REPRESENTATIVE)) {
            liistingUpdatedNotification.sendToRepresentative(details.getData(), caseNumber);
        }
        if (notificationPartiesSet.contains(NotificationParties.RESPONDENT)) {
            liistingUpdatedNotification.sendToRespondent(details.getData(), caseNumber);
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

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        final CaseData caseDataBefore = detailsBefore.getData();

        recordListHelper.populatedVenuesData(caseData);

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

}
