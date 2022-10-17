package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.payment.PaymentService;

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
            .mandatory(RecordListing::getHearingVenues)
            .optional(RecordListing::getRoomAtVenue)
            .optional(RecordListing::getAddlInstr)
            .label("hearingDateObj", "<h4>Hearing, date and start time</h4>")
            .mandatory(RecordListing::getHearingDateTime)
            .mandatory(RecordListing::getSession)
            .mandatory(RecordListing::getNumberOfDays)
            .mandatory(RecordListing::getAdditionalHearingDate, "recordNumberOfDays = \"Yes\"")
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();


        data.getRecordListing().setHearingVenueName("name");
        data.getRecordListing().setHearingVenueAddress("address");

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
