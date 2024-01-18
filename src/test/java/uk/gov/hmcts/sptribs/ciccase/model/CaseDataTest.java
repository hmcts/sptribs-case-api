package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import static org.assertj.core.api.Assertions.assertThat;

class CaseDataTest {

    @Test
    void shouldReturnNullIfApplicant2EmailIsNullAndCaseInviteIsNull() {
        final CaseData caseData = CaseData.builder().build();

        assertThat(caseData.getCicCase().getApplicantFullName()).isNull();
    }

    @Test
    void shouldGetHearingDate() {
        final DateTimeFormatter dateFormatter = ofPattern("dd MMM yyyy", UK);
        final LocalDate now = LocalDate.now();
        final Listing listing = Listing.builder().hearingStatus(HearingState.Listed).date(now).build();
        final ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        final CaseData caseData = CaseData.builder()
            .hearingList(List.of(listingListValue))
            .build();

        final String result = caseData.getFirstHearingDate();

        assertThat(result).isEqualTo(dateFormatter.format(now));
    }

    @Test
    void shouldGetHearingLocation() {
        final String someAddress = "WC1";
        final Listing listing = Listing.builder()
            .hearingStatus(HearingState.Listed)
            .date(LocalDate.now())
            .hearingVenueNameAndAddress(someAddress)
            .build();
        final ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        final CaseData caseData = CaseData.builder()
            .hearingList(List.of(listingListValue))
            .build();

        final String result = caseData.getHearingVenueName();

        assertThat(result).isEqualTo(someAddress);
    }

    @Test
    void shouldGetCompletedHearingDate() {
        final LocalDate now = LocalDate.now();
        final Listing listing = Listing.builder().hearingStatus(HearingState.Complete).date(now).build();
        final ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        final CaseData caseData = CaseData.builder()
            .hearingList(List.of(listingListValue))
            .build();

        //When
        final Listing result = caseData.getLatestCompletedHearing();

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo(now);
    }
}
