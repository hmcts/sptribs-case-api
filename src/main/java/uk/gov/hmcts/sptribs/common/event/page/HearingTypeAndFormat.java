package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.hearingvenue.LocationService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HearingTypeAndFormat implements CcdPageConfiguration {

    @Autowired
    private LocationService locationService;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("hearingTypeAndFormat", this::midEvent)
            .label("hearingTypeAndFormatObj", "<h1>Hearing type and format</h1>")
            .complex(CaseData::getRecordListing)
            .mandatory(RecordListing::getHearingType)
            .mandatory(RecordListing::getHearingFormat)
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        final List<String> errors = new ArrayList<>();
        String regionId = caseData.getRecordListing().getSelectedRegionId();

        if(null != regionId) {
            caseData.getRecordListing().setHearingVenues(locationService.getHearingVenuesByRegion(regionId));
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }
}
