package uk.gov.hmcts.sptribs.caseworker.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.VenueNotListed;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.parseHyphen;

@Service
@Slf4j
public class RecordListHelper {

    @Autowired
    private LocationService locationService;

    public void regionData(CaseData caseData) {

        DynamicList regionList = locationService.getAllRegions();
        caseData.getListing().setRegionList(regionList);

        String regionMessage = regionList == null || regionList.getListItems().isEmpty()
            ? "Unable to retrieve Region data"
            : null;
        caseData.getListing().setRegionsMessage(regionMessage);
        caseData.setCurrentEvent(CASEWORKER_EDIT_RECORD_LISTING);
    }


    public void populatedVenuesData(CaseData caseData) {

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

    }

    private String getRegionId(String selectedRegion) {
        String[] values = selectedRegion != null
            ? Arrays.stream(selectedRegion.split(HYPHEN)).map(String::trim).toArray(String[]::new)
            : null;
        return values != null && values.length > 0 ? values[0] : null;
    }


    public boolean checkNullCondition(CicCase cicCase) {
        return null != cicCase
            && CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())
            && CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
            && CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent());
    }

    public void getNotificationParties(CaseData caseData) {
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
    }

    public List<String> getErrorMsg(CicCase cicCase) {
        final List<String> errors = new ArrayList<>();

        if (checkNullCondition(cicCase)) {
            errors.add("One party must be selected.");
        }
        return errors;
    }


    public void addRemoteHearingInfo(PageBuilder pageBuilder) {
        pageBuilder.page("remoteHearingInformation")
            .pageLabel("Remote hearing information")
            .label("labelRemoteHearingInfo", "")
            .complex(CaseData::getListing)
            .optional(Listing::getVideoCallLink)
            .optional(Listing::getConferenceCallNumber)
            .done();
    }

    public void addOtherInformation(PageBuilder pageBuilder) {
        pageBuilder.page("otherInformation")
            .pageLabel("Other information")
            .label("labelOtherInfo", "")
            .complex(CaseData::getListing)
            .label("labelOtherInfoDetails",
                "\nEnter any other important information about this hearing.\n"
                    + "\nThis may include any reasonable adjustments that need to be made, or details"
                    + " of anyone who should be excluded from attending this hearing. (Optional)\n")
            .optional(Listing::getImportantInfoDetails)
            .done();
    }

    public Listing checkAndUpdateVenueInformation(Listing listing) {
        if (!listing.getVenueNotListedOption().contains(VenueNotListed.VENUE_NOT_LISTED)) {
            String selectedVenue = listing.getSelectedVenue();
            listing.setHearingVenueNameAndAddress(selectedVenue);
            listing.setReadOnlyHearingVenueName(parseHyphen(selectedVenue, 0));
        } else {
            listing.setReadOnlyHearingVenueName(null);
        }
        return listing;
    }

    public Listing checkAndUpdateVenueInformationSummary(Listing listing) {
        if ((null == listing.getVenueNotListedOption()
            || !listing.getVenueNotListedOption().contains(VenueNotListed.VENUE_NOT_LISTED))
            && null != listing.getReadOnlyHearingVenueName() && !listing.getReadOnlyHearingVenueName().isEmpty()) {
            listing.setHearingVenueNameAndAddress(listing.getReadOnlyHearingVenueName());
        }
        return listing;
    }

    public Listing saveSummary(CaseData caseData) {
        caseData.getListing().setHearingFormat(caseData.getListing().getHearingFormat());
        caseData.getListing().setHearingType(caseData.getListing().getHearingType());
        caseData.getListing().getSummary().setSubjectName(caseData.getCicCase().getFullName());
        caseData.setCurrentEvent("");
        if (null != caseData.getListing()
            && null != caseData.getListing().getNumberOfDays()
            && caseData.getListing().getNumberOfDays().equals(YesOrNo.NO)) {
            caseData.getListing().setAdditionalHearingDate(null);
        }

        return checkAndUpdateVenueInformationSummary(caseData.getListing());
    }
}
