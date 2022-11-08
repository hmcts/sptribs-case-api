package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.VenueNotListed;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HearingVenues implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("listingDetails", this::midEvent)
            .label("listingDetailsObj", "<h1>Listing details</h1>")
            .complex(CaseData::getRecordListing)
            .readonly(RecordListing::getHearingVenuesMessage)
            .mandatory(RecordListing::getHearingVenues)
            .optional(RecordListing::getVenueNotListedOption)
            .optional(RecordListing::getHearingVenueName, "recordVenueNotListedOption= \"VenueNotListed\"")
            .optional(RecordListing::getHearingVenueAddress, "recordVenueNotListedOption= \"VenueNotListed\"")
            .optional(RecordListing::getRoomAtVenue)
            .optional(RecordListing::getAddlInstr)
            .label("hearingDateObj", "<h4>Hearing date</h4>")
            .mandatory(RecordListing::getHearingDate)
            .mandatory(RecordListing::getSession)
            .mandatory(RecordListing::getHearingTime)
            .mandatory(RecordListing::getNumberOfDays)
            .mandatory(RecordListing::getAdditionalHearingDate, "recordNumberOfDays = \"Yes\"")
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();
        final RecordListing recordListing = data.getRecordListing();

        if (!recordListing.getVenueNotListedOption().contains(VenueNotListed.VENUE_NOT_LISTED)) {
            recordListing.setHearingVenueName(data.getRecordListing().getSelectedVenueName());
            recordListing.setHearingVenueAddress(data.getRecordListing().getSelectedVenueName());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
