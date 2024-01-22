package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.VenueNotListed;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_HEARING_SUMMARY;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_HEARING_SUMMARY;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CURRENT_EVENT;

@Slf4j
@Component
public class HearingVenues implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "venueNotListedOption=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("listingDetails", this::midEvent)
            .pageLabel("Listing Details")
            .label("LabelListingDetails", "")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .readonly(CaseData::getCurrentEvent, ALWAYS_HIDE)
            .complex(CaseData::getListing)
            .readonly(Listing::getHearingVenuesMessage)
            .optional(Listing::getHearingVenues,
                "venueNotListedOption!= \"VenueNotListed\" AND (" + CURRENT_EVENT + CASEWORKER_RECORD_LISTING + "\""
                    + " OR " + CURRENT_EVENT + CASEWORKER_EDIT_RECORD_LISTING + "\")")
            .readonly(Listing::getReadOnlyHearingVenueName,
                CURRENT_EVENT + CASEWORKER_CREATE_HEARING_SUMMARY + "\"" + " OR " + CURRENT_EVENT + CASEWORKER_EDIT_HEARING_SUMMARY + "\"")
            .optionalWithLabel(Listing::getVenueNotListedOption, " ")
            .mandatory(Listing::getHearingVenueNameAndAddress, "venueNotListedOption= \"VenueNotListed\"")
            .optional(Listing::getRoomAtVenue)
            .optional(Listing::getAddlInstr,
                CURRENT_EVENT + CASEWORKER_RECORD_LISTING + "\"" + " OR " + CURRENT_EVENT + CASEWORKER_EDIT_RECORD_LISTING + "\"")
            .label("theLabelListingDetails", "<h4>Hearing date</h4>")
            .mandatory(Listing::getDate)
            .mandatory(Listing::getSession)
            .mandatory(Listing::getHearingTime)
            .mandatory(Listing::getNumberOfDays)
            .mandatory(Listing::getAdditionalHearingDate, "numberOfDays = \"Yes\"")
            .readonly(Listing::getHearingSummaryExists, ALWAYS_HIDE)
            .readonly(Listing::getHearingStatus, ALWAYS_HIDE)
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();
        final Listing listing = data.getListing();

        if (!listing.getVenueNotListedOption().contains(VenueNotListed.VENUE_NOT_LISTED)) {
            String selectedVenue = data.getListing().getSelectedVenue();
            listing.setHearingVenueNameAndAddress(selectedVenue);
        } else {
            listing.setReadOnlyHearingVenueName(null);
        }
        if (StringUtils.isBlank(listing.getHearingVenueNameAndAddress())
            && StringUtils.isBlank(listing.getReadOnlyHearingVenueName())) {
            errors.add("Please enter valid Hearing venue");
        }
        data.setListing(listing);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
