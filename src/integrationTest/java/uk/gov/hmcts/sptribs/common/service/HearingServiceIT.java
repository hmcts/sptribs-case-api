package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.RetiredFields;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.service.HearingService.isMatchingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Complete;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getHearingList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class HearingServiceIT {

    @Autowired
    private HearingService hearingService;

    @Test
    void shouldCreateListedHearingDynamicList() {
        final CaseData caseData = CaseData.builder()
            .hearingList(getHearingList())
            .build();

        DynamicList dynamicList = hearingService.getListedHearingDynamicList(caseData);

        assertThat(dynamicList.getListItems()).isNotEmpty();
        assertThat(dynamicList.getListItems().get(0).getLabel()).isEqualTo("1 - Final - 14 Aug 2024 10:00");
        assertThat(dynamicList.getListItems().get(1).getLabel()).isEqualTo("2 - Interlocutory - 14 Aug 2024 14:00");
    }

    @Test
    void shouldAddOldExistingHearings() {
        final Listing listing = getRecordListing();

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .retiredFields(new RetiredFields())
            .build();

        hearingService.addListingIfExists(caseData);

        assertThat(caseData.getHearingList()).isNotEmpty();
        assertThat(caseData.getHearingList().get(0).getValue()).isEqualTo(listing);
    }

    @Test
    void shouldCreateCompletedHearingDynamicList() {
        final List<ListValue<Listing>> listings = getHearingList();
        listings.get(0).getValue().setHearingStatus(Complete);
        listings.get(1).getValue().setHearingStatus(Complete);

        final CaseData caseData = CaseData.builder()
            .hearingList(listings)
            .build();

        DynamicList dynamicList = hearingService.getCompletedHearingDynamicList(caseData);

        assertThat(dynamicList.getListItems()).isNotEmpty();
        assertThat(dynamicList.getListItems().get(0).getLabel()).isEqualTo("1 - Final - 14 Aug 2024 10:00");
        assertThat(dynamicList.getListItems().get(1).getLabel()).isEqualTo("2 - Interlocutory - 14 Aug 2024 14:00");
    }

    @Test
    void shouldAddListingToHearingList() {
        final Listing listing = getRecordListing();

        final CaseData caseData = CaseData.builder()
            .hearingList(new ArrayList<>())
            .build();

        hearingService.addListing(caseData, listing);

        assertThat(caseData.getHearingList()).isNotEmpty();
        assertThat(caseData.getHearingList().get(0).getValue()).isEqualTo(listing);
    }

    @Test
    void shouldUpdateHearingList() {
        final Listing listing = getRecordListing();

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .hearingList(getHearingList())
            .build();

        final String hearingName = "1 - Final - 14 Aug 2024 10:00";

        hearingService.updateHearingList(caseData, hearingName);

        assertThat(caseData.getHearingList().get(0).getValue()).isEqualTo(listing);
    }

    @Test
    void shouldValidateMatchingHearingName() {
        final Listing listing = getRecordListing();

        final ListValue<Listing> listingListValue = ListValue.<Listing>builder().value(listing).build();

        final String hearingName = "1 - Final - 21 Apr 2023 10:00";

        assertThat(isMatchingHearing(listingListValue, hearingName)).isTrue();
    }

    @Test
    void shouldValidateNotMatchingHearingName() {
        final Listing listing = getRecordListing();

        final ListValue<Listing> listingListValue = ListValue.<Listing>builder().value(listing).build();

        final String hearingName = "1 - Final - 14 Aug 2024 10:00";

        assertThat(isMatchingHearing(listingListValue, hearingName)).isFalse();
    }
}
