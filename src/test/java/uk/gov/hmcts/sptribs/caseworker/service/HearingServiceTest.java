package uk.gov.hmcts.sptribs.caseworker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.HearingCancellationReason;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.model.PostponeReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.RetiredFields;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;

@ExtendWith(MockitoExtension.class)
class HearingServiceTest {

    @Mock
    private CicCase cicCase;

    @Mock
    private DynamicList cicCaseHearingList;

    @Mock
    private DynamicListElement cicCaseHearingLabel;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private HearingService hearingService;


    @Test
    void shouldPopulateListedHearingDateDynamicList() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(getRecordListing());
        List<ListValue<Listing>> listValueList = new ArrayList<>();
        listValueList.add(listingListValue);
        final CaseData caseData = CaseData.builder().hearingList(listValueList).build();
        details.setData(caseData);

        DynamicList hearingList = hearingService.getListedHearingDynamicList(caseData);

        assertThat(hearingList).isNotNull();
    }

    @Test
    void shouldAddOldListingIfNotExists() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .listing(getRecordListing())
            .retiredFields(new RetiredFields())
            .build();
        details.setData(caseData);

        hearingService.addListingIfExists(caseData);

        assertThat(caseData.getHearingList()).isNotEmpty();
    }

    @Test
    void shouldNotAddOldListingIfNotExists() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .listing(Listing.builder().build())
            .build();
        details.setData(caseData);

        hearingService.addListingIfExists(caseData);

        assertThat(caseData.getHearingList()).isEmpty();
    }

    @Test
    void shouldAddOldListingIfNotExistsForOldCancelledListing() {
        RetiredFields retiredFields = new RetiredFields();
        retiredFields.setCicCaseCancelHearingAdditionalDetail("cancelAddlDetail");
        retiredFields.setCicCaseHearingCancellationReason(HearingCancellationReason.CASE_REJECTED);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .listing(getRecordListing())
            .retiredFields(retiredFields)
            .build();
        details.setData(caseData);

        hearingService.addListingIfExists(caseData);

        assertThat(caseData.getHearingList()).isNotEmpty();
        assertThat(caseData.getHearingList().get(0).getValue()).isNotNull();
        assertThat(caseData.getHearingList().get(0).getValue().getHearingCancellationReason().getReason()).isEqualTo("Case Rejected");
        assertThat(caseData.getHearingList().get(0).getValue().getCancelHearingAdditionalDetail()).isEqualTo("cancelAddlDetail");
    }

    @Test
    void shouldAddOldListingIfNotExistsForOldPostponedListing() {
        RetiredFields retiredFields = new RetiredFields();
        retiredFields.setCicCasePostponeReason(PostponeReason.BEREAVEMENT);
        retiredFields.setCicCasePostponeAdditionalInformation("postponeInfo");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .listing(getRecordListing())
            .retiredFields(retiredFields)
            .build();
        details.setData(caseData);

        hearingService.addListingIfExists(caseData);

        assertThat(caseData.getHearingList()).isNotEmpty();
        assertThat(caseData.getHearingList().get(0).getValue()).isNotNull();
        assertThat(caseData.getHearingList().get(0).getValue().getPostponeReason().getReason()).isEqualTo("Bereavement");
        assertThat(caseData.getHearingList().get(0).getValue().getPostponeAdditionalInformation()).isEqualTo("postponeInfo");
    }


    @Test
    void shouldPopulateCompletedHearingDynamicList() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        ListValue<Listing> listingListValue = new ListValue<>();
        Listing listing = getRecordListing();
        listing.setHearingStatus(HearingState.Complete);
        listingListValue.setValue(listing);
        List<ListValue<Listing>> listValueList = new ArrayList<>();
        listValueList.add(listingListValue);
        final CaseData caseData = CaseData.builder().hearingList(listValueList).build();
        details.setData(caseData);

        DynamicList hearingList = hearingService.getCompletedHearingDynamicList(caseData);

        assertThat(hearingList).isNotNull();
    }

    @Test
    void shouldAddListingToCaseData() {
        CaseData caseData = CaseData.builder().build();
        Listing listing = getRecordListing();
        assertThat(caseData.getHearingList()).isEmpty();

        hearingService.addListing(caseData, listing);

        assertThat(caseData.getHearingList()).isNotEmpty();
    }

    @Test
    void shouldUpdateHearingList() throws JsonProcessingException {
        final Listing listing = getRecordListing();

        final CaseData caseData = CaseData.builder()
            .hearingList(List.of(ListValue.<Listing>builder().value(listing).build()))
            .build();

        final String completedListingString = "test string";
        final String hearingName = "1 - Final - 21 Apr 2023 10:00";

        final Listing completedListing = getRecordListing();
        completedListing.setHearingStatus(HearingState.Complete);

        doReturn(completedListingString)
            .when(objectMapper)
            .writeValueAsString(any(Listing.class));

        doReturn(completedListing)
            .when(objectMapper)
            .readValue(completedListingString, Listing.class);

        hearingService.updateHearingList(caseData, hearingName);

        assertEquals(caseData.getHearingList().get(0).getValue(), completedListing);
    }

    @Test
    void shouldNotUpdateHearingList() {
        final ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(getRecordListing());

        final Listing completedListing = getRecordListing();
        completedListing.setHearingStatus(HearingState.Complete);

        final CaseData caseData = CaseData.builder()
            .hearingList(List.of(listingListValue))
            .build();

        final String hearingName = "1 - Interlocutory - 21 Apr 2023 10:00";

        hearingService.updateHearingList(caseData, hearingName);

        assertNotEquals(caseData.getHearingList().get(0).getValue(), completedListing);
    }
}
