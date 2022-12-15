package uk.gov.hmcts.sptribs.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;

@ExtendWith(MockitoExtension.class)
class HearingServiceTest {

    @InjectMocks
    private HearingService hearingService;


    @Test
    void shouldPopulateHearingDateDynamicList() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().recordListing(getRecordListing()).build();
        details.setData(caseData);
        //When

        DynamicList hearingList = hearingService.getHearingDateDynamicList(details);

        //Then
        assertThat(hearingList).isNotNull();
    }

    @Test
    void shouldPopulateHearingDateDynamicListNull() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        details.setData(caseData);
        //When

        DynamicList regionList = hearingService.getHearingDateDynamicList(details);

        //Then
        assertThat(regionList).isNull();
    }

}
