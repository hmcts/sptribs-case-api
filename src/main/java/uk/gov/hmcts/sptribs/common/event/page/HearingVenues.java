package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_HEARING_SUMMARY;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CURRENT_EVENT;

@Slf4j
@Component
public class HearingVenues implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "recordVenueNotListedOption=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("listingDetails", this::midEvent)
            .pageLabel("Hearing location and duration")
            .readonly(CaseData::getCurrentEvent, ALWAYS_HIDE)
            .complex(CaseData::getRecordListing)
            .readonly(RecordListing::getHearingVenuesMessage)
            .optional(RecordListing::getHearingVenues,
                CURRENT_EVENT + CASEWORKER_RECORD_LISTING + "\"" + " OR " + CURRENT_EVENT + CASEWORKER_EDIT_RECORD_LISTING + "\"")
            .readonly(RecordListing::getReadOnlyHearingVenueName,
                CURRENT_EVENT + CASEWORKER_CREATE_HEARING_SUMMARY + "\"")
            .optional(RecordListing::getVenueNotListedOption)
            .mandatory(RecordListing::getHearingVenueNameAndAddress, "recordVenueNotListedOption= \"VenueNotListed\"")
            .optional(RecordListing::getRoomAtVenue)
            .optional(RecordListing::getAddlInstr,
                CURRENT_EVENT + CASEWORKER_RECORD_LISTING + "\"" + " OR " + CURRENT_EVENT + CASEWORKER_EDIT_RECORD_LISTING + "\"")
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
            String selectedVenue = data.getRecordListing().getSelectedVenue();
            recordListing.setHearingVenueNameAndAddress(selectedVenue);
        } else {
            recordListing.setReadOnlyHearingVenueName(null);
        }
        if (StringUtils.isBlank(recordListing.getHearingVenueNameAndAddress())) {
            errors.add("Please enter valid Hearing venue");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
