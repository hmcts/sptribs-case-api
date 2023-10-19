package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
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
    private CaseData caseData;

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
        //When

        DynamicList hearingList = hearingService.getListedHearingDynamicList(caseData);

        //Then
        assertThat(hearingList).isNotNull();
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
        //When

        DynamicList hearingList = hearingService.getCompletedHearingDynamicList(caseData);

        //Then
        assertThat(hearingList).isNotNull();

    }

    @Test
    void shouldAddListingToCaseData() {
        //Given
        CaseData caseData = CaseData.builder().build();
        Listing listing = getRecordListing();
        assertThat(caseData.getHearingList()).isEmpty();

        //When
        hearingService.addListing(caseData, listing);

        //Then
        assertThat(caseData.getHearingList()).isNotEmpty();
    }

    @Test
    void shouldUpdateHearingList() {
        //Given
        ListValue<Listing> listingListValue = new ListValue<>();
        // A listed Listing
        listingListValue.setValue(getRecordListing());
        List<ListValue<Listing>> listValueList = new ArrayList<>();
        listValueList.add(listingListValue);
        // A completed listing
        Listing completedListing = getRecordListing();
        completedListing.setHearingStatus(HearingState.Complete);

        when(cicCaseHearingLabel.getLabel()).thenReturn("Final 10:00");
        when(cicCaseHearingList.getValue()).thenReturn(cicCaseHearingLabel);
        when(cicCase.getHearingList()).thenReturn(cicCaseHearingList);
        when(caseData.getCicCase()).thenReturn(cicCase);
        when(caseData.getListing()).thenReturn(completedListing);
        when(caseData.getHearingList()).thenReturn(listValueList);

        //When
        hearingService.updateHearingList(caseData);

        //Then
        // Have replaced listed Listing with completed Listing
        Assertions.assertEquals(listValueList.get(0).getValue(), completedListing);

    }

}
