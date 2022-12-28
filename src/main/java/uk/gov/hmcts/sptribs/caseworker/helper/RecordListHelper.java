package uk.gov.hmcts.sptribs.caseworker.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;

@Service
@Slf4j
public class RecordListHelper {

    @Autowired
    private LocationService locationService;

    public void regionData(CaseData caseData) {

        DynamicList regionList = locationService.getAllRegions();
        caseData.getRecordListing().setRegionList(regionList);

        String regionMessage = regionList == null || regionList.getListItems().isEmpty()
            ? "Unable to retrieve Region data"
            : null;
        caseData.getRecordListing().setRegionsMessage(regionMessage);
    }


    public void populatedVenuesData(CaseData caseData) {

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

    }

    private String getRegionId(String selectedRegion) {
        String[] values = selectedRegion != null
            ? Arrays.stream(selectedRegion.split(HYPHEN)).map(String::trim).toArray(String[]::new)
            : null;
        return values != null && values.length > 0 ? values[0] : null;
    }


    public boolean checkNullCondition(CicCase cicCase) {
        return null != cicCase
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartySubject())
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartyRepresentative())
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartyRespondent());
    }

    public void getNotificationParties(CaseData caseData) {
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
    }

    public List<String> getErrorMsg(CicCase cicCase) {
        final List<String> errors = new ArrayList<>();

        if (checkNullCondition(cicCase)) {
            errors.add("One party must be selected.");
        }
        return errors;
    }

    public void addHearingTypeAndFormat(PageBuilder pageBuilder) {
        pageBuilder.page("hearingTypeAndFormat")
            .pageLabel("Hearing type and format")
            .complex(CaseData::getRecordListing)
            .mandatory(RecordListing::getHearingType)
            .mandatory(RecordListing::getHearingFormat)
            .done();
    }

    public void addRemoteHearingInfo(PageBuilder pageBuilder) {
        pageBuilder.page("remoteHearingInformation")
            .label("remoteHearingInfoObj", "<h1>Remote hearing information</h1>")
            .complex(CaseData::getRecordListing)
            .optional(RecordListing::getVideoCallLink)
            .optional(RecordListing::getConferenceCallNumber)
            .done();
    }

    public void addOtherInformation(PageBuilder pageBuilder) {
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


}
