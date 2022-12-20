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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.RecordNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.SelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.UploadHearingNotice;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.HearingVenues;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerRecordListing implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_RECORD_LISTING = "caseworker-record-listing";

    private static final CcdPageConfiguration hearingVenues = new HearingVenues();
    private static final CcdPageConfiguration uploadHearingNotice = new UploadHearingNotice();
    private static final CcdPageConfiguration selectTemplate = new SelectTemplate();
    private static final CcdPageConfiguration recordNotifyParties = new RecordNotifyParties();

    @Autowired
    private LocationService locationService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_RECORD_LISTING)
            .forStates(CaseManagement, AwaitingHearing)
            .name("Record listing")
            .description("Record listing")
            .showEventNotes()
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));

        addHearingTypeAndFormat(pageBuilder);
        addRegionInfo(pageBuilder);
        hearingVenues.addTo(pageBuilder);
        addRemoteHearingInfo(pageBuilder);
        addOtherInformation(pageBuilder);
        addHearingNotice(pageBuilder);
        uploadHearingNotice.addTo(pageBuilder);
        selectTemplate.addTo(pageBuilder);
        recordNotifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        DynamicList regionList = locationService.getAllRegions();
        caseData.getRecordListing().setRegionList(regionList);

        String regionMessage = regionList == null || regionList.getListItems().isEmpty()
            ? "Unable to retrieve Region data"
            : null;
        caseData.getRecordListing().setRegionsMessage(regionMessage);

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
        final List<String> errors = new ArrayList<>();

        if (checkNullCondition(details.getData().getCicCase())) {
            errors.add("One party must be selected.");
        }
        Set<NotificationParties> partiesSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getRecordNotifyPartySubject())) {
            partiesSet.add(NotificationParties.SUBJECT);
        }
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getRecordNotifyPartyRepresentative())) {
            partiesSet.add(NotificationParties.REPRESENTATIVE);
        }
        if (!CollectionUtils.isEmpty(caseData.getCicCase().getRecordNotifyPartyRespondent())) {
            partiesSet.add(NotificationParties.RESPONDENT);
        }
        caseData.getRecordListing().setNotificationParties(partiesSet);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(AwaitingHearing)
            .errors(errors)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Listing record created")
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        String selectedRegion = caseData.getRecordListing().getSelectedRegionVal();
        String regionId = getRegionId(selectedRegion);

        if (null != regionId) {
            DynamicList hearingVenueList = locationService.getHearingVenuesByRegion(regionId);
            caseData.getRecordListing().setHearingVenues(hearingVenueList);

            String hearingVenueMessage = hearingVenueList == null || hearingVenueList.getListItems().isEmpty()
                ? "Unable to retrieve Hearing Venues data"
                : null;
            caseData.getRecordListing().setHearingVenuesMessage(hearingVenueMessage);

        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private boolean checkNullCondition(CicCase cicCase) {
        return null != cicCase
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartySubject())
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartyRepresentative())
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartyRespondent());
    }

    private void addHearingTypeAndFormat(PageBuilder pageBuilder) {
        pageBuilder.page("hearingTypeAndFormat")
            .pageLabel("Hearing type and format")
            .complex(CaseData::getRecordListing)
            .mandatory(RecordListing::getHearingType)
            .mandatory(RecordListing::getHearingFormat)
            .done();
    }

    private void addRegionInfo(PageBuilder pageBuilder) {
        pageBuilder.page("regionInfo", this::midEvent)
            .label("regionInfoObj", "<h1>Region Data</h1>")
            .complex(CaseData::getRecordListing)
            .readonly(RecordListing::getRegionsMessage)
            .optional(RecordListing::getRegionList)
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
            .label("remoteHearingInfoObj", "<h1>Remote hearing information</h1>")
            .complex(CaseData::getRecordListing)
            .optional(RecordListing::getVideoCallLink)
            .optional(RecordListing::getConferenceCallNumber)
            .done();
    }

    private void addOtherInformation(PageBuilder pageBuilder) {
        pageBuilder.page("otherInformation")
            .label("otherInformationObj", "<h1>Other information</h1>")
            .complex(CaseData::getRecordListing)
            .label("otherInfoLabel",
                "\nEnter any other important information about this hearing."
                    + " This may include any reasonable adjustments that need to be made, or details"
                    + "\n of anyone who should be excluded from attending this hearing.\n")
            .optional(RecordListing::getImportantInfoDetails)
            .done();
    }

    private void addHearingNotice(PageBuilder pageBuilder) {
        pageBuilder.page("hearingNotice")
            .label("hearingNoticeObj", "<h1>Create a hearing Notice</h1>")
            .complex(CaseData::getRecordListing)
            .mandatory(RecordListing::getHearingNotice)
            .done();
    }

}
