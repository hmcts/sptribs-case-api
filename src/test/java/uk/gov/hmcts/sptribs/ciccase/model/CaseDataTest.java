package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import static org.assertj.core.api.Assertions.assertThat;

class CaseDataTest {

    @Test
    void shouldReturnNullIfApplicant2EmailIsNullAndCaseInviteIsNull() {
        //When
        final CaseData caseData = CaseData.builder()
            .build();
        //Then
        assertThat(caseData.getCicCase().getApplicantFullName()).isNull();
    }

    @Test
    void shouldGetHearingDate() {
        //When
        DateTimeFormatter dateFormatter = ofPattern("dd MMM yyyy", UK);
        LocalDate now = LocalDate.now();
        Listing listing = Listing.builder().hearingStatus(HearingState.Listed).date(now).build();
        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        final CaseData caseData = CaseData.builder()
            .hearingList(List.of(listingListValue))
            .build();

        //When
        String result = caseData.getFirstHearingDate();

        //Then
        assertThat(result).isEqualTo(dateFormatter.format(now));
    }

    @Test
    void shouldGetHearingLocation() {
        //When
        String someAddress = "WC1";
        Listing listing = Listing.builder()
            .hearingStatus(HearingState.Listed)
            .date(LocalDate.now())
            .hearingVenueNameAndAddress(someAddress)
            .build();
        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        final CaseData caseData = CaseData.builder()
            .hearingList(List.of(listingListValue))
            .build();

        //When
        String result = caseData.getHearingVenueName();

        //Then
        assertThat(result).isEqualTo(someAddress);
    }

    @Test
    void shouldGetCompletedHearingDate() {
        //When
        DateTimeFormatter dateFormatter = ofPattern("dd MMM yyyy", UK);
        LocalDate now = LocalDate.now();
        Listing listing = Listing.builder().hearingStatus(HearingState.Complete).date(now).build();
        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        final CaseData caseData = CaseData.builder()
            .hearingList(List.of(listingListValue))
            .build();

        //When
        Listing result = caseData.getLatestCompletedHearing();

        //Then
        assertThat(result).isNotNull();
    }
}
