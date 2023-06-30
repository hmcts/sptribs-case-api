package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;

@ExtendWith(MockitoExtension.class)
class HearingServiceTest {

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

}
