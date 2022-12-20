package uk.gov.hmcts.sptribs.caseworker.helper;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.*;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;

@Component
public class RecordListingHelper {
    public void populatedRegionData(CaseData caseData, LocationService locationService) {
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


    public static void addHearingTypeAndFormat(PageBuilder pageBuilder) {
        pageBuilder.page("hearingTypeAndFormat")
            .pageLabel("Hearing type and format")
            .complex(CaseData::getRecordListing)
            .mandatory(RecordListing::getHearingType)
            .mandatory(RecordListing::getHearingFormat)
            .done();
    }


    public static void caseDataList(CaseData caseData, LocationService locationService) {
        DynamicList regionList = locationService.getAllRegions();
        caseData.getRecordListing().setRegionList(regionList);

        String regionMessage = regionList == null || regionList.getListItems().isEmpty()
            ? "Unable to retrieve Region data"
            : null;
        caseData.getRecordListing().setRegionsMessage(regionMessage);
    }

    public static List<String> partySelector(CaseDetails<CaseData, State> details, CaseData caseData) {

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
        return errors;
    }

    private static boolean checkNullCondition(CicCase cicCase) {
        return null != cicCase
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartySubject())
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartyRepresentative())
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartyRespondent());
    }

}
