package uk.gov.hmcts.sptribs.caseworker.helper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.parseHyphen;

@Service
@Slf4j
public class RecordListHelper {

    private final LocationService locationService;

    @Autowired
    public RecordListHelper(LocationService locationService) {
        this.locationService = locationService;
    }

    public void regionData(CaseData caseData) {

        DynamicList regionList = locationService.getAllRegions();
        caseData.getListing().setRegionList(regionList);

        String regionMessage = regionList == null || regionList.getListItems().isEmpty()
            ? "Unable to retrieve Region data"
            : null;
        caseData.getListing().setRegionsMessage(regionMessage);
    }


    public void populateVenuesData(CaseData caseData) {

        String selectedRegion = caseData.getListing().getSelectedRegionVal();
        String regionId = locationService.getRegionId(selectedRegion);

        if (regionId != null) {
            DynamicList hearingVenueList = locationService.getHearingVenuesByRegion(regionId);
            caseData.getListing().setHearingVenues(hearingVenueList);

            String hearingVenueMessage = hearingVenueList == null || hearingVenueList.getListItems().isEmpty()
                ? "Unable to retrieve Hearing Venues data"
                : null;
            caseData.getListing().setHearingVenuesMessage(hearingVenueMessage);
        }
    }

    public boolean checkNullCondition(CicCase cicCase) {
        return cicCase == null
            || (CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())
                && CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
                && CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent()));
    }

    public void getNotificationParties(CaseData caseData) {
        Set<NotificationParties> partiesSet = new HashSet<>();

        if (CollectionUtils.isNotEmpty(caseData.getCicCase().getNotifyPartySubject())) {
            partiesSet.add(NotificationParties.SUBJECT);
        }
        if (CollectionUtils.isNotEmpty(caseData.getCicCase().getNotifyPartyRepresentative())) {
            partiesSet.add(NotificationParties.REPRESENTATIVE);
        }
        if (CollectionUtils.isNotEmpty(caseData.getCicCase().getNotifyPartyRespondent())) {
            partiesSet.add(NotificationParties.RESPONDENT);
        }
        if (CollectionUtils.isNotEmpty(caseData.getCicCase().getNotifyPartyApplicant())) {
            partiesSet.add(NotificationParties.APPLICANT);
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
            .label("LabelRemoteHearingInfo", "")
            .complex(CaseData::getListing)
            .optional(Listing::getVideoCallLink)
            .optional(Listing::getConferenceCallNumber)
            .done();
    }

    public void addOtherInformation(PageBuilder pageBuilder) {
        pageBuilder.page("otherInformation")
            .pageLabel("Other information")
            .label("LabelOtherInfo", "")
            .complex(CaseData::getListing)
            .label("labelOtherInfoDetails",
                """

                    Enter any other important information about this hearing.

                    This may include any reasonable adjustments that need to be made, or details of anyone who should be excluded \
                    from attending this hearing. (Optional)
                    """)
            .optional(Listing::getImportantInfoDetails)
            .done();
    }

    public Listing checkAndUpdateVenueInformation(Listing listing) {
        if (listing.getVenueNotListedOption() != null
            && !listing.getVenueNotListedOption().contains(VenueNotListed.VENUE_NOT_LISTED)) {
            String selectedVenue = listing.getSelectedVenue();
            listing.setHearingVenueNameAndAddress(selectedVenue);
            listing.setReadOnlyHearingVenueName(parseHyphen(selectedVenue, 0));
        } else {
            listing.setReadOnlyHearingVenueName(null);
            listing.setHearingVenues(null);
        }
        return listing;
    }

    public Listing checkAndUpdateVenueInformationSummary(Listing listing) {
        if ((listing.getVenueNotListedOption() == null
            || !listing.getVenueNotListedOption().contains(VenueNotListed.VENUE_NOT_LISTED))
            && listing.getReadOnlyHearingVenueName() != null && !listing.getReadOnlyHearingVenueName().isEmpty()) {
            listing.setHearingVenueNameAndAddress(listing.getReadOnlyHearingVenueName());
        }
        return listing;
    }

    public Listing saveSummary(CaseData caseData) {
        final Listing listing = caseData.getListing();
        if (listing != null) {
            listing.setHearingFormat(listing.getHearingFormat());
            listing.setHearingType(listing.getHearingType());
            listing.getSummary().setSubjectName(caseData.getCicCase().getFullName());
            caseData.setCurrentEvent("");

            if (listing.getNumberOfDays() != null
                && listing.getNumberOfDays().equals(YesOrNo.NO)) {
                listing.setAdditionalHearingDate(null);
            }
            return checkAndUpdateVenueInformationSummary(listing);
        }
        return null;
    }
}
