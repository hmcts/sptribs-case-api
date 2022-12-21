package uk.gov.hmcts.sptribs.caseworker.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.Arrays;

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
}
