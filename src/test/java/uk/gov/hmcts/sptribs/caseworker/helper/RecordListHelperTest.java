package uk.gov.hmcts.sptribs.caseworker.helper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class RecordListHelperTest {

    @InjectMocks
    private RecordListHelper recordListHelper;

    @Mock
    private LocationService locationService;

    @Test
    void shouldAboutToStartMethodSuccessfullyPopulateRegionData() {
        //Given
        final CaseData caseData = caseData();

        //When
        when(locationService.getAllRegions()).thenReturn(getMockedRegionData());
        recordListHelper.regionData(caseData);

        //Then
        assertThat(caseData.getRecordListing().getRegionList()).isNotNull();
        assertThat(caseData.getRecordListing().getRegionList().getListItems()).hasSize(1);

    }

    @Disabled
    void shouldAboutToStartMethodSuccessfullyPopulateVenuesData() {
        final CaseData caseData = caseData();
        recordListHelper.populatedVenuesData(caseData);

        DynamicList hearingVenueList = locationService.getHearingVenuesByRegion("1");
        caseData.getRecordListing().setHearingVenues(hearingVenueList);

        //when
        when(locationService.getHearingVenuesByRegion("1")).thenReturn(getMockedHearingVenueData());

        //Then
        assertThat(caseData.getRecordListing().getHearingVenues()).isNotNull();
        assertThat(caseData.getRecordListing().getHearingVenues()
            .getValue().getLabel()).isEqualTo("courtname-courtAddress");
        assertThat(caseData.getRecordListing().getHearingVenues().getListItems()).hasSize(1);
        assertThat(caseData.getRecordListing().getHearingVenues()
            .getListItems().get(0).getLabel()).isEqualTo("courtname-courtAddress");
    }

    @Test
    void shouldMidEventMethodSuccessfullyPopulateHearingVenueData(){
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final RecordListing recordListing = new RecordListing();
        recordListing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        recordListing.setRegionList(getMockedRegionData());
        caseData.setRecordListing(recordListing);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(locationService.getHearingVenuesByRegion("1")).thenReturn(getMockedHearingVenueData());
        AboutToStartOrSubmitResponse<CaseData, State> response = recordListHelper.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getRecordListing().getHearingVenues()
            .getValue().getLabel()).isEqualTo("courtname-courtAddress");
        assertThat(response.getData().getRecordListing().getHearingVenues().getListItems()).hasSize(1);
        assertThat(response.getData().getRecordListing().getHearingVenues()
            .getListItems().get(0).getLabel()).isEqualTo("courtname-courtAddress");



    }





    private DynamicList getMockedRegionData() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("1-region")
            .code(UUID.randomUUID())
            .build();

        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }


    private DynamicList getMockedHearingVenueData() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("courtname-courtAddress")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }


}
